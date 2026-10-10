package com.teahack.exambot

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class OverlayService : Service() {

    private lateinit var wm: WindowManager
    private var steveView: SteveView? = null
    private var menuView: View? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val autoSolver by lazy { AutoSolver(this) }
    private val antiDetect by lazy { AntiDetect(this) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotif())
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        spawnSteve()
        antiDetect.enable()
    }

    private fun buildNotif(): Notification {
        val ch = NotificationChannel("eb", "ExamBot", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(ch)
        return NotificationCompat.Builder(this, "eb")
            .setContentTitle("ExamBot aktif")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()
    }

    private fun spawnSteve() {
        // steve muncul, tap 1x baru keluar menu
        val v = SteveView(this) { showMenuNearSteve() }
        val params = WindowManager.LayoutParams(
            300, 400,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 200; y = 400
        }
        antiDetect.applyFlags(params)
        wm.addView(v, params)
        steveView = v
    }

    // menu muncul di atas kepala steve
    private fun showMenuNearSteve() {
        if (menuView != null) { removeMenu(); return }

        val v = LayoutInflater.from(this).inflate(R.layout.menu_overlay, null)

        val steveParams = steveView?.layoutParams as? WindowManager.LayoutParams
        val sx = steveParams?.x ?: 200
        val sy = steveParams?.y ?: 400

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            // posisi di atas kepala steve
            x = sx - 30
            y = (sy - 320).coerceAtLeast(0)
        }

        val opt1 = v.findViewById<View>(R.id.optHide)
        val opt2 = v.findViewById<View>(R.id.optAuto)
        val opt3 = v.findViewById<View>(R.id.optStop)
        val opt4 = v.findViewById<View>(R.id.optAnti)

        setActive(opt1, false); setActive(opt2, false)
        setActive(opt3, false); setActive(opt4, antiDetect.isOn())

        opt1.setOnClickListener { steveView?.visibility = View.GONE; removeMenu() }
        opt2.setOnClickListener {
            setActive(opt2, true); setActive(opt3, false)
            scope.launch { autoSolver.runAuto() }
        }
        opt3.setOnClickListener {
            setActive(opt3, true); setActive(opt2, false)
            autoSolver.stop()
        }
        opt4.setOnClickListener {
            val on = antiDetect.toggle()
            setActive(opt4, on)
            steveView?.let {
                val p = it.layoutParams as WindowManager.LayoutParams
                antiDetect.applyFlags(p)
                wm.updateViewLayout(it, p)
            }
        }

        wm.addView(v, params)
        menuView = v
    }

    private fun setActive(view: View, on: Boolean) {
        view.alpha = if (on) 1f else 0.45f
        view.setBackgroundResource(if (on) R.drawable.glass_bg else R.drawable.glass_dim)
    }

    private fun removeMenu() {
        menuView?.let { runCatching { wm.removeView(it) } }
        menuView = null
    }

    override fun onDestroy() {
        scope.cancel()
        steveView?.let { runCatching { wm.removeView(it) } }
        removeMenu()
        antiDetect.disable()
        super.onDestroy()
    }
}