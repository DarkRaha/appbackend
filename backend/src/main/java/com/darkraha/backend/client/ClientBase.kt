package com.darkraha.backend.client


import com.darkraha.backend.*
import com.darkraha.backend.components.mainthread.MainThread
import com.darkraha.backend.components.mainthread.MainThreadDefault
import com.darkraha.backend.components.qmanager.QueryManager
import com.darkraha.backend.components.qmanager.QueryManagerDefault
import java.io.File
import java.util.concurrent.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Base class for clients. Main purpose of client prepare queries for one or more services.
 * @author Verma Rahul
 */
open class ClientBase : Client, QueryLifeListener {

    protected val lock = ReentrantLock()
    protected val condEndAny = lock.newCondition()
    protected val condFree = lock.newCondition()
    protected val common = Query()

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
    }

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

    override fun prepareQuery(): Query {
        val q = queryManager.newQuery()
        q.assignFrom(common)
        return q
    }


    override fun buildQuery(): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().asQueryBuilder()
    }

    /**
     * Used by composite clients that can have own executors, special callbacks and etc.
     */
    override fun buildClientQuery(): WorkflowBuilder1 {
        return prepareQuery().asWorkflowBuilder()
    }

    override fun buildQueryWithUrl(url: String): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().url(url)
    }

    override fun buildQueryWithCommand(cmd: String): QueryBuilder<WorkflowBuilder1> {
        return prepareQuery().command(cmd)
    }

    //--------------------------------------------------------
    // query life listener

    override fun onQueryStart(q: Query) {
        //  queryManager!!.onQueryStart(q)
    }

    override fun onQueryEnd(q: Query) {
        lock.withLock {
            condEndAny.signalAll()
        }
    }

    override fun onQueryFree(q: Query) {
        lock.withLock {
            condFree.signalAll()
        }
    }


    inline fun <reified T : Service> getServiceAs(): T? {
        return service as T
    }


    override fun onAddToBackend(backend: Backend) {
        mainThread = backend.mainThread
        queryManager = backend.queryManager
    }

    override fun waitQuery(query: Query, time: Long) {
        if (query.client() != this) {
            throw IllegalStateException("Query don't belong to this client")
        }
        query.workflow.waitFinish(time)
    }


    override fun waitQueryAny(time: Long) {
        lock.withLock {
            if (time > 0) {
                condEndAny.await(time, TimeUnit.MILLISECONDS)
            } else {
                condEndAny.await()
            }
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
        }


        open protected fun checkService(){
            if(_service==null){
                throw IllegalStateException("Service not assigned.")
            }
        }

        open protected fun checkExecutor() {
            if(_service!=null){
                if(_executorService==null){
                    _executorService = defaultExecuterService()
                }
            }

        }

        open protected fun checkQueryManager(){
            if(_queryManager==null){
                _queryManager = _backend?.queryManager ?:  QueryManagerDefault()
            }
        }

        open protected fun defaultExecuterService(): ExecutorService? = ThreadPoolExecutor(
            0, 6, 2L, TimeUnit.MINUTES,
            LinkedBlockingQueue<Runnable>()
        )



        open fun backend(back: Backend): Builder {
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
