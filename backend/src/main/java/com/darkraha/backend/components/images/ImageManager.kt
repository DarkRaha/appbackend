package com.darkraha.backend.components.images

import com.darkraha.backend.*
import com.darkraha.backend.components.diskcache.DiskCacheClient
import com.darkraha.backend.components.endecode.EndecodeClient
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.infos.CancelInfo


/**
 * @author Verma Rahul
 */
open class ImageManager protected constructor() : ImageManagerClientA() {


    override fun onLoadSuccessCallback(query: UserQuery) {

        if (query.chainTypeResponse() == ChainType.LAST_ELEMENT) {
            assignImage(query)
        } else {
            produceDecodesQueries(query)
        }


    }

    override fun onLoadFinishCallback(query: UserQuery) {
        synchronized(loadingFiles) {
            loadingFiles.remove(query.url())
        }
    }

    override fun onDecodeSuccessCallback(query: UserQuery) {

        if (query.chainTypeResponse() == ChainType.LAST_ELEMENT
                || query.chainTypeResponse() == ChainType.STAND_ALONE
        ) {
            assignImage(query)
        }
    }

    override fun onPrepareLoad(q: UserQuery, response: ClientQueryEditor) {


        val url = q.url()!!

        if (!url.startsWith("http")) {
            response.error("Http or https schemes supported")
            return
        }

        if (!checkCachesBeforeLoad(q, response)) {


            synchronized(loadingFiles) {
                val qAlready = loadingFiles[url]
                if (qAlready?.appendQuery(WorkflowAppend(), q) ?: false) {

                    response.cancel(1, "Query was appended to existing query.")
                } else {
                    loadingFiles[url] = q
                }

                q.uiParam()?.run {
                    uiUrlMap[this] = url
                }
            }
        }
    }

    override fun onPreLoad(q: UserQuery, response: ClientQueryEditor) {
        checkCachesBeforeLoad(q, response)
    }


    override fun onPrepareDecode(q: UserQuery, response: ClientQueryEditor) {
        if (q.isStandAloneQuery()) {//?
            q.uiParam()?.let {
                uiUrlMap[it] = q.getQueryId()
            }
        }
    }

    override fun onPreDecode(q: UserQuery, response: ClientQueryEditor) {
        q.uiParam()?.let {
            if (q.getQueryId() != uiUrlMap[it]) {
                q.cancel(CancelInfo.CANCEL_BY_USER, "Canceled, ui object not corresponds to url ${q.getQueryId()}.")
                println("ImageManagerClientDefault ${q.cancelInfo()}")
                return
            }
        }

        checkMemCache(q, response)
    }

    override fun onPostDecode(q: UserQuery, response: ClientQueryEditor) {
        q.getQueryId()?.let {
            val img = q.result()!!
            if (cache[it] == null) {
                cache[it] = img
                cache.cleanup()
            }
        }
    }


    class Builder : ClientBuilder<ImageManager, ImageManagerService, Builder>() {

        var _diskCacheClient: DiskCacheClient? = null
        var _httpClient: HttpClient? = null
        var _endecodeClient: EndecodeClient? = null
        var _imagePlatformHelper: ImagePlatformHelper = ImagePlatformHelperBase()

        override fun newResult(): ImageManager {
            return ImageManager()
        }

        fun diskCacheClient(dc: DiskCacheClient): Builder {
            _diskCacheClient = dc
            return builder
        }

        fun httpClient(httpClient: HttpClient): Builder {
            _httpClient = httpClient
            return builder
        }

        fun endecodeClient(endecodeClient: EndecodeClient): Builder {
            _endecodeClient = endecodeClient
            return builder
        }

        fun imagePlatformHelper(imagePlatformHelper: ImagePlatformHelper): Builder {
            _imagePlatformHelper = imagePlatformHelper
            return builder
        }


        override fun checkService() {

        }

        override fun checkWorkdir() {
            _workdir = _diskCacheClient?.workdir
            super.checkWorkdir()
        }

        open fun checkDiskcache() {
            if (_diskCacheClient == null) {
                if (_backend != null) {
                    _diskCacheClient = _backend!!.diskCacheClient
                } else {
                    val dcBuilder = DiskCacheClient.Builder()
                    if (_workdir != null) {
                        dcBuilder.workdir(_workdir!!)
                    }

                    _diskCacheClient = dcBuilder.build()
                }
            }
        }

        open fun checkHttpClient() {
            if (_httpClient == null) {
                _httpClient = _backend?.httpClient ?: HttpClient.Builder().build()
            }
        }

        open fun checkEndecodeClient() {
            if (_endecodeClient == null) {
                _endecodeClient = _backend?.endecodeClient
                        ?: EndecodeClient.Builder().build()
            }
        }


        override fun build(): ImageManager {
            super.build()
            checkDiskcache()
            checkHttpClient()
            checkEndecodeClient()


            result.diskCacheClient = _diskCacheClient!!
            result.httpClient = _httpClient!!
            result.endecoder = _endecodeClient!!
            result.imagePlatformHelper = _imagePlatformHelper ?: ImagePlatformHelperBase()

            result.attachImagePlatformHelper(result.imagePlatformHelper)

            return result
        }

    }


    companion object {
        fun newInstance(): ImageManager {
            return Builder().build()
        }
    }

}