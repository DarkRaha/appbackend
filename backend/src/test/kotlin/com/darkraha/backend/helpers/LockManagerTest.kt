package com.darkraha.backend.helpers

import org.junit.Assert.*
import org.junit.Test

class LockManagerTest {

    @Test
    fun test() {
        LockManager.lock("str")
        assertTrue(LockManager.size() == 1)
        LockManager.unLock("str")
        assertTrue(LockManager.size() == 0)
    }
}
