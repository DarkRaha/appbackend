package com.darkraha.backend_android.ui.progresslistener

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.darkraha.backend.Backend
import com.darkraha.backend.ProgressListener
import com.darkraha.backend.UIProgressListenerBase
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Changes progress bar progress while query handling.
 *
 * @param progressBar android progress bar
 * @param percentDelta notify ui when progress changed on  percentDelta. Default notify every percent
 * @author Verma Rahul
 */

class UIProgressListener(val progressBar: ProgressBar) :
        UIProgressListenerBase() {



    init {
        mainThread = Backend.sharedInstance.mainThread
    }

    override fun onUiProgress(current: Float, total: Float, currentPercent: Float) {
        progressBar.progress = currentPercent.toInt()
    }

    override fun onUiActive(v: Boolean) {
        progressBar.visibility = if (v) View.VISIBLE else View.GONE
    }

    override fun onUiEnd() {
        progressBar.visibility = View.GONE
    }

}
