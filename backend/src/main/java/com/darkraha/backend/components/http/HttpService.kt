package com.darkraha.backend.components.http

import com.darkraha.backend.*
import com.darkraha.backend.extensions.encodeMd5
import com.darkraha.backend.extensions.extractFileExtension
import com.darkraha.backend.extensions.isTextMimetype
import com.darkraha.backend.extraparams.UploadEP
import com.darkraha.backend.helpers.Units
import com.darkraha.backend.infos.ErrorInfo
import com.darkraha.backend.infos.ResponseInfo
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit
import okio.Okio
import java.io.IOException
import okhttp3.FormBody
import okhttp3.RequestBody
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.Exception


/**
 * @author Verma Rahul
 */
interface HttpService : Service {
    fun checkLink(url: String, params: Map<String, String>? = null): ResponseInfo
    fun loadText(url: String, params: Map<String, String>? = null): ResponseInfo
    fun loadFile(url: String, fileDst: File): ResponseInfo
    fun postForm(url: String, params: Map<String, String>? = null): ResponseInfo
    fun uploadFile(url: String, extraParam: UploadEP): ResponseInfo
}

/**
 * Http service based on okhttp engine.
 *
 * @author Verma Rahul
 */
open class HttpServiceDefault : HttpService {

    val mSchemes = HttpConsts.listOfSchemes()
    val mCommands = HttpConsts.listOfCommands()

    override val schemes: List<String>
        get() = mSchemes


    override val commands: List<String>
        get() = mCommands


    var httpOk = getHttpOkDefault()


    override fun checkLink(url: String, params: Map<String, String>?): ResponseInfo {
        val request = Request.Builder()
                .url(url)
                .head()

        addUrlParameters(request, params, url)

        val ret = ResponseInfo()

        runCatching {
            httpOk.newCall(request.build()).execute().use {
                if (!it.isSuccessful)
                    error(ret, it)
            }
        }.onFailure {
            error(ret, it)
        }

        return ret
    }

    override fun loadText(url: String, params: Map<String, String>?): ResponseInfo {
        val request = Request.Builder()
                .url(url)
                .get()

        addUrlParameters(request, params, url)


        val ret = ResponseInfo()

        runCatching {
            httpOk.newCall(request.build()).execute().use {
                if (it.isSuccessful) {
                    ret.rawResultString = it.body()?.string()
                    ret.result = ret.rawResultString
                } else {
                    error(ret, it)
                }
            }
        }.onFailure {
            error(ret, it)
        }

        return ret
    }

    override fun loadFile(url: String, fileDst: File): ResponseInfo {

        val request = Request.Builder()
                .url(url)
                .get()


        val ret = ResponseInfo()
        val tmpFile = getTmpFileFor(fileDst)!!

        runCatching {
            httpOk.newCall(request.build()).execute().use {
                if (it.isSuccessful) {
                    copy(it.body(), tmpFile)
                    tmpFile.renameTo(fileDst)
                    ret.rawResultFile = fileDst
                } else {
                    error(ret, it)
                }
            }
        }.onFailure {
            error(ret, it)
            tmpFile.delete()
        }


        return ret
    }

    override fun postForm(url: String, params: Map<String, String>?): ResponseInfo {
        val formBody = getFormBody(params)
        val request = Request.Builder().url(url).post(formBody)
        val ret = ResponseInfo()


        runCatching {
            httpOk.newCall(request.build()).execute().use {
                if (it.isSuccessful) {
                    ret.rawResultString = it.body()?.string()
                } else {
                    error(ret, it)
                }

            }
        }.onFailure {
            error(ret, it)
        }

        return ret
    }


