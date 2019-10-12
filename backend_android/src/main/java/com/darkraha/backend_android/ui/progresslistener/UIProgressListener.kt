package com.darkraha.backend_android.ui.progresslistener

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.darkraha.backend.Backend
import com.darkraha.backend.UIProgressListenerBase

/**
 * Changes progress bar progress while query handling.
 *
 * @param progressBar android progress bar
 * @param percentDelta notify ui when progress changed on  percentDelta. Default notify every percent
 * @author Verma Rahul
 */

class UIProgressListener(val progressBar: ProgressBar) :
        UIProgressListenerBase() {


    override var indeterminate: Boolean = false
        set(value) {
            field = value
            mainThread.execute { progressBar.isIndeterminate = value }
        }

    init {
        mainThread = Backend.sharedInstance.mainThread
    }

    override fun onUiProgress(current: Float, total: Float, currentPercent: Float) {

        progressBar.progress = currentPercent.toInt()
        if (progressBar.visibility != 0) {
             println("UIProgressListener hidden visiblity = ${progressBar.visibility}")
            progressBar.visibility = View.VISIBLE
        }

    }

    override fun onUiActive(v: Boolean) {
        progressBar.visibility = if (v) View.VISIBLE else View.GONE
    }

    override fun onUiEnd() {
        progressBar.visibility = View.GONE
        //indeterminate = false
    }

    override fun onUiStart() {
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
    }


    override fun onUiIndetermediateChanged() {
        progressBar.apply {
            isIndeterminate = indeterminate
            scaleY = if (indeterminate) 0.1f else 1f
//            layoutParams = layoutParams.apply {
//                height = if(indeterminate) 10 else ViewGroup.LayoutParams.WRAP_CONTENT
//            }
        }
    }


}
