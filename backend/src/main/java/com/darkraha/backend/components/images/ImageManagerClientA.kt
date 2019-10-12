package com.darkraha.backend.components.images

import com.darkraha.backend.*
import com.darkraha.backend.cache.MemoryUsage
import com.darkraha.backend.cache.SoftUsageLRUCache
import com.darkraha.backend.client.BackendClientBase
import com.darkraha.backend.components.diskcache.DiskCacheClient
import com.darkraha.backend.components.endecode.EndecodeClient
import com.darkraha.backend.components.endecode.FileDecoder
import com.darkraha.backend.components.endecode.FileEncoder
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.extraparams.ImageLoadEP
import com.darkraha.backend.infos.CancelInfo
import java.io.File
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap
import kotlin.reflect.KClass

abstract class ImageManagerClientA : BackendClientBase() {

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
    protected val decodingFiles = HashMap<String, UserQuery>()


    protected val loadingQueries = LinkedBlockingQueue<WorkflowManager>()
    protected val decodingQueries = LinkedBlockingQueue<WorkflowManager>()

    protected val loadingPool = ThreadPoolExecutor(
            0, 6,
            1L, TimeUnit.MINUTES,
            loadingQueries as LinkedBlockingQueue<Runnable>
    )


    protected val decodingPool = ThreadPoolExecutor(
            0, 6,
            1L, TimeUnit.MINUTES,
            decodingQueries as LinkedBlockingQueue<Runnable>
    )


    /**
     *
     */
    // protected val imgConverters = HashMap<KClass<*>, ImageConverter>()


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

        //  println("ImageManagerClientA not in memImg = ${cache[q.getQueryId()!!]} url=${q.getQueryId()!!}")
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
                    q.progressListener()

            ).chainTypeCreate(ChainType.LAST_ELEMENT)


            usercallbacks.forEach { cb -> builder.addCallback(cb) }

            uiUrlMap[uiParam] = url

            builder.exeAsync()
        }

        q.getAppendedQueries().forEach {
            println("ImageManagerClientA append it.ui=${it.ui}")
            if (it.ui != null) {
                buildDecode(
                        q.url()!!, q.fileDestination()!!, it.ui!!,
                        it.callback, it.extraParam as ImageLoadEP?, it.progressListener
                ).chainTypeCreate(ChainType.LAST_ELEMENT).exeAsync()
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
            // BackendImage
            assignImage(img, ui, query.getExtraParamAs<ImageLoadEP>())
        }
    }


    protected fun assignImage(img: Any?, ui: Any?, ep: ImageLoadEP?): Boolean {

        if (img == null || ui == null) {
            return false
        }


//        ep?.userAssignImage?.apply {
//            invoke(ui, imagePlatformHelper.getBackendImage(img))
//        } ?:


        imagePlatformHelper.getBackendImage(img)?.assignTo(ui)
        uiUrlMap.remove(ui)

        return true
    }

    open fun cancelLoad(ui: Any) {
        uiUrlMap.remove(ui)?.apply {
            loadingFiles.remove(this)?.apply {
                cancelUi(ui, null, true, CancelInfo.REJECTED_BY_CLIENT)
            } ?: decodingFiles.remove(this)?.apply {
                cancelUi(ui, null, true, CancelInfo.REJECTED_BY_CLIENT)
            }
        }
    }

//    open fun addImageConverter(cls: KClass<*>, imgConv: ImageConverter) {
//        imgConverters[cls] = imgConv
//    }

    //  open fun convertImage(img: Any): Any? = imgConverters[img::class]?.invoke(img)

    open fun removeFromDiskcache(url: String): File? = diskCacheClient.getFile(url)

    open fun removeFromMemory(url: String): Any? = cache.remove(url)

    open fun addImageSizeCalculator(cls: KClass<*>, calc: MemoryUsage) =
            cache.addMemoryCalculator(cls, calc)

    open fun addImageDecoder(imageDecoder: FileDecoder) =
            endecoder.addDecoder(imageDecoder)

    open fun addImageEncoder(imageEncoder: FileEncoder) =
            endecoder.addEncoder(imageEncoder)

    open fun attachImagePlatformHelper(imgHelper: ImagePlatformHelper) = imgHelper.let {
        imagePlatformHelper = it
        imagePlatformHelper.onAttach(this)
    }

    open fun loadFromMemory(url: String, ui: Any?, ep: ImageLoadEP? = null): Boolean =
            assignImage(cache[url], ui, ep)


    open fun buildLoad(
            url: String,
            ui: Any?,
            cb: Callback<UserQuery>?,
            ep: ImageLoadEP?,
            progressListener: ProgressListener?
    ): QueryBuilder<WorkflowBuilder1> {

        return prepareQuery(httpClient.prepareQuery())
                .executor(loadingPool)
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
    }

    open fun buildDecode(
            url: String?,
            file: File,
            ui: Any?,
            cb: Callback<UserQuery>? = null,
            ep: ImageLoadEP? = null,
            progressListener: ProgressListener? = null
    ): WorkflowBuilder<WorkflowBuilder1> {
        val _url = url ?: file.toURI().toString()

        progressListener?.apply {
            if (this is UIProgressListenerBase) {
            //    indeterminate = true
            }
        }

        return prepareQuery(endecoder.prepareDecode(file, "image/*", null, ep, cb))
                .executor(decodingPool)
                .queryId(_url)
                .uiParam(ui)
                .allowAppend(false)
                .resSync(_url)
                .addPrepareProcessor(::onPrepareDecode)
                .addPreProcessor(::onPreDecode)
                .addPostProcessor(::onPostDecode)
                .callbackFirst(callbackDecode)
                .chainTypeCreate(ChainType.STAND_ALONE)
               // .progressListener(progressListener)
    }

    fun buildDecodeFile(
            file: File, ui: Any?, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
            progressListener: ProgressListener? = null
    ): QueryBuilder<WorkflowBuilder1> {
        return buildDecode(null, file, ui, cb, ep, progressListener)
    }

    //---------------------------------------------------------------------------------------------------
    fun decodeFile(
            file: File, ui: Any?, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
            progressListener: ProgressListener? = null
    ) {
        buildDecodeFile(file, ui, cb, ep, progressListener).exeAsync()
    }

    fun download(
            url: String, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
            progressListener: ProgressListener? = null
    ) {
        buildLoad(url, null, cb, ep, progressListener).exeAsync()
    }


    fun load(
            url: String,
            ui: Any?,
            cb: Callback<UserQuery>? = null,
            ep: ImageLoadEP? = null,
            progressListener: ProgressListener? = null
    ) {
        buildLoad(url, ui, cb, ep, progressListener).exeAsync()
    }


    /**
     * Load image from disk cache.
     * @return false if image file don't exist in disk cache
     */
    fun loadFromDiskCache(
            url: String, ui: Any?, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
            progressListener: ProgressListener? = null
    ): Boolean {
        val file = diskCacheClient.getFile(url)

        if (file != null) {
             buildDecode(url, file, ui, cb, ep, progressListener).exeAsync()
            return true
        }

        return false
    }

    open fun imageFile(url: String, ep: ImageLoadEP? = null): File {
        return diskCacheClient.genFile(url)
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