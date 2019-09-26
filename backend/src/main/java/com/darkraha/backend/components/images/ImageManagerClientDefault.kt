package com.darkraha.backend.components.images

import com.darkraha.backend.*
import com.darkraha.backend.components.diskcache.DiskCacheClient
import com.darkraha.backend.components.diskcache.DiskCacheClientDefault
import com.darkraha.backend.components.endecode.EndecodeClient
import com.darkraha.backend.components.endecode.EndecodeClientDefault
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.components.http.HttpClientDefault
import com.darkraha.backend.infos.CancelInfo
import java.util.concurrent.ExecutorService


/**
 * @author Verma Rahul
 */
open class ImageManagerClientDefault protected constructor() : ImageManagerClientA() {


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
//            val url = query.getQueryId()
//            val img = query.result()

//            if (url != null && img != null) {
//                cache[url] = img
//                 println("ImageManagerClientA (Default) save to memory result: result=${img?.javaClass?.simpleName} url: ${url} img=${cache[url]}")
//            } else {
//                println("ImageManagerClientA (Default)Can not save to memory result: result=${img?.javaClass?.simpleName} url: ${url} ")
//            }
        }
    }

    override fun onPrepareLoad(q: UserQuery, response: ClientQueryEditor) {


        if (!checkCachesBeforeLoad(q, response)) {

            val url = q.url()!!

            synchronized(loadingFiles) {
                val qAlready = loadingFiles[url]
                if (qAlready?.appendQuery(WorkflowAppend()) ?: false) {
                    response.cancel(1, "Query was appended to existing query.")
                } else {
                    loadingFiles[url] = q
                }

                val ui = q.uiParam()
                if (ui != null) {
                    uiUrlMap[ui] = url
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
            }
        }
    }


    class Builder : ClientBuilder<ImageManagerClientDefault, ImageManagerService, Builder>() {

        var _diskCacheClient: DiskCacheClient? = null
        var _httpClient: HttpClient? = null
        var _endecodeClient: EndecodeClient? = null
        var _imagePlatformHelper: ImagePlatformHelper = ImagePlatformHelperEmpty()

        override fun newResult(): ImageManagerClientDefault {
            return ImageManagerClientDefault()
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
            _workdir = _diskCacheClient?.getWorkDir()
            super.checkWorkdir()
        }

        open fun checkDiskcache() {
            if (_diskCacheClient == null) {
                if (_backend != null) {
                    _diskCacheClient = _backend!!.diskCacheClient
                } else {
                    val dcBuilder = DiskCacheClientDefault.Builder()
                    if (_workdir != null) {
                        dcBuilder.workdir(_workdir!!)
                    }

                    dcBuilder.queryManager(_queryManager!!)
                    _diskCacheClient = dcBuilder.build()
                }
            }
        }

        open fun checkHttpClient() {
            if (_httpClient == null) {
                _httpClient = _backend?.httpClient ?: HttpClientDefault.Builder().queryManager(
                        _queryManager!!
                ).build()
            }
        }

        open fun checkEndecodeClient() {
            if (_endecodeClient == null) {
                _endecodeClient = _backend?.endecodeClient
                        ?: EndecodeClientDefault.Builder().queryManager(result.queryManager).build()
            }
        }


        override fun build(): ImageManagerClientDefault {
            super.build()
            checkDiskcache()
            checkHttpClient()
            checkEndecodeClient()


            result.diskCacheClient = _diskCacheClient!!
            result.httpClient = _httpClient!!
            result.endecoder = _endecodeClient!!
            result.imagePlatformHelper = _imagePlatformHelper ?: ImagePlatformHelperEmpty()

            result.attachImagePlatformHelper(result.imagePlatformHelper)

            return result
        }

    }


    companion object {
        fun newInstance(): ImageManagerClientDefault {
            return Builder().build()
        }
    }

}