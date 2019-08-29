package com.darkraha.backend

import com.darkraha.backend.infos.ResponseInfo
import org.junit.Assert.*
import org.junit.Test

class ResponseInfoTest {

    @Test
    fun test() {
        val response = ResponseInfo()


        response.setOptionSaveBytes()
        assertTrue(response.isOptionSaveBytes())
        assertTrue(!response.isOptionSaveFile())
        assertTrue(!response.isOptionSaveObject())
        assertTrue(!response.isOptionSaveString())

        response.setOptionSaveFile()
        assertTrue(response.isOptionSaveBytes())
        assertTrue(response.isOptionSaveFile())
        assertTrue(!response.isOptionSaveObject())
        assertTrue(!response.isOptionSaveString())

        response.setOptionSaveObject()
        assertTrue(response.isOptionSaveBytes())
        assertTrue(response.isOptionSaveFile())
        assertTrue(response.isOptionSaveObject())
        assertTrue(!response.isOptionSaveString())

        response.setOptionSaveString()
        assertTrue(response.isOptionSaveBytes())
        assertTrue(response.isOptionSaveFile())
        assertTrue(response.isOptionSaveObject())
        assertTrue(response.isOptionSaveString())

    }

}