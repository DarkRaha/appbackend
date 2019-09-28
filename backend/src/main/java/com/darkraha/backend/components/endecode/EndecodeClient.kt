package com.darkraha.backend.components.endecode

import com.darkraha.backend.*
import com.darkraha.backend.client.BackendClientBase
import java.io.File
import kotlin.reflect.KClass

//interface EndecodeClient : Client {
//
//    fun addDecoder(fileDecoder: FileDecoder)
//    fun addEncoder(fileEncoder: FileEncoder)
//
//    fun getDecodersList(): List<FileDecoder>
//    fun getEncodersList(): List<FileEncoder>
//
//
//    fun prepareDecode(
//        src: File,
//        srcMimetype: String?,
//        dstClass: KClass<*>?,
//        extra: Any? = null,
//        cb: Callback<UserQuery>? = null
//    ): Query
//
//
//    fun buildDecode(
//        src: File,
//        srcMimetype: String?,
//        dstClass: KClass<*>?,
//        extra: Any? = null,
//        cb: Callback<UserQuery>? = null
//    ): QueryBuilder<WorkflowBuilder1> {
//        return prepareDecode(src, srcMimetype, dstClass, extra, cb)
//    }
//
//
//    fun decode(
//        src: File,
//        srcMimetype: String?,
//        dstClass: KClass<*>,
//        extra: Any? = null,
//        cb: Callback<UserQuery>? = null
//    ): UserQuery {
//        return prepareDecode(src, srcMimetype, dstClass, extra, cb).exeAsync()
//    }
//
//
//    fun prepareEncode(
//        src: Any,
//        dst: File,
//        extra: Any? = null,
//        cb: Callback<UserQuery>? = null
//    ): Query
//
//    fun encode(
//        src: Any,
//        dst: File,
//        extra: Any? = null,
//        cb: Callback<UserQuery>? = null
//    ): UserQuery {
//        return prepareEncode(src, dst, extra, cb).exeAsync()
//    }
//
//
//    fun buildEncode(
//        src: Any,
//        dst: File,
//        extra: Any? = null,
//        cb: Callback<UserQuery>? = null
//    ): QueryBuilder<WorkflowBuilder1> {
//        return prepareEncode(src, dst, extra, cb)
//    }
//
//}


open class EndecodeClient protected constructor() : BackendClientBase() {


    open fun addDecoder(fileDecoder: FileDecoder) {
        (service as EndecodeService).addFileDecoder(fileDecoder)
    }


    open fun addEncoder(fileEncoder: FileEncoder) {
        (service as EndecodeService).addFileEncoder(fileEncoder)
    }


    open fun getDecodersList(): List<FileDecoder> {
        return (service as EndecodeService).getDecodersList()
    }

    open fun getEncodersList(): List<FileEncoder> {
        return (service as EndecodeService).getEncodersList()
    }

    open fun prepareDecode(
            src: File,
            srcMimetype: String?,
            dstClass: KClass<*>?,
            extra: Any?,
            cb: Callback<UserQuery>?
    ): Query {
        return prepareQuery().apply {
            source(src, srcMimetype)
                    .destinationClass(dstClass)
                    .extraParam(extra)
                    .command(EndecodeConsts.CMD_DECODE)
                    .addCallback(cb)
        }
    }

    open fun prepareEncode(src: Any, dst: File, extra: Any?, cb: Callback<UserQuery>?): Query {
        return prepareQuery()?.apply {
            source(src).destination(dst).extraParam(extra).command(EndecodeConsts.CMD_ENCODE)
                    .addCallback(cb)
        }
    }


    //----------------------------------------------------------------------------
    class Builder : ClientBuilder<EndecodeClient, EndecodeService, Builder>() {
        override fun newResult(): EndecodeClient {
            return EndecodeClient()
        }

        override fun checkService() {
            if (_service == null) {
                _service = EndecodeServiceDefault()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): EndecodeClient {
            return Builder().build()
        }
    }


}