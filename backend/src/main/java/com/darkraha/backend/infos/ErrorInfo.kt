package com.darkraha.backend.infos


interface ErrorReader {

    fun errorCode(): Int
    fun errorMessage(): String?
    fun errorException(): Throwable?
    fun errorCallbacks(): List<Throwable>
}


class ErrorInfo : ErrorReader {

    var clientClassName: String? = null
    var serviceClassName: String? = null
    var code = 0
    var srvCode = 0
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
            srvCode = src.srvCode
            message = src.message
            exception = src.exception
            clientClassName = src.clientClassName
            serviceClassName = src.serviceClassName
            exceptionsCallbacks.clear()
            exceptionsCallbacks.addAll(src.exceptionsCallbacks)
        }
    }


    override fun toString(): String {
        return "ErrorInfo{ code=${code} srvCode=${srvCode} message=${message} exception=${exception} client='${clientClassName}' service='${serviceClassName}' callbacks error count=${exceptionsCallbacks.size}}"
    }

    fun clear() {
        code = 0
        srvCode = 0
        message = null
        exception = null
        serviceClassName = null
        clientClassName = null
        exceptionsCallbacks.clear()
    }

    override fun errorCallbacks(): List<Throwable> = errorCallbacks()
    override fun errorCode(): Int = errorCode()
    override fun errorException(): Throwable? = exception
    override fun errorMessage(): String? = message


    companion object {

        @JvmStatic
        val ERR_INTERNET_URI_PARSE = -10

        @JvmStatic
        val ERR_INTERNET_HOST = -11

        @JvmStatic
        val ERR_INTERNET_CONNECTION = -12

        /**
         * Used when known that device in offline mode.
         */
        @JvmStatic
        val ERR_INTERNET_OFFLINE = -13

        @JvmStatic
        val ERR_SERVER_ERROR = -30

        @JvmStatic
        val ERR_SERVER_UNAVAILABLE = -31

        @JvmStatic
        val ERR_FILE_WRITE = -50

        @JvmStatic
        val ERR_FILE_READ = -51

        @JvmStatic
        val ERR_FILE_NO_FREE_SPACE = -52

        @JvmStatic
        val ERR_FILE_ACCESS_DENIED = -53

        @JvmStatic
        val ERR_JSON_PARSE = -70

        @JvmStatic
        val ERR_IO = -90

        @JvmStatic
        val ERR_WORKFLOW = -1000
        @JvmStatic
        val ERR_WORKFLOW_PROCESSOR = -1001
    }

}