package com.darkraha.backend.client

import com.darkraha.backend.*

interface Client {


    /**
     * Adds extra processor.
     */
    fun addPrepareProcessor(p: Processor)

    /**
     * Adds extra pre processor.
     */
    fun addPreProcessor(p: Processor)

    /**
     * Adds extra post processor
     */
    fun addPostProcessor(p: Processor)

    /**
     * Adds extra processor.
     */
    fun addPrepareProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit)

    /**
     * Adds extra pre processor.
     */
    fun addPreProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit)

    /**
     * Adds extra post processor
     */
    fun addPostProcessor(block: (q: UserQuery, r: ClientQueryEditor) -> Unit)


    /**
     * Adds common workflow listener for all queries of client.
     */
    fun addWorkflowListener(wl: WorkflowListener)


    /**
     * Handles adding client to backend event.
     */
    fun onAddToBackend(backend: Backend)


    fun prepareDefaultQuery(): Query {
        return prepareQuery(null)
    }

    fun prepareQuery(otherClient: Client?): Query

    /**
     * For complex clients.
     */
    fun prepareQueryFrom(srcQuery: Query): Query


    fun buildQuery(): QueryBuilder<WorkflowBuilder1>

    /**
     * Used by composite clients that can have own executors, special callbacks and etc.
     */
    fun buildClientQuery(): WorkflowBuilder1

    fun buildQueryWithUrl(url: String): QueryBuilder<WorkflowBuilder1>

    fun buildQueryWithCommand(cmd: String): QueryBuilder<WorkflowBuilder1>

    /**
     * Waits finishing of any query.
     */
    fun waitQueryAny(time: Long = 0)


    /**
     * Waits finishing of query.
     */
    fun waitQuery(query: Query, time: Long = 0)


}