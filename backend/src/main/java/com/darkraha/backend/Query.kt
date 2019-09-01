package com.darkraha.backend

import com.darkraha.backend.client.ClientBase
import com.darkraha.backend.components.mainthread.MainThread
import com.darkraha.backend.infos.*
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass


/**
 * If query has assigned client, then it will be free for reusing,
 * all data will be cleared.
 *
 *
 *@author Verma Rahul
 */
class Query(
    val params: ParamInfo = ParamInfo(),
    val meta: MetaInfo = MetaInfo(),
    val workflow: WorkflowManager = WorkflowManager()
) :
    WorkflowBuilder1,
    UserQuery,
    ClientQueryEditor,
    ServiceWorkflowHelper,
    ParamReader by params,
    MetaInfoReader by meta,
    WorkflowState by workflow, WorkflowReader by workflow, Workflow by workflow,
    WorkflowExecutor by workflow,
    ResultReader by workflow.response, ResultOptionsReader by workflow.response,
    ErrorReader by workflow.response.errorInfo {


    internal var _command: String? = null

    /**
     * Queries don't must have same queryId with same command.
     */
    internal var _queryId: String? = null

    // internal var _chainType = ChainType.STAND_ALONE


    //------------------------------------------------------------------------------------------
    // Service workflow helper

    init {
        workflow.owner = this
    }

    override fun dispatchProgress(current: Long, total: Long) {
        this.workflow.dispatchProgress(current, total)
    }

    override fun getMetaInfo(): MetaInfo {
        return meta
    }

    override fun isProgressListenerPossible(): Boolean {
        return this.workflow.isAllowAppend() || this.workflow.progressListener != null
    }

    override fun assignFrom(src: ResponseInfo) {
        this.workflow.response.assignFrom(src)
    }


    //------------------------------------------------------------------------------------------
    // client query editor


    override fun getUserCallbacks(): List<Callback<UserQuery>> {
        return workflow.callbacks
    }

    override fun setResult(r: Any?) {
        workflow.response.result = r
    }

    override fun setRawMimetype(mt: String?) {
        workflow.response.rawMimetype = mt
    }

    override fun setRawSize(s: Long) {
        workflow.response.rawSize = s
    }

    override fun setRawString(s: String?, asResult: Boolean) {
        workflow.response.rawResultString = s
        if (asResult) {
            workflow.response.result = s
        }
    }

    override fun setRawBytes(b: ByteArray?, asResult: Boolean) {
        workflow.response.rawResultBytes = b
        if (asResult) {
            workflow.response.result = b
        }
    }

    override fun setResultFile(file: File?) {
        workflow.response.rawResultFile = file
    }

    override fun cancel(code: Int, message: String?) {
        workflow.cancel(code, message)
    }

    override fun success(newResult: Any?, resultChainType: ChainType) {
        workflow.success(newResult, resultChainType)
    }

    override fun error(e: Throwable?) {
        workflow.error(e)
    }

    override fun error(code: Int, msg: String?, e: Throwable?) {
        workflow.error(code, msg, e)
    }

    override fun error(responseInfo: ResponseInfo) {
        workflow.error(responseInfo)
    }

    override fun resultCode(code: Int) {
        workflow.response.resultCode = code
    }

    override fun resultMessage(msg: String?) {
        workflow.response.resultMessage = msg
    }

    override fun responseInfo(): ResponseInfo {
        return workflow.response
    }

    //------------------------------------------------------------------------------------------
    // Reader

    override fun getCmdQueryId(): String? {
        if (_command == null && _queryId == null) {
            return null
        }
        return (_command ?: "") + "_" + (_queryId ?: "")
    }

    override fun getCommand(): String? {
        return _command
    }

    override fun getQueryId(): String? {
        return _queryId
    }

    override fun getComment(): String? {
        return params.comment
    }


    //---------------------------------------------------------------------------
    // builder

    override fun chainTypeCreate(v: ChainType): WorkflowBuilder1 {
        params.chainTypeCreate = v
        return this
    }

    override fun assignFrom(q: Query): WorkflowBuilder1 {

        this.workflow.assignFrom(q.workflow)
        params.assignFrom(q.params)
        meta.assignFrom(q.meta)
        _command = q._command
        _queryId = q._queryId
        return this
    }

    override fun executor(v: ExecutorService): WorkflowBuilder1 {
        this.workflow.executor = v
        return this
    }

    override fun service(v: Service): WorkflowBuilder1 {
        this.workflow.service = v
        return this
    }

    override fun callbackFirst(cb: Callback<UserQuery>): WorkflowBuilder1 {
        this.workflow.callbakFirst = cb
        return this
    }

    override fun callbackLast(cb: Callback<UserQuery>): WorkflowBuilder1 {
        this.workflow.callbackLast = cb
        return this
    }

    override fun mainThread(m: MainThread): WorkflowBuilder1 {
        this.workflow.mainThread = m
        return this
    }

    override fun client(v: ClientBase): WorkflowBuilder1 {
        this.workflow.client = v
        return this
    }

    override fun addPrepareProcessor(p: Processor): WorkflowBuilder1 {
        synchronized(this.workflow.prepareProcessor) {
            this.workflow.prepareProcessor.add(p)
        }
        return this
    }

    override fun addPreProcessor(p: Processor): WorkflowBuilder1 {
        synchronized(this.workflow.preProcessor) {
            this.workflow.preProcessor.add(p)
        }
        return this
    }

    override fun addPostProcessor(p: Processor): WorkflowBuilder1 {
        synchronized(this.workflow.postProcessor) {
            this.workflow.postProcessor.add(p)
        }
        return this
    }

    //-----------------------------------------------------------------------------------------
    // Query builder methods
    override fun method(v: String): WorkflowBuilder1 {
        meta.method = v
        return this
    }

    override fun head(): WorkflowBuilder1 {
        meta.methodHead()
        return this
    }

    override fun put(): WorkflowBuilder1 {
        meta.methodPut()
        return this
    }

    override fun get(): WorkflowBuilder1 {
        meta.methodGet()
        return this
    }

    override fun post(): WorkflowBuilder1 {
        meta.methodPost()
        return this
    }

    override fun delete(): WorkflowBuilder1 {
        meta.methodDelete()
        return this
    }

    //-----------------------------------------------------------------------------------------
    // Query builder


    override fun addHeader(name: String, v: String): WorkflowBuilder1 {
        var hval = meta.inHeaders[name]

        if (hval == null) {
            hval = mutableListOf(v)
            meta.inHeaders[name] = hval
        } else {
            hval.add(v)
        }

        return this
    }

    override fun addCookie(name: String, v: String): WorkflowBuilder1 {
        meta.inCookies[name] = v
        return this
    }

    override fun optSaveOutHeaders(): WorkflowBuilder1 {
        meta.saveHeaders()
        return this
    }

    override fun optSaveOutCookies(): WorkflowBuilder1 {
        meta.saveCookies()
        return this
    }

    override fun url(v: String): WorkflowBuilder1 {
        params.urlBuilder.url = v
        return this
    }

    override fun urlBase(v: String): WorkflowBuilder1 {
        params.urlBuilder.base = v
        return this
    }

    override fun addUrlPath(v: String): WorkflowBuilder1 {
        params.urlBuilder.addpathes.add(v)
        return this
    }

    override fun addUrlReplace(key: String, v: String): WorkflowBuilder1 {
        params.urlBuilder.addReplacement(key, v)
        return this
    }

    override fun extraParam(extraParam: Any?): WorkflowBuilder1 {
        params.extraParam = extraParam
        return this
    }

    override fun objectParam(v: Any?): WorkflowBuilder1 {
        params.objectParam = v
        return this
    }

    override fun weakObjectParam(v: Any?): WorkflowBuilder1 {

        params.objectParam = if (v != null) {
            WeakReference<Any>(v)
        } else null

        return this
    }

    override fun uiParam(v: Any?): WorkflowBuilder1 {
        params.uiObject = v
        return this
    }

    override fun weakUiParam(v: Any?): WorkflowBuilder1 {
        params.uiObject = if (v != null) {
            WeakReference<Any>(v)
        } else null

        return this
    }

    override fun source(v: Any?, mimetype: String?, autoclose: Boolean): WorkflowBuilder1 {
        params.source.value = v
        params.source.mimetype = mimetype
        params.autoCloseSource()
        return this
    }

    override fun sourceClass(cls: KClass<*>?, clsItem: KClass<*>?): WorkflowBuilder1 {
        params.source.cls = cls
        params.source.clsItem = clsItem
        return this
    }

    override fun destination(v: Any?, mimetype: String?, autoclose: Boolean): WorkflowBuilder1 {
        params.destination.value = v
        params.destination.mimetype = mimetype
        params.autoCloseDestination()
        return this
    }

    override fun destinationClass(cls: KClass<*>?, clsItem: KClass<*>?): WorkflowBuilder1 {
        params.destination.cls = cls
        params.destination.clsItem = clsItem
        return this
    }

    override fun addParam(v: Any?): WorkflowBuilder1 {
        if (v != null) {
            params.param.add(v)
        }
        return this
    }

    override fun addNamedParam(key: String, v: String?): WorkflowBuilder1 {
        if (v != null) {
            params.namedParams[key] = v
        }
        return this
    }

    override fun addNamedParamsAll(nparams: Map<String, String>?): WorkflowBuilder1 {
        if (nparams != null) {
            params.namedParams.putAll(nparams)
        }

        return this
    }

    override fun addNamedSpecialParam(key: Any, v: Any?): WorkflowBuilder1 {
        if (v != null) {
            params.namedSpecialParams[key] = v
        }
        return this
    }

    override fun optAsString(): WorkflowBuilder1 {
        this.workflow.response.setOptionSaveString()
        return this
    }

    override fun optAsBytes(): WorkflowBuilder1 {
        this.workflow.response.setOptionSaveBytes()
        return this
    }

    override fun optAsFile(): WorkflowBuilder1 {
        this.workflow.response.setOptionSaveFile()
        return this
    }

    override fun addCallback(cb: Callback<UserQuery>?): WorkflowBuilder1 {
        if (cb != null) {
            this.workflow.callbacks.add(cb)
        }
        return this
    }

    override fun addWorkflowListener(wl: WorkflowListener): WorkflowBuilder1 {
        synchronized(this.workflow.workflowListener) {
            this.workflow.workflowListener.add(wl)
        }
        return this
    }

    override fun resSync(v: Any): WorkflowBuilder1 {
        this.workflow.syncResource = v
        return this
    }


    override fun progressListener(pl: ProgressListener?): WorkflowBuilder1 {
        this.workflow.progressListener = pl
        return this
    }


    override fun progressListener(block: (current: Long, total: Long) -> Unit): WorkflowBuilder1 {
        this.workflow.progressListener = object : ProgressListener {
            override fun onProgress(current: Long, total: Long) {
                block(current, total)
            }
        }
        return this
    }

    override fun command(cmd: String?): WorkflowBuilder1 {
        _command = cmd
        return this
    }

    override fun queryId(qId: String?): WorkflowBuilder1 {
        _queryId = qId
        return this
    }


    override fun comment(c: String?): WorkflowBuilder1 {
        params.comment = c
        return this
    }


    override fun allowAppend(v: Boolean): WorkflowBuilder1 {
        this.workflow.setAllowAppend(true)
        return this
    }


    //  fun asQueryBuilder() = this as QueryBuilder<T:>
    fun asQueryReader() = this as UserQuery

    fun asWorkflowBuilder() = this as WorkflowBuilder1

    fun clear() {
        _command = null
        _queryId = null
        params.clear()
        workflow.clear()
        meta.clear()
    }


}




