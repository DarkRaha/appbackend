package com.darkraha.backend_android.components.images

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.darkraha.backend.components.images.BackendImage
import com.darkraha.backend.components.images.ImageManagerClientA
import com.darkraha.backend.components.images.ImagePlatformHelper
import com.darkraha.backend.components.images.ImagePlatformHelperBase
import com.darkraha.backend_android.components.endecode.GifDecoder
import com.darkraha.backend_android.components.endecode.GifImageDecoder
import com.darkraha.backend_android.components.endecode.JpegImageDecoder
import com.darkraha.backend_android.components.endecode.PngImageDecoder
import com.darkraha.backend_android.components.images.gif.GifDrawable


open class AndroidImagePlatformHelper : ImagePlatformHelperBase() {

    init {
        backendImages = mutableListOf(
            BackendImageBitmap(JpegImageDecoder(), null),
            BackendImageBitmap(PngImageDecoder(), null),
            BackendImageGif(GifImageDecoder(), null)
        )

    }

    override fun onAttach(imageManager: ImageManagerClientA) {

        this.imageManager = imageManager

        backendImages.forEach {
            addBackendImage(it)
        }
    }


    override fun assignImage(img: Any?, ui: Any) {
        if (ui is ImageView) {
            when {
                img is Drawable -> {
                    println("AndroidImagePlatformHelper assignImage drawable")
                    ui.setImageDrawable(img)
                }
                img is Bitmap -> ui.setImageBitmap(img)
            }
        }
    }
}