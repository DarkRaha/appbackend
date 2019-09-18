package com.darkraha.backend.livedata

import com.darkraha.backend.Backend

open class UIObservable<T>(valInit: T?, val name: String? = null,
                           val autoReset: Boolean = false,
                           val autoResetValue: T? = null) {


    protected val listeners = mutableListOf<ChangeListener<T>>()
    protected var onReset = false


    var value: T? = valInit
        set(value) {
            val oldValue = field
            field = value
            println(toString() + " set newValue=${value} oldValue=${oldValue}")
            if (!onReset && oldValue != value) {
                notifyDataChanged()
            }
        }


    fun getValue(defaultValue: T): T {
        return value.let {
            if (it == null) defaultValue else it
        }
    }

    open fun reset(resetValue: T? = null) {
        Backend.sharedInstance.mainThread.execute {
            onReset = true
            value = resetValue
            onReset = false
            println(toString() + " reset")
        }
    }

    open fun addChangeListener(listener: ChangeListener<T>) {

        listeners.add(listener)
        println(toString() + " addChangeListener")
    }


    open fun removeChangeListener(listener: ChangeListener<T>) {

        listeners.remove(listener)
        println(toString() + " removeChangeListener")
    }


    open fun notifyDataChanged() {

        val newValue = value

        Backend.sharedInstance.mainThread.execute {
            println(toString() + " notifyDataChanged")
            listeners.forEach {
                it.onChange(newValue)
            }
            if (autoReset) {
                reset(autoResetValue)
            }
        }
    }


    override fun toString(): String {
        return "UIObservable {name=${name} value=${try {
            value.toString()
        } catch (e: Exception) {
            "invalid"
        }} listeners count=${listeners.size}}"
    }

}


interface ChangeListener<T> {
    fun onChange(newValue: T?)


    companion object {
        inline operator fun <T> invoke(crossinline op: (arg1: T?) -> Unit) =
                object : ChangeListener<T> {
                    override fun onChange(newValue: T?) {
                        op(newValue)
                    }
                }
    }
}


