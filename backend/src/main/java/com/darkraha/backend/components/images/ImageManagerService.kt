package com.darkraha.backend.components.images

import com.darkraha.backend.*
import kotlin.reflect.KClass

/**
 * @author Verma Rahul
 */
interface ImageManagerService : Service


interface ImagePlatformHelper {
    /**
     * can add platform dependent codecs and etc.
     */
    fun onAttach(imageManager: ImageManagerClient)
    fun assignImage(img: Any?, ui: Any)
    fun startAnimation(ui: Any?)
    fun stopAnimation(ui: Any?)
}


class ImagePlatformHelperEmpty() : ImagePlatformHelper {
    override fun onAttach(imageManager: ImageManagerClient) {
    }

    override fun assignImage(img: Any?, ui: Any) {
    }

    override fun startAnimation(ui: Any?) {
    }

    override fun stopAnimation(ui: Any?) {
    }

}