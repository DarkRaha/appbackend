package com.darkraha.backend.cache

import org.junit.Assert.*
import org.junit.Test

/**
 * @author Verma Rahul
 */
class LRUCacheTest {

    @Test
    fun test() {

        val lruCache = LRUCache(10)
        lruCache.clear()

        for (i in 0..19) {
            lruCache[i] = i
        }

        assertTrue(lruCache.size == 10)

        var a: Int? = -1

        a = lruCache.getAs(0)
        assertTrue(a == null)

        a = lruCache.getAs(9)
        assertTrue(a == null)

        a = lruCache.getAs(10)
        assertTrue(a == 10)

        a = lruCache.getAs(19)
        assertTrue(a == 19)

        lruCache[5] = 5

        assertTrue(lruCache[5]==5)

        a = lruCache.getAs(11)
        assertTrue(a == null)


        lruCache.remove(5)
        assertTrue(lruCache.size == 9)

        lruCache.clear()
        assertTrue(lruCache.size == 0)
    }

}