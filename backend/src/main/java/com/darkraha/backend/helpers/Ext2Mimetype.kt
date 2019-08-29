package com.darkraha.backend.helpers


/**
 * Map file srcFileExtensions to srcMimetype
 * @author Verma Rahul
 */
object Ext2Mimetype {

    val JPEG_MIMETYPE = "image/jpeg"
    val PNG_MIMETYPE = "image/png"
    val SVG_MIMETYPE = "image/svg+xml"
    val GIF_MIMETYPE = "image/gif"


    private val map = HashMap<String, String>()

    operator fun get(keyExt: String) = map[keyExt.toLowerCase()]

    operator fun set(keyExt: String, v: String) {
        map[keyExt.toLowerCase()] = v.toLowerCase()
    }


    fun addImages() {
        this["jpg"] = JPEG_MIMETYPE
        this["jpeg"] = JPEG_MIMETYPE
        this["gif"] = GIF_MIMETYPE
        this["png"] = PNG_MIMETYPE
        this["svg"] = SVG_MIMETYPE
    }
}