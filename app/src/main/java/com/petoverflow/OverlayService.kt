package com.petoverflow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout

class OverlayService : Service() {

    companion object {
        var isRunning = false
            private set
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "pet_channel"
    }

    private lateinit var wm: WindowManager
    private var overlayView: View? = null
    private var wv: WebView? = null
    private var ix = 0
    private var iy = 0
    private var itx = 0f
    private var ity = 0f

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createChan()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startForeground(NOTIFICATION_ID, noti())
        if (overlayView == null) createView()
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        overlayView?.let { if (it.isAttachedToWindow) try { wm.removeView(it) } catch (_: Exception) {} }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(CHANNEL_ID, "猫猫", NotificationManager.IMPORTANCE_LOW)
            c.description = "小易猫猫"
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c)
        }
    }

    private fun noti(): Notification {
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, CHANNEL_ID) else Notification.Builder(this)
        return b.setContentTitle("小易猫猫").setContentText("在桌面上等你～").setSmallIcon(android.R.drawable.ic_menu_compass).setOngoing(true).build()
    }

    private fun createView() {
        val container = FrameLayout(this)
        container.setBackgroundColor(0x00000000)

        wv = WebView(this).apply {
            setBackgroundColor(0x0000FF00.toInt()) // 亮绿色背景
            settings.javaScriptEnabled = true
            settings.setSupportZoom(false)
            setWebViewClient(WebViewClient())
            loadDataWithBaseURL(null, """
<!DOCTYPE html>
<html><body style="margin:0;padding:0;background:transparent;display:flex;align-items:center;justify-content:center;width:100%;height:100%">
<div style="width:60px;height:60px;border-radius:50%;background:#FFB74D;display:flex;align-items:center;justify-content:center;font-size:24px">😺</div>
</body></html>
""".trimIndent(), "text/html", "UTF-8", null)
        }

        container.addView(wv, FrameLayout.LayoutParams(-1, -1))

        val d = resources.displayMetrics.density
        val sz = (120 * d).toInt()

        val p = WindowManager.LayoutParams(
            sz, sz,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ix = p.x; iy = p.y; itx = event.rawX; ity = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    p.x = ix + (event.rawX - itx).toInt()
                    p.y = iy + (event.rawY - ity).toInt()
                    wm.updateViewLayout(container, p)
                    true
                }
                else -> true
            }
        }

        wm.addView(container, p)
        overlayView = container
    }
}
