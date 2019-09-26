package com.darkraha.backend.components.images

import com.darkraha.backend.cache.MemoryUsage
import java.io.Closeable

/**
 * Wrapper under real image object.
 * @author Verma Rahul
 */
interface BackendImage : Closeable, MemoryUsage {

    /**
     * Source of image.
     */
    val srcImage: Any
    /**
     * Width of image. -1 for vector image.
     */
    val width: Int
    /**
     * Height of image. -1 for vector image.
     */
    val height: Int

    /**
     * Assigns this image to view like ImageView in android.
     */
    fun assignTo(view: Any?)

    /**
     * Assigns this image to view as background.
     */
    fun assignAsBackground(view: Any?)

    fun newPlatformImageObject(): Any
}