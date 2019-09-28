package com.darkraha.backend.components.mainthread

import java.util.concurrent.Executors
import kotlin.concurrent.timer

/**
 * Wrapper over main/gui thread.
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

    abstract fun postDelayed(timeDelay: Long, block: () -> Unit)

    companion object {
        @JvmStatic
        var sharedInstance: MainThread? = null

    }
}


class MainThreadDefault() : MainThread() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun execute(block: () -> Unit) = executor.execute(block)
    override fun post(block: () -> Unit) = executor.execute(block)
    override fun postDelayed(timeDelay: Long, block: () -> Unit) {
        //timer(null,false,)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}



