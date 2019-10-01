package com.darkraha.backend.extraparams

import com.darkraha.backend.components.images.BackendImage

enum class ImageFileModificationType {
    ORIGINAL, SCREEN, THUMB_SCREEN2, THUMB_SCREEN4, THUMB_SCREEN8, THUMB_SCREEN16, RASTER,
    THUMB_64, THUMB_128, THUMB_256, THUMB_512
}


typealias UserAssignImage = (ui: Any, img: BackendImage?) -> Unit

/**
 * Extra param for image loading.
 * Not all feature implemented.
 */
class ImageLoadEP {

    var imageLoadConfig: ImageLoadConfig? = null
    var imageDecodeConfig: ImageDecodeConfig? = null
    var userAssignImage: UserAssignImage?=null


    fun saveOriginalImage(v: ImageFileModificationType): ImageLoadEP = apply {
        imageLoadConfig().saveOriginalImage = v
    }


    fun saveRasterSize(width: Int, height: Int): ImageLoadEP = apply {
        imageLoadConfig().run {

            rasterWidth = width
            rasterHeight = height
        }
    }

    fun decodeType(v: ImageFileModificationType): ImageLoadEP = apply {
        imageDecodeConfig().run {
            decodeType = v
        }
    }

    fun decodeFirstFrame(v: Boolean): ImageLoadEP = apply {
        imageDecodeConfig().run { firstFrame = v }
    }

    private fun imageLoadConfig(): ImageLoadConfig = imageLoadConfig ?: let {
        imageLoadConfig = ImageLoadConfig()
        imageLoadConfig!!
    }

    private fun imageDecodeConfig(): ImageDecodeConfig = imageDecodeConfig ?: let {
        imageDecodeConfig = ImageDecodeConfig()
        imageDecodeConfig!!
    }


}


class ImageLoadConfig {
    /**
     * Saves image file as is.
     */
    var saveOriginalImage = ImageFileModificationType.ORIGINAL
    var saveFiles: Set<ImageFileModificationType>? = null

    var rasterWidth: Int = 0
    var rasterHeight: Int = 0
}

class ImageDecodeConfig {
    var decodeType = ImageFileModificationType.ORIGINAL
    var firstFrame = false
}