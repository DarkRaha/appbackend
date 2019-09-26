package com.darkraha.backend.client


import com.darkraha.backend.*
import com.darkraha.backend.components.mainthread.MainThread
import com.darkraha.backend.components.mainthread.MainThreadDefault
import com.darkraha.backend.components.qmanager.QueryManager
import com.darkraha.backend.components.qmanager.QueryManagerDefault
import java.io.File
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.HashSet
import kotlin.concurrent.withLock

/**
 * Base class for clients. Main purpose of client prepare queries for one or more services.
 * @author Verma Rahul
 */
open class ClientBase : Client, QueryLifeListener {

    protected val lock = ReentrantLock()
    protected val common = Query()
    protected var commandForTracking = HashSet<String>()
    protected var trackedQueries: LinkedList<Query>? = null
    protected var backend: Backend? = null

    /**
     * Callbacks that will be called for every query of this client.
     * When client is part of complex client, queries can be produced not for this client.
     * In this case callbacks will not use.
     */
    val clientCallbacks = mutableListOf<QueryCallback>()

    lateinit var queryManager: QueryManager
        protected set

    var workdir: File = File("")
        protected set

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


    var service: Service?
        protected set(v) {
            common.workflow.service = v
        }
        get() = common.workflow.service


    init {
        common.client(this)
        addPostProcessor(this::onPostProcessor)
        addPreProcessor(this::onPreProcessor)
        addPrepareProcessor(this::onPrepareProcessor)
    }


    //------------------------------------------------------------------------------
    // Client processors
    open protected fun onPrepareProcessor(q: UserQuery, response: ClientQueryEditor) {

    }

    open protected fun onPreProcessor(q: UserQuery, response: ClientQueryEditor) {

    }

    open protected fun onPostProcessor(q: UserQuery, response: ClientQueryEditor) {
    }

    //------------------------------------------------------------------------------
    // Client implementation

    /**
     * Adds extra processor.
     */
    override fun addPrepareProcessor(p: Processor) {
        common.addPrepareProcessor(p)
    }

    /**
     * Adds extra pre processor.
     */
    override fun addPreProcessor(p: Processor) {
        common.addPreProcessor(p)
    }

    /**
     * Adds extra post processor
     */
    override fun addPostProcessor(p: Processor) {
        common.addPostProcessor(p)
    }


