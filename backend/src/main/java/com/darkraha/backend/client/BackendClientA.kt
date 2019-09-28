package com.darkraha.backend.client

import com.darkraha.backend.*
import com.darkraha.backend.components.mainthread.MainThread
import com.darkraha.backend.components.mainthread.MainThreadDefault
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


/**
 * Base class of backend client. Main purpose create query for service and run it in background.
 * @author Verma rahul
 */
abstract class BackendClientA : QueryLifeListener {

    protected val common = Query()
    protected var backend: Backend? = null

    protected var executorService: ExecutorService?
        set(value) {
            common.workflow.executor = value
        }
        get() = common.workflow.executor

    protected var mainThread: MainThread?
        set(value) {
            common.workflow.mainThread = value
        }
        get() = common.workflow.mainThread


    var workdir: File = File("")
        protected set

    var service: Service?
        protected set(v) {
            common.workflow.service = v
        }
        get() = common.workflow.service

    /**
     * Callbacks that will be called for every query of this client.
     * When client is part of complex client, queries can be produced not for this client.
     * In this case callbacks will not use.
     */
    val clientCallbacks = mutableListOf<QueryCallback>()

    init {
        common.client(this)
        addPostProcessor(this::onPostProcessor)
        addPreProcessor(this::onPreProcessor)
        addPrepareProcessor(this::onPrepareProcessor)
    }


    //------------------------------------------------------------------------------
    //
    /**
     * Adds extra processor.
     */
    fun addPrepareProcessor(p: Processor) = common.addPrepareProcessor(p)


    /**
     * Adds extra pre processor.
     */
    fun addPreProcessor(p: Processor) = common.addPreProcessor(p)

    /**
     * Adds extra post processor
     */
    fun addPostProcessor(p: Processor) = common.addPostProcessor(p)

    /**
     * Adds extra processor.
     */
    fun addPrepareProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit) = common.addPrepareProcessor(block)

    /**
     * Adds extra pre processor.
     */
    fun addPreProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit) = common.addPreProcessor(block)

    /**
     * Adds extra post processor
     */
    fun addPostProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit) = common.addPostProcessor(block)


    /**
     * Adds common workflow listener for all queries of this client.
     */
    fun addWorkflowListener(wl: WorkflowListener) = common.addWorkflowListener(wl)


    //------------------------------------------------------------------------------
    // Client processors

    /**
     * Client automatically add this method as prepare processor.
     */
    open protected fun onPrepareProcessor(q: UserQuery, response: ClientQueryEditor) {

    }

    /**
     * Client automatically add this method as pre processor.
     */
    open protected fun onPreProcessor(q: UserQuery, response: ClientQueryEditor) {

    }

    /**
     * Client automatically add this method as post processor.
     */
    open protected fun onPostProcessor(q: UserQuery, response: ClientQueryEditor) {
    }


    //------------------------------------------------------------------------------
    // Query building

    /**
     * @param srcQuery non null value used in complex clients (that uses other clients)
     */
    open fun prepareQuery(srcQuery: Query? = null): Query {
        return srcQuery?.also {
            it.workflow.workflowListener.clear()
            it.client(this)
                    .addPostProcessorAll(common.workflow.postProcessor)
                    .addPrepareProcessorAll(common.workflow.prepareProcessor)
                    .addPreProcessorAll(common.workflow.preProcessor)
                    .addWorkflowListenerAll(common.workflow.workflowListener)
        } ?: backend?.queryPool?.getObject()?.also { it.addOrSetFrom(common) }
        ?: Query().apply { addOrSetFrom(common) }
    }

    open fun buildQuery(): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().asQueryBuilder()
    }


    fun buildQueryWith(url: String?, cmd: String?, queryId: String)
            : QueryBuilder<WorkflowBuilder1> = buildQuery().also {
        url?.apply { it.url(this) }
        it.command(cmd)
        it.queryId(queryId)
    }


    //------------------------------------------------------------------------------
    // Query life cycle

    /**
     * Default empty implementation.
     */
    override fun onQueryStart(q: Query) {

    }


    override fun onQueryEnd(q: Query) {
        backend?.also {
            if (q.isError()) {
                if (it.errorFilter.isNotifyError(q)) {
                    q.use()
                    it.error.notifyObserversWith(q) {
                        it.free()
                    }
                }
            }
        }
    }

    override fun onQueryFree(q: Query) {
        backend?.queryPool?.backObject(q)
    }

    //------------------------------------------------------------------------------
    //

    open fun onAddToBackend(backend: Backend) {
        this.backend = backend
        mainThread = backend.mainThread

    }


    //------------------------------------------------------------------------------

    abstract class ClientBuilder<TClient : BackendClientA, TService : Service, Builder : ClientBuilder<TClient, TService, Builder>> {


        protected var result: TClient
        protected var builder: Builder

        protected var _workdir: File? = null
        protected var _service: TService? = null
        protected var _executorService: ExecutorService? = null
        protected var _mainThread: MainThread? = null
        protected var _backend: Backend? = null


        init {
            result = newResult()
            builder = this as Builder
        }


        protected abstract fun newResult(): TClient


        open protected fun checkWorkdir() {
            if (_workdir == null) {
                _workdir = _backend?.workdir ?: File("workdir")
                _workdir!!.mkdirs()
            }
        }

        open protected fun checkMainthread() {
            if (_mainThread == null) {
                _mainThread = _backend?.mainThread ?: MainThreadDefault()
            }

            println(
                    "ClientBase _backend = ${_backend} mainThread = ${_backend?.mainThread?.javaClass?.simpleName} _mainThread=${
                    _mainThread?.javaClass?.simpleName} client=${result.javaClass.simpleName}"
            )
        }


        open protected fun checkService() {
            if (_service == null) {
                throw IllegalStateException("Service not assigned.")
            }
        }

        open protected fun checkExecutor() {
            if (_service != null) {
                if (_executorService == null) {
                    _executorService = defaultExecuterService()
                }
            }

        }

        open protected fun defaultExecuterService(): ExecutorService? = ThreadPoolExecutor(
                0, 6, 2L, TimeUnit.MINUTES,
                LinkedBlockingQueue<Runnable>()
        )


        open fun backend(back: Backend?): Builder {
            _backend = back
            return builder
        }

        fun workdir(wd: File): Builder {
            _workdir = wd
            return builder
        }

        fun service(srv: TService): Builder {
            _service = srv
            return builder
        }

        fun executorService(exeSrv: ExecutorService): Builder {
            _executorService = exeSrv
            return builder
        }

        fun mainThread(mt: MainThread): Builder {
            _mainThread = mt
            return builder
        }

        open fun build(): TClient {
            checkWorkdir()
            checkMainthread()
            checkService()
            checkExecutor()

            result.mainThread = _mainThread!!
            result.service = _service
            result.executorService = _executorService
            result.workdir = _workdir!!
            return result
        }
    }
}