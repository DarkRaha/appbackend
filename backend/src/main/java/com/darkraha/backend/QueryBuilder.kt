package com.darkraha.backend

import com.darkraha.backend.client.ClientBase
import com.darkraha.backend.components.mainthread.MainThread

import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass


interface WorkflowBuilder1 : WorkflowBuilder<WorkflowBuilder1>

interface WorkflowBuilder<Self> : QueryBuilder<Self>
        where Self : WorkflowBuilder<Self> {


    fun addOrSetFrom(q: Query): Self

    /**
     * Set executor.
     */
    fun executor(v: ExecutorService): Self

    /**
     * Sets service, that will make main work.
     */
    fun service(v: Service): Self

    /**
     * Sets first callback.
     */
    fun callbackFirst(cb: Callback<UserQuery>): Self

    /**
     * Sets last callback.
     */
    fun callbackLast(cb: Callback<UserQuery>): Self

    /**
     * Sets main thread wrapper.
     */
    fun mainThread(m: MainThread): Self

    /**
     * Sets client own query.
     */
    fun client(v: ClientBase): Self

    /**
     * Adds extra work for prepare stage.
     */
    fun addPrepareProcessor(p: Processor): Self

    /**
     * Adds extra work at start background work.
     */
    fun addPreProcessor(p: Processor): Self

    /**
     * Adds extra work after service work done.
     */
    fun addPostProcessor(p: Processor): Self

    /**
     * Chain type value, when query created.
     */
    fun chainTypeCreate(v: ChainType): Self

}

interface QueryBuilder1 : QueryBuilder<QueryBuilder1>

