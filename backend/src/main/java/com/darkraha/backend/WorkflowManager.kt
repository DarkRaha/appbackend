package com.darkraha.backend

import com.darkraha.backend.client.ClientBase
import com.darkraha.backend.components.mainthread.MainThread
import com.darkraha.backend.components.qmanager.QueryManager
import com.darkraha.backend.helpers.AtomicFlag
import com.darkraha.backend.helpers.LockManager
import com.darkraha.backend.infos.CancelInfo
import com.darkraha.backend.infos.ErrorInfo
import com.darkraha.backend.infos.ResponseInfo
import java.io.File

import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


interface WorkflowExecutor {

    /**
     * Run query asynchronously.
     */
    fun exeAsync(): UserQuery

    /**
     * Post task for running query asynchronously to the main thread.
     */
    fun postExeAsync(): UserQuery

    /**
     * Run query synchronously.
     */
    fun exeSync(): UserQuery
}


interface WorkflowCancel {
    fun cancel(code: Int=CancelInfo.CANCEL_BY_USER, message: String?="Canceled by user.")
    /**
     * @param uiWithProgressListener when true, then progress listener that correspond to the ui object will be removed
     */
    fun cancelUi(ui: Any?, progressListener: ProgressListener?, uiWithProgressListener: Boolean)
}


interface WorkflowState {
    fun isSuccess(): Boolean
    fun isError(): Boolean
    fun isCanceled(): Boolean
    fun isFinished(): Boolean
    fun workflowStep(): Int
}

interface WorkflowReader {
    fun service(): Service?
    fun client(): ClientBase?
    fun lock(): ReentrantLock
    fun cancelInfo(): CancelInfo
//    fun chainTypeCreate(): ChainType
//    fun chainTypeResponse(): ChainType
}

interface Workflow {

    /**
     * Try append additional query.
     */
    fun appendQuery(append: WorkflowAppend): Boolean

    fun getAppendedQueries(): List<WorkflowAppend>


    fun waitFinish(time: Long = 0): UserQuery


    /**
     * When user don't need query, it can free. Used when waitFinish method invoked.
     */
    fun free()
}

interface ClientQueryEditor: WorkflowCancel {

    fun assignFrom(src: ResponseInfo)
    fun setResult(r: Any?)
    fun setRawString(s: String?, asResult: Boolean = false)
    fun setRawBytes(b: ByteArray?, asResult: Boolean = false)
    fun setRawSize(s: Long)
    fun setRawMimetype(mt: String?)
    fun setResultFile(file: File?)
    fun success(newResult: Any? = null, resultChainType: ChainType = ChainType.UNDEFINED)
    fun error(code: Int, msg: String?, e: Throwable?)
    fun error(responseInfo: ResponseInfo)
    fun resultCode(code: Int)
    fun resultMessage(msg: String? = null)
    fun responseInfo(): ResponseInfo
    fun getUserCallbacks(): List<Callback<UserQuery>>
    fun error(e: Throwable?)
    fun error(msg: String?)

}



/**
 * Describe query handling workflow. If query has assigned client, then it will be _free for reusing,
 * all data will be cleared.
 *
 * @author Verma Rahul
 */
class WorkflowManager : WorkflowState, WorkflowReader, Workflow, WorkflowExecutor, ClientQueryEditor, WorkflowCancel {

    lateinit var owner: Query
    val response = ResponseInfo()

    val lock = ReentrantLock()
    val conditionFinished = lock.newCondition()

    val prepareProcessor = mutableListOf<Processor>()
    val preProcessor = mutableListOf<Processor>()
    val postProcessor = mutableListOf<Processor>()

    val workflowListener = mutableListOf<WorkflowListener>()

    val appended = mutableListOf<WorkflowAppend>()

    private var bgThread: Thread? = null
    var executor: ExecutorService? = null
    var service: Service? = null
    var client: ClientBase? = null
    var mainThread: MainThread? = null
    var queryManager: QueryManager? = null

    val callbacks = mutableListOf<Callback<UserQuery>>()
    var callbakFirst: Callback<UserQuery>? = null
    var callbackLast: Callback<UserQuery>? = null

