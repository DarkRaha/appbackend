package com.darkraha.backend_android.components.endecode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import com.darkraha.backend.components.endecode.BinaryFileDecoder
import com.darkraha.backend.extraparams.ImageLoadEP
import java.io.InputStream

class JpegImageDecoder : BinaryFileDecoder("image/jpeg", arrayOf("jpeg", "jpg"), Bitmap::class, ImageLoadEP::class) {

    override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {
        return BitmapFactory.decodeStream(inStream)
    }


}