package com.darkraha.backend.cache

import org.junit.Assert.*
import org.junit.Test

class SoftUsageLRUCacheTest {
    @Test
    fun testCalc() {
        val c = SoftUsageLRUCache()

        val str = "My string"

        c.addMemoryCalculator(String::class.java.kotlin) {
            return@addMemoryCalculator 2 * (it as String).length
        }

        assertTrue(c.calcMemory(str) == 18)

    }


    @Test
    fun test() {
        val c = SoftUsageLRUCache(20)

        c.addMemoryCalculator(String::class.java.kotlin) {
            return@addMemoryCalculator (it as String).length
        }


        c[1] = "012345678901234"
        assertTrue(c.usage.get() == 15)

        c[2] = "0123456789"
        assertTrue(c.usage.get() == 10)
        assertTrue(c[1] == null)

        c[3] = "0"
        assertTrue(c.usage.get() == 11)

        c.remove(3)
        assertTrue(c.usage.get() == 10)
        assertTrue(c[3] == null)
    }

}
