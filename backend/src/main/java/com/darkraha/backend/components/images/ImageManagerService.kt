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
    abstract fun getBackendImage(forObj: Any?, objMimetype: String? = null): BackendImage?
}


open class ImagePlatformHelperBase : ImagePlatformHelper() {
    protected lateinit var imageManager: ImageManagerClientA
    protected lateinit var backendImages: MutableList<BackendImage>


    override fun onAttach(imageManager: ImageManagerClientA) {
        this.imageManager = imageManager
        backendImages.forEach {
            addBackendImage(it)
        }

    }

    override fun assignImage(img: Any?, ui: Any) {
    }

    override fun getBackendImage(obj: Any?, objMimetype: String?): BackendImage? = obj?.run {
        if (objMimetype != null) {
            backendImages.find { it.srcImageClass == this.javaClass && objMimetype == it.mimetype }
                    ?.newInstance(obj)
        } else {
            backendImages.find { it.srcImageClass == this.javaClass }
                    ?.newInstance(obj)
        }
    }

    fun addBackendImage(bImage: BackendImage) {
        imageManager.run {
            addImageSizeCalculator(bImage.srcImageClass.kotlin, bImage::getMemoryUsageOf)
            //addImageConverter(bImage.srcImageClass.kotlin, bImage::convertImage)
            bImage.fileDecoder?.apply { addImageDecoder(this) }
            bImage.fileEncoder?.apply { addImageEncoder(this) }
        }
    }

}