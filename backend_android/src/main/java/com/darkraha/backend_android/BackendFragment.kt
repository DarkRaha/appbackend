package com.darkraha.backend_android

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View

open class BackendFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerListeners()
    }

    override fun onDestroyView() {
        unregisterListeners()
        super.onDestroyView()

    }

    open fun registerListeners() {

    }

    open fun unregisterListeners() {

    }
}