    /**
     * Adds extra processor.
     */
    override fun addPrepareProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit) {
        common.addPrepareProcessor(block)
    }

    /**
     * Adds extra pre processor.
     */
    override fun addPreProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit) {
        common.addPreProcessor(block)
    }

    /**
     * Adds extra post processor
     */
    override fun addPostProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit) {
        common.addPostProcessor(block)
    }


    /**
     * Adds common workflow listener.
     */
    override fun addWorkflowListener(wl: WorkflowListener) {
        common.addWorkflowListener(wl)
    }


    override fun prepareQuery(otherClient: Client?): Query {
        val q = otherClient?.prepareDefaultQuery() ?: queryManager.newQuery()
        if (otherClient == null) {
            q.addOrSetFrom(common)
        } else {
            q.workflow.workflowListener.clear()
            q.client(this)
                    .addPostProcessorAll(common.workflow.postProcessor)
                    .addPrepareProcessorAll(common.workflow.prepareProcessor)
                    .addPreProcessorAll(common.workflow.preProcessor)
                    .addWorkflowListenerAll(common.workflow.workflowListener)
        }

        return q
    }


    override fun prepareQueryFrom(srcQuery: Query): Query {
       return srcQuery.also {
            it.workflow.workflowListener.clear()
            it.client(this)
                    .addPostProcessorAll(common.workflow.postProcessor)
                    .addPrepareProcessorAll(common.workflow.prepareProcessor)
                    .addPreProcessorAll(common.workflow.preProcessor)
                    .addWorkflowListenerAll(common.workflow.workflowListener)
        }
    }



    override fun buildQuery(): QueryBuilder<WorkflowBuilder1> {
        return prepareDefaultQuery().asQueryBuilder()
    }

    /**
     * Used by composite clients that can have own executors, special callbacks and etc.
     */
    override fun buildClientQuery(): WorkflowBuilder1 {
        return prepareDefaultQuery().asWorkflowBuilder()
    }

    override fun buildQueryWithUrl(url: String): QueryBuilder<WorkflowBuilder1> {
        return prepareDefaultQuery().url(url)
    }

    override fun buildQueryWithCommand(cmd: String): QueryBuilder<WorkflowBuilder1> {
        return prepareDefaultQuery().command(cmd)
    }


    //--------------------------------------------------------
    // query life listener

    override fun onQueryStart(q: Query) {
        synchronized(commandForTracking) {
            q.getCommand()?.apply {
                if (this in commandForTracking) {
                    onTrack(q)
                }
            }
        }
    }

    override fun onQueryEnd(q: Query) {
        synchronized(commandForTracking) {
            q.getCommand()?.apply {
                if (this in commandForTracking) {
                    trackedQueries!!.remove(q)
                }
            }
        }



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

    }


    inline fun <reified T : Service> getServiceAs(): T? {
        return service as T
    }


    override fun onAddToBackend(backend: Backend) {
        this.backend = backend
        mainThread = backend.mainThread
        queryManager = backend.queryManager
    }

    override fun waitQuery(query: Query, time: Long) {

    }


    override fun waitQueryAny(time: Long) {

    }

    //-----------------------------------------------------------------------------------
    fun addCommandForTracking(cmd: String) {
        synchronized(commandForTracking) {
            commandForTracking.add(cmd)
            if (trackedQueries == null) {
                trackedQueries = LinkedList()
            }
        }
    }

    fun addCommandsForTracking(vararg cmds: String) {
        synchronized(commandForTracking) {
            commandForTracking.addAll(cmds)
            if (trackedQueries == null) {
                trackedQueries = LinkedList()
            }
        }
    }

    fun removeCommandFromTracking(cmd: String) {
        synchronized(commandForTracking) {
            commandForTracking.remove(cmd)
            if (commandForTracking.size == 0) {
                trackedQueries = null
            }
        }
    }


    fun isRunning(command: String?, queryId: String?): Boolean {
        synchronized(commandForTracking) {
            if (command == null && queryId == null) {
                return trackedQueries?.size != 0
            }

            return trackedQueries?.find {
                var ret = if (command != null) it.getCommand() == command else true
                ret = ret && if (queryId != null) it.getQueryId() == queryId else true
                ret
            } != null
        }
    }


    protected open fun onTrack(query: Query) {
        val cmdQueryId = query.getCmdQueryId()

        if (trackedQueries!!.find {
                    cmdQueryId == it.getCmdQueryId()
                } == null) {
            trackedQueries!!.add(query)
        } else {
            query.cancel(-2, "Canceled by client, query already executed.")
        }
    }

    //======================================================================================================
    abstract class ClientBuilder<TClient : ClientBase, TService : Service, Builder : ClientBuilder<TClient, TService, Builder>> {


        protected var result: TClient
        protected var builder: Builder

        protected var _workdir: File? = null
        protected var _service: TService? = null
        protected var _executorService: ExecutorService? = null
        protected var _mainThread: MainThread? = null
        protected var _queryManager: QueryManager? = null
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

            println("ClientBase _backend = ${_backend} mainThread = ${_backend?.mainThread?.javaClass?.simpleName} _mainThread=${
            _mainThread?.javaClass?.simpleName} client=${result.javaClass.simpleName}")
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

        open protected fun checkQueryManager() {
            if (_queryManager == null) {
                _queryManager = _backend?.queryManager ?: QueryManagerDefault()
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

        fun queryManager(qm: QueryManager): Builder {
            _queryManager = qm
            return builder
        }

        fun mainThread(mt: MainThread): Builder {
            _mainThread = mt
            return builder
        }

        open fun build(): TClient {
            checkWorkdir()
            checkMainthread()
            checkQueryManager()
            checkService()
            checkExecutor()

            result.queryManager = _queryManager!!
            result.mainThread = _mainThread!!
            result.service = _service
            result.executorService = _executorService
            result.workdir = _workdir!!

            return result
        }


    }
}