    var syncResource: Any? = null
    val syncCallbacks = "syncCallbacks"
    val syncProgress = "syncProgress"
    val syncBg = "syncBg"


    val state = AtomicFlag()

    var workflowStep = 0
        internal set

    var progressListener: ProgressListener? = null
    var externalUsers = 0
    val callbackException: CallbackErrorHandler = { it: Exception ->
        run {
            response.errorInfo.exceptionsCallbacks.add(it)
            it.printStackTrace()
        }
    }


    fun addOrSetFrom(src: WorkflowManager) {
        prepareProcessor.addAll(src.prepareProcessor)
        preProcessor.addAll(src.preProcessor)
        postProcessor.addAll(src.postProcessor)
        service = src.service
        client = src.client
        executor = src.executor
        callbakFirst = src.callbakFirst
        callbacks.addAll(src.callbacks)
        callbackLast = src.callbackLast
        mainThread = src.mainThread

    }

    fun isAllowAppend() = state.isFlag(F_ALLOW_APPEND)


    fun hasCallbacks() = callbakFirst != null || callbackLast != null || callbacks.size > 0

    /**
     * Check is query used.
     */
    fun isUsed() = state.isFlag(F_USED)

    /**
     * Checks if the query is running in background.
     */
    fun isBackground() = state.isFlag(F_BACKGROUND)

    /**
     * Checks, if query is preparing.
     */
    fun isPreparing() = state.isFlag(F_PREPARING)


    fun isCallbacksDone() = state.isFlag(F_CALLBACKS_DONE)

    /**
     * Checks, if query ready for _free.
     */
    fun isReadyForFree() = state.isFlag(F_READY_FOR_FREE)

    /**
     * Checks, if workflow engine can interrupt bacground thread.
     */
    fun isAllowInterrupt() = state.isFlag(F_ALLOW_INTERRUPT)

    /**
     * Checks if query handing completed.
     */
    fun isComplete() = state.isFlagAndAny(F_USED, F_SUCCESS, F_ERROR)


    fun isWorkflowPossible() = state.isFlagAndNotAny(F_USED, F_SUCCESS, F_ERROR, F_CANCELED)

    fun setUsed(v: Boolean) {
        if (v) {
            state.lock.withLock {
                if (isWorkflowPossible()) {
                    throw IllegalStateException("Flag used already true, so query already used.")
                } else {
                    state.setFlag(F_USED)
                }
            }
        } else {
            state.clearFlag(F_USED)
        }

    }


    private fun setBackground(v: Boolean) {
        if (!state.setFlagIfAny(v, F_BACKGROUND, F_USED)) {
            throw IllegalStateException(ERR_QUERY_NOT_USED)
        }
    }


    private fun readyForFree() {
        if (isWorkflowPossible()) {
            throw IllegalStateException(ERR_QUERY_NOT_FINISHED)
        }

        state.setFlagMustAnyAndNon(F_READY_FOR_FREE, F_USED, ERR_QUERY_NOT_USED, 0)
    }


    fun setAllowInterrupt() {
        state.setFlag(F_ALLOW_INTERRUPT)
    }

    fun setAllowAppend(v: Boolean) {
        state.setFlag(v, F_ALLOW_APPEND)
    }

    private fun setPrepare() {
        state.setFlag(F_PREPARING)

    }

    private fun setPrepareDone() {
        state.clearFlag(F_PREPARING)
    }

    private fun setRunSync() {
        state.setFlag(F_RUN_SYNC)
    }

    private fun setRunAsync() {
        state.clearFlag(F_RUN_SYNC)
    }


//-------------------------------------------------------------------------------------------------
// workflow

    override fun exeSync(): UserQuery {
        owner.params.urlBuilder.build()

        if (service == null) {
            throw IllegalStateException("Service not assigned.")
        }

        setRunSync()
        setUsed(true)

        prepare()
        if (isFinished()) {
            finish()
        } else {
            background()
            finish()
        }

        return owner
    }


    override fun postExeAsync(): UserQuery {

//        GlobalScope.launch(Dispatchers.Main) {
//            exeAsync()
//        }


        mainThread!!.post { exeAsync() }
        return owner
    }

