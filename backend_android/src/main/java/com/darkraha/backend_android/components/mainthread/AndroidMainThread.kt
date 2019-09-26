package com.darkraha.backend_android.components.mainthread

import android.os.Handler
import android.os.Looper
import com.darkraha.backend.components.mainthread.MainThread

class AndroidMainThread : MainThread() {
    val handler = Handler(Looper.getMainLooper())

    override fun isMainThread(): Boolean {
        return Looper.getMainLooper().getThread().equals(Thread.currentThread())
    }

    override fun execute(block: () -> Unit) {
        if (isMainThread()) {
            block()
        } else {
            handler.post { block() }
        }
    }

    override fun post(block: () -> Unit) {
        handler.post(block)
    }

    override fun postDelayed(timeDelay: Long, block: () -> Unit) {
        handler.postDelayed(block, timeDelay)
    }

}