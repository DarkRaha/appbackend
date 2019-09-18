package com.darkraha.backend.components.endecode

import com.darkraha.backend.*
import com.darkraha.backend.client.Client
import com.darkraha.backend.client.ClientBase
import java.io.File
import kotlin.reflect.KClass

interface EndecodeClient : Client {

    fun addDecoder(fileDecoder: FileDecoder)
    fun addEncoder(fileEncoder: FileEncoder)

    fun getDecodersList(): List<FileDecoder>
    fun getEncodersList(): List<FileEncoder>


    fun prepareDecode(
        src: File,
        srcMimetype: String?,
        dstClass: KClass<*>?,
        extra: Any? = null,
        cb: Callback<UserQuery>? = null
    ): Query


    fun buildDecode(
        src: File,
        srcMimetype: String?,
        dstClass: KClass<*>?,
        extra: Any? = null,
        cb: Callback<UserQuery>? = null
    ): QueryBuilder<WorkflowBuilder1> {
        return prepareDecode(src, srcMimetype, dstClass, extra, cb)
    }


    fun decode(
        src: File,
        srcMimetype: String?,
        dstClass: KClass<*>,
        extra: Any? = null,
        cb: Callback<UserQuery>? = null
    ): UserQuery {
        return prepareDecode(src, srcMimetype, dstClass, extra, cb).exeAsync()
    }


    fun prepareEncode(
        src: Any,
        dst: File,
        extra: Any? = null,
        cb: Callback<UserQuery>? = null
    ): Query

    fun encode(
        src: Any,
        dst: File,
        extra: Any? = null,
        cb: Callback<UserQuery>? = null
    ): UserQuery {
        return prepareEncode(src, dst, extra, cb).exeAsync()
    }


    fun buildEncode(
        src: Any,
        dst: File,
        extra: Any? = null,
        cb: Callback<UserQuery>? = null
    ): QueryBuilder<WorkflowBuilder1> {
        return prepareEncode(src, dst, extra, cb)
    }

}


open class EndecodeClientDefault protected constructor() : EndecodeClient, ClientBase() {


    override fun addDecoder(fileDecoder: FileDecoder) {
        (service as EndecodeService).addFileDecoder(fileDecoder)
    }


    override fun addEncoder(fileEncoder: FileEncoder) {
        (service as EndecodeService).addFileEncoder(fileEncoder)
    }


    override fun getDecodersList(): List<FileDecoder> {
        return (service as EndecodeService).getDecodersList()
    }

    override fun getEncodersList(): List<FileEncoder> {
        return (service as EndecodeService).getEncodersList()
    }

    override fun prepareDecode(
        src: File,
        srcMimetype: String?,
        dstClass: KClass<*>?,
        extra: Any?,
        cb: Callback<UserQuery>?
    ): Query {
        val ret = prepareDefaultQuery()
        ret.source(src, srcMimetype)
            .destinationClass(dstClass)
            .extraParam(extra)
            .command(EndecodeConsts.CMD_DECODE)
            .addCallback(cb)
        return ret
    }

    override fun prepareEncode(src: Any, dst: File, extra: Any?, cb: Callback<UserQuery>?): Query {
        val ret = prepareDefaultQuery()
        ret.source(src).destination(dst).extraParam(extra).command(EndecodeConsts.CMD_ENCODE)
            .addCallback(cb)
        return ret
    }


    //----------------------------------------------------------------------------
    class Builder : ClientBuilder<EndecodeClientDefault, EndecodeService, Builder>() {
        override fun newResult(): EndecodeClientDefault {
            return EndecodeClientDefault()
        }

        override fun checkService() {
            if (_service == null) {
                _service = EndecodeServiceDefault()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): EndecodeClientDefault {
            return Builder().build()
        }
    }


}