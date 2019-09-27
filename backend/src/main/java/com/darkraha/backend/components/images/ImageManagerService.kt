package com.darkraha.backend.components.images

import com.darkraha.backend.*
import kotlin.reflect.KClass

/**
 * @author Verma Rahul
 */
interface ImageManagerService : Service


abstract class ImagePlatformHelper {
    /**
     * can add platform dependent codecs and etc.
     */
    abstract fun onAttach(imageManager: ImageManagerClient)

    abstract fun assignImage(img: Any?, ui: Any)
    abstract fun getBackendImage(forObj: Any?): BackendImage?
}


class ImagePlatformHelperEmpty : ImagePlatformHelper() {
    override fun onAttach(imageManager: ImageManagerClient) {
    }

    override fun assignImage(img: Any?, ui: Any) {
    }

    override fun getBackendImage(forObj: Any?): BackendImage? = null
//    override fun startAnimation(ui: Any?) {
//    }
//
//    override fun stopAnimation(ui: Any?) {
//    }

}