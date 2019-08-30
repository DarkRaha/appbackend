package com.darkraha.backend_android

import android.app.Application
import com.darkraha.backend.Backend
import com.darkraha.backend.components.images.ImageManagerClientDefault
import com.darkraha.backend_android.components.images.AndroidImagePlatformHelper
import com.darkraha.backend_android.components.mainthread.AndroidMainThread
import java.io.File

open class BackendApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupBackend()


    }


    open fun setupBackend() {
        val builder = Backend.Builder()


        builder.mainThread(AndroidMainThread())
            .workdir(File(filesDir, "workdir"))
            .setAsShared()
            .imagePlatformHelper(AndroidImagePlatformHelper())
            .build()
    }

}
