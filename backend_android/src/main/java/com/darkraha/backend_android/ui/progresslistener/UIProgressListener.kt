package com.darkraha.backend_android.ui.progresslistener

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.darkraha.backend.ProgressListener
import java.lang.ref.SoftReference

/**
 * Used with UIProgressListenerWrapper for running methods in ui/main thread.
 *
 * @author Verma Rahul
 */
class UIProgressListener(progressBar: ProgressBar) : ProgressListener {

    constructor(progressBar: ProgressBar, remove: Boolean) : this(progressBar) {
        this.remove = remove
    }

    internal var percent = 0f
    internal var prev = 0f

    private var refProgressBar = SoftReference<ProgressBar>(progressBar)
    private var remove = false

    override fun onProgress(current: Long, total: Long) {

        if (percent == 0f) {
            percent = total / 100f
        }

        if (current - prev > percent || current >= total) {
            refProgressBar.get()?.progress = (100 * current / total).toInt()

        }
    }

    override fun onStart() {
        val pb = refProgressBar.get()
        if (pb != null) {
            pb.visibility = View.VISIBLE
            pb.progress = 0
        }
    }

    override fun onEnd() {
        val pb = refProgressBar.get()

        if (pb == null) {
            return
        }

        pb.visibility = View.GONE

        if (remove) {
            (pb.getParent() as ViewGroup).removeView(pb)
        }

    }

}
