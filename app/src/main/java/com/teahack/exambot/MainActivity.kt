package com.teahack.exambot

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.app.Activity

class MainActivity : Activity() {

    private val overlayReq = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // langsung minta izin overlay pas app dibuka
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")), overlayReq
            )
        } else {
            launchAndExit()
        }

        findViewById<android.widget.Button>(R.id.btnStart).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")), overlayReq
                )
            } else launchAndExit()
        }

        findViewById<android.widget.Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, OverlayService::class.java))
        }

        findViewById<android.widget.Button>(R.id.btnAcc).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<android.widget.Button>(R.id.btnCap).setOnClickListener {
            startActivity(Intent(this, ScreenCaptureActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == overlayReq && Settings.canDrawOverlays(this)) {
            launchAndExit()
        }
    }

    // start service lalu keluar dari app
    private fun launchAndExit() {
        val i = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i)
        else startService(i)

        // minimize app ke background
        moveTaskToBack(true)
    }
}