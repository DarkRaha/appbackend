package com.darkraha.backend_android.components.images

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.darkraha.backend.components.images.BackendImage
import com.darkraha.backend.components.images.ImageManagerClientA
import com.darkraha.backend.components.images.ImagePlatformHelper
import com.darkraha.backend_android.components.endecode.GifDecoder
import com.darkraha.backend_android.components.endecode.GifImageDecoder
import com.darkraha.backend_android.components.endecode.JpegImageDecoder
import com.darkraha.backend_android.components.endecode.PngImageDecoder
import com.darkraha.backend_android.components.images.gif.GifDrawable

// todo replace by BackendImage
open class AndroidImagePlatformHelper : ImagePlatformHelper() {
    protected lateinit var imageManager: ImageManagerClientA

    protected var backendImages = mutableListOf(
            BackendImageJpg(JpegImageDecoder(), null),
            BackendImagePng(PngImageDecoder(), null),
            BackendImageGif(GifImageDecoder(), null)
    )

    override fun onAttach(imageManager: ImageManagerClientA) {

        this.imageManager = imageManager

        backendImages.forEach {
            addBackendImage(it)
        }
    }


    override fun getBackendImage(forObj: Any?): BackendImage? = forObj?.run {
        backendImages.find { it.srcImageClass == this.javaClass }?.newInstance(forObj)
    }

    protected fun addBackendImage(bImage: BackendImage) {
        imageManager.run {
            addImageSizeCalculator(bImage.srcImageClass.kotlin, bImage::getMemoryUsageOf)
            addImageConverter(bImage.srcImageClass.kotlin, bImage::convertImage)
            bImage.fileDecoder?.apply { addImageDecoder(this) }
            bImage.fileEncoder?.apply { addImageEncoder(this) }
        }
    }


    override fun assignImage(img: Any?, ui: Any) {
       getBackendImage(img)?.assignTo(ui)
//
//        if (ui is ImageView) {
//            when {
//                img is Drawable -> {
//                    ui.setImageDrawable(img)
//                }
//                img is Bitmap -> ui.setImageBitmap(img)
//            }
//
//        }
    }
}