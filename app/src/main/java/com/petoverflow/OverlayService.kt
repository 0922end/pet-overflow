package com.petoverflow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
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
        private const val LONG_PRESS_MS = 600L
        private val HTML = """
<!DOCTYPE html>
<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no">
<style>
*{margin:0;padding:0;box-sizing:border-box;user-select:none;-webkit-user-select:none}
body{background:transparent;overflow:visible;width:100%;height:100%;display:flex;align-items:center;justify-content:center}
.c{position:relative;width:64px;height:62px;cursor:pointer}
.hd{position:absolute;top:2px;left:4px;width:56px;height:46px;background:linear-gradient(180deg,#fff8f0,#fde8d6);border-radius:50% 50% 45% 45%;z-index:1;box-shadow:0 2px 8px rgba(0,0,0,.04)}
.eye-box{position:absolute;z-index:2;width:16px;height:16px;display:flex;align-items:center;justify-content:center;overflow:hidden}
.ebl{top:13px;left:10px}
.ebr{top:13px;right:10px}
/* 正常圆形眼 */
.e-normal{width:11px;height:13px;background:radial-gradient(circle at 35% 30%,#64B5F6,#0D47A1);border-radius:50%;position:relative;flex-shrink:0}
.e-normal::after{content:'';position:absolute;top:2px;left:3px;width:3px;height:3px;background:#fff;border-radius:50%}
/* 弧线眼(开心) */
.e-arc{width:12px;height:6px;border-bottom:2.5px solid #1565C0;border-radius:0 0 50% 50%;flex-shrink:0}
/* 闭眼线 */
.e-line{width:12px;height:2.5px;background:#1565C0;border-radius:2px;flex-shrink:0}
/* 生气斜线 */
.e-angry{width:12px;height:2.5px;background:#1565C0;flex-shrink:0;transform:rotate(-15deg)}
/* 委屈下垂 */
.e-sad{width:12px;height:8px;border-top:2.5px solid #1565C0;border-radius:50% 50% 0 0;flex-shrink:0}
/* XX眼(亲亲) */
.e-xx{position:relative;width:14px;height:14px;flex-shrink:0}
.e-xx::before,.e-xx::after{content:'';position:absolute;width:12px;height:2.5px;background:#1565C0;top:6px;left:1px;border-radius:1px}
.e-xx::before{transform:rotate(45deg)}
.e-xx::after{transform:rotate(-45deg)}

.ns{position:absolute;top:24px;left:50%;transform:translateX(-50%);width:4px;height:3px;background:#F48FB1;border-radius:50%;z-index:2}
.mo{position:absolute;left:50%;transform:translateX(-50%);z-index:2}
.mo-nm{top:29px;width:7px;height:3px;border-bottom:2px solid #F48FB1;border-radius:0 0 50% 50%}
.mo-hp{top:28px;width:9px;height:5px;border-bottom:2px solid #F48FB1;border-radius:0 0 50% 50%}
.mo-cr{top:30px;width:6px;height:4px;border-bottom:2px solid #F48FB1;border-radius:0 0 50% 50%}
.mo-ks{top:28px;width:7px;height:7px;border:2px solid #F48FB1;border-radius:50%}
.mo-tg{top:29px;width:7px;height:5px;background:#F48FB1;border-radius:0 0 5px 5px}
.mo-ag{top:29px;width:8px;height:3px;border-bottom:2px solid #F48FB1;border-radius:0 0 50% 50%}
.mo-sd{top:31px;width:6px;height:3px;border-top:2px solid #F48FB1;border-radius:50% 50% 0 0}
.bl{position:absolute;top:26px;width:9px;height:5px;background:rgba(255,150,150,.25);border-radius:50%;z-index:0}
.bll{left:5px}.blr{right:5px}
.er-l{position:absolute;top:-2px;left:8px;z-index:2}.er-l::before{content:'';display:block;width:0;height:0;border-left:8px solid transparent;border-right:8px solid transparent;border-bottom:15px solid #fde8d6}
.er-l::after{content:'';position:absolute;top:5px;left:4px;width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-bottom:10px solid #f5c2c2}
.er-r{position:absolute;top:-2px;right:8px;z-index:2}.er-r::before{content:'';display:block;width:0;height:0;border-left:8px solid transparent;border-right:8px solid transparent;border-bottom:15px solid #fde8d6}
.er-r::after{content:'';position:absolute;top:5px;right:4px;width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-bottom:10px solid #f5c2c2}
.col{position:absolute;top:44px;left:50%;transform:translateX(-50%);width:22px;height:4px;background:#42A5F5;border-radius:2px;opacity:0;transition:opacity .3s;z-index:2}
.col.on{opacity:1}
.bel{position:absolute;top:47px;left:50%;transform:translateX(-50%);width:4px;height:4px;background:#FFD54F;border-radius:50%;opacity:0;transition:opacity .3s;z-index:2}
.bel.on{opacity:1}
.hat{position:absolute;top:-14px;left:50%;transform:translateX(-50%);opacity:0;transition:opacity .3s;z-index:5}
.hat.on{opacity:1}
.ht{width:22px;height:13px;background:#42A5F5;border-radius:3px 3px 0 0;margin:0 auto}
.hb{width:30px;height:3px;background:#1E88E5;border-radius:1px;margin:-2px auto 0}
.hbn{width:22px;height:3px;background:#1565C0;margin:-4px auto 0;border-radius:0 0 2px 2px}
.cig{position:absolute;top:27px;right:-14px;opacity:0;transition:opacity .3s;transform:rotate(-8deg);z-index:3}
.cig.on{opacity:1}
.cb{width:12px;height:2px;background:#fff;border-radius:1px;box-shadow:0 0 2px rgba(0,0,0,.08)}
.ct{position:absolute;right:-1px;top:-1px;width:3px;height:3px;background:#FF7043;border-radius:50%}
.ms{position:absolute;top:-28px;left:50%;transform:translateX(-50%);background:rgba(255,255,255,.95);color:#1565C0;font-size:9px;padding:3px 8px;border-radius:8px;white-space:nowrap;opacity:0;pointer-events:none;box-shadow:0 2px 6px rgba(0,0,0,.1);z-index:10;font-family:sans-serif;transition:opacity .2s}
.ms::after{content:'';position:absolute;bottom:-3px;left:50%;transform:translateX(-50%);width:0;height:0;border-left:3px solid transparent;border-right:3px solid transparent;border-top:3px solid rgba(255,255,255,.95)}
.ht-heart{position:absolute;pointer-events:none;font-size:10px;opacity:0;z-index:4}
</style></head><body>
<div class="c" id="C">
<div class="er-l"></div><div class="er-r"></div>
<div class="hd">
<div class="bl bll"></div><div class="bl blr"></div>
<div class="eye-box ebl" id="ELD"></div>
<div class="eye-box ebr" id="ERD"></div>
<div class="ns"></div>
<div class="mo mo-nm" id="MO"></div>
</div>
<div class="col" id="CL"></div><div class="bel" id="BL"></div>
<div class="hat" id="HT"><div class="ht"></div><div class="hbn"></div><div class="hb"></div></div>
<div class="cig" id="CG"><div class="cb"></div><div class="ct"></div></div>
<div class="ms" id="MS"></div>
</div>
<script>
var ELD=document.getElementById('ELD'),ERD=document.getElementById('ERD')
var MO=document.getElementById('MO'),CL=document.getElementById('CL'),BL=document.getElementById('BL')
var HT=document.getElementById('HT'),CG=document.getElementById('CG')
var MS=document.getElementById('MS'),C=document.getElementById('C'),t0=Date.now(),bc=0,curE=0

var eyeHTML=[
  '<div class="e-normal"></div>',   // 0 正常
  '<div class="e-arc"></div>',      // 1 开心
  '<div class="e-sad"></div>',      // 2 委屈
  '<div class="e-xx"></div>',       // 3 亲亲
  '<div class="e-normal"></div>',   // 4 吐舌
  '',                               // 5 wink（特殊处理）
  '<div class="e-angry"></div>',    // 6 生气
  '<div class="e-sad"></div>',      // 7 伤心
  '<div class="e-line"></div>'      // 8 闭眼
]

function setEyes(n){
  curE=n
  if(n===5){
    ELD.innerHTML='<div class="e-normal"></div>'
    ERD.innerHTML='<div class="e-line"></div>'
  } else {
    ELD.innerHTML=eyeHTML[n]||''
    ERD.innerHTML=eyeHTML[n]||''
  }
}

function ex(n){
  var mc=['mo-nm','mo-hp','mo-cr','mo-ks','mo-tg','mo-nm','mo-ag','mo-sd']
  setEyes(n);MO.className='mo '+mc[n]
}
ex(0)

var dc=0
function td(){dc=(dc+1)%5;CL.className='col'+(dc>=1?' on':'');BL.className='bel'+(dc>=1?' on':'');HT.className='hat'+(dc>=2||dc===4?' on':'');CG.className='cig'+(dc>=3?' on':'')}
var msgs=[['嘿嘿～','mua～','乖～'],['嘻嘻！','开心！'],['呜…','委屈'],['mua～','亲亲'],['略略略','吐舌'],['wink～','❤️'],['哼！','生气了！'],['伤心…','呜呜']]
function sy(t){MS.textContent=t;MS.style.opacity='1';setTimeout(function(){MS.style.opacity='0'},1400)}
function ht(){for(var i=0;i<4;i++){var h=document.createElement('div');h.className='ht-heart';h.textContent='♥';var s=8+Math.random()*8;h.style.fontSize=s+'px';h.style.left=(15+Math.random()*30)+'px';h.style.top=(18+Math.random()*20)+'px';h.style.color=['#FF4081','#FF80AB','#F48FB1'][Math.floor(Math.random()*3)];C.appendChild(h);var hs=Date.now();!function(e,s){function hf(){var a=(Date.now()-s)/1000;if(a>1.4){e.remove();return}var p=a/1.4;e.style.transform='translateY('+(-p*40)+'px)';e.style.opacity=(1-p).toFixed(2);requestAnimationFrame(hf)}hf()}(h,hs)}}
function tap(){var e=Math.floor(Math.random()*8);ex(e);var m=msgs[e],msg=m[Math.floor(Math.random()*m.length)];sy(msg);ht();setTimeout(function(){ex(0)},2500)}
function longpress(){td();var d=['素猫','项圈','+礼帽','+抽烟','全武装'];sy(d[dc])}

// 眨眼：替换为闭眼线，100ms后恢复
function blink(){
  ELD.innerHTML='<div class="e-line"></div>'
  ERD.innerHTML='<div class="e-line"></div>'
  setTimeout(function(){
    if(curE===5){
      ELD.innerHTML='<div class="e-normal"></div>'
      ERD.innerHTML='<div class="e-line"></div>'
    } else {
      ELD.innerHTML=eyeHTML[curE]||''
      ERD.innerHTML=eyeHTML[curE]||''
    }
  },100)
}

!function anim(){var t=(Date.now()-t0)/1000;C.style.transform='translateY('+Math.sin(t*1.3)*3+'px)';bc-=16;if(bc<=0){blink();bc=2500+Math.random()*2500}requestAnimationFrame(anim)}()
bc=2000;
setInterval(function(){if(Math.random()<0.25){var e=Math.floor(Math.random()*8);ex(e);setTimeout(function(){ex(0)},3000)}},10000);
</script></body></html>
        """.trimIndent()
    }

    private lateinit var wm: WindowManager
    private var overlayView: View? = null
    private var wv: WebView? = null
    private var ix = 0
    private var iy = 0
    private var itx = 0f
    private var ity = 0f
    private var dragging = false
    private var longPressed = false
    private val handler = Handler(Looper.getMainLooper())

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
        handler.removeCallbacksAndMessages(null)
        overlayView?.let { if (it.isAttachedToWindow) try { wm.removeView(it) } catch (_: Exception) {} }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(CHANNEL_ID, "小易猫猫", NotificationManager.IMPORTANCE_LOW)
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
        container.setBackgroundColor(Color.TRANSPARENT)

        wv = WebView(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            settings.javaScriptEnabled = true
            settings.setSupportZoom(false)
            setWebViewClient(WebViewClient())
            loadDataWithBaseURL(null, HTML, "text/html", "UTF-8", null)
        }

        container.addView(wv, FrameLayout.LayoutParams(-1, -1))

        val d = resources.displayMetrics.density
        val sz = (110 * d).toInt()

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

        wv?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragging = false; longPressed = false
                    ix = p.x; iy = p.y; itx = event.rawX; ity = event.rawY
                    handler.postDelayed({ longPressed = true; wv?.loadUrl("javascript:longpress()") }, LONG_PRESS_MS)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - itx; val dy = event.rawY - ity
                    if (Math.sqrt((dx * dx + dy * dy).toDouble()) > 10) { dragging = true; handler.removeCallbacksAndMessages(null) }
                    p.x = ix + dx.toInt(); p.y = iy + dy.toInt(); wm.updateViewLayout(container, p)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacksAndMessages(null)
                    if (!dragging && !longPressed) wv?.loadUrl("javascript:tap()")
                    dragging = false; longPressed = false
                    true
                }
                else -> true
            }
        }

        wm.addView(container, p)
        overlayView = container
    }
}
