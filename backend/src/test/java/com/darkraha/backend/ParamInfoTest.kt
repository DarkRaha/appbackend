package com.darkraha.backend

import com.darkraha.backend.infos.ParamInfo
import org.junit.Assert.*
import org.junit.Test

class ParamInfoTest {

    @Test
    fun test() {

        val src = ParamInfo()

        val dst = ParamInfo()


        src.urlBuilder.url = "http://example.com"
        src.destination.value = "destination"
        src.source.value = "source"
        src.extraParam = "extraparam"
        src.objectParam = "objectparam"
        src.uiObject = "ui"
        src.param.add("param1")
        src.param.add("param2")
        src.namedParams.put("key1", "val1")

        dst.assignFrom(src)
        assertTrue(dst.urlBuilder.url == "http://example.com")
        assertTrue(dst.destination == src.destination)
        assertTrue(dst.source == src.source)
        assertTrue(dst.extraParam == src.extraParam)
        assertTrue(dst.objectParam == src.objectParam)
        assertTrue(dst.uiObject == src.uiObject)
        assertTrue(dst.param.size == 2)
        assertTrue(dst.param[1] == "param2")
        assertTrue(dst.namedParams.size == 1)
        assertTrue(dst.namedParams.get("key1") == "val1")


        assertTrue(!dst.isOptionAutoCloseDestination())
        assertTrue(!dst.isOptionAutoCloseSource())

        dst.autoCloseDestination()
        assertTrue(dst.isOptionAutoCloseDestination())
        assertTrue(!dst.isOptionAutoCloseSource())

        dst.autoCloseSource()
        assertTrue(dst.isOptionAutoCloseDestination())
        assertTrue(dst.isOptionAutoCloseSource())
    }

}