interface QueryBuilder<Self>
        where Self : QueryBuilder<Self> {

    fun getBuilder(): Self
    //-------------------------------------------------------------------------
    // meta info
    /**
     * Meta info, string like "GET" in http.
     */
    fun method(v: String): Self

    /**
     * Same as method("HEAD")
     */
    fun head(): Self

    /**
     * Same as method("PUT")
     */
    fun put(): Self

    /**
     * Same as method("GET")
     */
    fun get(): Self

    /**
     * Same as method("POST")
     */
    fun post(): Self

    /**
     * Same as method("DELETE")
     */
    fun delete(): Self

    /**
     * Adds header to meta info
     */
    fun addHeader(name: String, v: String): Self

    /**
     * Adds cookie to meta info
     */
    fun addCookie(name: String, v: String): Self

    /**
     * Asks service save output headers
     */
    fun optSaveOutHeaders(): Self

    /**
     * Asks service save output cookies as key/value. If you need full cookies, you must save headers,
     * and get "Set-Cookie" from that.
     */
    fun optSaveOutCookies(): Self

    //-------------------------------------------------------------------------
    // params info

    /**
     * Sets url of query.
     */
    fun url(v: String): Self

    /**
     * Sets base url of query, string like "http://example.com/{something}/api{api_version}
     */
    fun urlBase(v: String): Self

    /**
     * Adds part of path to url
     * http://example.com
     * addUrlPath("/docs")
     * http://example.com/docs
     */
    fun addUrlPath(v: String): Self

    /**
     * Replaces part of url.
     * Assume base url was "http://example.com/{something}
     * after addUrlReplace("something", "documents") final url will be
     * http://example.com/documents
     * @param can be in {} or without this
     */
    fun addUrlReplace(key: String, v: String): Self

    fun extraParam(extraParam: Any?): Self

    /**
     * Sets some object parameter that can be used by service.
     */
    fun objectParam(v: Any?): Self

    /**
     * Sets some object parameter that can be used by service as weak reference.
     */
    fun weakObjectParam(v: Any?): Self

    /**
     * Sets some ui object that can be related with query
     */
    fun uiParam(v: Any?): Self

    /**
     * Sets some ui object that can be related with query as weak reference.
     */
    fun weakUiParam(v: Any?): Self


    fun source(v: Any?, mimetype: String? = null, autoclose: Boolean = false): Self

    fun sourceClass(cls: KClass<*>?, clsItem: KClass<*>? = null): Self

    /**
     * Object that can be used as destination of data.
     */
    fun destination(v: Any?, mimetype: String? = null, autoclose: Boolean = false): Self

    fun destinationClass(cls: KClass<*>?, clsItem: KClass<*>? = null): Self

    /**
     * Add some unnamed parameter.
     */
    fun addParam(v: Any?): Self

    /**
     * @param v null value must be ignored
     * Add named param, that can be used as part of url, post query etc.
     */
    fun addNamedParam(key: String, v: String?): Self

    fun addNamedParamsAll(nparams: Map<String, String>?): Self

    /**
     * @param v null value must be ignored
     * Add special named param, that service can use.
     */
    fun addNamedSpecialParam(key: Any, v: Any?): Self

    /**
     * Saves result in memory as String if possible
     */
    fun optAsString(): Self

    /**
     * Saves result in memory as bytes if possible
     */
    fun optAsBytes(): Self

    /**
     * Saves result in file if possible
     */
    fun optAsFile(): Self


    //-------------------------------------------------------------------------
    // user workflow
    /**
     * @param cb null value must be ignored
     *
     * Adds callback, it will be invoked only in main thread.
     */
    fun addCallback(cb: Callback<UserQuery>?): Self

    /**
     * Adds workflow listener.
     */
    fun addWorkflowListener(wl: WorkflowListener): Self

    /**
     * Sets external object for synchronize background work
     */
    fun resSync(v: Any): Self

    fun progressListener(pl: ProgressListener?): Self
    fun progressListener(block: (current: Float, total: Float) -> Unit): Self

    /**
     * Is allow append user data to this query. As example while loading image for show in ImageView one,
     * you can append other ImageView.
     */
    fun allowAppend(v: Boolean): Self

    //-------------------------------------------------------------------------
    // others
    /**
     * Sets command for query.
     */
    fun command(cmd: String?): Self

    /**
     * Sets id for query
     * @param qId must be unique between queries with same command
     */
    fun queryId(qId: String?): Self

    fun comment(c: String?): Self

    //-------------------------------------------------------------------------
    //

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

inline fun <T : WorkflowBuilder<T>> WorkflowBuilder<T>.asQueryBuilder(): QueryBuilder<T> {
    return this
}


fun <T : QueryBuilder<T>> QueryBuilder<T>.addWorkflowListener(wlBlock: (query: UserQuery) -> Unit): T {
    return addWorkflowListener(object : WorkflowListener {
        inline override fun onWorkflowStep(q: UserQuery) {
            wlBlock(q)
        }
    })
}

fun <T : WorkflowBuilder<T>> WorkflowBuilder<T>.addWorkflowListenerAll(p: List<WorkflowListener>): T {
    p.forEach { addWorkflowListener(it) }
    return getBuilder()
}

fun <T : WorkflowBuilder<T>> WorkflowBuilder<T>.addPrepareProcessor(p: (q: UserQuery, r: ClientQueryEditor) -> Unit): T {
    return addPrepareProcessor(object : Processor {
        inline override fun process(q: UserQuery, response: ClientQueryEditor) {
            p(q, response)
        }
    })
}


fun <T : WorkflowBuilder<T>> WorkflowBuilder<T>.addPrepareProcessorAll(p: List<Processor>): T {
    p.forEach { addPrepareProcessor(it) }
    return getBuilder()
}


fun <T : WorkflowBuilder<T>> WorkflowBuilder<T>.addPreProcessor(p: (q: UserQuery, r: ClientQueryEditor) -> Unit): T {
    return addPreProcessor(object : Processor {
        inline override fun process(q: UserQuery, response: ClientQueryEditor) {
            p(q, response)
        }
    })
}

fun <T : WorkflowBuilder<T>> WorkflowBuilder<T>.addPreProcessorAll(p: List<Processor>): T {
    p.forEach { addPreProcessor(it) }
    return getBuilder()
}


fun <T : WorkflowBuilder<T>> WorkflowBuilder<T>.addPostProcessor(p: (q: UserQuery, r: ClientQueryEditor) -> Unit): T {
    return addPostProcessor(object : Processor {
        inline override fun process(q: UserQuery, response: ClientQueryEditor) {
            p(q, response)
        }
    })
}

fun <T : WorkflowBuilder<T>> WorkflowBuilder<T>.addPostProcessorAll(p: List<Processor>): T {
    p.forEach { addPostProcessor(it) }
    return getBuilder()
}


inline fun <T : QueryBuilder<T>> QueryBuilder<T>.addOnSuccess(cb: CallbackSuccess<UserQuery>): T {
    return addCallback(cb)
}

inline fun <T : QueryBuilder<T>> QueryBuilder<T>.addOnError(cb: CallbackError<UserQuery>): T {
    return addCallback(cb)
}


inline fun <T : QueryBuilder<T>> QueryBuilder<T>.addOnComplete(cb: CallbackComplete<UserQuery>): T {
    return addCallback(cb)
}

inline fun <T : QueryBuilder<T>> QueryBuilder<T>.addOnFinish(cb: CallbackFinish<UserQuery>): T {
    return addCallback(cb)
}


fun <T : QueryBuilder<T>> QueryBuilder<T>.addOnSuccess(block: (q: UserQuery) -> Unit): T {

    val cb = object : CallbackSuccess<UserQuery> {
        inline override fun onSuccess(query: UserQuery) {
            block(query)
        }
    }

    return addOnSuccess(cb)
}


fun <T : QueryBuilder<T>> QueryBuilder<T>.addOnComplete(block: (q: UserQuery) -> Unit): T {

    val cb = object : CallbackComplete<UserQuery> {
        inline override fun onComplete(query: UserQuery) {
            block(query)
        }
    }

    return addOnComplete(cb)
}


fun <T : QueryBuilder<T>> QueryBuilder<T>.addOnFinish(block: (q: UserQuery) -> Unit): T {

    val cb = object : CallbackFinish<UserQuery> {
        inline override fun onFinish(query: UserQuery) {
            block(query)
        }
    }

    return addOnFinish(cb)
}


fun <T : QueryBuilder<T>> QueryBuilder<T>.addOnError(block: (q: UserQuery) -> Unit): T {

    val cb = object : CallbackError<UserQuery> {
        inline override fun onError(query: UserQuery) {
            block(query)
        }
    }

    return addOnError(cb)
}