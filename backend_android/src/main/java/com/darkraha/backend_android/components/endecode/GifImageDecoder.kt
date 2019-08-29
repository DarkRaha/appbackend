package com.darkraha.backend_android.components.endecode

import com.darkraha.backend.components.endecode.BinaryFileDecoder
import com.darkraha.backend.extraparams.ImageLoadEP
import java.io.InputStream

open class GifImageDecoder : BinaryFileDecoder("image/gif", arrayOf("gif"), GifDecoder::class, ImageLoadEP::class) {

    override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {
        return GifDecoder(super.onDecoding(inStream, dst, extraParam) as ByteArray)
    }
}