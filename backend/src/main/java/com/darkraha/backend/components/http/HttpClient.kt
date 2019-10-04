package com.darkraha.backend.components.http

import com.darkraha.backend.QueryCallback
import com.darkraha.backend.client.BackendClientBase
import com.darkraha.backend.extraparams.UploadEP
import com.darkraha.backend.infos.MetaInfo
import java.io.File

/**
 * @author Verma Rahul
 */
open class HttpClient protected constructor() : BackendClientBase() {


    open fun checkLink(url: String, params: Map<String, String>?, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .addNamedParamsAll(params)
            .method(MetaInfo.METHOD_HEAD)
            .exeAsync()
    }

    open fun loadText(url: String, params: Map<String, String>?, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .addNamedParamsAll(params)
            .exeAsync()
    }

    open fun loadFile(url: String, fileDst: File, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .destination(fileDst)
            .exeAsync()
    }

    open fun postForm(url: String, params: Map<String, String>?, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .addNamedParamsAll(params)
            .method(MetaInfo.METHOD_POST)
            .exeAsync()
    }

    open fun uploadFile(url: String, extraParam: UploadEP, cb: QueryCallback?) {
        prepareQuery()
            .url(url)
            .addCallback(cb)
            .extraParam(extraParam)
            .exeAsync()
    }


    class Builder : ClientBuilder<HttpClient, HttpService, Builder>() {

        override fun newResult(): HttpClient {
            return HttpClient()
        }

        override fun checkService() {
            if (_service == null) {
                _service = HttpServiceDefault()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): HttpClient {
            return Builder().build()
        }
    }

}
