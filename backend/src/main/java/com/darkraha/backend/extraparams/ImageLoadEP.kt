package com.darkraha.backend.extraparams

enum class DecodeType {
    ORIGINAL, SCREEN, THUMB2, THUMB4, THUMB8, THUMB16, RASTER
}

/**
 * Extra param for image loading.
 * Not all feature implemented.
 */
class ImageLoadEP {

    var imageLoadConfig: ImageLoadConfig? = null
    var imageDecodeConfig: ImageDecodeConfig? = null

    fun saveOriginalImage(v: Boolean): ImageLoadEP = apply {
        imageLoadConfig().saveOriginalImage = v
    }

    fun saveScreenImage(v: Boolean): ImageLoadEP = apply { imageLoadConfig().saveScreenImage = v }

    fun saveThumb2(v: Boolean): ImageLoadEP = apply { imageLoadConfig().saveThumb2 = v }

    fun saveThumb4(v: Boolean): ImageLoadEP = apply { imageLoadConfig().saveThumb4 = v }

    fun saveThumb8(v: Boolean): ImageLoadEP = apply { imageLoadConfig().saveThumb8 = v }

    fun saveFrame(v: Boolean): ImageLoadEP = apply { imageLoadConfig().saveFrame = v }

    fun saveRaster(v: Boolean, width: Int, height: Int): ImageLoadEP = apply {
        imageLoadConfig().run {
            saveRaster = v
            rasterWidth = width
            rasterHeight = height
        }
    }

    fun decodeType(v: DecodeType): ImageLoadEP = apply {
        imageDecodeConfig().run {
            decodeType = v
        }
    }

    fun decodeFirstFrame(v:Boolean): ImageLoadEP = apply {
        imageDecodeConfig().run { firstFrame=v }
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
    var saveOriginalImage = true

    /**
     * Saves image file that will be less or equal to screen size.
     */
    var saveScreenImage = false

    /**
     * If possible and necessary
     * saves image file so width of image will be less or equal to half of width screen
     * or to half of height for landscape image.
     */
    var saveThumb2 = false

    var saveThumb4 = false

    var saveThumb8 = false

    /**
     * Saves first frame as separate image for animated image like gif.
     */
    var saveFrame = false

    /**
     * Save raster version of vector image. (todo Default size is thumb2?)
     */
    var saveRaster = false

    var rasterWidth = 0

    var rasterHeight = 0

}

class ImageDecodeConfig {
    var decodeType = DecodeType.ORIGINAL
    var firstFrame = false
}