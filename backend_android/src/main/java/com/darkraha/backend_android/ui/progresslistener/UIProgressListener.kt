package com.darkraha.backend_android.ui.progresslistener

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.darkraha.backend.Backend
import com.darkraha.backend.ProgressListener
import java.lang.ref.SoftReference

/**
 * Changes progress bar progress while query handling.
 *
 * @param progressBar android progress bar
 * @param percentDelta notify ui when progress changed on  percentDelta. Default notify every percent
 * @author Verma Rahul
 */

class UIProgressListener(progressBar: ProgressBar, val percentDelta: Float = 1f) :
    ProgressListener {


    /**
     *
     */
    constructor(progressBar: ProgressBar, removeFromParentAtEnd: Boolean) : this(progressBar) {
        this.removeFromParentAtEnd = removeFromParentAtEnd
    }

    internal var percent = 0f
    internal var prev = 0f

    private val mainThread = Backend.sharedInstance.mainThread
    private var refProgressBar = SoftReference<ProgressBar>(progressBar)
    private var removeFromParentAtEnd = false

    override fun onProgress(current: Long, total: Long) {

        if (refProgressBar.get() != null) {

            if (percent == 0f) {
                percent = total / 100f
            }

            if (current >= total || current - prev > percent * percentDelta) {
                mainThread.execute {
                    refProgressBar.get()?.progress = (100 * current / total).toInt()
                }
            }
        }
    }

    override fun onStart() {

        if (refProgressBar.get() != null) {
            mainThread.execute {
                val pb = refProgressBar.get()
                if (pb != null) {
                    pb.visibility = View.VISIBLE
                    pb.progress = 0
                }
            }
        }
    }

    override fun onEnd() {

        if (refProgressBar.get() != null) {

            mainThread.execute {

                val pb = refProgressBar.get()

                if (pb != null) {
                    pb.visibility = View.GONE

                    if (removeFromParentAtEnd) {
                        (pb.getParent() as ViewGroup).removeView(pb)
                    }
                }
            }
        }
    }

}
