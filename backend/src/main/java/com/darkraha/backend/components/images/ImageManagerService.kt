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
    abstract fun onAttach(imageManager: ImageManagerClientA)

    abstract fun assignImage(img: Any?, ui: Any)
    abstract fun getBackendImage(forObj: Any?): BackendImage?
}


class ImagePlatformHelperEmpty : ImagePlatformHelper() {
    override fun onAttach(imageManager: ImageManagerClientA) {
    }

    override fun assignImage(img: Any?, ui: Any) {
    }

    override fun getBackendImage(forObj: Any?): BackendImage? = null
}