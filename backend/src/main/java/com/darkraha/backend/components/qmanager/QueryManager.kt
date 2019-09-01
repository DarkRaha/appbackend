package com.darkraha.backend.components.qmanager

import com.darkraha.backend.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

interface QueryManager : QueryLifeListener {


    fun sizeOfPool(): Int

    fun sizeOfExecuted(): Int

    /**
     * Borrow object from pool or create new object.
     */
    fun newQuery(): Query

    /**
     *Back query object to the pool if it's possible.
     */
    fun backQuery(query: Query)


    /**
     * Select queries.
     */
    fun select(block: (Query) -> Boolean): List<Query> = listOf()

    /**
     * Search first object that match to block.
     */
    fun findFirstOrNull(block: (Query) -> Boolean): Query? = null

    /**
     * Search last object that match to block.
     */
    fun findLastOrNull(block: (Query) -> Boolean): Query? = null


    fun clearPool()

    /**
     * Wait when all queries executed.
     */
    fun waitEmpty(time: Long = 0, timeUnit: TimeUnit = TimeUnit.MILLISECONDS)

    /**
     * Wait when some query executed.
     */
    fun waitQuery(time: Long = 0, timeUnit: TimeUnit = TimeUnit.MILLISECONDS)

}

/**
 * Default implementation of QueryManager.
 * todo awaitility
 *
 * @author Verma Rahul
 */
open class QueryManagerDefault : QueryManager {

    protected val lock = ReentrantLock()
    protected val condEmptyExecuted = lock.newCondition()
    protected val condQueryExecuted = lock.newCondition()
    protected val freeList = mutableListOf<Query>()
    protected val executedCount = AtomicInteger()


    //---------------------------------------------------------------------------
    // query life listener
    override fun onQueryStart(q: Query) {
        if (q.chainTypeCreate() == ChainType.FIRST_ELEMENT || q.chainTypeCreate() == ChainType.STAND_ALONE) {
            executedCount.incrementAndGet()
        }
    }

    override fun onQueryEnd(q: Query) {

        if (q.chainTypeResponse() == ChainType.LAST_ELEMENT || q.chainTypeResponse() == ChainType.STAND_ALONE) {
            if (executedCount.decrementAndGet() == 0) {
                lock.withLock {
                    condEmptyExecuted.signalAll()
                }
            }
        }

        lock.withLock {
            condQueryExecuted.signalAll()
        }
    }

    override fun onQueryFree(q: Query) {

    }

    //---------------------------------------------------------------------------
    // query manager

    override fun sizeOfExecuted(): Int {
        return executedCount.get()
    }

    override fun sizeOfPool(): Int {
        synchronized(freeList) {
            return freeList.size
        }
    }

    override fun newQuery(): Query {

        var ret: Query = synchronized(freeList) {
            if (freeList.size > 0) {
                freeList.removeAt(freeList.size - 1)
            } else {
                Query()
            }
        }

        ret.workflow.queryManager = this
        return ret
    }

    override fun backQuery(query: Query) {
        synchronized(freeList) {
            freeList.add(query)
        }
    }

    override fun clearPool() {
        synchronized(freeList) {
            freeList.clear()
        }
    }

    override fun waitEmpty(time: Long, timeUnit: TimeUnit) {

        if (executedCount.get() == 0) {
            return
        }

        lock.withLock {

            if (time > 0) {
                condEmptyExecuted.await(time, timeUnit)
            } else {
                condEmptyExecuted.await()
            }
        }
    }

    override fun waitQuery(time: Long, timeUnit: TimeUnit) {
        if (executedCount.get() == 0) {
            return
        }

        lock.withLock {

            if (time > 0) {
                condQueryExecuted.await(time, timeUnit)
            } else {
                condQueryExecuted.await()
            }
        }
    }


}

