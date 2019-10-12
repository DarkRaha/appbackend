package com.darkraha.backend

import com.darkraha.backend.infos.MetaInfo


interface ServiceWorkflowHelper {
    fun dispatchProgress(current: Float, total: Float)
    fun getMetaInfo(): MetaInfo

    /**
     * Check is query has progress listener, or it can be appear in future (when query allow appending).
     */
    fun isProgressListenerPossible(): Boolean
}

// TODO make class with lock
interface Service {

    val schemes: List<String>
        get() =
            listOf()


    val commands: List<String>
        get() = listOf()


    /**
     * Check is service available.
     */
    fun isAvailable(): Boolean

    /**
     *
     */
    fun handle(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor);

    fun isIndeterminateProgress() = false



}
