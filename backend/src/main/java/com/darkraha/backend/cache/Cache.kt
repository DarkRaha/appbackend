package com.darkraha.backend.cache

import java.lang.ref.SoftReference


/**
 * Interface fo cache.
 */
interface Cache {
    val size: Int

    operator fun set(key: Any, value: Any)

    operator fun get(key: Any): Any?

    fun remove(key: Any): Any?

    fun clear()

    fun cleanup()
}


inline fun <reified T> Cache.getAs(key: Any): T? {
    return get(key) as T
}


/**
 * Helper extension of SoftReference, that allow keep memory usage of data.
 */
class SoftRef(v: Any) : SoftReference<Any>(v) {
    var usage: Int = 0
}

//interface MemoryUsage {
//    fun getMemoryUsage(obj:Any):Int
//}

typealias MemoryUsage = (Any) -> Int

