package com.darkraha.backend_android.components.images

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.darkraha.backend.components.endecode.FileDecoder
import com.darkraha.backend.components.endecode.FileEncoder
import com.darkraha.backend.components.images.BackendImage
import com.darkraha.backend_android.components.endecode.GifDecoder
import com.darkraha.backend_android.components.images.gif.GifDrawable


open class BackendImageJpg() : BackendImage() {

    constructor(decoder: FileDecoder?, encoder: FileEncoder?) : this() {
        fileDecoder = decoder
        fileEncoder = encoder
    }

    init {
        srcImageClass = Bitmap::class.java
    }

    override val width: Int
        get() = srcImage?.run { (this as Bitmap).width } ?: 0

    override val height: Int
        get() = srcImage?.run { (this as Bitmap).height } ?: 0

    override fun getMemoryUsageOf(obj: Any): Int = (obj as Bitmap).byteCount

    override fun assignTo(view: Any?) {

        view?.apply {
            when {
                this is ImageView -> setImageBitmap(srcImage as Bitmap?)
                this is View -> background = if (srcImage == null) null else BitmapDrawable(srcImage as Bitmap)
            }
        }
    }

    override fun assignAsBackground(view: Any?) {
        view?.apply { (this as View).background = if (srcImage == null) null else BitmapDrawable(srcImage as Bitmap) }
    }


    override fun convertImage(obj: Any?): Any {
        return obj?.run { this as Bitmap } ?: Unit
    }

}


open class BackendImagePng() : BackendImage() {

    constructor(decoder: FileDecoder?, encoder: FileEncoder?) : this() {
        fileDecoder = decoder
        fileEncoder = encoder
    }

    init {
        srcImageClass = Bitmap::class.java
    }

    override val width: Int
        get() = srcImage?.run { (this as Bitmap).width } ?: 0

    override val height: Int
        get() = srcImage?.run { (this as Bitmap).height } ?: 0


    override fun getMemoryUsageOf(obj: Any): Int = (obj as Bitmap).byteCount

    override fun assignTo(view: Any?) {

        view?.apply {
            when {
                this is ImageView -> setImageBitmap(srcImage as Bitmap?)
                this is View -> background = srcImage?.run { BitmapDrawable(srcImage as Bitmap) }
            }
        }
    }

    override fun assignAsBackground(view: Any?) {
        view?.apply { (this as View).background = if (srcImage == null) null else BitmapDrawable(srcImage as Bitmap) }
    }

    override fun convertImage(obj: Any?): Any {
        return obj?.run { this as Bitmap } ?: Unit
    }
}

open class BackendImageGif() : BackendImage() {

    constructor(decoder: FileDecoder?, encoder: FileEncoder?) : this() {
        fileDecoder = decoder
        fileEncoder = encoder
    }

    init {
        srcImageClass = GifDecoder::class.java
    }

    override val width: Int
        get() = srcImage?.run { (this as GifDecoder).width } ?: 0

    override val height: Int
        get() = srcImage?.run { (this as GifDecoder).height } ?: 0


    override fun getMemoryUsageOf(obj: Any): Int = (obj as GifDecoder).size


    override fun assignTo(view: Any?) {

        view?.apply {
            when {
                this is ImageView -> setImageDrawable(srcImage?.run { GifDrawable(this as GifDecoder) })
                this is View -> background = srcImage?.run { GifDrawable(this as GifDecoder) }
            }
        }
    }

    override fun assignAsBackground(view: Any?) {
        view?.apply {
            (this as View).background = srcImage?.run { GifDrawable(this as GifDecoder) }
        }
    }

    override fun convertImage(obj: Any?): Any {
        return obj?.run { GifDrawable(this as GifDecoder) } ?: Unit
    }
}
