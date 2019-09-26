package com.darkraha.backend.components.endecode

import com.sun.org.apache.xpath.internal.operations.Bool
import java.io.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Allow decode data object from file, output stream or byte array.
 *
 *
 * @author Verma Rahul
 */
abstract class FileDecoder(
        val srcMimetype: String,
        val srcFileExtensions: Array<String>,
        val dstClass: KClass<*>,
        val extraParamClass: KClass<*>? = null
) {

    val mimetypeMain = srcMimetype.substringBefore("/")

    open fun isSupportDecode(srcValue: Any?, aSrcMimetype: String?, dst: Any?, aDstClass: KClass<*>?, extraParam: Any?): Boolean {

        var ret = isSupportFile(srcValue) || isSupportMimetype(aSrcMimetype)

        aDstClass?.apply {
            ret = ret && this.isSubclassOf(dstClass)
        }

        extraParam?.apply {
            ret = ret && extraParamClass?.isInstance(extraParam) ?: false
        }

        return ret
    }


    fun isSupportMimetype(mimetype: String?): Boolean {
        return srcMimetype == mimetype
    }

    fun isPossibleMimetype(mimetype: String?): Boolean {
        return mimetype?.startsWith(mimetypeMain) ?: false
    }


    fun isSupportFile(srcFile: Any?): Boolean {
        srcFile?.apply {
            if (this is File) {
                return extension.toLowerCase() in srcFileExtensions
            }
        }

        return false
    }

    fun isSupportExtraParam(ep: Any?): Boolean {
        ep?.apply {
            return extraParamClass?.isInstance(ep) ?: false
        }

        return return true
    }


//    fun isSupportFile(file: File): Boolean {
//        return if (srcFileExtensions.size == 0) true else file.extension.toLowerCase() in srcFileExtensions
//    }

    open fun decodeAny(src: Any?, dst: Any? = null, extraParam: Any? = null): Any? {
        var result: Any? = when {
            src is File -> decodeFile(src, dst, extraParam)
            src is InputStream -> decode(src, dst, extraParam)
            src is ByteArray -> decodeBytes(src, dst, extraParam)
            else -> null
        }

        return result
    }

    open fun decode(inStream: InputStream, dst: Any? = null, extraParam: Any? = null): Any? {
        val result = onDecoding(inStream, dst, extraParam);
        if (result != null && !dstClass.isInstance(result)) {
            throw IllegalStateException("Expected result of type ${dstClass.simpleName}, got type ${result::class.simpleName}")
        }
        return result
    }

    abstract fun onDecoding(inStream: InputStream, dst: Any? = null, extraParam: Any? = null): Any?

    open fun decodeFile(file: File, dst: Any? = null, extraParam: Any? = null): Any? {
        BufferedInputStream(FileInputStream(file)).use {
            return decode(it, dst, extraParam)
        }
    }

    open fun decodeBytes(data: ByteArray, dst: Any? = null, extraParam: Any? = null): Any? {
        ByteArrayInputStream(data).use { return decode(it, dst, extraParam) }
    }
}

/**
 * Decode file as ByteArray.
 */
open class BinaryFileDecoder(
        srcMimetype: String = "application/x-binary",
        srcFileExtensions: Array<String> = arrayOf(),
        dstClass: KClass<*> = ByteArray::class,
        extraParamClass: KClass<*>? = null
) : FileDecoder(srcMimetype, srcFileExtensions, dstClass, extraParamClass) {

    override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {
        return inStream.readBytes()
    }
}

/**
 * Decode file as String.
 */
open class TextFileDecoder(
        srcMimetype: String = "text/plain",
        srcFileExtensions: Array<String> = arrayOf(),
        dstClass: KClass<*> = String::class,
        extraParamClass: KClass<*>? = null
) : FileDecoder(srcMimetype, srcFileExtensions, dstClass, extraParamClass) {

    override fun onDecoding(inStream: InputStream, dst: Any?, extraParam: Any?): Any? {
        return inStream.reader().readText()
    }
}