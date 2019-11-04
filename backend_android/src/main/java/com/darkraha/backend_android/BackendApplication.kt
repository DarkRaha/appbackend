package com.darkraha.backend_android

import android.app.Application
import android.content.Context
import com.darkraha.backend.Backend
import com.darkraha.backend.helpers.ErrorFilterManager
import com.darkraha.backend_android.components.images.AndroidImagePlatformHelper
import com.darkraha.backend_android.components.mainthread.AndroidMainThread
import java.io.File
import android.graphics.Point
import android.view.WindowManager
import com.darkraha.backend.components.screen.DeviceScreen


open class BackendApplication : Application() {

    lateinit var backend: Backend


    override fun onCreate() {
        super.onCreate()
        setupBackend()
        setupErrorFilter(backend.errorFilter)

    }


    open fun setupBackend() {
        val builder = Backend.Builder()


        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay

        val size = Point()
        display.getSize(size)

        backend = builder.mainThread(AndroidMainThread())
                .deviceScreen(DeviceScreen(size.x, size.y))
                .workdir(File(filesDir, "workdir"))
                .setAsShared()
                .imagePlatformHelper(AndroidImagePlatformHelper())
                .build()
    }

    open fun setupErrorFilter(errorFilter: ErrorFilterManager) {

    }

}
