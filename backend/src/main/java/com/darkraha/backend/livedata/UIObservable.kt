package com.darkraha.backend.livedata

import com.darkraha.backend.Backend
import com.darkraha.backend.components.mainthread.MainThread

/**
 * @author Verma Rahul
 */
abstract class UIObservable<TValue, TListener>(val name: String? = null, val resetValueDefault: TValue?, val autoReset: Boolean, val mainThread: MainThread = Backend.sharedInstance.mainThread) {

    protected val listeners = mutableListOf<TListener>()

    @Volatile
    protected var observable: TValue? = null

    val listenersCount
        get() = listeners.size

    open fun addListener(listener: TListener) {
        listeners.add(listener)
        println(toString() + " addChangeListener")
    }


    open fun removeListener(listener: TListener) {
        listeners.remove(listener)
        println(toString() + " removeChangeListener")
    }

    open fun reset(resetValue: TValue? = null) {
        observable = resetValue ?: resetValueDefault
    }

    /**
     * Notifies observers with current observable data.
     */
    open fun notifyObservers() {
        mainThread.execute {

            println(toString() + " notifyDataChanged observable${try {
                observable
            } catch (e: Exception) {
                "invalid"
            }}")


            observable?.apply {
                listeners.forEach {
                    invokeListener(it, this)
                }
            }

            if (autoReset) {
                reset()
            }
        }
    }

    /**
     * Notifies observers only if observable data changed.
     */
    open fun notifyObserversWith(newValue: TValue, afterNotify: ((TValue) -> Unit)? = null) {
        if (observable != newValue) {

            mainThread.execute {
                if (observable != newValue) {
                    observable = newValue

                    println(toString() + " notifyDataChanged")
                    listeners.forEach {
                        invokeListener(it, newValue)
                    }

                    afterNotify?.invoke(newValue)

                    if (autoReset) {
                        reset()
                    }
                }
            }
        }
    }

    protected abstract fun invokeListener(listener: TListener, newValue: TValue)

}