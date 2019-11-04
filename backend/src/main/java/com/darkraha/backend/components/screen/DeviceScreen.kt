package com.darkraha.backend.components.screen

open class DeviceScreen(val scrWidth: Int = 0,
                        val scrHeight: Int = 0) {

    val size: Pair<Int, Int>
        get() = scrWidth to scrHeight

    fun thumbSize(factor: Int = 1) = (scrWidth / factor) to (scrHeight / factor)
}