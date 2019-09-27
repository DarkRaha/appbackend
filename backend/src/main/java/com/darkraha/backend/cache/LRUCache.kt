package com.darkraha.backend.cache

/**
 * Simple implemetation of lru cache.
 *
 * @author Verma Rahul
 */
class LRUCache(maxItems: Int = 200) : Cache {
    val maxItems = maxItems
    val cache = LinkedHashMap<Any, Any?>(20, 0.75f, true)

    override val size: Int
        get() = synchronized(cache) { cache.size }


    override fun set(key: Any, value: Any) {
        synchronized(cache) {
            cache[key] = value

            if (cache.size > maxItems) {
                val it = cache.entries.iterator()

                if (it.hasNext()) {
                    it.next()
                    it.remove()
                }
            }
        }
    }

    override fun get(key: Any): Any? {
        synchronized(cache) { return cache[key] }
    }

    override fun remove(key: Any): Any? {
        synchronized(cache) { return cache.remove(key) }
    }

    override fun clear() {
        synchronized(cache) { cache.clear() }
    }

    override fun cleanup() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}