package com.darkraha.backend_android

import android.app.Application
import com.darkraha.backend.Backend
import com.darkraha.backend.helpers.ErrorFilterManager
import com.darkraha.backend_android.components.images.AndroidImagePlatformHelper
import com.darkraha.backend_android.components.mainthread.AndroidMainThread
import java.io.File

open class BackendApplication : Application() {

    lateinit var backend: Backend


    override fun onCreate() {
        super.onCreate()
        setupBackend()
        setupErrorFilter(backend.errorFilter)

    }


    open fun setupBackend() {
        val builder = Backend.Builder()


        backend = builder.mainThread(AndroidMainThread())
                .workdir(File(filesDir, "workdir"))
                .setAsShared()
                .imagePlatformHelper(AndroidImagePlatformHelper())
                .build()
    }

    open fun setupErrorFilter(errorFilter: ErrorFilterManager) {

    }

}
