package com.darkraha.backend_android.components.endecode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.darkraha.backend.components.endecode.BinaryFileDecoder
import com.darkraha.backend.extraparams.ImageLoadEP
import java.io.InputStream

class PngImageDecoder : BinaryFileDecoder("image/png", arrayOf("png"), Bitmap::class, ImageLoadEP::class) {

    override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {
        return BitmapFactory.decodeStream(inStream)
    }

}