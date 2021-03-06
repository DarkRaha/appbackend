package com.darkraha.backend

import com.darkraha.backend.components.diskcache.DiskCacheClient
import com.darkraha.backend.components.endecode.EndecodeClient
import com.darkraha.backend.components.http.HttpClient
import com.darkraha.backend.components.images.*
import com.darkraha.backend.components.json.JsonManager
import com.darkraha.backend.components.json.JsonManagerDefault
import com.darkraha.backend.components.mainthread.MainThread
import com.darkraha.backend.components.mainthread.MainThreadDefault
import com.darkraha.backend.components.screen.DeviceScreen

import com.darkraha.backend.helpers.ErrorFilterManager
import com.darkraha.backend.helpers.ObjectPool
import com.darkraha.backend.livedata.UIEventT

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

    lateinit var httpClient: HttpClient
        protected set


    lateinit var diskCacheClient: DiskCacheClient
        protected set


    lateinit var endecodeClient: EndecodeClient
        protected set

    lateinit var imageManager: ImageManagerClientA
        protected set

    lateinit var workdir: File
        protected set

    lateinit var error: UIEventT<UserQuery>
        protected set

    lateinit var errorFilter: ErrorFilterManager
        protected set


    lateinit var deviceScreen: DeviceScreen


    val queryPool = ObjectPool { Query() }

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

        private var _httpClient: HttpClient? = null
        private var _diskCacheClient: DiskCacheClient? = null
        private var _endecodeClient: EndecodeClient? = null
        private var _imageManager: ImageManagerClientA? = null
        private var _asShared = false
        private var _imagePlatformHelper: ImagePlatformHelper? = null
        private var _workdir: File? = null
        private var _jsonManager: JsonManager? = null
        private var _errorFilter: ErrorFilterManager? = null
        private var _deviceScreen: DeviceScreen? = null


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

        fun deviceScreen(dc: DeviceScreen): Builder {
            _deviceScreen = dc
            return this
        }

        fun httpClient(hc: HttpClient): Builder {
            _httpClient = hc
            return this
        }

        fun diskcache(dc: DiskCacheClient): Builder {
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

        fun errorFilter(ef: ErrorFilterManager): Builder {
            _errorFilter = ef
            return this
        }


        fun build(): Backend {

            val file = File("")

            result.jsonManager = _jsonManager ?: JsonManagerDefault()
            result.mainThread = _mainThread ?: MainThreadDefault()
            result.error = UIEventT("Backend error", null, result.mainThread)

            result.workdir = _workdir ?: File("workdir")

            result.diskCacheClient =
                    _diskCacheClient ?: DiskCacheClient.Builder()
                            .backend(result)
                            .build()

            result.httpClient =
                    _httpClient ?: HttpClient.Builder().backend(result).build()

            result.endecodeClient =
                    _endecodeClient
                            ?: EndecodeClient.Builder().backend(result).build()

            result.deviceScreen = _deviceScreen ?: DeviceScreen()

            result.imageManager = _imageManager ?: ImageManager.Builder()
                    .backend(result)
                    .diskCacheClient(result.diskCacheClient.subClient("images"))
                    .endecodeClient(result.endecodeClient)
                    .httpClient(result.httpClient)
                    .imagePlatformHelper(_imagePlatformHelper ?: ImagePlatformHelperBase())
                    .build()

            result.executor = _executor ?: ThreadPoolExecutor(
                    0, 30,
                    2L, TimeUnit.MINUTES,
                    LinkedBlockingQueue<Runnable>()
            )

            if (_asShared || _sharedInstance == null) {
                _sharedInstance = result
            }


            result.errorFilter = _errorFilter
                    ?: ErrorFilterManager()

            result.setup()
            return result
        }
    }

}