package com.darkraha.backend_android.extensions

import android.app.Activity
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.View
import com.darkraha.backend.Backend

val Activity.backend: Backend
    get() = Backend.sharedInstance

val Fragment.backend: Backend
    get() = Backend.sharedInstance

val View.backend: Backend
    get() = Backend.sharedInstance

val RecyclerView.ViewHolder.backend: Backend
    get() = Backend.sharedInstance