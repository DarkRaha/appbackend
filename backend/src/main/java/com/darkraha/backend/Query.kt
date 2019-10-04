package com.darkraha.backend

import com.darkraha.backend.client.BackendClientA
import com.darkraha.backend.client.BackendClientBase
import com.darkraha.backend.components.mainthread.MainThread
import com.darkraha.backend.helpers.Poolable
import com.darkraha.backend.infos.*
import java.lang.StringBuilder
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.ThreadPoolExecutor
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
        Poolable,
        WorkflowBuilder1,
        UserQuery,
        ClientQueryEditor by workflow,
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


    override fun dispatchProgress(current: Float, total: Float) {
        this.workflow.dispatchProgress(current, total)
    }

    override fun getMetaInfo(): MetaInfo {
        return meta
    }

    override fun isProgressListenerPossible(): Boolean {
        return this.workflow.isAllowAppend() || this.workflow.progressListener != null
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

    override fun addOrSetFrom(q: Query): WorkflowBuilder1 = apply {
        workflow.addOrSetFrom(q.workflow)
        params.addOrSetFrom(q.params)
        meta.addOrSetFrom(q.meta)
        _command = q._command
        _queryId = q._queryId
    }

    override fun executor(v: ThreadPoolExecutor): WorkflowBuilder1 = apply {
        workflow.executor = v
    }

    override fun service(v: Service): WorkflowBuilder1 = apply {
        workflow.service = v
    }

    override fun callbackFirst(cb: Callback<UserQuery>): WorkflowBuilder1 = apply {
        workflow.callbakFirst = cb
    }

    override fun callbackLast(cb: Callback<UserQuery>): WorkflowBuilder1 = apply {
        workflow.callbackLast = cb
    }

    override fun mainThread(m: MainThread): WorkflowBuilder1 = apply {
        workflow.mainThread = m
    }

    override fun client(v: BackendClientA): WorkflowBuilder1 = apply {
        workflow.client = v
    }

    override fun addPrepareProcessor(p: Processor): WorkflowBuilder1 = apply {
        synchronized(workflow.prepareProcessor) {
            workflow.prepareProcessor.add(p)
        }
    }

    override fun addPreProcessor(p: Processor): WorkflowBuilder1 = apply {
        synchronized(workflow.preProcessor) {
            workflow.preProcessor.add(p)
        }
    }

    override fun addPostProcessor(p: Processor): WorkflowBuilder1 = apply {
        synchronized(workflow.postProcessor) {
            workflow.postProcessor.add(p)
        }
    }

    //-----------------------------------------------------------------------------------------
    // Query meta builder methods
    override fun method(v: String): WorkflowBuilder1 = apply { meta.method = v }

    override fun head(): WorkflowBuilder1 = apply { meta.methodHead() }

    override fun put(): WorkflowBuilder1 = apply { meta.methodPut() }

    override fun get(): WorkflowBuilder1 = apply { meta.methodGet() }

    override fun post(): WorkflowBuilder1 = apply { meta.methodPost() }

    override fun delete(): WorkflowBuilder1 = apply { meta.methodDelete() }


    //-----------------------------------------------------------------------------------------
    // Query builder

    override fun getBuilder(): WorkflowBuilder1 = this


    override fun addHeader(name: String, v: String): WorkflowBuilder1 = apply {
        var hval = meta.inHeaders[name]

        if (hval == null) {
            hval = mutableListOf(v)
            meta.inHeaders[name] = hval
        } else {
            hval.add(v)
        }
    }

    override fun addCookie(name: String, v: String): WorkflowBuilder1 =
            apply { meta.inCookies[name] = v }

    override fun optSaveOutHeaders(): WorkflowBuilder1 = apply { meta.saveHeaders() }

    override fun optSaveOutCookies(): WorkflowBuilder1 = apply { meta.saveCookies() }

    override fun url(v: String): WorkflowBuilder1 = apply { params.urlBuilder.url = v }

    override fun urlBase(v: String): WorkflowBuilder1 = apply { params.urlBuilder.base = v }

    override fun addUrlPath(v: String): WorkflowBuilder1 = apply {
        params.urlBuilder.addpathes.add(v)
    }

    override fun addUrlReplace(key: String, v: String): WorkflowBuilder1 = apply {
        params.urlBuilder.addReplacement(key, v)
    }

    override fun extraParam(extraParam: Any?): WorkflowBuilder1 = apply {
        params.extraParam = extraParam
    }

    override fun objectParam(v: Any?): WorkflowBuilder1 = apply {
        params.objectParam = v
    }

    override fun weakObjectParam(v: Any?): WorkflowBuilder1 = apply {
        params.objectParam = if (v != null) {
            WeakReference<Any>(v)
        } else null
    }

    override fun uiParam(v: Any?): WorkflowBuilder1 = apply { params.uiObject = v }

    override fun weakUiParam(v: Any?): WorkflowBuilder1 = apply {
        params.uiObject = if (v != null) {
            WeakReference<Any>(v)
        } else null
    }

    override fun source(v: Any?, mimetype: String?, autoclose: Boolean): WorkflowBuilder1 = apply {
        params.run {
            source.value = v
            source.mimetype = mimetype
            autoCloseSource()
        }
    }

    override fun sourceClass(clsSource: KClass<*>?, clsSourceItem: KClass<*>?): WorkflowBuilder1 =
            apply {
                params.source.run {
                    cls = clsSource
                    clsItem = clsSourceItem
                }
            }

    override fun destination(v: Any?, mimetype: String?, autoclose: Boolean): WorkflowBuilder1 =
            apply {
                params.run {
                    destination.value = v
                    destination.mimetype = mimetype
                    autoCloseDestination()
                }
            }

    override fun destinationClass(cls: KClass<*>?, clsItem: KClass<*>?): WorkflowBuilder1 = apply {
        params.destination.cls = cls
        params.destination.clsItem = clsItem
    }

    override fun addParam(v: Any?): WorkflowBuilder1 = apply {
        if (v != null) {
            params.param.add(v)
        }
    }

    override fun addNamedParam(key: String, v: String?): WorkflowBuilder1 = apply {
        if (v != null) {
            params.namedParams[key] = v
        }
    }

    override fun addNamedParamsAll(nparams: Map<String, String>?): WorkflowBuilder1 = apply {
        if (nparams != null) {
            params.namedParams.putAll(nparams)
        }
    }

    override fun addNamedSpecialParam(key: Any, v: Any?): WorkflowBuilder1 = apply {
        if (v != null) {
            params.namedSpecialParams[key] = v
        }
    }

    override fun optAsString(): WorkflowBuilder1 = apply {
        workflow.response.setOptionSaveString()
    }

    override fun optAsBytes(): WorkflowBuilder1 = apply {
        workflow.response.setOptionSaveBytes()
    }

    override fun optAsFile(): WorkflowBuilder1 = apply {
        workflow.response.setOptionSaveFile()
    }

    override fun addCallback(cb: Callback<UserQuery>?): WorkflowBuilder1 = apply {
        cb?.also { workflow.callbacks.add(it) }
    }

    override fun addWorkflowListener(wl: WorkflowListener): WorkflowBuilder1 = apply {
        synchronized(workflow.workflowListener) {
            workflow.workflowListener.add(wl)
        }
    }

    override fun resSync(v: Any): WorkflowBuilder1 = apply {
        workflow.syncResource = v
    }


    override fun progressListener(pl: ProgressListener?): WorkflowBuilder1 = apply {
        workflow.progressListener = pl
    }


    override fun progressListener(block: (current: Float, total: Float) -> Unit): WorkflowBuilder1 =
            apply {
                workflow.progressListener = object : ProgressListener {
                    override fun onProgress(current: Float, total: Float) {
                        block(current, total)
                    }
                }
            }

    override fun command(cmd: String?): WorkflowBuilder1 = apply { _command = cmd }

    override fun queryId(qId: String?): WorkflowBuilder1 = apply { _queryId = qId }


    override fun comment(c: String?): WorkflowBuilder1 = apply { params.comment = c }

    override fun allowAppend(v: Boolean): WorkflowBuilder1 = apply { workflow.setAllowAppend(true) }

    //  fun asQueryBuilder() = this as QueryBuilder<T:>
    fun asQueryReader() = this as UserQuery

    fun asWorkflowBuilder() = this as WorkflowBuilder1

    override fun reset() {
        _command = null
        _queryId = null
        params.clear()
        workflow.clear()
        meta.clear()
    }

    //----------------------------------------------------------------

    override fun toString(): String {
        val sb = StringBuilder()

        sb.append("Query {")

        sb.append(" isSync=${workflow.isRunSync()} ")


        if (isError()) {
            sb.append("isError=${isError()} ").append(workflow.response.errorInfo).append("\n")
        }

        if (isCanceled()) {
            sb.append(workflow.response.cancelInfo).append("\n")
        }

        getCommand()?.also {
            sb.append("cmd = ").append(it)
        }


        getQueryId()?.also {
            sb.append(" queryId = ").append(it).append("\n")
        }

        url()?.also {
            sb.append("url = '").append(it).append("'\n")
        }


        sb.append("}")

        return sb.toString()
    }



    companion object{
        fun getCmdQueryIdString(cmd: String?, queryId: String?): String?{
            if (cmd == null && queryId == null) {
                return null
            }
            return (cmd ?: "") + "_" + (queryId ?: "")
        }
    }


}




