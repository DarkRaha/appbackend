package com.darkraha.backend.extraparams

import com.darkraha.backend.components.images.BackendImage

enum class ImageSizes {
    ORIGINAL, SCREEN, THUMB_SCREEN2, THUMB_SCREEN4, THUMB_SCREEN8, THUMB_SCREEN16, RASTER,
    THUMB_64, THUMB_128, THUMB_256, THUMB_512;


    fun calcSize(scrWidth: Int, scrHeight: Int,
                 desiredWidth: Int = 128, desiredHeight: Int = 128):
            Pair<Int, Int> {
        return when (this) {
            SCREEN -> scrWidth to scrHeight
            THUMB_SCREEN2 -> (scrWidth / 2) to (scrHeight / 2)
            THUMB_SCREEN4 -> (scrWidth / 4) to (scrHeight / 4)
            THUMB_SCREEN8 -> (scrWidth / 8) to (scrHeight / 8)
            THUMB_SCREEN16 -> (scrWidth / 16) to (scrHeight / 16)


            RASTER -> desiredWidth to desiredHeight
            THUMB_64 -> 64 to 64
            THUMB_256 -> 256 to 256
            THUMB_512 -> 512 to 512
            else -> 128 to 128
        }
    }


}


typealias UserAssignImage = (ui: Any, img: BackendImage?) -> Unit

/**
 * Extra param for image loading.
 * Not all feature implemented.
 */
class ImageLoadEP {

    var imageLoadConfig: ImageLoadConfig? = null
    var imageDecodeConfig: ImageDecodeConfig? = null
    var userAssignImage: UserAssignImage? = null


    fun saveImageSize(v: ImageSizes): ImageLoadEP = apply {
        imageLoadConfig().imageSize = v
    }


    fun saveImageSize(width: Int, height: Int): ImageLoadEP = apply {
        imageLoadConfig().run {

            rasterWidth = width
            rasterHeight = height
        }
    }

    fun decodeImageSize(v: ImageSizes): ImageLoadEP = apply {
        imageDecodeConfig().run {
            imageSize = v
        }
    }

    fun decodeFirstFrame(v: Boolean): ImageLoadEP = apply {
        imageDecodeConfig().run { firstFrame = v }
    }

    private fun imageLoadConfig(): ImageLoadConfig = imageLoadConfig
            ?: ImageLoadConfig().apply { imageLoadConfig = this }


    private fun imageDecodeConfig(): ImageDecodeConfig = imageDecodeConfig
            ?: ImageDecodeConfig().apply { imageDecodeConfig = this }


}


class ImageLoadConfig {
    /**
     * Saves image file as is.
     */
    var imageSize = ImageSizes.ORIGINAL
    var saveFiles: Set<ImageSizes>? = null
    var rasterWidth: Int = 128
    var rasterHeight: Int = 128

    fun isThumb(screenWidth: Int = 0, screenHeight: Int = 0): Boolean {
        return imageSize != ImageSizes.ORIGINAL &&
                if (imageSize in ImageSizes.SCREEN..ImageSizes.THUMB_SCREEN16) {
                    screenWidth > 0 && screenHeight > 0
                } else true
    }
}

class ImageDecodeConfig {
    var imageSize = ImageSizes.ORIGINAL
    var firstFrame = false
    var rasterWidth: Int = 128
    var rasterHeight: Int = 128


    fun isThumb(screenWidth: Int = 0, screenHeight: Int = 0): Boolean {
        return imageSize != ImageSizes.ORIGINAL &&
                if (imageSize in ImageSizes.SCREEN..ImageSizes.THUMB_SCREEN16) {
                    screenWidth > 0 && screenHeight > 0
                } else true
    }

}