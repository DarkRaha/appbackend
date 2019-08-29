package com.darkraha.backend.cache

import com.darkraha.backend.helpers.Units
import kotlin.reflect.KClass

/**
 * @author Verma Rahul
 */
class UsageLRUCache(maxMemory: Int = 8 * Units.Mb) : Cache {


    val evals = HashMap<KClass<*>, MemUsageCalculator>()
    val cache = LinkedHashMap<Any, Any?>(20, 0.75f, true)
    val maxUsage = maxMemory

    var usage: Int = 0

    fun addMemoryCalculator(clazz: KClass<*>, calc: MemUsageCalculator) {
        evals[clazz] = calc
    }

    /**
     * Calc memory usage in bytes. If calculator not supplied, return 0.
     */
    fun calcMemory(v: Any): Int {
        return evals[v::class]?.invoke(v) ?: 0
    }

    override val size: Int
        get() = synchronized(cache) { cache.size }

    override fun get(key: Any): Any? {
        synchronized(cache) { return cache[key] }
    }

    override fun remove(key: Any): Any? {
        synchronized(cache) {
            val ret = cache.remove(key)

            if (ret != null) {
                usage -= calcMemory(ret)
            }

            return ret
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
            usage += calcMemory(value)

            val it = cache.entries.iterator()

            while (usage > maxUsage && it.hasNext()) {

                val v = it.next()

                if (v != null) {
                    val mem = calcMemory(v.value!!)
                    usage -= mem
                    it.remove()
                }
            }
            cache[key] = value
        }
    }

}