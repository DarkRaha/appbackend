package com.darkraha.backend.infos


interface ErrorReader {

    fun errorCode(): Int
    fun errorMessage(): String?
    fun errorException(): Throwable?
    fun errorCallbacks(): List<Throwable>
}


class ErrorInfo : ErrorReader {

    var code = 0
    var message: String? = null
    var exception: Throwable? = null
    val exceptionsCallbacks = mutableListOf<Throwable>()

    inline fun isError() = message != null || code != 0 || exception != null

    fun set(c: Int, msg: String?, e: Throwable?) {
        code = c
        message = msg
        exception = e
    }

    fun set(src: ErrorInfo) {
        if (src != this) {
            code = src.code
            message = src.message
            exception = src.exception
        }
    }

    fun clear() {
        code = 0
        message = null
        exception = null
        exceptionsCallbacks.clear()
    }

    override fun errorCallbacks(): List<Throwable> = errorCallbacks()
    override fun errorCode(): Int = errorCode()
    override fun errorException(): Throwable? = exception
    override fun errorMessage(): String? = message
}