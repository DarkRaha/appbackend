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
    fun onProgress(current: Float, total: Float)
}

class UIProgressListenerWrapper(pl: ProgressListener, mt: MainThread = Backend.sharedInstance!!.mainThread) :
        ProgressListener {

    val mainThread = mt
    val progressListener = pl

    override fun onProgress(current: Float, total: Float) {
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

typealias CallbackErrorHandler = ((Exception) -> Unit)

/**
 * All methods invoked in main/gui thread.
 *
 * @author Verma Rahul
 */
interface Callback<T> {

    /**
     * Prepare query before executing in background.
     */
    @JvmDefault
    fun onPrepare(query: T) {

    }

    /**
     *
     */
    @JvmDefault
    fun onError(query: T) {

    }

    @JvmDefault
    fun onSuccess(query: T) {

    }

    @JvmDefault
    fun onCancel(query: T) {

    }

    /**
     * Invoked if query finished successful or with error.
     */
    @JvmDefault
    fun onComplete(query: T) {

    }

    /**
     * Invoked when query handling finished (onError, onSuccess, OnCancel)
     */
    @JvmDefault
    fun onFinish(query: T) {

    }


}



object CallbackUtils{

    val CB_PREPARE = 1

    val CB_ERROR = 2

    val CB_SUCCESS = 3

    val CB_CANCELED = 4

    val CB_COMPLETE = 5

    val CB_FINISHED = 6

    fun <T> dispatchCallbacks(cbState: Int, value: T, callbacks: Iterable<Callback<T>>, onException: CallbackErrorHandler? = null) {
        when (cbState) {
            CB_PREPARE -> callbacks.forEach {
                try {
                    it.onPrepare(value)
                } catch (e: Exception) {
                    onException?.invoke(e)
                }
            }
            CB_ERROR -> callbacks.forEach {
                try {
                    it.onError(value)
                } catch (e: Exception) {
                    onException?.invoke(e)
                }
            }
            CB_SUCCESS -> callbacks.forEach {
                try {
                    it.onSuccess(value)
                } catch (e: Exception) {
                    onException?.invoke(e)
                }
            }
            CB_CANCELED -> callbacks.forEach {
                try {
                    it.onCancel(value)
                } catch (e: Exception) {
                    onException?.invoke(e)
                }
            }
            CB_COMPLETE -> callbacks.forEach {
                try {
                    it.onComplete(value)
                } catch (e: Exception) {
                    onException?.invoke(e)
                }
            }
            CB_FINISHED -> callbacks.forEach {
                try {
                    it.onFinish(value)
                } catch (e: Exception) {
                    onException?.invoke(e)
                }
            }
        }
    }
}


typealias QueryCallback = Callback<UserQuery>


interface CallbackSuccess<T> : Callback<T> {
    @JvmDefault
    override fun onSuccess(query: T)
}

typealias QueryOnSuccess = CallbackSuccess<UserQuery>

interface CallbackError<T> : Callback<T> {
    @JvmDefault
    override fun onError(query: T)
}

typealias QueryOnError = CallbackError<UserQuery>

interface CallbackComplete<T> : Callback<T> {
    @JvmDefault
    override fun onComplete(query: T)
}

typealias QueryOnComplete = CallbackComplete<UserQuery>


interface CallbackFinish<T> : Callback<T> {
    @JvmDefault
    override fun onFinish(query: T)
}

typealias QueryOnFinish = CallbackFinish<UserQuery>



