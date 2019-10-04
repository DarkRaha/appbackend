package com.darkraha.backend.components.images

import com.darkraha.backend.cache.MemoryUsage
import com.darkraha.backend.components.endecode.FileDecoder
import com.darkraha.backend.components.endecode.FileEncoder
import java.io.Closeable

/**
 * Wrapper under real image object.
 * @author Verma Rahul
 */


abstract class BackendImage : Closeable {

    val mimetype: String?
        get() {
            return fileDecoder?.srcMimetype ?: fileEncoder?.dstMimetype
        }

    var fileDecoder: FileDecoder? = null
        protected set

    var fileEncoder: FileEncoder? = null
        protected set

    /**
     * Source of image.
     */
    var srcImage: Any? = 0

    lateinit var srcImageClass: Class<*>
        protected set


    open fun getMemoryUsageOf(obj: Any): Int {
        return 0
    }

    fun getMemoryUsage(): Int = getMemoryUsageOf(srcImage!!)

    /**
     * Width of image. -1 for vector image.
     */
    open val width: Int
        get() = 0
    /**
     * Height of image. -1 for vector image.
     */
    open val height: Int
        get() = 0

    /**
     * Assigns this image to view like ImageView in android.
     */
    open fun assignTo(view: Any?) {

    }

    /**
     * Assigns this image to view as run.
     */
    open fun assignAsBackground(view: Any?) {

    }

    open fun newInstance(imgObject: Any?): BackendImage {
        return this.javaClass.newInstance().also {
            it.srcImage = imgObject
            it.fileDecoder = fileDecoder
            it.fileEncoder = fileEncoder
        }
    }

    open fun convertImage(obj: Any?): Any {
        return Unit
    }

    fun convertImage(): Any = convertImage(srcImage)

    override fun close() {

    }
}