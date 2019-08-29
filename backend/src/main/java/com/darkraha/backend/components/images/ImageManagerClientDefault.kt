package com.darkraha.backend.components.images

import com.darkraha.backend.*
import com.darkraha.backend.components.diskcache.DiskCacheClient
import com.darkraha.backend.components.diskcache.DiskCacheClientDefault
import com.darkraha.backend.components.endecode.EndecodeClient
import com.darkraha.backend.components.endecode.EndecodeClientDefault
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.components.http.HttpClientDefault
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
            val url = query.getQueryId()
            val img = query.result()

            if (url != null && img != null) {
                cache[url] = img
            }
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

    override fun onPreDecode(q: UserQuery, response: ClientQueryEditor) {
        checkMemCache(q, response)
    }

    override fun onPrepareDecode(q: UserQuery, response: ClientQueryEditor) {
        if (q.isStandAloneQuery()) {
            val ui = q.uiParam()
            if (ui != null) {
                uiUrlMap[ui] = q.getQueryId()
            }
        }
    }

    class Builder : ClientBuilder<ImageManagerClientDefault, ImageManagerService, Builder>() {

        var _diskCacheClient: DiskCacheClient? = null
        var _httpClient: HttpClient? = null
        var _endecodeClient: EndecodeClient? = null
        var _imagePlatformHelper: ImagePlatformHelper? = null

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


        override fun defaultService(): Service? {
            return null
        }

        override fun defaultExecuterService(): ExecutorService? {
            return null
        }

        override fun build(): ImageManagerClientDefault {
            super.build()
            if (_diskCacheClient != null) {
                _workdir = _diskCacheClient!!.getWorkDir()
            }

            result.httpClient = _httpClient ?: HttpClientDefault.Builder().queryManager(result.queryManager).build()


            if (_diskCacheClient == null) {
                val dcBuilder = DiskCacheClientDefault.Builder()
                if (_workdir != null) {
                    dcBuilder.workdir(_workdir!!)
                }

                dcBuilder.queryManager(result.queryManager)
                _diskCacheClient = dcBuilder.build()
            }

            result.diskCacheClient = _diskCacheClient!!
            _workdir = result.diskCacheClient.getWorkDir()
            result.workdir = _workdir!!
            result.workdir.mkdirs()

            result.endecoder =
                _endecodeClient ?: EndecodeClientDefault.Builder().queryManager(result.queryManager).build()
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