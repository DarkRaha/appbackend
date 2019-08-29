package com.darkraha.backend

import com.darkraha.backend.components.mainthread.MainThread


interface QueryLifeListener {
    fun onQueryStart(q: Query)

    /**
     * Called when all callbacks invoked
     */
    fun onQueryEnd(q: Query)

    /**
     * May be not called, if some user hold query and don't back
     */
    fun onQueryFree(q: Query)

}

interface ProgressListener {
    fun onStart() {}
    fun onEnd() {}
    fun onProgress(current: Long, total: Long)
}

class UIProgressListenerWrapper(pl: ProgressListener, mt: MainThread = Backend.sharedInstance!!.mainThread) :
    ProgressListener {

    val mainThread = mt
    val progressListener = pl

    override fun onProgress(current: Long, total: Long) {
        mainThread.post {
            progressListener.onProgress(current, total)
        }
    }

    override fun onEnd() {
        mainThread.post {
            progressListener.onEnd()
        }
    }

    override fun onStart() {
        mainThread.post {
            progressListener.onStart()
        }
    }
}


interface WorkflowListener {
    fun onWorkflowStep(q: UserQuery)
}

interface Processor {
    fun process(q: UserQuery, response: ClientQueryEditor)
}

/**
 * All methods invoked in main/gui thread.
 *
 * @author Verma Rahul
 */
interface Callback<T> {

    /**
     * Prepare query before executing in background.
     */
    fun onPrepare(query: T) {

    }

    /**
     *
     */
    fun onError(query: T) {

    }

    fun onSuccess(query: T) {

    }

    fun onCancel(query: T) {

    }

    /**
     * Invoked if query finished successful or with error.
     */
    fun onComplete(query: T) {

    }

    /**
     * Invoked when query handling finished (onError, onSuccess, OnCancel)
     */
    fun onFinish(query: T) {

    }

}

typealias QueryCallback = Callback<UserQuery>

interface CallbackSuccess<T> : Callback<T> {
    override fun onSuccess(query: T)
}

typealias QueryOnSuccess = CallbackSuccess<UserQuery>

interface CallbackError<T> : Callback<T> {
    override fun onError(query: T)
}

typealias QueryOnError = CallbackError<UserQuery>

interface CallbackComplete<T> : Callback<T> {
    override fun onComplete(query: T)
}

typealias QueryOnComplete = CallbackComplete<UserQuery>


interface CallbackFinish<T> : Callback<T> {
    override fun onFinish(query: T)
}

typealias QueryOnFinish = CallbackFinish<UserQuery>



