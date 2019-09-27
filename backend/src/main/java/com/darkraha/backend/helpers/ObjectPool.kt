package com.darkraha.backend.helpers

import java.util.*

interface Poolable {
    fun reset() {

    }
}

class ObjectPool<T : Poolable>(val maxItems: Int = 100, val factory: () -> T) {
    val list = LinkedList<T>()

    fun getObject(): T {
        synchronized(list) {
            return list.poll() ?: factory()
        }
    }

    fun backObject(obj: T) {
        obj.reset()
        synchronized(list) {
            if (list.size < maxItems) {
                list.add(obj)
            }
        }
    }

}

