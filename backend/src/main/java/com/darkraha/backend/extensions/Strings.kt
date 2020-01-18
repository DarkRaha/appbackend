package com.darkraha.backend.extensions

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Extracts filename from url string
 */
fun String.extractFilename(): String? {


    val qmark = indexOf('?')
    val hmark = lastIndexOf('#')
    var end = 0


    if (qmark >= 0) {
        end = qmark
    } else if (hmark >= 0) {
        end = hmark
    } else {
        end = length
    }


    val indexof = lastIndexOf('/', end, false)


    if (indexof >= 0) {
        return substring(indexof + 1, end)
    }

    return null
}

/**
 * Extracts file extension from url string
 */
fun String.extractFileExtension(def: String = "html"): String {
    var filename = extractFilename()

    if (filename == null) {
        filename = this
    }

    val ind = filename.lastIndexOf('.')
    if (ind >= 0) {
        return filename.substring(ind + 1)
    }

    return def;
}

/**
 * Encodes string by md5 algorithm. Useful for mapping url to file.
 */
fun String.encodeMd5(): String {
    val digest = MessageDigest.getInstance("MD5");
    digest.update(this.toByteArray())
    return BigInteger(digest.digest()).abs().toString(36)
}


fun String.isTextMimetype(): Boolean {
    // mimetype = application/json;charset=UTF-8
    return startsWith("text/", true) || contains("/json")
}