package com.darkraha.backend.components.http

import com.darkraha.backend.QueryCallback
import com.darkraha.backend.Service
import com.darkraha.backend.client.Client
import com.darkraha.backend.client.ClientBase
import com.darkraha.backend.extraparams.UploadEP
import com.darkraha.backend.infos.MetaInfo
import java.io.File


interface HttpClient : Client {
    fun subClient(name: String?): HttpClient
    fun checkLink(url: String, params: Map<String, String>? = null, cb: QueryCallback? = null)
    fun loadText(url: String, params: Map<String, String>? = null, cb: QueryCallback? = null)
    fun loadFile(url: String, fileDst: File, cb: QueryCallback? = null)
    fun postForm(url: String, params: Map<String, String>? = null, cb: QueryCallback? = null)
    fun uploadFile(url: String, extraParam: UploadEP, cb: QueryCallback? = null)
}

/**
 * @author Verma Rahul
 */
open class HttpClientDefault protected constructor() : ClientBase(), HttpClient {

    override fun subClient(name: String?): HttpClient {
        val builder = Builder()

        builder.mainThread(mainThread!!)
            .queryManager(queryManager!!)
            .service(service as HttpService)

        builder.workdir(
            if (name != null && name.length > 0) {
                File(workdir, name)
            } else {
                workdir
            }
        )

        return builder.build()
    }

    override fun checkLink(url: String, params: Map<String, String>?, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .addNamedParamsAll(params)
            .method(MetaInfo.METHOD_HEAD)
            .exeAsync()
    }

    override fun loadText(url: String, params: Map<String, String>?, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .addNamedParamsAll(params)
            .exeAsync()
    }

    override fun loadFile(url: String, fileDst: File, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .destination(fileDst)
            .exeAsync()
    }

    override fun postForm(url: String, params: Map<String, String>?, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .addNamedParamsAll(params)
            .method(MetaInfo.METHOD_POST)
            .exeAsync()
    }

    override fun uploadFile(url: String, extraParam: UploadEP, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .extraParam(extraParam)
            .exeAsync()
    }


    class Builder : ClientBuilder<HttpClientDefault, HttpService, Builder>() {

        override fun newResult(): HttpClientDefault {
            return HttpClientDefault()
        }

        override fun checkService() {
            if (_service == null) {
                _service = HttpServiceDefault()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): HttpClientDefault {
            return Builder().build()
        }
    }

}
