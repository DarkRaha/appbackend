package com.darkraha.backend.helpers

import com.darkraha.backend.UserQuery
import java.util.concurrent.atomic.AtomicBoolean


typealias ErrorFilter = (UserQuery) -> Boolean


abstract class AllowErrorFilterWithTimeout {
    private var timeOfLastErrorShow = 0L
    var showErrorTimeout = 30L * 1000L

    fun onFilter(query: UserQuery): Boolean {
        if (System.currentTimeMillis() - timeOfLastErrorShow > showErrorTimeout) {
            val ret = isAllowed(query)
            if (ret) {
                println("AllowErrorFilterWithTimeout ok")
                timeOfLastErrorShow = System.currentTimeMillis()
            }
             return ret
        }

        return false
    }

    abstract protected fun isAllowed(query: UserQuery): Boolean

}

open class ErrorFilterManager {

    protected val _isActive: AtomicBoolean = AtomicBoolean(true)
    var isActive
        set(value) {
            _isActive.set(value)
        }
        get() = _isActive.get()

    protected var filtersAllowed = mutableListOf<ErrorFilter>()
    protected var filtersDisallowed = mutableListOf<ErrorFilter>()

    fun addAllowFilter(errorFilter: ErrorFilter) {
        synchronized(filtersAllowed) {
            filtersAllowed.add(errorFilter)
        }
    }

    fun removeAllowFilter(errorFilter: ErrorFilter) {
        synchronized(filtersAllowed) {
            filtersAllowed.remove(errorFilter)
        }
    }

    fun addAllowFilter(errorFilter: AllowErrorFilterWithTimeout) {
        synchronized(filtersAllowed) {
            filtersAllowed.add(errorFilter::onFilter)
        }
    }

    fun removeAllowFilter(errorFilter: AllowErrorFilterWithTimeout) {
        synchronized(filtersAllowed) {
            filtersAllowed.remove(errorFilter::onFilter)
        }
    }

    fun addDisallowFilter(errorFilter: ErrorFilter) {
        synchronized(filtersDisallowed) {
            filtersDisallowed.add(errorFilter)
        }
    }

    fun removeDisallowFilter(errorFilter: ErrorFilter) {
        synchronized(filtersDisallowed) {
            filtersDisallowed.remove(errorFilter)
        }
    }

    open fun isNotifyError(query: UserQuery): Boolean {
        if (!isActive) {
            return false
        }

        var ret = true

        synchronized(filtersDisallowed) {
            filtersDisallowed.forEach {
                if (it(query)) {
                    ret = false
                    return@forEach
                }
            }
        }

        if (ret) {
            synchronized(filtersAllowed) {

                if(filtersAllowed.size>0) {
                    filtersAllowed.forEach {
                        if (it(query)) {
                            return true
                        }
                    }
                }else{
                    return true
                }
            }
        }

        return false
    }


}