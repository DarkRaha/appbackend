package com.darkraha.backend_android

import android.support.v7.app.AppCompatActivity
import com.darkraha.backend.Backend
import com.darkraha.backend.UserQuery
import com.darkraha.backend_android.extensions.backend

/**
 * @author Verma Rahul
 */
open class BackendActivity : AppCompatActivity() {


    override fun onResume() {
        super.onResume()
        registerListeners()
    }

    override fun onPause() {
        unregisterListeners()
        super.onPause()

    }


    open fun registerListeners() {
        backend.error.addListener(this::onError)
    }

    open fun unregisterListeners() {
        backend.error.removeListener(this::onError)
    }

    /**
     * Handles errors of backend.
     */
    open fun onError(newValue: UserQuery?) {


    }
}