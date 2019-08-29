package com.darkraha.backend.extraparams

/**
 * Extra param for image loading.
 * Not all feature implemented.
 */
class ImageLoadEP {


    var isAutoAnimate: Boolean = false

    var isThumbnail: Boolean = false

    var isResizeToScreenSize = false

    var thumbWidth = 0

    var thumbHeight = 0


    fun isThumbnail(v: Boolean): ImageLoadEP {
        isThumbnail = v
        return this
    }

    /**
     * Animates image if possible when it assigned to ui.
     */
    fun isAutoAnimate(v: Boolean): ImageLoadEP {
        isAutoAnimate = v
        return this
    }

    /**
     * Resizes image file after loading, so it will't exceed size of screen.
     */
    fun isResizeToScreenSize(v: Boolean): ImageLoadEP {
        isResizeToScreenSize = v
        return this
    }

    fun thumbWidth(v: Int): ImageLoadEP {
        thumbWidth = v
        return this
    }

    fun thumbHeight(v: Int): ImageLoadEP {
        thumbHeight = v
        return this

    }


}