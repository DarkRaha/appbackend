package com.darkraha.backend.components.mainthread

import java.util.concurrent.Executors

/**
 * Wrapper over main/gui thread.
 * TODO use kotlin opportunities
 *
 * @author Verma Rahul
 */
abstract class MainThread {
    /**
     * Checks whether the current thread is the main thread.
     */
    open fun isMainThread(): Boolean = false

    /**
     * If current thread is main thread then executes block.
     * Otherwise adds block to the queue of main thread.
     */
    abstract fun execute(block: () -> Unit)

    /**
     * Adds block to queue of main thread.
     */
    abstract fun post(block: () -> Unit)


    companion object {
        @JvmStatic
        var sharedInstance: MainThread? = null

    }
}


class MainThreadDefault() : MainThread() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun execute(block: () -> Unit) = executor.execute(block)
    override fun post(block: () -> Unit) = executor.execute(block)

}



