package com.darkraha.backend.infos


interface MetaInfoReader {
    fun method(): String?
    fun outputHeaders(): Map<String, List<String>>
    fun outputCookies(): Map<String, String>
    fun inputHeaders(): Map<String, List<String>>
    fun inputCookies(): Map<String, String>
    fun isOptionSaveOutputHeaders(): Boolean
    fun isOptionSaveOutputCookies(): Boolean
}


/**
 * Meta information for service.
 */
class MetaInfo : MetaInfoReader {
    var method: String? = METHOD_GET
    val inHeaders = mutableMapOf<String, MutableList<String>>()
    val inCookies = mutableMapOf<String, String>()

    val outHeaders = mutableMapOf<String, MutableList<String>>()
    val outCookies = mutableMapOf<String, String>()

    var state = 0
        private set


    fun assignFrom(src: MetaInfo) {
        method = src.method
        inHeaders.putAll(src.inHeaders)
        inCookies.putAll(src.inCookies)
    }

    fun clear() {
        method = METHOD_GET
        inHeaders.clear()
        outHeaders.clear()
        inCookies.clear()
        outCookies.clear()
    }

    inline fun saveHeaders() = state.or(F_SAVE_HEADERS)
    inline fun saveCookies() = state.or(F_SAVE_COOKIES)


    inline fun methodPut() {
        method = METHOD_PUT
    }

    inline fun methodGet() {
        method = METHOD_GET
    }

    inline fun methodDelete() {
        method = METHOD_DELETE
    }

    inline fun methodHead() {
        method = METHOD_HEAD
    }

    inline fun methodPost() {
        method = METHOD_POST
    }

    inline fun methodPatch() {
        method = METHOD_PATCH
    }

    inline fun methodOptions() {
        method = METHOD_OPTIONS
    }


    //---------------------------------------------------------------------------------------
    override fun inputCookies(): Map<String, String> = inCookies
    override fun inputHeaders(): Map<String, List<String>> = inHeaders
    override fun method(): String? = method
    override fun outputCookies(): Map<String, String> = outCookies
    override fun outputHeaders(): Map<String, List<String>> = outHeaders
    override fun isOptionSaveOutputCookies(): Boolean = state.and(F_SAVE_COOKIES) > 0
    override fun isOptionSaveOutputHeaders(): Boolean = state.and(F_SAVE_HEADERS) > 0
    //---------------------------------------------------------------------------------------

    companion object {
        @JvmStatic
        val F_SAVE_HEADERS = 1
        @JvmStatic
        val F_SAVE_COOKIES = 2

        @JvmStatic
        val METHOD_GET = "GET"
        @JvmStatic
        val METHOD_PUT = "PUT"
        @JvmStatic
        val METHOD_HEAD = "HEAD"
        @JvmStatic
        val METHOD_DELETE = "DELETE"
        @JvmStatic
        val METHOD_POST = "POST"
        @JvmStatic
        val METHOD_PATCH = "PATCH"
        @JvmStatic
        val METHOD_OPTIONS = "OPTIONS"
    }

}