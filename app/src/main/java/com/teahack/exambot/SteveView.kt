package com.teahack.exambot

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin

class SteveView(context: Context, private val onTap: () -> Unit) : View(context) {

    private var angle = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var moved = false
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init { setLayerType(LAYER_TYPE_HARDWARE, null) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val s = width / 4f
        angle += 0.8f

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#33FF7A18")
        canvas.drawCircle(cx, cy, width * 0.45f, paint)

        val headShade = (sin(Math.toRadians(angle.toDouble())) * 30 + 80).toInt()
        paint.color = Color.rgb(headShade + 120, headShade + 80, headShade + 60)
        val head = RectF(cx - s, cy - s * 2, cx + s, cy)
        canvas.drawRoundRect(head, 6f, 6f, paint)

        paint.color = Color.WHITE
        canvas.drawRect(cx - s*0.6f, cy - s*1.4f, cx - s*0.2f, cy - s*1.0f, paint)
        canvas.drawRect(cx + s*0.2f, cy - s*1.4f, cx + s*0.6f, cy - s*1.0f, paint)
        paint.color = Color.parseColor("#3B2A1E")
        canvas.drawRect(cx - s*0.5f, cy - s*1.35f, cx - s*0.3f, cy - s*1.05f, paint)
        canvas.drawRect(cx + s*0.3f, cy - s*1.35f, cx + s*0.5f, cy - s*1.05f, paint)

        paint.color = Color.parseColor("#2E7D32")
        canvas.drawRoundRect(RectF(cx - s, cy, cx + s, cy + s*2), 6f, 6f, paint)

        paint.color = Color.parseColor("#4E342E")
        val swing = sin(Math.toRadians(angle.toDouble())).toFloat() * s * 0.2f
        canvas.drawRoundRect(RectF(cx - s*1.6f, cy + swing, cx - s*1.05f, cy + s*1.8f), 6f, 6f, paint)
        canvas.drawRoundRect(RectF(cx + s*1.05f, cy - swing, cx + s*1.6f, cy + s*1.8f), 6f, 6f, paint)
        canvas.drawRoundRect(RectF(cx - s*0.95f, cy + s*2, cx - s*0.15f, cy + s*3.2f), 6f, 6f, paint)
        canvas.drawRoundRect(RectF(cx + s*0.15f, cy + s*2, cx + s*0.95f, cy + s*3.2f), 6f, 6f, paint)

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX; lastY = event.rawY; moved = false
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY
                if (Math.abs(dx) > 5 || Math.abs(dy) > 5) moved = true

                val p = layoutParams as? android.view.WindowManager.LayoutParams
                p?.let {
                    it.x += dx.toInt()
                    it.y += dy.toInt()
                    (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager)
                        .updateViewLayout(this, it)
                }
                lastX = event.rawX; lastY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                // tap 1x = keluar menu
                if (!moved) onTap()
            }
        }
        return true
    }
}