    /**
     *
     */
    override fun exeAsync(): UserQuery {
        owner.params.urlBuilder.build()

        if (service == null) {
            throw IllegalStateException("Service not assigned.")
        }

        setUsed(true)
        setRunAsync()

//
//        GlobalScope.launch(Dispatchers.Main) {
//            prepare()
//            if (isFinished()) {
//                finish()
//            } else {
//                executor!!.execute {
//                    background()
//                    finish()
//                }
//            }
//        }
        mainThread!!.execute {
            prepare()
            if (isFinished()) {
                finish()
            } else {
                executor!!.execute {
                    background()
                    finish()
                }
            }
        }

        return owner
    }

    private fun prepare() {
        setPrepare()
        queryManager?.onQueryStart(owner)
        client?.onQueryStart(owner)
        dispatchWorkflowListeners(WORKFLOW_PREPARE_START)


        dispatchPrepare()

        if (isWorkflowPossible()) {
            dispatchCallbacksPrepare()
        }

        setPrepareDone()
        dispatchWorkflowListeners(WORKFLOW_PREPARE_END)
    }


    private fun background() {
        synchronized(syncBg) {
            setBackground(true)
            bgThread = Thread.currentThread()
        }
        dispatchProgressStart()
        dispatchWorkflowListeners(WORKFLOW_BACKGROUND_START)
        if (syncResource != null) {
            LockManager.lock(syncResource!!)
        }
        runCatching {
            if (isWorkflowPossible()) {
                dispatchPre()
                service!!.handle(owner, owner, owner)
                println("after service " + response.rawResultString + " isWorkflowPossible " + isWorkflowPossible())
                dispatchPost()
            } else {
                throw IllegalStateException("WorkflowManager not possible. canceled='${response.cancelInfo.message}' err='${response.errorInfo.message}'")
            }
        }.onFailure {
            error(ErrorInfo.ERR_WORKFLOW, it.message, it)
            it.printStackTrace()
        }
        if (syncResource != null) {
            LockManager.unLock(syncResource!!)
        }

        synchronized(syncBg) {
            dispatchProgressEnd()
            setBackground(false)
            bgThread = null
        }

        success()
        dispatchWorkflowListeners(WORKFLOW_BACKGROUND_END)
    }


    private fun finish() {

        dispatchWorkflowListeners(WORKFLOW_FINISH_START)

        if (hasCallbacks()) {
            if (state.isFlag(F_RUN_SYNC)) {
                finishCallbacks()
            } else {
                mainThread!!.execute {
                    finishCallbacks()
                }
            }
        } else {
            finish_end()
        }
    }


    private fun finishCallbacks() {
        dispatchWorkflowListeners(WORKFLOW_FINISH_CALLBACK_START)
        dispatchCallbacks(owner)
        dispatchWorkflowListeners(WORKFLOW_FINISH_CALLBACK_END)
        finish_end()
    }

    private fun finish_end() {
        if (isError()) {
            val backendErrorInfo = Backend._sharedInstance?.error?.value
            if (backendErrorInfo != null) {
                response.errorInfo.clientClassName = client?.javaClass?.simpleName
                response.errorInfo.serviceClassName = service?.javaClass?.simpleName
                backendErrorInfo.set(response.errorInfo)
                Backend._sharedInstance?.error?.notifyDataChanged()
            }
            println("Error notification: ${response.errorInfo}")
            response.errorInfo.exception?.printStackTrace()
        } else if (isCanceled()) {
            println("WorkflowManager query canceled: " + response.cancelInfo.message + " code = " + response.cancelInfo.code)
        }



        dispatchWorkflowListeners(WORKFLOW_FINISH_END)
        queryManager?.onQueryEnd(owner)
        client?.onQueryEnd(owner)
        lock.lock()
        conditionFinished.signalAll()
        lock.unlock()
        readyForFree()
        dispatchWorkflowListeners(WORKFLOW_READY_FOR_FREE)
        _free()
    }

