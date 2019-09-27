package com.darkraha.backend.cache

import com.darkraha.backend.helpers.Units
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.HashMap
import kotlin.reflect.KClass

/**
 * LRU
 *
 * @author Verma Rahul
 */
class SoftUsageLRUCache(maxMemory: Int = 12 * Units.Mb) : Cache {


    val evals = HashMap<KClass<*>, MemoryUsage>()
    val cache = LinkedHashMap<Any, SoftRef>(20, 0.75f, true)
    val maxUsage = maxMemory
    val isCleaning = AtomicBoolean(false)

    var usage: AtomicInteger = AtomicInteger(0)

    fun addMemoryCalculator(clazz: KClass<*>, calc: MemoryUsage) {
        evals[clazz] = calc
    }


    /**
     * Calc memory usage in bytes. If calculator not supplied, return 0.
     */
    fun calcMemory(v: Any?): Int {
        return v?.run {
            return  evals[v::class]?.invoke(v) ?: 0
        } ?: 0
    }

    override val size: Int
        get() = synchronized(cache) { cache.size }

    override fun get(key: Any): Any? = synchronized(cache) { cache[key]?.get() }


    override fun remove(key: Any): Any? {
        return synchronized(cache) {
            cache.remove(key)?.let {
                usage.addAndGet(-it.usage)
                it.get()
            }
        }
    }

    override fun clear() {
        synchronized(cache) {
            cache.clear()
            usage.set(0)
        }
    }

    override fun set(key: Any, value: Any) {

        SoftRef(value).also {
            it.usage = calcMemory(value)
            synchronized(cache) {
                usage.addAndGet(it.usage)
                cache[key] = it
            }
        }
    }


    override fun cleanup() {

        synchronized(cache) {
            if (isCleaning.get()) {
                return
            }

            isCleaning.set(true)
        }
        cleanupEmpty()
        cleanupForMemoryUsage()
        isCleaning.set(false)
    }


    private fun cleanupForMemoryUsage() {
        if (usage.get() > maxUsage) {
            val entryCopy: MutableList<Map.Entry<Any, SoftRef>> = mutableListOf()

            synchronized(cache) {
                entryCopy.addAll(cache.entries)
            }

            val max = maxUsage / 2
            entryCopy.forEach {
                remove(it.key)

                if (usage.get() < max) {
                    return
                }
            }
        }

    }

    /**
     * Removes empty soft reference.
     */
    private fun cleanupEmpty() {
        if (usage.get() > maxUsage && !isCleaning.get()) {
            val entryCopy: MutableList<Map.Entry<Any, SoftRef>> = mutableListOf()
            synchronized(cache) {
                entryCopy.addAll(cache.entries)
            }

            entryCopy.forEach {
                it.value.get() ?: remove(it.key)
            }
        }
    }

}

