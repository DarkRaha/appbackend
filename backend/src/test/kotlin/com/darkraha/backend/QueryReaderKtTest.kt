package com.darkraha.backend

import org.junit.Assert.*
import org.junit.Test

class QueryReaderKtTest{


    @Test
    fun testAs(){

        val query = Query()

        var str = "String"
        var str2 : String?=null

        query.objectParam(str)
        str2 = query.getObjectParamAs()
        assertTrue(str==str2)

        str="S1"
        query.uiParam(str)
        str2=query.getUiParamAs()
        assertTrue(str==str2)

        str="s2"
        query.extraParam(str)
        str2 = query.getExtraParamAs()
        assertTrue(str==str2)

        str="s3"
        query.destination(str)
        str2 = query.getDestValueAs()
        assertTrue(str==str2)

        str="s4"
        query.source(str)
        str2= query.getSrcValueAs()
        assertTrue(str==str2)



    }



}