    private fun _free() {
        lock.lock()

        if (client != null) {
            if (!isUsed() || (isReadyForFree() && externalUsers <= 0)) {
                dispatchWorkflowListeners(WORKFLOW_ON_FREE)
                client!!.onQueryFree(owner)
                state.clear()
                owner.clear()
                queryManager?.backQuery(owner)
            }
        }

        lock.unlock()
    }


//-------------------------------------------------------------------------------------------------

    private fun dispatchPrepare() = dispatchProcessor(prepareProcessor)
    private fun dispatchPre() = dispatchProcessor(preProcessor)
    private fun dispatchPost() = dispatchProcessor(postProcessor)


    /**
     * Must be invoked from main thread
     * todo invoking from any thread and post it to the main thread
     */
    fun append(a: WorkflowAppend): Boolean {


        if (state.flags and F_ALLOW_APPEND > 0) {
            synchronized(appended) {
                appended.add(a)
                a.progressListener?.onStart()
                a.callback?.onPrepare(owner)
            }

            return true
        }

        return false
    }

    private fun dispatchProcessor(p: List<Processor>) {
        println("WorkflowManager dispatchProcessor workPossible=" + isWorkflowPossible())
        if (isWorkflowPossible()) {
            try {
                synchronized(p) {
                    p.forEach {
                        if (isFinished()) {
                            return@forEach
                        }
                        it.process(owner, owner)
                    }
                }
            } catch (e: Exception) {
                error(ErrorInfo.ERR_WORKFLOW_PROCESSOR, e.message, e)
            }
        }
    }

    private fun dispatchCallbacks(q: Query) {
        when {
            isSuccess() -> dispatchCallbacksSuccess(q)
            isError() -> dispatchCallbacksError(q)
            isCanceled() -> dispatchCallbacksCanceled(q)
        }

        if (isComplete()) {
            dispatchCallbacksComplete(q)
        }

        if (isFinished()) {
            dispatchCallbacksFinish(q)
        }
    }

