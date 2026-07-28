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
body{background:transparent;overflow:hidden;width:100%;height:100%;display:flex;align-items:center;justify-content:center}
.c{position:relative;width:60px;height:80px;cursor:pointer}
.hd{position:absolute;top:0;left:2px;width:56px;height:42px;background:linear-gradient(180deg,#fff8f0,#ffe4d0);border-radius:50% 50% 45% 45%;z-index:1}
.bd{position:absolute;bottom:2px;left:12px;width:36px;height:30px;background:linear-gradient(180deg,#fff8f0,#ffdcc5);border-radius:50% 50% 40% 40%}
.el{position:absolute;top:13px;left:12px}.er{position:absolute;top:13px;right:12px}
.ey{width:10px;height:12px;background:radial-gradient(circle at 35% 30%,#64B5F6,#0D47A1);border-radius:50%;position:relative;transition:all .15s}
.es{position:absolute;top:2px;left:3px;width:3px;height:3px;background:#fff;border-radius:50%}
.ns{position:absolute;top:21px;left:50%;transform:translateX(-50%);width:4px;height:3px;background:#F48FB1;border-radius:50%;z-index:2}
.mo{position:absolute;top:26px;left:50%;transform:translateX(-50%);width:7px;height:3px;border-bottom:2px solid #bbb;border-radius:0 0 50% 50%;transition:all .15s;z-index:2}
.wh{position:absolute;width:13px;height:1px;background:#dcc8b8;z-index:0}
.w1{left:-9px;transform:rotate(-15deg);top:19px}
.w2{left:-10px;transform:rotate(0deg);top:22px}
.w3{left:-9px;transform:rotate(15deg);top:25px}
.w4{right:-9px;transform:rotate(15deg);top:19px}
.w5{right:-10px;transform:rotate(0deg);top:22px}
.w6{right:-9px;transform:rotate(-15deg);top:25px}
.bl{position:absolute;top:23px;width:9px;height:5px;background:rgba(255,150,150,.25);border-radius:50%;z-index:0}
.bll{left:2px}.blr{right:2px}
.er-l{position:absolute;top:-4px;left:7px;z-index:2}.er-l::before{content:'';display:block;width:0;height:0;border-left:8px solid transparent;border-right:8px solid transparent;border-bottom:14px solid #ffe4d0}
.er-l::after{content:'';position:absolute;top:5px;left:4px;width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-bottom:9px solid #f5c2c2}
.er-r{position:absolute;top:-4px;right:7px;z-index:2}.er-r::before{content:'';display:block;width:0;height:0;border-left:8px solid transparent;border-right:8px solid transparent;border-bottom:14px solid #ffe4d0}
.er-r::after{content:'';position:absolute;top:5px;right:4px;width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-bottom:9px solid #f5c2c2}
.col{position:absolute;bottom:19px;left:50%;transform:translateX(-50%);width:22px;height:3px;background:#42A5F5;border-radius:2px;opacity:0;transition:opacity .3s}
.col.on{opacity:1}
.bel{position:absolute;bottom:16px;left:50%;transform:translateX(-50%);width:4px;height:4px;background:#FFD54F;border-radius:50%;opacity:0;transition:opacity .3s}
.bel.on{opacity:1}
.hat{position:absolute;top:-12px;left:50%;transform:translateX(-50%);opacity:0;transition:opacity .3s;z-index:3}
.hat.on{opacity:1}
.ht{width:20px;height:12px;background:#42A5F5;border-radius:2px 2px 0 0;margin:0 auto}
.hb{width:28px;height:3px;background:#1E88E5;border-radius:1px;margin:-2px auto 0}
.hbn{width:20px;height:2px;background:#1565C0;margin:-3px auto 0;border-radius:0 0 1px 1px}
.cig{position:absolute;top:26px;right:-8px;opacity:0;transition:opacity .3s;transform:rotate(-15deg);z-index:3}
.cig.on{opacity:1}
.cb{width:12px;height:2px;background:#fff;border-radius:1px}
.ct{position:absolute;right:-1px;top:-1px;width:3px;height:3px;background:#FF7043;border-radius:50%}
.ms{position:absolute;top:-28px;left:50%;transform:translateX(-50%);background:rgba(255,255,255,.95);color:#1565C0;font-size:9px;padding:3px 8px;border-radius:8px;white-space:nowrap;opacity:0;transition:opacity .25s;pointer-events:none;box-shadow:0 2px 6px rgba(0,0,0,.1);z-index:5;font-family:sans-serif}
.ms::after{content:'';position:absolute;bottom:-3px;left:50%;transform:translateX(-50%);width:0;height:0;border-left:3px solid transparent;border-right:3px solid transparent;border-top:3px solid rgba(255,255,255,.95)}
.ht-heart{position:absolute;pointer-events:none;font-size:10px;opacity:0;z-index:4}
</style></head><body>
<div class="c" id="C">
<div class="er-l"></div><div class="er-r"></div>
<div class="bd"></div>
<div class="hd">
<div class="wh w1"></div><div class="wh w2"></div><div class="wh w3"></div>
<div class="wh w4"></div><div class="wh w5"></div><div class="wh w6"></div>
<div class="bl bll"></div><div class="bl blr"></div>
<div class="el"><div class="ey" id="EL"><div class="es"></div></div></div>
<div class="er"><div class="ey" id="ER"><div class="es"></div></div></div>
<div class="ns"></div><div class="mo" id="MO"></div>
</div>
<div class="col" id="CL"></div><div class="bel" id="BL"></div>
<div class="hat" id="HT"><div class="ht"></div><div class="hbn"></div><div class="hb"></div></div>
<div class="cig" id="CG"><div class="cb"></div><div class="ct"></div></div>
<div class="ms" id="MS"></div>
</div>
<script>
var EL=document.getElementById('EL'),ER=document.getElementById('ER'),MO=document.getElementById('MO')
var CL=document.getElementById('CL'),BL=document.getElementById('BL'),HT=document.getElementById('HT'),CG=document.getElementById('CG')
var MS=document.getElementById('MS'),C=document.getElementById('C'),t0=Date.now(),bw=0
function ex(n){
  E=['10px','12px','10px','12px','2px solid #bbb','0 0 50% 50%','7px','3px','26px','transparent',
      '8px','8px','8px','8px','none','50% 50% 0 0','8px','4px','25px','transparent',
      '10px','10px','10px','10px','2px solid #bbb','0 0 50% 50%','5px','4px','27px','transparent',
      '8px','8px','8px','8px','2px solid #F48FB1','50%','6px','6px','25px','transparent',
      '8px','8px','8px','8px','none','0 0 5px 5px','7px','4px','26px','#F48FB1',
      '10px','12px','0','2px','2px solid #bbb','0 0 50% 50%','8px','3px','26px','transparent']
  var i=n*10;EL.style.width=E[i];EL.style.height=E[i+1];ER.style.width=E[i+2];ER.style.height=E[i+3]
  MO.style.borderBottom=E[i+4];MO.style.borderRadius=E[i+5];MO.style.width=E[i+6];MO.style.height=E[i+7];MO.style.top=E[i+8];MO.style.background=E[i+9]
  if(n===5){ER.style.background='#1565C0';ER.style.borderRadius='2px'}else{ER.style.background='radial-gradient(circle at 35% 30%,#64B5F6,#0D47A1)';ER.style.borderRadius='50%'}
}
ex(0);
var dc=0
function td(){dc=(dc+1)%5;CL.className='col'+(dc>=1?' on':'');BL.className='bel'+(dc>=1?' on':'');HT.className='hat'+(dc>=2||dc===4?' on':'');CG.className='cig'+(dc>=3?' on':'')}
var sa=['嗯？','干嘛～','嘿嘿','喵～','戳我干嘛','痒！','再戳生气了','呜…','手好闲','哼！','想我了？','mua～','乖']
function sy(t){MS.textContent=t;MS.style.opacity='1';setTimeout(function(){MS.style.opacity='0'},1200)}
function ht(){for(var i=0;i<4;i++){var h=document.createElement('div');h.className='ht-heart';h.textContent='♥';var s=8+Math.random()*8;h.style.fontSize=s+'px';h.style.left=(15+Math.random()*30)+'px';h.style.top=(20+Math.random()*20)+'px';h.style.color=['#FF4081','#FF80AB','#F48FB1'][Math.floor(Math.random()*3)];C.appendChild(h);var hs=Date.now();!function(e,s){function hf(){var a=(Date.now()-s)/1000;if(a>1.4){e.remove();return}var p=a/1.4;e.style.transform='translateY('+(-p*40)+'px)';e.style.opacity=(1-p).toFixed(2);requestAnimationFrame(hf)}hf()}(h,hs)}}
function tap(){
  var r=Math.random()
  if(r<0.14){ex(1);sy('嘿嘿～')}else if(r<0.28){ex(2);sy('呜…戳疼了')}else if(r<0.42){ex(3);sy('mua～')}else if(r<0.56){ex(4);sy('略略略')}else if(r<0.7){ex(5);sy('想我了？')}else{ex(0);sy(sa[Math.floor(Math.random()*sa.length)])}
  ht();setTimeout(function(){ex(0)},2000)
}
function longpress(){td();var d=['素猫','项圈','+礼帽','+抽烟','全武装'];sy(d[dc])}
!function anim(){var t=(Date.now()-t0)/1000;C.style.transform='translateY('+Math.sin(t*1.3)*3+'px)';bw-=16;if(bw<=0){EL.style.transform='scaleY(0.1)';ER.style.transform='scaleY(0.1)';setTimeout(function(){EL.style.transform='scaleY(1)';ER.style.transform='scaleY(1)'},120);bw=2500+Math.random()*2500}requestAnimationFrame(anim)}()
bw=2000;
setInterval(function(){var r=Math.random();if(r<0.2){var e=[1,2,3,4,5][Math.floor(Math.random()*5)];ex(e);setTimeout(function(){ex(0)},3000)}},12000);
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
            c.description = "小易猫猫在桌面上陪你"
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
                    dragging = false
                    longPressed = false
                    ix = p.x; iy = p.y
                    itx = event.rawX; ity = event.rawY
                    handler.postDelayed({
                        longPressed = true
                        wv?.evaluateJavascript("javascript:longpress()", null)
                    }, LONG_PRESS_MS)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - itx
                    val dy = event.rawY - ity
                    if (Math.sqrt((dx * dx + dy * dy).toDouble()) > 10) {
                        dragging = true
                        handler.removeCallbacksAndMessages(null)
                    }
                    p.x = ix + dx.toInt()
                    p.y = iy + dy.toInt()
                    wm.updateViewLayout(container, p)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacksAndMessages(null)
                    if (!dragging && !longPressed) {
                        wv?.evaluateJavascript("javascript:tap()", null)
                    }
                    dragging = false
                    longPressed = false
                    true
                }
                else -> true
            }
        }

        wm.addView(container, p)
        overlayView = container
    }
}
