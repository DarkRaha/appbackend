package com.darkraha.backend.cache

import com.darkraha.backend.helpers.Units
import java.lang.ref.SoftReference
import kotlin.reflect.KClass

/**
 * LRU
 *
 * @author Verma Rahul
 */
class SoftUsageLRUCache(maxMemory: Int = 8 * Units.Mb) : Cache {


    val evals = HashMap<KClass<*>, MemUsageCalculator>()
    val cache = LinkedHashMap<Any, SoftRef>(20, 0.75f, true)
    val maxUsage = maxMemory

    var usage: Int = 0

    fun addMemoryCalculator(clazz: KClass<*>, calc: MemUsageCalculator) {
        evals[clazz] = calc
    }


    /**
     * Calc memory usage in bytes. If calculator not supplied, return 0.
     */
    fun calcMemory(v: Any): Int {

        if (v != null) {
            return evals[v::class]?.invoke(v) ?: 0
        }

        return 0
    }

    override val size: Int
        get() = synchronized(cache) { cache.size }

    override fun get(key: Any): Any? {
        synchronized(cache) {
            return cache[key]?.get() ?: null
        }
    }

    override fun remove(key: Any): Any? {
        synchronized(cache) {
            val ret = cache.remove(key)

            if (ret != null) {
                usage -= ret.usage
            }

            return ret?.get()
        }
    }

    override fun clear() {
        synchronized(cache) {
            cache.clear()
            usage = 0
        }
    }

    override fun set(key: Any, value: Any) {
        synchronized(cache) {
            val ref = SoftRef(value)
            ref.usage = calcMemory(value)
            usage += ref.usage


            if (usage > maxUsage) {
                cleanup()
            }

            if (usage > maxUsage) {

                val it = cache.entries.iterator()

                while (usage > maxUsage && it.hasNext()) {

                    val entry = it.next()
                    val v = entry.value
                    usage -= v.usage
                    it.remove()
                }
            }

            cache[key] = ref
        }
    }


    private fun cleanup() {
        val it = cache.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val ref = entry.value
            if (ref.get() == null) {
                usage -= ref.usage
                it.remove()
            }
        }
    }

}

