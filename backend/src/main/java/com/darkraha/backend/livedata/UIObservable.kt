package com.darkraha.backend.livedata

import com.darkraha.backend.Backend
import com.darkraha.backend.components.mainthread.MainThread

/**
 * @author Verma Rahul
 */
abstract class UIObservable<TValue, TListener>(val name: String? = null,
                                               val resetValueDefault: TValue?,
                                               val autoReset: Boolean,
                                               val mainThread: MainThread = Backend.sharedInstance.mainThread) {

    protected val listeners = mutableListOf<TListener>()

    @Volatile
    protected var observable: TValue? = null

    val listenersCount
        get() = listeners.size

     fun addListener(listener: TListener) {
        listeners.add(listener)
    }


     fun removeListener(listener: TListener) {
        listeners.remove(listener)
    }

    fun reset(resetValue: TValue? = null) {
        observable = resetValue ?: resetValueDefault
    }

    /**
     * Notifies observers with current observable data.
     */
    open fun notifyObservers() {
        mainThread.execute {
            try {
                observable?.apply {
                    dispatchToListeners(this)
                }
            } catch (e: Exception) {
                //observable can be invald
                e.printStackTrace()
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

                    dispatchToListeners(newValue)
                    afterNotify?.invoke(newValue)

                    if (autoReset) {
                        reset()
                    }
                }
            }
        }
    }

    protected fun dispatchToListeners(newValue: TValue) {
        listeners.forEach {
            try {
                invokeListener(it, newValue)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    protected abstract fun invokeListener(listener: TListener, newValue: TValue)

}