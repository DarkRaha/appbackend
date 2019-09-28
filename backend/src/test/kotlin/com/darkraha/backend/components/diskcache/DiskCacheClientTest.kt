package com.darkraha.backend.components.diskcache

import org.junit.Assert.*
import org.junit.Test

class DiskCacheClientTest {

    val dc = DiskCacheClient.newInstance()

    @Test
    fun test() {
        assertTrue(dc.workdir.isDirectory)
        val subClient = dc.subClient("tst")
        assertTrue(subClient.getWorkDir().isDirectory)
        dc.buildClear().exeSync()
        assertTrue(!subClient.getWorkDir().isDirectory)
        //  println(dc.genFile("https://pm1.narvii.com/6652/96cfbc896f4f277f98f09d049bd835baed62a0bf_hq.jpg"))
        // println(dc.g("https://pm1.narvii.com/6652/96cfbc896f4f277f98f09d049bd835baed62a0bf_hq.jpg"))
        dc.workdir.delete()

    }


}