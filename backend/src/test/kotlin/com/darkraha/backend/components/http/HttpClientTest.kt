package com.darkraha.backend.components.http

import com.darkraha.backend.Backend
import com.darkraha.backend.addOnSuccess
//import com.darkraha.backend.injection.DaggerBackendComponentTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


class HttpClientTest {

    @Rule
    @JvmField
    var folder = TemporaryFolder()

    val urlText = "http://google.com"
    val urlTextError = "http://google.com1"
    val urlFile = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"
    val urlFileError = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92d.png"

    //  val components = DaggerBackendComponentTest.create()

    val backend = Backend.newInstance()
    var http: HttpClient = backend.httpClient

    @Test
    fun loadText() {
        var cbOk = false

        http.buildQueryWith(urlText).addOnSuccess {
            assertTrue(it.rawString()!!.startsWith("<!doctype html>"))
            cbOk = true
        }.exeAsync().waitFinish()


        assertTrue(cbOk)
    }

    @Test
    fun loadFile(){
        var cbOk = false;




    }




}