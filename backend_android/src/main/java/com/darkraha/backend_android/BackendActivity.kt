package com.darkraha.backend_android

import android.support.v7.app.AppCompatActivity
import com.darkraha.backend.Backend
import com.darkraha.backend.infos.ErrorInfo
import com.darkraha.backend.livedata.ChangeListener

/**
 * @author Verma Rahul
 */
open class BackendActivity : AppCompatActivity(), ChangeListener<ErrorInfo> {

    override fun onResume() {
        super.onResume()
        Backend._sharedInstance?.error?.addChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        Backend._sharedInstance?.error?.removeChangeListener(this)
    }

    override fun onChange(newValue: ErrorInfo?) {


    }

}