    override fun uploadFile(url: String, extraParam: UploadEP): ResponseInfo {

        val formBody = getMultipartBody(extraParam, null)
        val request = Request.Builder().url(url).post(formBody)
        val ret = ResponseInfo()

        ret.result = extraParam.file

        runCatching {
            httpOk.newCall(request.build()).execute().use {

                if (!it.isSuccessful) {
                    error(ret, it)
                }
            }
        }.onFailure {
            error(ret, it)
        }

        return ret
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun handle(q: UserQuery, swh: ServiceWorkflowHelper, response: ClientQueryEditor) {

        try {
            val url = q.url()!!

            var fileDst = q.fileDestination()

            if (fileDst != null) {
                fileDst.parentFile.mkdirs()
            }

            with(HttpConsts) {
                when (q.getCommand()) {
                    CMD_CHECK_LINK -> response.assignFrom(checkLink(url, q.namedParams()))
                    CMD_LOAD_TEXT -> response.assignFrom(loadText(url, q.namedParams()))
                    CMD_LOAD_FILE -> response.assignFrom(loadFile(url, q.fileDestination()!!))
                    CMD_POST_FORM -> response.assignFrom(postForm(url, q.namedParams()))
                    CMD_UPLOAD_FILE -> response.assignFrom(
                            uploadFile(
                                    url,
                                    q.getExtraParamAs<UploadEP>()!!
                            )
                    )
                    else -> {
                        handleAuto(q, swh, response)
                    }
                }
            }
        } catch (e: Exception) {
            error(response, e)
             println("HttpService error ${q.url()}")
            e.printStackTrace()
        }

        if (q.isError()) {
            setStdError(response.responseInfo())
        }
    }


    protected fun handleAuto(
            q: UserQuery,
            swh: ServiceWorkflowHelper,
            response: ClientQueryEditor
    ) {

        val url = q.url()!!
        var fileDst = q.fileDestination()


        val uploadEP = if (q.extraParam() is UploadEP) {
            q.extraParam() as UploadEP
        } else null


        val request = Request.Builder()
                .url(url)

        var method = q.method()!!

        if (uploadEP != null) {
            method = "POST"
        }

        if ("POST" != method) {
            addUrlParameters(request, q.namedParams(), url)
        }

        addHeaders(request, q.inputHeaders())
        addCookies(request, q.inputCookies())


        when {
            isMethodUrlParams(method) -> request.method(method, null)
            uploadEP != null -> request.method(method, getMultipartBody(uploadEP, q.namedParams()))
            else -> request.method(method, getFormBody(q.namedParams()))
        }



        runCatching {
            httpOk.newCall(request.build()).execute().use {
                if (it.isSuccessful) {

                    response.resultCode(it.code())
                    response.resultMessage(it.message())

                    val contentSize = it.body()?.contentLength() ?: 0
                    response.setRawSize(contentSize)

                    val mimetype = it.body()?.contentType().toString()
                    response.setRawMimetype(mimetype)
                    val mimeTypeTxt = mimetype.isTextMimetype()

                    val body = it.body()!!
                    var tmpFile: File? = getTmpFileFor(fileDst)
                    val meta = swh.getMetaInfo()

                    if (q.isOptionSaveOutputHeaders()) {
                        meta.outHeaders.putAll(it.headers().toMultimap())
                    }

                    if (q.isOptionSaveOutputCookies()) {
                        it.headers().values("Set-Cookie")?.forEach {
                            val split1 = it.split(';')
                            val split2 = split1[0].split('=')
                            meta.outCookies[split2[0]] = split2[1]
                        }
                    }

                    if (tmpFile == null && ((q.isOptionSaveFile() || contentSize > Units.Kb * 500)
                                    || (!mimeTypeTxt && !q.isOptionSaveBytes()))
                    ) {
                        fileDst = File("${url.encodeMd5()}.${url.extractFileExtension()}")
                        tmpFile = getTmpFileFor(fileDst)
                    }

                    if (tmpFile != null) {
                        response.setResultFile(fileDst)
                        copy(body, tmpFile, q, swh, body.contentLength())

                        tmpFile.renameTo(fileDst)

                        if (q.isOptionSaveBytes()) {
                            response.setRawBytes(fileDst?.readBytes(), true)
                        }

                        if (q.isOptionSaveString()) {
                            response.setRawString(fileDst?.readText(), true)
                        }


                    } else {

                        if (q.isOptionSaveBytes()) {
                            response.setRawBytes(body.bytes(), true)
                        } else {
                            response.setRawString(body.string(), true)
                        }
                    }


                } else {
                    error(q, response, it)
                }

                true
            }
        }.onFailure {
            error(response, it)
        }

        if (q.isError()) {
            getTmpFileFor(fileDst)?.delete()
        }
    }


    //-----------------------------------------------------------------------------------
    //

    protected fun isMethodUrlParams(m: String): Boolean {
        return m == "GET" || m == "HEAD" || m == "DELETE"
    }


    protected fun getFormBody(params: Map<String, String>?): RequestBody {
        val ret = FormBody.Builder()

        if (params != null && params.size > 0) {

            params.forEach {
                ret.add(it.key, it.value)
            }

        }
        return ret.build()
    }


    protected fun getMultipartBody(upload: UploadEP?, params: Map<String, String>?): RequestBody {

        val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)

        if (upload != null) {
            body.addFormDataPart(
                    upload.name, upload.srvFilename,
                    RequestBody.create(MediaType.parse(upload.mimetype), upload.file)
            )
        }

        if (params != null && params.size > 0) {
            params.forEach {
                body.addFormDataPart(it.key, it.value)
            }
        }

        return body.build()
    }


