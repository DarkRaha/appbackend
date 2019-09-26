package com.darkraha.backend.components.images

import com.darkraha.backend.*
import com.darkraha.backend.cache.MemUsageCalculator
import com.darkraha.backend.cache.SoftUsageLRUCache
import com.darkraha.backend.client.ClientBase
import com.darkraha.backend.components.diskcache.DiskCacheClient
import com.darkraha.backend.components.endecode.EndecodeClient
import com.darkraha.backend.components.endecode.FileDecoder
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.extraparams.ImageLoadEP
import java.io.File
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

abstract class ImageManagerClientA : ImageManagerClient, ClientBase() {

    lateinit var diskCacheClient: DiskCacheClient
        protected set

    lateinit var endecoder: EndecodeClient
        protected set

    lateinit protected var httpClient: HttpClient

    lateinit var imagePlatformHelper: ImagePlatformHelper
        protected set

    protected var cache: SoftUsageLRUCache = SoftUsageLRUCache()

    /**
     * For checking is image actual for this ui object.
     */
    protected val uiUrlMap = Collections.synchronizedMap(WeakHashMap<Any?, String>())
    /**
     *
     */
    protected val loadingFiles = HashMap<String, UserQuery>()

    /**
     *
     */
    protected val imgConverters = HashMap<KClass<*>, ImageConverter>()


    val callbackLoad: Callback<UserQuery> = object : Callback<UserQuery> {
        override fun onSuccess(query: UserQuery) = onLoadSuccessCallback(query)
        override fun onCancel(query: UserQuery) = onLoadCancelCallback(query)
        override fun onError(query: UserQuery) = onLoadErrorCallback(query)
        override fun onComplete(query: UserQuery) = onLoadCompleteCallback(query)
        override fun onFinish(query: UserQuery) = onLoadFinishCallback(query)
        override fun onPrepare(query: UserQuery) = onLoadPrepareCallback(query)
    }

    val callbackDecode: Callback<UserQuery> = object : Callback<UserQuery> {
        override fun onSuccess(query: UserQuery) = onDecodeSuccessCallback(query)
        override fun onCancel(query: UserQuery) = onDecodeCancelCallback(query)
        override fun onError(query: UserQuery) = onDecodeErrorCallback(query)
        override fun onComplete(query: UserQuery) = onDecodeCompleteCallback(query)
        override fun onFinish(query: UserQuery) = onDecodeFinishCallback(query)
        override fun onPrepare(query: UserQuery) = onDecodePrepareCallback(query)
    }


    //--------------------------------------------------------------------------------------

    open protected fun checkMemCache(q: UserQuery, response: ClientQueryEditor): Boolean {
        q.uiParam()?.let {
            val result = cache[q.getQueryId()!!]

            if (result != null) {
                response.success(result, ChainType.LAST_ELEMENT)
                return true
            }
        }

        println("ImageManagerClientA not in memImg = ${cache[q.getQueryId()!!]} url=${q.getQueryId()!!}")
        return false
    }


    open protected fun checkCachesBeforeLoad(q: UserQuery, response: ClientQueryEditor): Boolean {
        if (!checkMemCache(q, response)) {

            q.fileDestination()?.apply {
                if (exists() && length() > 0) {
                    produceDecodesQueries(q)
                    response.success(q.result(), ChainType.FIRST_ELEMENT)
                    return true
                }
            }

            return false
        }
        return false
    }


    fun produceDecodesQueries(q: UserQuery) {
        val uiParam = q.uiParam()
        val url = q.url()

        var usercallbacks = (q as ClientQueryEditor).getUserCallbacks()

        if (uiParam != null) {

            val builder = buildDecode(
                    q.url()!!,
                    q.fileDestination()!!,
                    uiParam,
                    null,
                    q.extraParam() as ImageLoadEP?,
                    null
            ).chainTypeCreate(ChainType.LAST_ELEMENT)


            usercallbacks.forEach { cb -> builder.addCallback(cb) }

            uiUrlMap[uiParam] = url

            builder.exeAsync()
        }

        q.getAppendedQueries().forEach {
            if (it.ui != null) {
                buildDecode(
                        q.url()!!, q.fileSource()!!, it.ui!!,
                        it.callback, it.extraParam as ImageLoadEP
                ).chainTypeCreate(ChainType.STAND_ALONE).exeAsync()
            }
        }
    }


    /**
     * must invoked in main thread.
     */
    open protected fun assignImage(query: UserQuery) {
        val ui = query.uiParam()
        val url = query.url() ?: query.getQueryId()!!
        val urlExpected = uiUrlMap[ui]

        //
        var img = cache[url]

        if (img == null) {
            img = query.result()
            cache[url] = img!!
        }

        if (ui != null && (urlExpected == null || url == urlExpected)) {
            assignImage(img, ui, query.getExtraParamAs<ImageLoadEP>())
        }
    }


