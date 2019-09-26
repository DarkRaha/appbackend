package com.darkraha.backend_android.ui.progresslistener

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.darkraha.backend.Backend
import com.darkraha.backend.ProgressListener
import java.lang.ref.SoftReference
import java.util.concurrent.atomic.AtomicBoolean

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


    private val _isActive = AtomicBoolean(true)
    var isUsed = false

    var isActive: Boolean
        set(value) {
            _isActive.set(value)
             refProgressBar.get()?.apply {
                visibility = if (value && isUsed) View.VISIBLE else View.GONE
            }
        }
        get() = _isActive.get()

    internal var percent = 0f
    internal var prev = 0f

    private val mainThread = Backend.sharedInstance.mainThread
    private var refProgressBar = SoftReference<ProgressBar>(progressBar)
    private var removeFromParentAtEnd = false

    override fun onProgress(current: Float, total: Float) {

        if (!_isActive.get()) {
            return
        }

        refProgressBar.get()?.also {

            if (percent == 0f) {
                percent = total / 100f
            }

            if (current >= total || current - prev > percent * percentDelta) {
                mainThread.execute {
                    refProgressBar.get()?.progress = (100 * current / total).toInt()
                }
                prev = current
            }
        }
    }

    override fun onStart() {
        if (refProgressBar.get() != null) {
            mainThread.execute {
                isUsed = true
                isActive = true

                refProgressBar.get()?.apply {
                    progress = 0
                }
            }
        }
    }

    override fun onEnd() {

        refProgressBar.get()?.also {
            mainThread.execute {

                refProgressBar.get()?.apply {
                    isUsed = false
                    isActive = false

                    if (removeFromParentAtEnd) {
                        (getParent() as ViewGroup).removeView(this)
                    }
                }
            }
        }
    }

}
