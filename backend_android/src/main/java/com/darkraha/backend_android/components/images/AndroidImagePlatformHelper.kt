package com.darkraha.backend_android.components.images

import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.darkraha.backend.components.images.ImageManagerClient
import com.darkraha.backend.components.images.ImagePlatformHelper
import com.darkraha.backend_android.components.endecode.GifDecoder
import com.darkraha.backend_android.components.endecode.GifImageDecoder
import com.darkraha.backend_android.components.endecode.JpegImageDecoder
import com.darkraha.backend_android.components.endecode.PngImageDecoder
import com.darkraha.backend_android.components.images.gif.GifDrawable
// todo replace by BackendImage
open class AndroidImagePlatformHelper : ImagePlatformHelper {
    override fun onAttach(imageManager: ImageManagerClient) {
        imageManager.addImageDecoder(JpegImageDecoder())
        imageManager.addImageDecoder(PngImageDecoder())
        imageManager.addImageDecoder(GifImageDecoder())

        imageManager.addImageSizeCalculator(Bitmap::class) {
            val bmp = it as Bitmap
            return@addImageSizeCalculator bmp.byteCount
        }

        imageManager.addImageSizeCalculator(GifDecoder::class) {
            val gifDecoder = it as GifDecoder
            return@addImageSizeCalculator 2 * gifDecoder.height * gifDecoder.width
        }

        imageManager.addImageConverter(GifDecoder::class) {
            return@addImageConverter GifDrawable(it as GifDecoder)
        }
    }

    override fun assignImage(img: Any?, ui: Any) {
        if (ui is ImageView) {
            when {
                img is Drawable -> {
                    ui.setImageDrawable(img)
                }
                img is Bitmap -> ui.setImageBitmap(img)
            }

        }
    }
}