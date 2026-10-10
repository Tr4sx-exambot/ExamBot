package com.teahack.exambot

import android.content.Context
import android.os.Build
import android.view.WindowManager

class AntiDetect(private val ctx: Context) {

    private var enabled = false

    fun enable() { enabled = true }
    fun disable() { enabled = false }
    fun toggle(): Boolean { enabled = !enabled; return enabled }
    fun isOn() = enabled

    fun applyFlags(params: WindowManager.LayoutParams) {
        if (!enabled) return
        params.flags = params.flags or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        params.flags = params.flags and
                WindowManager.LayoutParams.FLAG_SECURE.inv()
    }

    fun hideFromRecents(): Boolean = enabled

    fun spoofDeviceName(): String =
        if (enabled) "Pixel ${(1..8).random()}" else Build.MODEL
}