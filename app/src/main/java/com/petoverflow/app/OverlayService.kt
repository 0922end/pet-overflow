package com.petoverflow.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.NotificationCompat

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var overlay: View
    private lateinit var wv: WebView
    private var params: WindowManager.LayoutParams? = null
    private var startX = 0; private var startY = 0
    private var startTX = 0f; private var startTY = 0f

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        overlay = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        wv = overlay.findViewById(R.id.pet_webview)
        wv.settings.javaScriptEnabled = true
        wv.settings.setBackgroundColor(0x00000000)
        wv.setBackgroundColor(0x00000000)
        wv.loadUrl("file:///android_asset/pet.html")
        
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE
        
        params = WindowManager.LayoutParams(300, 300, flag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0; y = 200
        }
        
        overlay.setOnTouchListener { _, ev -> onTouch(ev) }
        wm.addView(overlay, params)
        
        val ch = android.app.NotificationChannel("pet", "Pet", android.app.NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager).createNotificationChannel(ch)
        startForeground(1, NotificationCompat.Builder(this, "pet")
            .setContentTitle("Pet Overflow").setContentText("meow")
            .setSmallIcon(android.R.drawable.ic_menu_compass).setOngoing(true).build())
    }

    private fun onTouch(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = params!!.x; startY = params!!.y
                startTX = ev.rawX; startTY = ev.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                params!!.x = startX + (ev.rawX - startTX).toInt()
                params!!.y = startY + (ev.rawY - startTY).toInt()
                wm.updateViewLayout(overlay, params)
            }
            MotionEvent.ACTION_UP -> {
                wv.evaluateJavascript("javascript:onTap()", null)
            }
        }
        return true
    }

    override fun onBind(intent: Intent?) = null
    override fun onDestroy() { wm.removeView(overlay); super.onDestroy() }
}