package com.darkraha.backend.components.endecode

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.reflect.KClass


/**
 * Allow save data object to file or output stream.
 *
 * @author Verma Rahul
 */
abstract class FileEncoder(
    srcClass: KClass<*>,
    dstMimetype: String,
    dstFileExtension: String,
    extraParamClass: KClass<*>? = null
) {

    val srcClass = srcClass
    val dstMimetype = dstMimetype
    val dstFileExtension = dstFileExtension
    val extraParamClass = extraParamClass


    fun isSupportEncode(src: Any?, dst: Any?, aDstMimetype: String? = null, extraParam: Any? = null): Boolean {
        return srcClass.isInstance(src)
                && (dstMimetype == aDstMimetype || (dst is File && dst.extension.toLowerCase() == dstFileExtension))
                && (extraParam == null || extraParamClass?.isInstance(extraParam) ?: false)
    }

    open fun encodeToAny(src: Any?, dst: Any?, extraParam: Any? = null): Boolean {
        if (src == null || dst == null || !srcClass.isInstance(src)) {
            return false
        }

        val lExtraParam = if (extraParamClass == null || !extraParamClass.isInstance(extraParam)) {
            null
        } else {
            extraParam
        }

        return when {
            dst is File -> encodeToFile(src, dst, lExtraParam)
            dst is OutputStream -> encode(src, dst, lExtraParam)
            else -> false
        }
    }

    open fun encodeToFile(src: Any?, fileDst: File, extraParam: Any? = null): Boolean {
        BufferedOutputStream(FileOutputStream(fileDst)).use {
            return encode(src, it)
        }
    }

    abstract fun encode(src: Any?, outStream: OutputStream, extraParam: Any? = null): Boolean

}