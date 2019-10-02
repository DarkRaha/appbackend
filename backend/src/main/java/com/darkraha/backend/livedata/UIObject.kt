package com.darkraha.backend.livedata


import com.darkraha.backend.Backend
import com.darkraha.backend.components.mainthread.MainThread

typealias UIObjectListener<T> = (T?) -> Unit

class UIObjectEvent<T>(name: String?, resetValue: T? = null) : UIObject<T>(name, resetValue, true)

/**
 * @author Verma Rahul
 */
open class UIObject<TValue>(val name: String? = null, val resetValueDefault: TValue? = null, val autoReset: Boolean = false, val mainThread: MainThread = Backend.sharedInstance.mainThread) {

    protected val listeners = mutableListOf<UIObjectListener<TValue>>()

    @Volatile
    protected var observable: TValue? = null

    fun addListener(listener: UIObjectListener<TValue>) {
        listeners.add(listener)
    }


    fun removeListener(listener: UIObjectListener<TValue>) {
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

            dispatchToListeners()

            if (autoReset) {
                reset()
            }
        }
    }

    /**
     * Notifies observers only if observable data changed.
     */
    open fun notifyObserversWith(newValue: TValue?, afterNotify: ((TValue?) -> Unit)? = null) {
        if (observable != newValue) {

            mainThread.execute {
                if (observable != newValue) {
                    observable = newValue

                    dispatchToListeners()
                    afterNotify?.invoke(newValue)

                    if (autoReset) {
                        reset()
                    }
                }
            }
        }
    }

    fun dispatchToListeners() {
        listeners.forEach {
            try {
                it(observable)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}

