package com.darkraha.backend

import com.darkraha.backend.infos.ResponseInfo
import java.io.File

//interface ClientQueryEditor {
//
//    fun assignFrom(src: ResponseInfo)
//    fun setResult(r: Any?)
//    fun setRawString(s: String?, asResult: Boolean = false)
//    fun setRawBytes(b: ByteArray?, asResult: Boolean = false)
//    fun setRawSize(s: Long)
//    fun setResultMimetype(mt: String?)
//    fun setResultFile(file: File?)
//    fun cancel(code: Int, message: String?)
//    fun success(newResult: Any? = null, resultChainType: ChainType = ChainType.UNDEFINED)
//    fun error(code: Int, msg: String?, e: Throwable?)
//    fun error(responseInfo: ResponseInfo)
//    fun resultCode(code: Int)
//    fun resultMessage(msg: String? = null)
//    fun responseInfo(): ResponseInfo
//
//    fun getUserCallbacks(): List<Callback<UserQuery>>
//
//    fun error(e: Throwable?) {
//        error(-1, e?.message, e)
//    }
//
//
//    fun error(msg: String?) {
//        error(-1, msg, null)
//    }
//
//    fun cancel() {
//        cancel(0, null)
//    }
//
//}