    private fun dispatchCallbacksPrepare() {
        synchronized(syncCallbacks) {

            if (callbakFirst != null) {
                try {
                    callbakFirst!!.onPrepare(owner)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // user callbacks
            CallbackUtils.dispatchCallbacks(CallbackUtils.CB_PREPARE, owner as UserQuery, callbacks, callbackException)
            client?.apply {
                CallbackUtils.dispatchCallbacks(CallbackUtils.CB_PREPARE, owner, clientCallbacks, callbackException)
            }

            if (callbackLast != null) {
                try {
                    callbackLast!!.onPrepare(owner)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    }


    private fun dispatchCallbacksCanceled(q: Query) {
        synchronized(syncCallbacks) {


            try {
                callbakFirst?.onCancel(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }

            // user callbacks
            CallbackUtils.dispatchCallbacks(CallbackUtils.CB_CANCELED, q as UserQuery, callbacks, callbackException)
            client?.apply {
                CallbackUtils.dispatchCallbacks(CallbackUtils.CB_CANCELED, q as UserQuery, clientCallbacks, callbackException)
            }

            try {
                callbackLast?.onCancel(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }

            if (appended.size > 0) {
                appended.forEach {
                    try {
                        it.callback?.onCancel(q)
                    } catch (e: Exception) {
                        response.errorInfo.exceptionsCallbacks.add(e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun dispatchCallbacksSuccess(q: Query) {
        synchronized(syncCallbacks) {

            try {
                callbakFirst?.onSuccess(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }

            // user callbacks
            if (response.chainTypeResponse() == ChainType.LAST_ELEMENT || response.chainTypeResponse() == ChainType.STAND_ALONE) {
                CallbackUtils.dispatchCallbacks(CallbackUtils.CB_SUCCESS, q as UserQuery, callbacks, callbackException)
                client?.apply {
                    CallbackUtils.dispatchCallbacks(CallbackUtils.CB_SUCCESS, q as UserQuery, clientCallbacks, callbackException)
                }

                if (appended.size > 0) {
                    appended.forEach {
                        try {
                            it.callback?.onSuccess(q)
                        } catch (e: Exception) {
                            response.errorInfo.exceptionsCallbacks.add(e)
                            e.printStackTrace()
                        }
                    }
                }

            }


            try {
                callbackLast?.onSuccess(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }


        }
    }


    private fun dispatchCallbacksError(q: Query) {
        synchronized(syncCallbacks) {

            try {
                callbakFirst?.onError(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }

            // user callbacks
            CallbackUtils.dispatchCallbacks(CallbackUtils.CB_ERROR, q as UserQuery, callbacks, callbackException)
            client?.apply {
                CallbackUtils.dispatchCallbacks(CallbackUtils.CB_ERROR, q as UserQuery, clientCallbacks, callbackException)
            }

            try {
                callbackLast?.onError(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }

            if (appended.size > 0) {
                appended.forEach {
                    try {
                        it.callback?.onError(q)
                    } catch (e: Exception) {
                        response.errorInfo.exceptionsCallbacks.add(e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun dispatchCallbacksComplete(q: Query) {
        synchronized(syncCallbacks) {


            try {
                callbakFirst?.onComplete(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }



            if (isError() ||
                    response.chainTypeResponse() == ChainType.LAST_ELEMENT
                    || response.chainTypeResponse() == ChainType.STAND_ALONE
            ) {
                // user callbacks
                CallbackUtils.dispatchCallbacks(CallbackUtils.CB_COMPLETE, q as UserQuery, callbacks, callbackException)
                client?.apply {
                    CallbackUtils.dispatchCallbacks(CallbackUtils.CB_COMPLETE, q as UserQuery, clientCallbacks, callbackException)
                }

                // appended callbacks
                if (appended.size > 0) {
                    appended.forEach {
                        try {
                            it.callback?.onComplete(q)
                        } catch (e: Exception) {
                            response.errorInfo.exceptionsCallbacks.add(e)
                            e.printStackTrace()
                        }
                    }
                }
            }


            try {
                callbackLast?.onComplete(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }


        }
    }

    private fun dispatchCallbacksFinish(q: Query) {
        synchronized(syncCallbacks) {


            try {
                callbakFirst?.onFinish(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }


            if (!q.isSuccess() || response.chainTypeResponse == ChainType.STAND_ALONE
                    || response.chainTypeResponse == ChainType.LAST_ELEMENT
            ) {
                CallbackUtils.dispatchCallbacks(CallbackUtils.CB_FINISHED, q as UserQuery, callbacks, callbackException)
                client?.apply {
                    CallbackUtils.dispatchCallbacks(CallbackUtils.CB_FINISHED, q as UserQuery, clientCallbacks, callbackException)
                }
            }

            try {
                callbackLast?.onFinish(q)
            } catch (e: Exception) {
                response.errorInfo.exceptionsCallbacks.add(e)
                e.printStackTrace()
            }

            if (appended.size > 0) {
                appended.forEach {
                    try {
                        it.callback?.onFinish(q)
                    } catch (e: Exception) {
                        response.errorInfo.exceptionsCallbacks.add(e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun dispatchWorkflowListeners(newStep: Int) {
        workflowStep = newStep
        synchronized(workflowListener) {
            workflowListener.forEach {
                try {
                    it.onWorkflowStep(owner)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    companion object {

        @JvmStatic
        val ERR_QUERY_NOT_USED = "Query not used (flag F_USED must be true)"

        @JvmStatic
        val ERR_QUERY_NOT_FINISHED = "Query not finished"

        @JvmStatic
        val F_USED = 1
        @JvmStatic
        val F_BACKGROUND = 2
        @JvmStatic
        val F_PREPARING = 4
        @JvmStatic
        val F_CANCELED = 8
        @JvmStatic
        val F_SUCCESS = 16
        @JvmStatic
        val F_ERROR = 32

        @JvmStatic
        val F_CALLBACKS_DONE = 64

        @JvmStatic
        val F_READY_FOR_FREE = 128

        @JvmStatic
        val F_ALLOW_INTERRUPT = 256

        @JvmStatic
        val F_RUN_SYNC = 512

        @JvmStatic
        val F_ALLOW_APPEND = 1024


        @JvmStatic
        val WORKFLOW_PREPARE_START = 1
        @JvmStatic
        val WORKFLOW_PREPARE_END = 2

        @JvmStatic
        val WORKFLOW_BACKGROUND_START = 3

        @JvmStatic
        val WORKFLOW_BACKGROUND_END = 4

        @JvmStatic
        val WORKFLOW_FINISH_START = 5

        @JvmStatic
        val WORKFLOW_FINISH_END = 6


        @JvmStatic
        val WORKFLOW_FINISH_CALLBACK_START = 7

        @JvmStatic
        val WORKFLOW_FINISH_CALLBACK_END = 8


        @JvmStatic
        val WORKFLOW_READY_FOR_FREE = 9

        @JvmStatic
        val WORKFLOW_ON_FREE = 10

    }


    fun dispatchProgressStart() {
        synchronized(syncProgress) {
            progressListener?.onStart()
        }
    }

    fun dispatchProgressEnd() {
        synchronized(syncProgress) {
            progressListener?.onEnd()
        }

        synchronized(appended) {
            if (appended.size > 0) {
                appended.forEach {
                    it.progressListener?.onEnd()
                }
            }
        }
    }

    fun dispatchProgress(current: Long, total: Long) {

        synchronized(syncProgress) {
            progressListener?.onProgress(current, total)
        }


        synchronized(appended) {
            if (appended.size > 0) {
                appended.forEach {
                    it.progressListener?.onProgress(current, total)
                }
            }
        }

    }


    internal fun clear() {
        response.clear()
        state.clear()
        workflowStep = 0
        bgThread = null
        executor = null
        service = null
        client = null
        preProcessor.clear()
        postProcessor.clear()
        prepareProcessor.clear()
        callbacks.clear()
        syncResource = null
        workflowListener.clear()
        progressListener = null
        externalUsers = 0
        appended.clear()
    }

    //------------------------------------------------------------------------------------
    /**
     * Checks if query handling finished
     */
    override fun isFinished() = state.isFlagAndAny(F_USED, F_SUCCESS, F_ERROR, F_CANCELED)

    /**
     * Checks, if query canceled.
     */
    override fun isCanceled() = state.isFlag(F_CANCELED)

    /**
     * Checks, if query finished with success.
     */
    override fun isSuccess() = state.isFlag(F_SUCCESS)

    /**
     * Checks, if query finished with error.
     */
    override fun isError() = state.isFlag(F_ERROR)

    //----------------------------------------------------------------------------------------

    override fun client(): ClientBase? = client
    override fun lock(): ReentrantLock = lock
    override fun service(): Service? = service
    override fun workflowStep(): Int = workflowStep

    //----------------------------------------------------------------------------------------

    override fun waitFinish(time: Long): UserQuery {
        if (isWorkflowPossible()) {
            lock.lock()
            externalUsers++
            if (time <= 0) {
                conditionFinished.await()
            } else {
                conditionFinished.await(time, TimeUnit.MILLISECONDS)
            }
            lock.unlock()
        }

        return owner
    }


    override fun free() {
        lock.lock()
        externalUsers--
        _free()
        lock.unlock()
    }


    override fun appendQuery(append: WorkflowAppend): Boolean {
        return append(append)
    }

    override fun getAppendedQueries(): List<WorkflowAppend> {
        return appended
    }

    override fun cancelInfo(): CancelInfo {
        return response.cancelInfo
    }

    override fun cancelUi(ui: Any?, progressListener: ProgressListener?, uiWithProgressListener: Boolean) {

        if (ui != null) {
            if (owner.params.uiObject == ui) {
                owner.params.uiObject = null
                if (uiWithProgressListener) {
                    synchronized(syncProgress) {
                        if (this.progressListener != null) {
                            progressListener?.onEnd()
                            this.progressListener = null
                        }
                    }
                }
                if (appended.size == 0) {
                    cancel(-1)
                }
            }
        }

        synchronized(syncProgress) {
            if (this.progressListener == progressListener) {
                progressListener?.onEnd()
                this.progressListener = null
            }
        }

        synchronized(appended) {
            appended.forEach {
                if (it.ui == ui) {
                    it.ui = null

                    if (uiWithProgressListener) {
                        progressListener?.onEnd()
                        it.progressListener = null
                    }
                }

                if (it.progressListener == progressListener) {
                    progressListener?.onEnd()
                    it.progressListener = null
                }
            }
        }


    }

//----------------------------------------------------------------------------------------

    override fun assignFrom(src: ResponseInfo) {
        response.assignFrom(src)
    }

    override fun getUserCallbacks(): List<Callback<UserQuery>> {
        return callbacks
    }

    override fun setResult(r: Any?) {
        response.result = r
    }

    override fun setRawMimetype(mt: String?) {
        response.rawMimetype = mt
    }

    override fun setRawSize(s: Long) {
        response.rawSize = s
    }

    override fun setRawString(s: String?, asResult: Boolean) {
        response.rawResultString = s
        if (asResult) {
            response.result = s
        }
    }

    override fun setRawBytes(b: ByteArray?, asResult: Boolean) {
        response.rawResultBytes = b
        if (asResult) {
            response.result = b
        }
    }

    override fun setResultFile(file: File?) {
        response.rawResultFile = file
    }


    override fun cancel(code: Int , message: String? ) {

        if (state.setFlagMustAnyAndNon(
                        F_CANCELED,
                        F_USED,
                        ERR_QUERY_NOT_USED,
                        F_SUCCESS or F_ERROR
                )
        ) {
            if (isAllowInterrupt()) {
                synchronized(syncBg) {
                    try {
                        bgThread?.let {
                            it.interrupt()
                            setBackground(false)
                            finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            response.chainTypeResponse = if (owner.chainTypeCreate() == ChainType.STAND_ALONE) {
                ChainType.STAND_ALONE
            } else {
                ChainType.LAST_ELEMENT
            }

            response.cancelInfo.set(code, message)
        }
    }

    override fun success(newResult: Any?, resultChainType: ChainType) {

        if (state.setFlagMustAnyAndNon(
                        F_SUCCESS,
                        F_USED,
                        ERR_QUERY_NOT_USED,
                        F_CANCELED or F_ERROR
                )
        ) {

            response.chainTypeResponse = if (resultChainType == ChainType.UNDEFINED) {
                owner.chainTypeCreate()
            } else {
                resultChainType
            }

            if (newResult != null) {
                response.result = newResult
            }
        }
    }


    override fun error(code: Int, msg: String?, e: Throwable?) {
        if (state.setFlagMustAnyAndNon(
                        F_ERROR,
                        F_USED,
                        ERR_QUERY_NOT_USED,
                        F_SUCCESS or F_CANCELED
                )
        ) {
            response.errorInfo.set(code, msg, e)
            state.setFlag(F_ERROR)

            response.chainTypeResponse = if (owner.chainTypeCreate() == ChainType.STAND_ALONE) {
                ChainType.STAND_ALONE
            } else {
                ChainType.LAST_ELEMENT
            }
        }
    }

    override fun error(responseInfo: ResponseInfo) {
        if (state.setFlagMustAnyAndNon(
                        F_ERROR,
                        F_USED,
                        ERR_QUERY_NOT_USED,
                        F_SUCCESS or F_CANCELED
                )
        ) {
            response.errorInfo.set(responseInfo.errorInfo)
            state.setFlag(F_ERROR)

            response.chainTypeResponse = if (owner.chainTypeCreate() == ChainType.STAND_ALONE) {
                ChainType.STAND_ALONE
            } else {
                ChainType.LAST_ELEMENT
            }
        }
    }


    override fun error(e: Throwable?) {
        error(-1, if (e != null) e.message else null, e)
    }

    override fun error(msg: String?){
        error(-1,msg, null)
    }

    override fun resultCode(code: Int) {
       response.resultCode = code
    }

    override fun resultMessage(msg: String?) {
        response.resultMessage = msg
    }

    override fun responseInfo(): ResponseInfo {
        return response
    }

}


class WorkflowAppend {
    var ui: Any? = null
    var extraParam: Any? = null
    var progressListener: ProgressListener? = null
    var callback: Callback<UserQuery>? = null
}