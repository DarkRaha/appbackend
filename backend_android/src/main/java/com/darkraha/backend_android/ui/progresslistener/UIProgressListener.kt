package com.darkraha.backend_android.ui.progresslistener

import android.view.View
import android.widget.ProgressBar
import com.darkraha.backend.ProgressListener


class UIProgressListener(private var progressBar: ProgressBar) : ProgressListener {

    internal var percent = 0f
    internal var prev = 0f


    override fun onProgress(current: Long, total: Long) {

        if (percent == 0f) {
            percent = total / 100f
        }

        if (current - prev > percent || current >= total) {
            progressBar?.progress = (100 * current / total).toInt()

        }
    }

    override fun onStart() {
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
    }

    override fun onEnd() {
        progressBar.visibility = View.GONE
    }

}
