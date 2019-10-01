package com.darkraha.backend.components.endecode

import com.darkraha.backend.*
import com.darkraha.backend.infos.DataInfo
import com.darkraha.backend.infos.ResponseInfo
import java.io.File

/**
 * @author Verma Rahul
 */


interface EndecodeService : Service {
    fun addFileDecoder(decoder: FileDecoder)
    fun addFileEncoder(encoder: FileEncoder)
    fun findDecoder(src: DataInfo?, dst: DataInfo?, extraParam: Any? = null): FileDecoder?
    fun findEncoder(src: DataInfo?, dst: DataInfo?, extraParam: Any?): FileEncoder?

    fun getDecodersList(): List<FileDecoder>
    fun getEncodersList(): List<FileEncoder>

    fun decode(src: DataInfo, dst: DataInfo, extraParam: Any?, result: ResponseInfo): ResponseInfo
    fun encode(src: DataInfo, dst: DataInfo, extraParam: Any?, result: ResponseInfo): ResponseInfo

}

/**
 *
 */
open class EndecodeServiceDefault : EndecodeService {


    protected val decoders = mutableListOf<FileDecoder>()
    protected val encoders = mutableListOf<FileEncoder>()


    override fun getDecodersList(): List<FileDecoder> {
        return decoders
    }

    override fun getEncodersList(): List<FileEncoder> {
        return encoders
    }


    override fun addFileDecoder(decoder: FileDecoder) {
        synchronized(decoders) {
            decoders.add(decoder)
        }
    }

    override fun addFileEncoder(encoder: FileEncoder) {
        synchronized(encoders) {
            encoders.add(encoder)
        }
    }

    override fun isAvailable(): Boolean {
        return true
    }


    override fun findDecoder(src: DataInfo?, dst: DataInfo?, extraParam: Any?): FileDecoder? {
      //  println("EndecodeService findDecoder src=${src} dst=${dst} count of decoders=${decoders.size}")
        if (src == null || dst == null || src.value == null /*|| (dst.value == null && dst.cls == null)*/) {
       //     println("EndecodeService findDecoder 1")
            return null
        }

        synchronized(decoders) {
            val ret = decoders.find {
                val r = it.isSupportDecode(src.value, src.mimetype, dst.value, dst.cls, extraParam)
             //   println("EndecodeService findDecoder decoder ${it.javaClass.simpleName} isSupport is found=${r} for ${src} ")
                r
            }

            return ret
        }

    }

    override fun findEncoder(src: DataInfo?, dst: DataInfo?, extraParam: Any?): FileEncoder? {
        if (src == null || dst == null || src.value == null || dst.value == null) {
            return null
        }

        synchronized(encoders) {
            return encoders.find {
                it.isSupportEncode(
                    src.value,
                    dst.value,
                    dst.mimetype,
                    extraParam
                )
            }
        }

    }


    override fun handle(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {


        val responseInfo = response.responseInfo()
        with(EndecodeConsts) {
            when (q.getCommand()) {
                CMD_ENCODE -> encode(q.source(), q.destination(), q.extraParam(), responseInfo)
                CMD_DECODE -> decode(q.source(), q.destination(), q.extraParam(), responseInfo)
                else -> {
                    response.error("Unknown command ${q.getCommand()}")
                }
            }
        }

        if (responseInfo.errorInfo.isError()) {
            response.error(responseInfo)
        }

    }


    override fun decode(
        src: DataInfo,
        dst: DataInfo,
        extraParam: Any?,
        result: ResponseInfo
    ): ResponseInfo {

        findDecoder(src, dst, extraParam)?.apply {
            val decoded = decodeAny(src?.value, extraParam)
            result.result = decoded
            result.resultMimetype = srcMimetype
            if (decoded is File) {
                result.rawResultFile = decoded
            }

            return result
        } ?: apply {

            decoders.forEach {
                if (it.isPossibleMimetype(src.mimetype)) {
                    it.decodeAny(src?.value, extraParam)?.apply {
                        result.result = this

                        if (this is File) {
                            result.rawResultFile = this
                        }

                        result.resultMimetype = it.srcMimetype
                        return result
                    }


                }
            }
        }

        result.errorInfo.code = EndecodeConsts.ERR_NOT_FOUND_DECODER
        result.errorInfo.message =
            "Can not decode ${src} to ${dst} with src srcMimetype ${src?.mimetype} and extra param ${extraParam}"

        return result
    }


    fun onEncode(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {

        val src: DataInfo = q.source()
        val dst: DataInfo = q.destination()

        val result =
            findEncoder(src, dst, q.extraParam())?.encodeToAny(
                src?.value,
                dst?.value,
                q.extraParam()
            )
                ?: false

        if (result) {
            response.setResult(result)

            if (dst.value is File) {
                response.setResultFile(dst?.value as File)
            }
        } else {
            response.error(
                EndecodeConsts.ERR_NOT_FOUND_ENCODER,
                "Can't encode ${src} to ${dst} with dst srcMimetype=${dst?.mimetype} and extra param ${q.extraParam()}",
                null
            )
        }
    }


    override fun encode(
        src: DataInfo,
        dst: DataInfo,
        extraParam: Any?,
        result: ResponseInfo
    ): ResponseInfo {
        val encoded =
            findEncoder(src, dst, extraParam)?.encodeToAny(src?.value, dst?.value, extraParam)
                ?: false

        if (encoded) {
            result.result = encoded

            if (dst.value is File) {
                result.rawResultFile = dst.value as File
            }
        } else {
            result.errorInfo.message =
                "Can't encode ${src} to ${dst} with dst srcMimetype=${dst.mimetype} and extra param ${extraParam}"
        }

        return result
    }


}