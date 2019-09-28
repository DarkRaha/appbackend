package com.darkraha.backend.components.images

import com.darkraha.backend.*
import com.darkraha.backend.cache.MemoryUsage

import com.darkraha.backend.components.diskcache.DiskCacheClient
import com.darkraha.backend.components.endecode.FileDecoder
import com.darkraha.backend.components.endecode.FileEncoder
import com.darkraha.backend.extraparams.ImageLoadEP
import java.io.File
import kotlin.reflect.KClass

typealias ImageConverter = (Any) -> Any

//interface ImageManagerClient : Client {
//
//    fun addImageSizeCalculator(cls: KClass<*>, calc: MemoryUsage)
//    fun addImageConverter(cls: KClass<*>, imgConv: ImageConverter)
//    fun convertImage(img: Any): Any?
//    fun addImageDecoder(imageDecoder: FileDecoder)
//    fun addImageEncoder(imageEncoder: FileEncoder)
//    fun attachImagePlatformHelper(imgHelper: ImagePlatformHelper)
//    fun getDiskcache(): DiskCacheClient
//    fun cancelLoad(ui: Any)
//    /**
//     * @return file that correspond to url. File exist when it downloaded.
//     */
//    fun imageFile(url: String): File {
//        return getDiskcache().genFile(url)
//    }
//
//    fun removeFromMemory(url: String): Any?
//    fun removeFromDiskcache(url: String): File?
//
//    //---------------------------------------------------------------------------------------------------
//    fun decodeFile(
//            file: File, ui: Any?, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
//            progressListener: ProgressListener? = null
//    ) {
//        buildDecodeFile(file, ui, cb, ep, progressListener).exeAsync()
//    }
//
//    fun download(
//            url: String, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
//            progressListener: ProgressListener? = null
//    ) {
//        buildLoad(url, null, cb, ep, progressListener).exeAsync()
//    }
//
//
//    fun load(
//            url: String,
//            ui: Any?,
//            cb: Callback<UserQuery>? = null,
//            ep: ImageLoadEP? = null,
//            progressListener: ProgressListener? = null
//    ) {
//        buildLoad(url, ui, cb, ep, progressListener).exeAsync()
//    }
//
//
//    /**
//     * Load image from disk cache.
//     * @return false if image file don't exist in disk cache
//     */
//    fun loadFromDiskCache(
//            url: String, ui: Any?, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
//            progressListener: ProgressListener? = null
//    ): Boolean {
//        val file = getDiskcache().getFile(url)
//
//        if (file != null) {
//            buildDecode(url, file, ui, cb, ep, progressListener).exeAsync()
//            return true
//        }
//
//        return false
//    }
//
//    /**
//     * Load image from memory cache.
//     * @return false if image not found in cache
//     */
//    fun loadFromMemory(url: String, ui: Any?, ep: ImageLoadEP? = null): Boolean
//
//
//    //----------------------------------------------------------------------------------------------
//    fun buildLoad(
//            url: String, ui: Any?, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
//            progressListener: ProgressListener? = null
//    ): QueryBuilder<WorkflowBuilder1>
//
//    fun buildDecode(
//            url: String?, file: File, ui: Any? = null,
//            cb: Callback<UserQuery>? = null,
//            ep: ImageLoadEP? = null,
//            progressListener: ProgressListener? = null
//    ): WorkflowBuilder<WorkflowBuilder1>
//
//
//    fun buildDecodeFile(
//            file: File, ui: Any?, cb: Callback<UserQuery>? = null, ep: ImageLoadEP? = null,
//            progressListener: ProgressListener? = null
//    ): QueryBuilder<WorkflowBuilder1> {
//        return buildDecode(null, file, ui, cb, ep, progressListener)
//    }
//
//
//}