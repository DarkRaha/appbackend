package com.darkraha.backend

import com.darkraha.backend.components.diskcache.DiskCacheClientDefault
import com.darkraha.backend.components.endecode.EndecodeClient
import com.darkraha.backend.components.endecode.EndecodeClientDefault
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.components.http.HttpClientDefault
import com.darkraha.backend.components.images.ImageManagerClient
import com.darkraha.backend.components.images.ImageManagerClientDefault
import com.darkraha.backend.components.images.ImagePlatformHelper
import com.darkraha.backend.components.images.ImagePlatformHelperEmpty
import com.darkraha.backend.components.json.JsonManager
import com.darkraha.backend.components.json.JsonManagerDefault
import com.darkraha.backend.components.mainthread.MainThread
import com.darkraha.backend.components.mainthread.MainThreadDefault
import com.darkraha.backend.components.qmanager.QueryManager
import com.darkraha.backend.components.qmanager.QueryManagerDefault
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

//import javax.inject.Inject

/**
 * @author Verma Rahul
 */
open class Backend private constructor() {

    lateinit var executor: ExecutorService
        protected set

    lateinit var jsonManager: JsonManager
        protected set

    lateinit var mainThread: MainThread
        protected set


    lateinit var queryManager: QueryManager
        protected set


    lateinit var httpClient: HttpClient
        protected set


    lateinit var diskCacheClient: DiskCacheClientDefault
        protected set


    lateinit var endecodeClient: EndecodeClient
        protected set

    lateinit var imageManager: ImageManagerClient
        protected set

    lateinit var workdir: File
        protected set

    /**
     *
     */
    fun exeSync(block: () -> Unit, cb: (() -> Unit)?) {
        block();
        if (cb != null)
            cb()
    }

    fun exeSync(block: () -> Unit) = exeSync(block, null)


    /**
     * Add block to queue of main thread, if it's possible.
     */
    fun exeAtMainThread(block: () -> Unit) = mainThread.post(block)


    /**
     * Execute block asynchronously.
     * @param block
     * @param cb
     */
    fun exeAsync(block: () -> Unit, cb: (() -> Unit)?) {
        executor.execute {
            block()
            if (cb != null) {
                mainThread.post(cb)
            }
        }
    }

    fun exeAsync(runnable: Runnable, cb: Runnable? = null) {
        executor.execute {
            runnable.run()
            if (cb != null) {
                mainThread.post { cb.run() }
            }
        }
    }


    inline fun exeAsync(noinline block: () -> Unit) = exeAsync(block, null)


    private fun setup() {
        httpClient.onAddToBackend(this)
        diskCacheClient.onAddToBackend(this)
        endecodeClient.onAddToBackend(this)
        imageManager.onAddToBackend(this)
    }

    companion object {
        @JvmStatic
        var _sharedInstance: Backend? = null
            protected set


        val sharedInstance get() = _sharedInstance!!


        @JvmStatic
        fun newInstance(): Backend {
            return Builder().build()
        }


        @JvmStatic
        fun newExecutorPool(maxThreads: Int): ThreadPoolExecutor {
            return ThreadPoolExecutor(
                0, maxThreads,
                2L, TimeUnit.MINUTES,
                LinkedBlockingQueue<Runnable>()
            )
        }

    }

    class Builder {
        private val result = Backend()

        private var _executor: ExecutorService? = null
        private var _mainThread: MainThread? = null
        private var _queryManager: QueryManager? = null
        private var _httpClient: HttpClient? = null
        private var _diskCacheClient: DiskCacheClientDefault? = null
        private var _endecodeClient: EndecodeClient? = null
        private var _imageManager: ImageManagerClient? = null
        private var _asShared = false
        private var _imagePlatformHelper: ImagePlatformHelper? = null
        private var _workdir: File? = null
        private var _jsonManager: JsonManager? = null


        fun setAsShared(): Builder {
            _asShared = true
            return this
        }

        fun executor(exe: ExecutorService): Builder {
            _executor = exe
            return this
        }

        fun mainThread(mt: MainThread): Builder {
            _mainThread = mt
            return this
        }

        fun queryManager(qm: QueryManager): Builder {
            _queryManager = qm
            return this
        }

        fun httpClient(hc: HttpClient): Builder {
            _httpClient = hc
            return this
        }

        fun diskcache(dc: DiskCacheClientDefault): Builder {
            _diskCacheClient = dc
            return this
        }


        fun endecoder(ed: EndecodeClient): Builder {
            _endecodeClient = ed
            return this
        }

        fun imagePlatformHelper(iph: ImagePlatformHelper): Builder {
            _imagePlatformHelper = iph
            return this
        }

        fun workdir(file: File): Builder {
            _workdir = file
            return this
        }

        fun jsonManager(jm: JsonManager): Builder {
            _jsonManager = jm
            return this
        }

        fun build(): Backend {

            result.jsonManager = _jsonManager ?: JsonManagerDefault()
            result.mainThread = _mainThread ?: MainThreadDefault()
            result.queryManager = _queryManager ?: QueryManagerDefault()
            result.workdir = _workdir ?: File("workdir")

            result.diskCacheClient =
                _diskCacheClient ?: DiskCacheClientDefault.Builder()
                    .backend(result)
                    .build()

            result.httpClient =
                _httpClient ?: HttpClientDefault.Builder().backend(result).build()

            result.endecodeClient =
                _endecodeClient
                    ?: EndecodeClientDefault.Builder().backend(result).build()

            result.imageManager = _imageManager ?: ImageManagerClientDefault.Builder()
                .mainThread(result.mainThread)
                .diskCacheClient(result.diskCacheClient.subClient("images"))
                .endecodeClient(result.endecodeClient)
                .imagePlatformHelper(_imagePlatformHelper ?: ImagePlatformHelperEmpty())
                .build()

            result.executor = _executor ?: ThreadPoolExecutor(
                0, 10,
                2L, TimeUnit.MINUTES,
                LinkedBlockingQueue<Runnable>()
            )

            if (_asShared || _sharedInstance == null) {
                _sharedInstance = result
            }

            result.setup()
            return result
        }
    }

}