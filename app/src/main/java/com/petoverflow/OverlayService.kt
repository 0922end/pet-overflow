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

class OverlayService : Service() {

    companion object {
        var isRunning = false
            private set
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "pet_overflow_channel"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var webView: WebView? = null
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startForeground(NOTIFICATION_ID, buildNotification())
        if (!::overlayView.isInitialized) createOverlayView()
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        if (::overlayView.isInitialized && overlayView.isAttachedToWindow) {
            try { windowManager.removeView(overlayView) } catch (_: Exception) {}
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "大鲸鱼桌宠", NotificationManager.IMPORTANCE_LOW).apply {
                description = "小易猫猫在桌面上～"
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, CHANNEL_ID)
        else Notification.Builder(this)
        return b.setContentTitle("小易猫猫桌宠")
            .setContentText("在桌面上等你～")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }

    private fun createOverlayView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as android.view.LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_pet, null)

        webView = overlayView.findViewById(R.id.pet_webview)
        webView?.settings?.apply {
            javaScriptEnabled = true
            setSupportZoom(false)
            allowFileAccess = true
            allowContentAccess = true
        }

        val density = resources.displayMetrics.density
        val petSize = (120 * density).toInt()

        val params = WindowManager.LayoutParams(
            petSize, petSize,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        val html = """
<!DOCTYPE html>
<html><body style="margin:0;padding:0;background:transparent;display:flex;align-items:center;justify-content:center;width:100%;height:100%">
<div style="width:60px;height:60px;border-radius:50%;background:#FFB74D;display:flex;align-items:center;justify-content:center;font-size:24px;box-shadow:0 2px 10px rgba(0,0,0,0.2)">😺</div>
<script>console.log('inline html loaded')</script>
</body></html>
        """.trimIndent()

        webView?.setBackgroundColor(0x00000000)
        webView?.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

        val touchListener = View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = false
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (Math.sqrt((dx * dx + dy * dy).toDouble()) > 8) isDragging = true
                    params.x = initialX + dx.toInt()
                    params.y = initialY + dy.toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        webView?.loadUrl("javascript:(function(){var m=document.getElementById('m');if(m){m.style.opacity='1';setTimeout(function(){m.style.opacity='0'},1000)}})()")
                    }
                    isDragging = false
                    true
                }
                else -> false
            }
        }

        overlayView.setOnTouchListener(touchListener)
    }
}
