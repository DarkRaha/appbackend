package com.darkraha.backend.livedata

import com.darkraha.backend.Backend
import com.darkraha.backend.components.mainthread.MainThread

typealias UIEventListener = () -> Unit
typealias UIEventTListener<T> = (T) -> Unit


class UIEvent(name: String? = null, mainThread: MainThread = Backend.sharedInstance.mainThread)
    : UIObservable<Unit, UIEventListener>(name, Unit, true, mainThread) {
    init {
        observable = Unit
    }

    override fun invokeListener(listener: UIEventListener, newValue: Unit) {
        listener()
    }
}


class UIEventT<T>(name: String? = null, resetValue: T? = null, mainThread: MainThread = Backend.sharedInstance.mainThread) :
        UIObservable<T, UIEventTListener<T>>(name, resetValue, true, mainThread) {

    override fun invokeListener(listener: UIEventTListener<T>, newValue: T) {
        newValue?.also { listener(it) }
    }

}

typealias UIPropertyListener<T> = (T) -> Unit

open class UIProperty<T>(initVal: T, name: String?, mainThread: MainThread = Backend.sharedInstance.mainThread) :
        UIObservable<T, UIPropertyListener<T>>(name, null, false, mainThread) {

    val value: T
        get() = observable!!

    init {
        observable = initVal
    }

    override fun invokeListener(listener: UIPropertyListener<T>, newValue: T) {
        listener(newValue)
    }

}