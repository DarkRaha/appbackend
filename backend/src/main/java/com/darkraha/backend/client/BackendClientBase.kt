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
    protected val _commandForTracking = HashSet<String>()
    protected val trackedQueries: LinkedList<Query> = LinkedList()

    val commandsForTracking: Set<String> get() = _commandForTracking

    //--------------------------------------------------------
    // query life listener

    override fun onQueryStart(q: Query) {
        synchronized(_commandForTracking) {
            q.getCommand()?.apply {
                if (this in _commandForTracking) {
                    onTrack(q)
                }
            }
        }
    }

    override fun onQueryEnd(q: Query) {
        synchronized(_commandForTracking) {
            q.getCommand()?.apply {
                if (this in _commandForTracking) {
                    trackedQueries.remove(q)
                }
            }
        }

        super.onQueryEnd(q)
    }


    //-----------------------------------------------------------------------------------
    fun isCommandTracked(cmd: String): Boolean {
        synchronized(_commandForTracking) {
            return cmd in _commandForTracking
        }
    }


    fun addCommandForTracking(cmd: String) {
        synchronized(_commandForTracking) {
            _commandForTracking.add(cmd)
//            if (trackedQueries == null) {
//                trackedQueries = LinkedList()
//            }
        }
    }

    fun addCommandsForTracking(vararg cmds: String) {
        synchronized(_commandForTracking) {
            _commandForTracking.addAll(cmds)
//            if (trackedQueries == null) {
//                trackedQueries = LinkedList()
//            }
        }
    }

    fun removeCommandFromTracking(cmd: String) {
        synchronized(_commandForTracking) {
            _commandForTracking.remove(cmd)
//            if (_commandForTracking.size == 0) {
//                trackedQueries = null
//            }
        }
    }


    fun isRunning(command: String?, queryId: String?): Boolean {
        synchronized(_commandForTracking) {

           val cmdId = Query.getCmdQueryIdString(command, queryId)

            if(cmdId!=null){
              return  trackedQueries.find {
                    cmdId == it.getCmdQueryId() } != null
            }else{
                return trackedQueries.size != 0
            }
        }
    }


    protected open fun onTrack(query: Query) {

        synchronized(_commandForTracking) {

            val running = isRunning(query.getCommand(), query.getQueryId())

            if (running) {
                query.cancel(-2, "Canceled by client, query already executed.")
            } else {
                trackedQueries.add(query)
            }
        }
    }
}
