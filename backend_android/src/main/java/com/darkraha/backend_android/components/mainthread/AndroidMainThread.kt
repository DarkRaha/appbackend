package com.darkraha.backend_android.components.mainthread

import android.os.Handler
import android.os.Looper
import com.darkraha.backend.components.mainthread.MainThread

class AndroidMainThread : MainThread() {
    val handler = Handler()

    override fun isMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }

    override fun execute(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            handler.post(block)
        }
    }

    override fun post(block: () -> Unit) {
        handler.post(block)
    }

    override fun postDelayed(timeDelay: Long, block: () -> Unit) {
        handler.postDelayed(block, timeDelay)
    }

}