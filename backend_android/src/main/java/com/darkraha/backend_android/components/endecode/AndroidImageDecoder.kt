package com.darkraha.backend_android.components.endecode

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.darkraha.backend.Backend
import com.darkraha.backend.components.endecode.BinaryFileDecoder
import com.darkraha.backend.extraparams.ImageLoadEP
import com.darkraha.backend.extraparams.ImageSizes
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.reflect.KClass

open class AndroidImageDecoder(srcMimetype: String,
                               srcFileExtensions: Array<String> = arrayOf(),
                               dstClass: KClass<*> = ByteArray::class,
                               extraParamClass: KClass<*>? = null
)


    : BinaryFileDecoder(srcMimetype, srcFileExtensions, dstClass, extraParamClass) {

    override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {
        return BitmapFactory.decodeStream(inStream)
    }


    override fun decodeFile(file: File, dst: Any?, extraParam: Any?): Any? {
        val screen = Backend.sharedInstance.deviceScreen

        extraParam?.takeIf {
            it is ImageLoadEP && it.imageDecodeConfig?.isThumb(screen.scrWidth, screen.scrHeight) ?: false
        }?.apply {
            this as ImageLoadEP

            imageDecodeConfig?.apply {
                imageSize.calcSize(
                        screen.scrWidth, screen.scrHeight,
                        rasterWidth, rasterHeight
                ).run {
                    return decodeSampledBitmap(file, first, second)
                }
            }
        }


        BufferedInputStream(FileInputStream(file)).use {
            return decode(it, dst, extraParam)
        }
    }


    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }


    fun decodeSampledBitmap(
            file: File,
            reqWidth: Int,
            reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {

            inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, this) //decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeFile(file.absolutePath, this)
        }
    }


}