    protected fun assignImage(img: Any?, ui: Any?, ep: ImageLoadEP?): Boolean {

        if (img == null || ui == null) {
            return false
        }


        val cvtImg = convertImage(img) ?: img
        imagePlatformHelper.assignImage(cvtImg, ui)
        uiUrlMap.remove(ui)

//        if (ep == null || (ep != null && ep.isAutoAnimate)) {
//            imagePlatformHelper.startAnimation(ui)
//        }

        return true
    }

    override fun cancelLoad(ui: Any) {
        var query = loadingFiles[uiUrlMap[ui]]
        if (query != null) {
            query.cancelUi(ui, null, true)
        }
    }

    override fun addImageConverter(cls: KClass<*>, imgConv: ImageConverter) {
        imgConverters[cls] = imgConv
    }

    override fun convertImage(img: Any): Any? {
        return imgConverters[img::class]?.invoke(img)
    }

    override fun removeFromDiskcache(url: String): File? {
        return diskCacheClient.getFile(url)
    }

    override fun removeFromMemory(url: String): Any? {
        return cache.remove(url)
    }

    override fun addImageSizeCalculator(cls: KClass<*>, calc: MemUsageCalculator) {
        cache.addMemoryCalculator(cls, calc)
    }

    override fun addImageDecoder(imageDecoder: FileDecoder) {
        endecoder.addDecoder(imageDecoder)
    }

    override fun attachImagePlatformHelper(imgHelper: ImagePlatformHelper) {
        imagePlatformHelper = imgHelper
        imagePlatformHelper.onAttach(this)
    }

    override fun getDiskcache(): DiskCacheClient {
        return diskCacheClient
    }

    override fun loadFromMemory(url: String, ui: Any?, ep: ImageLoadEP?): Boolean {
        return assignImage(cache[url], ui, ep)
    }

    override fun buildLoad(
            url: String,
            ui: Any?,
            cb: Callback<UserQuery>?,
            ep: ImageLoadEP?,
            progressListener: ProgressListener?
    ): QueryBuilder<WorkflowBuilder1> {
        val builder =
                prepareQueryFrom(httpClient.prepareDefaultQuery())
                        .queryId(url)
                        .url(url)
                        .uiParam(ui)
                        .destination(diskCacheClient.genFile(url))
                        .extraParam(ep)
                        .addCallback(cb)
                        .progressListener(progressListener)
                        .allowAppend(true)
                        .callbackFirst(callbackLoad)
                        .resSync(url)
                        .addPrepareProcessor(::onPrepareLoad)
                        .addPostProcessor(::onPostLoad)
                        .addPreProcessor(::onPreLoad)
                        .chainTypeCreate(if (ui == null) ChainType.STAND_ALONE else ChainType.FIRST_ELEMENT)


        return builder
    }

    override fun buildDecode(
            url: String?,
            file: File,
            ui: Any?,
            cb: Callback<UserQuery>?,
            ep: ImageLoadEP?,
            progressListener: ProgressListener?
    ): WorkflowBuilder<WorkflowBuilder1> {
        val _url = url ?: file.toURI().toString()

        return prepareQueryFrom(endecoder.prepareDecode(file, "image/*", null, ep, cb))
                .queryId(_url)
                .uiParam(ui)
                .allowAppend(false)
                .resSync(_url)
                .addPrepareProcessor(::onPrepareDecode)
                .addPreProcessor(::onPreDecode)
                .addPostProcessor(::onPostDecode)
                .callbackFirst(callbackDecode)
                .chainTypeCreate(ChainType.STAND_ALONE)
    }


    //-----------------------------------------------------------------------------------------------
    open fun onPrepareLoad(q: UserQuery, response: ClientQueryEditor) {

    }

    open fun onPreLoad(q: UserQuery, response: ClientQueryEditor) {

    }

    open fun onPostLoad(q: UserQuery, response: ClientQueryEditor) {

    }

    open fun onPrepareDecode(q: UserQuery, response: ClientQueryEditor) {

    }

    open fun onPreDecode(q: UserQuery, response: ClientQueryEditor) {

    }

    open fun onPostDecode(q: UserQuery, response: ClientQueryEditor) {

    }


    //---------------------------------------------------------------
    // Loading common callback
    open fun onLoadPrepareCallback(query: UserQuery) {

    }

    open fun onLoadSuccessCallback(query: UserQuery) {

    }

    open fun onLoadCancelCallback(query: UserQuery) {

    }

    open fun onLoadErrorCallback(query: UserQuery) {

    }

    open fun onLoadCompleteCallback(query: UserQuery) {

    }

    open fun onLoadFinishCallback(query: UserQuery) {

    }


    //---------------------------------------------------------------
    // Decoding common callback
    open fun onDecodePrepareCallback(query: UserQuery) {

    }

    open fun onDecodeSuccessCallback(query: UserQuery) {

    }

    open fun onDecodeCancelCallback(query: UserQuery) {

    }

    open fun onDecodeErrorCallback(query: UserQuery) {

    }

    open fun onDecodeCompleteCallback(query: UserQuery) {

    }

    open fun onDecodeFinishCallback(query: UserQuery) {

    }


}