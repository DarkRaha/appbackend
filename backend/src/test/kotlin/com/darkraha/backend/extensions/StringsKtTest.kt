package com.darkraha.backend.extensions

import org.junit.Test

import org.junit.Assert.*
import java.io.File

class StringsKtTest {

    @Test
    fun extractFilename() {
        val url = "http://example.com/download/qwert.jpg?s=\"ssdd/\\dddd.sd"
        val file = File("text.txt")

        assertTrue(url.extractFilename() == "qwert.jpg")
        assertTrue(url.extractFileExtension() == "jpg")
        assertTrue(file.name.extractFileExtension("") == "txt")
    }


}