    open protected fun getHttpOkDefault(): OkHttpClient {

        return OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build();
    }


    fun getTmpFileFor(file: File?): File? {

        if (file != null) {
            return File(file.getAbsolutePath() + ".tmp")
        }

        return null
    }

    protected fun addUrlParameters(r: Request.Builder, params: Map<String, String>?, url: String) {
        if (params != null && params.size > 0) {
            val httpBuider = HttpUrl.parse(url)!!.newBuilder()

            params.forEach {
                httpBuider.addQueryParameter(it.key, it.value)
            }

            r.url(httpBuider.build())
        }
    }


    protected fun addHeaders(r: Request.Builder, headers: Map<String, List<String>>?) {
        if (headers != null && headers.size > 0) {
            headers.forEach {
                it.value.forEach { value ->
                    r.addHeader(it.key, value)
                }
            }
        }
    }

    protected fun addCookies(r: Request.Builder, cookies: Map<String, String>?) {
        if (cookies != null && cookies.size > 0) {
            cookies.forEach {
                r.addHeader("Cookie", "${it.key}=${it.value}")
            }
        }
    }

    @Throws(IOException::class)
    protected fun copy(
            body: ResponseBody?, file: File, query: UserQuery, srvHelper: ServiceWorkflowHelper,
            contentLength: Long
    ) {

        body?.source()?.use { source ->
            Okio.sink(file).use {
                Okio.buffer(it).use { sink ->

                    val sinkBuffer = sink.buffer()
                    var totalBytesRead: Long = 0
                    var bytesRead: Long

                    while (!query.isFinished()) {

                        bytesRead = source.read(sinkBuffer, Units.BUFFER_SIZE)

                        if (bytesRead == -1L) {
                            break
                        } else {
                            sink.emit()
                            totalBytesRead += bytesRead
                            srvHelper.dispatchProgress(totalBytesRead.toFloat(), contentLength.toFloat())
                        }
                    }
                    sink.flush()
                }
            }
        }
    }


    @Throws(IOException::class)
    protected fun copy(
            body: ResponseBody?, file: File
    ) {

        body?.source()?.use { source ->
            Okio.sink(file).use {
                Okio.buffer(it).use { sink ->

                    val sinkBuffer = sink.buffer()

                    var totalBytesRead: Long = 0
                    var bytesRead: Long


                    while (true) {
                        bytesRead = source.read(sinkBuffer, Units.BUFFER_SIZE)

                        if (bytesRead == -1L) {
                            break
                        } else {
                            sink.emit()
                            totalBytesRead += bytesRead
                        }

                        sink.flush()
                    }
                }
            }
        }
    }


    fun error(responseInfo: ResponseInfo, it: Response) {
        responseInfo.errorInfo.set(it.code(), it.message() + "\n" + it.body().toString(), null)
    }


    fun error(query: UserQuery, response: ClientQueryEditor, it: Response) {
        response.error(it.code(), "Error with ${query.url()} code=${it.code()} msg=${it.message()} body=${it.body().toString()}", null)
    }

    fun error(responseInfo: ResponseInfo, it: Throwable) {
        responseInfo.errorInfo.set(0, it.message, it)
    }

    fun error(response: ClientQueryEditor, it: Throwable) {
        response.error(0, it.message, it)
    }


    fun setStdError(responseInfo: ResponseInfo) {

        when {
            responseInfo.resultCode in 400..499 -> {
                responseInfo.errorInfo.srvCode = responseInfo.resultCode
                responseInfo.errorInfo.code = ErrorInfo.ERR_SERVER_ERROR
            }

            responseInfo.resultCode in 500..599 -> {
                responseInfo.errorInfo.srvCode = responseInfo.resultCode
                responseInfo.errorInfo.code = ErrorInfo.ERR_SERVER_UNAVAILABLE
            }

            responseInfo.errorInfo.exception is UnknownHostException -> {
                responseInfo.errorInfo.code = ErrorInfo.ERR_INTERNET_HOST
            }

            responseInfo.errorInfo.exception is MalformedURLException -> {
                responseInfo.errorInfo.code = ErrorInfo.ERR_INTERNET_URI_PARSE
            }

            responseInfo.errorInfo.exception is SocketTimeoutException -> {
                responseInfo.errorInfo.code = ErrorInfo.ERR_INTERNET_CONNECTION
            }

            responseInfo.errorInfo.exception is SecurityException -> {
                responseInfo.errorInfo.code = ErrorInfo.ERR_FILE_ACCESS_DENIED
            }

            responseInfo.errorInfo.exception is IOException -> {
                responseInfo.errorInfo.code = ErrorInfo.ERR_IO
            }
        }


    }

}




