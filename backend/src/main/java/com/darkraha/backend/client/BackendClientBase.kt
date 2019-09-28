package com.darkraha.backend.client


import com.darkraha.backend.Query
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.HashSet

/**
 * Allow tracking some queries.
 * @author Verma Rahul
 */
open class BackendClientBase : BackendClientA() {

    protected val lock = ReentrantLock()
    protected var commandForTracking = HashSet<String>()
    protected var trackedQueries: LinkedList<Query>? = null


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

        super.onQueryEnd(q)
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

        synchronized(commandForTracking) {

            trackedQueries?.find { cmdQueryId == it.getCmdQueryId() }?.apply {
                query.cancel(-2, "Canceled by client, query already executed.")
            } ?: let {
                trackedQueries?.add(query)
            }

        }
    }
}
