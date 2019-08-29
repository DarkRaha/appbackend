package com.darkraha.backend.infos

import com.darkraha.backend.ChainType
import java.io.File


interface ResultReader {
    /**
     * Result of query.
     */
    fun result(): Any?

    fun resultCode(): Int

    fun resultMessage(): String?


    fun chainTypeResponse(): ChainType

    /**
     * Raw result srcMimetype.
     */
    fun rawMimetype(): String?

    /**
     * Raw string result.
     */
    fun rawString(): String?

    /**
     * Raw size
     */
    fun rawSize(): Long

    /**
     * Raw bytes result
     */
    fun rawBytes(): ByteArray?

    /**
     * Raw result as file
     */
    fun resultFile(): File?
}

interface ResultOptionsReader {
    /**
     * Is service must save raw string result in memory.
     */
    fun isOptionSaveString(): Boolean

    /**
     * Is service must save raw bytes result in memory.
     */
    fun isOptionSaveBytes(): Boolean

    /**
     * Is service must save raw result in file.
     */
    fun isOptionSaveFile(): Boolean

    /**
     * Is service must deserialize raw result if possible. Usually deserialization will be made by postprocessor.
     */
    fun isOptionSaveObject(): Boolean

}


/**
 * @author Verma Rahul
 */
class ResponseInfo : ResultReader, ResultOptionsReader {

    val errorInfo = ErrorInfo()
    val cancelInfo = CancelInfo()

    var raw: Any? = null
    var result: Any? = null
    var rawResultString: String? = null
    var rawResultBytes: ByteArray? = null
    var rawResultFile: File? = null
    var rawSize: Long = 0
    var rawMimetype: String? = null
    var resultCode = 0
    var resultMessage: String? = null


    // TODO class of result


    var responseOptions = 0
    var chainTypeResponse = ChainType.UNDEFINED


    fun assignFrom(src: ResponseInfo) {
        with(src.errorInfo) {
            errorInfo.message = message
            errorInfo.code = code
            errorInfo.exception = exception
        }

        chainTypeResponse = src.chainTypeResponse
        rawResultString = src.rawResultString
        rawResultFile = src.rawResultFile
        rawResultBytes = src.rawResultBytes
        raw = src.raw
        result = src.result
    }

    fun clear() {
        errorInfo.clear()
        cancelInfo.clear()
        raw = null
        result = null
        rawResultBytes = null
        rawResultString = null
        rawResultFile = null
        rawMimetype = null
        resultMessage = null
        responseOptions = 0
        rawSize = 0
        resultCode = 0
        chainTypeResponse = ChainType.UNDEFINED
    }


    inline fun setOptionSaveString() {
        responseOptions = responseOptions.or(OPT_RESPONSE_STRING)
    }


    inline fun setOptionSaveBytes() {
        responseOptions = responseOptions.or(OPT_RESPONSE_BYTES)
    }


    inline fun setOptionSaveFile() {
        responseOptions = responseOptions.or(OPT_RESPONSE_FILE)
    }


    /**
     *
     */
    inline fun setOptionSaveObject() {
        responseOptions = responseOptions.or(OPT_RESPONSE_OBJECT)
    }

    companion object {
        val OPT_RESPONSE_STRING = 1
        val OPT_RESPONSE_BYTES = 2
        val OPT_RESPONSE_FILE = 4
        val OPT_RESPONSE_OBJECT = 8
    }

    //------------------------------------------------------
    override fun result(): Any? = result

    override fun resultCode(): Int = resultCode
    override fun rawMimetype(): String? = rawMimetype
    override fun rawString(): String? = rawResultString
    override fun rawBytes(): ByteArray? = rawResultBytes
    override fun resultFile(): File? = rawResultFile
    override fun rawSize(): Long = rawSize
    override fun resultMessage(): String? = resultMessage
    override fun chainTypeResponse(): ChainType = chainTypeResponse

    //------------------------------------------------------

    override fun isOptionSaveString() = responseOptions.and(OPT_RESPONSE_STRING) > 0
    override fun isOptionSaveBytes() = responseOptions.and(OPT_RESPONSE_BYTES) > 0
    override fun isOptionSaveObject() = responseOptions.and(OPT_RESPONSE_OBJECT) > 0
    override fun isOptionSaveFile() = responseOptions.and(OPT_RESPONSE_FILE) > 0
}

