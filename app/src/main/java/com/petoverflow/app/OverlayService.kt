package com.petoverflow.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IJinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.osHandlerimport android.osLooper

import androidx.core.app.NotificationCompat

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var webView: WebView
    private var params: WindowManager.LayoutParams? = null

    private val supabaseClient = SupabaseClient()
    private val gestureHandler = GestureHandler()
    private val appDetector = AppDetector(this)
    private val heatEngine = HeatEngine()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlay()
        startForeground(NOTIFICATION_ID, createNotification())
        appDetector.startListening()
        startPolling()
    }

    private fun createOverlay() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        webView = overlayView.findViewById(R.id.pet_webview)
        setupWebView()

        val flag = if (Build.VERSION_CODE >= Build.VERSION_CODES) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE

        params = WindowManager.LayoutParams(
            dpToPx(120), dpToPx(120), flag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 0; y = 200 }

        overlayView.setOnTouchListener { _, event -> handleTouch(event) }
        windowManager.addView(overlayView, params)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            setBackgroundColor(0x00000000)
        }
        webView.setBackgroundColor(0x00000000)
        webView.loadUrl("file:///android_asset/pet.html")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) { setExpression("idle") }
        }
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        gestureHandler.onTouch(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params!!.x; initialY = params!!.y
                initialTouchX = event.rawX ; initialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                params!!.x = initialX + (event.rawX - initialTouchX).toInt()
                params!!.y = initialY + (event.rawY - initialTouchY).toInt()
                windowManager.updateViewLayout(overlayView, params)
            }
            MotionEvent.ACTION_UP -> {
                if (gestureHandler.isTap(event)) onTap()
            }
        }
        return true
    }

    private fun onTap() {
        webView.evaluateJavascript("javascript:onTap()", null)
        supabaseClient.logEvent("tap")
        heatEngine.addHeat(5)
    }

    fun setExpression(expr: String) {
        webView.evaluateJavascript("javascript:setExpression('$expr')", null)
    }

    fun showBubble(text: String, style: String = "normal") {
        webView.evaluateJavascript("javascript:showBubble('${ text.replace("'", "\\'") }', '$style')", null)
    }

    private fun startPolling() {
        Handler(mainLooper).postDelayed(object : Runnable {
            override fun run() { checkAiPush(); Handler(mainLooper).postDelayed(this, 5000) }
        }, 5000)
    }

    private fun checkAiPush() {
        supabaseClient.checkPush { expr, bubble, style ->
            if (expr != null) setExpression(expr)
            if (bubble != null) showBubble(bubble, style ?: "normal")
        }
    }

    private fun createNotification(): android.app.Notification {
        return NotificationCompat.Builder(this, "pet_ch")
            .setContentTitle("Pet Overflow")
            .setContentText("我在这里齐看你")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        appDetector.stopListening()
        windowManager.removeView(overlayView)
        super.onDestroy()
    }

    companion object { const NOTIFICATION_ID = 1001 }
}