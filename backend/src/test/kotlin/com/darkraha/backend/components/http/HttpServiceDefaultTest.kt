package com.darkraha.backend.components.http

import com.darkraha.backend.Query
import com.darkraha.backend.extensions.encodeMd5
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HttpServiceDefaultTest {

    @Rule
    @JvmField
    var folder = TemporaryFolder()

    val urlText = "http://google.com"
    val urlTextError = "http://google.com1"
    val urlFile = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92dp.png"
    val urlFileError = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_272x92d.png"

    val http = HttpServiceDefault()

    @Test
    fun checkLink() {
        val r = http.checkLink(urlText)
        assertTrue(!r.errorInfo.isError())

    }

    @Test
    fun loadText() {
        var r = http.loadText(urlText)
        assertTrue(!r.errorInfo.isError())
        assertTrue(r.rawResultString != null)
        assertTrue(r.rawResultString!!.startsWith("<!doctype html>", true))
    }

    @Test
    fun loadFile() {
        val dst = File(folder.root, urlFile.encodeMd5() + ".png")
        var r = http.loadFile(urlFile, dst)

        assertTrue(!r.errorInfo.isError())
        assertTrue(dst.exists())
        assertTrue(dst.length() > 0)
        assertTrue(r.rawResultFile == dst)

        val dst2 = File(folder.root, urlFileError.encodeMd5() + ".png")
        r = http.loadFile(urlFileError, dst)
        assertTrue(r.errorInfo.isError())


        assertTrue(!dst2.exists())
        assertTrue(r.rawResultFile == null)
    }

    @Test
    fun postForm() {
    }

    @Test
    fun uploadFile() {
    }

    @Test
    fun handleTestLoadTextAuto() {

        var q = Query()

        q.url(urlText).service(http)
        q.exeSync()

        if (q.isError()) {
            println("error " + q.errorMessage())
        }

        assertTrue(q.errorMessage(), q.isSuccess())
        assertTrue(q.rawString() != null)
        assertTrue(q.rawString()!!.startsWith("<!doctype html>"))


        q = Query()

        q.url(urlTextError).service(http)
        q.exeSync()
        assertTrue(!q.isSuccess())
        assertTrue(q.rawString() == null)
        assertTrue(q.errorMessage() != null)
        assertTrue(q.errorMessage()!!.startsWith("google.com1"))

        q = Query()
        q.url(urlText).service(http).optAsFile()
        q.exeSync()

        assertTrue(q.isSuccess())
        assertTrue(q.rawString() == null)
        assertTrue(q.resultFile() != null)
        assertTrue(q.resultFile()!!.readText().startsWith("<!doctype html>"))


        q = Query()
        q.url(urlText).service(http).optAsFile().optAsString()
        q.exeSync()

        assertTrue(q.isSuccess())
        assertTrue(q.rawString()!!.startsWith("<!doctype html>"))
        assertTrue(q.result() != null)
        assertTrue(q.result() is String)
        assertTrue(q.rawString() != null)
        assertTrue(q.resultFile() != null)
        assertTrue(q.resultFile()!!.readText().startsWith("<!doctype html>"))
    }


    @Test
    fun handleTestLoadFileAuto() {

        var q = Query()

        q.url(urlFile).service(http)
        q.exeSync()

        assertTrue(q.isSuccess())
        assertTrue(q.rawString() == null)
        assertTrue(q.resultFile() != null)
        var bytes = q.resultFile()?.readBytes()!!

        assertTrue(
            bytes[1].toChar() == 'P' && bytes[2].toChar() == 'N' &&
                    bytes[3].toChar() == 'G'
        )


        q = Query()

        q.url(urlFile).service(http).optAsBytes()
        q.exeSync()

        assertTrue(q.isSuccess())
        assertTrue(q.rawString() == null)
        assertTrue(q.resultFile() == null)
        bytes = q.rawBytes()!!

        assertTrue(
            bytes[1].toChar() == 'P' && bytes[2].toChar() == 'N' &&
                    bytes[3].toChar() == 'G'
        )

        q = Query()

        q.url(urlFile).service(http).optAsBytes().optAsFile()
        q.exeSync()

        assertTrue(q.isSuccess())
        assertTrue(q.rawString() == null)
        assertTrue(q.resultFile() != null)
        bytes = q.rawBytes()!!

        assertTrue(
            bytes[1].toChar() == 'P' && bytes[2].toChar() == 'N' &&
                    bytes[3].toChar() == 'G'
        )

        bytes = q.resultFile()?.readBytes()!!
        assertTrue(
            bytes[1].toChar() == 'P' && bytes[2].toChar() == 'N' &&
                    bytes[3].toChar() == 'G'
        )
    }


}


