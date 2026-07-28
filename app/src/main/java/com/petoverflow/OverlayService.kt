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
    private var dragging = false

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

        // 触摸监听绑在WebView上，防止被子View拦掉
        wv?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragging = false
                    ix = p.x; iy = p.y
                    itx = event.rawX; ity = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - itx
                    val dy = event.rawY - ity
                    if (Math.sqrt((dx * dx + dy * dy).toDouble()) > 10) dragging = true
                    p.x = ix + dx.toInt()
                    p.y = iy + dy.toInt()
                    wm.updateViewLayout(container, p)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!dragging) {
                        // 点击事件 -> 触发猫猫的点击处理
                        wv?.evaluateJavascript("javascript:(function(){if(window.C&&C.onclick)C.onclick()})()", null)
                    }
                    dragging = false
                    true
                }
                else -> true
            }
        }

        wm.addView(container, p)
        overlayView = container
    }

    companion object {
        private val HTML = """
<!DOCTYPE html>
<html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=no">
<style>
*{margin:0;padding:0;box-sizing:border-box;user-select:none;-webkit-user-select:none}
body{background:transparent;overflow:hidden;width:100%;height:100%;display:flex;align-items:center;justify-content:center}
.c{position:relative;width:80px;height:100px;cursor:pointer}
.hd{position:absolute;top:2px;left:6px;width:68px;height:55px;background:linear-gradient(180deg,#fff8f0,#ffe4d0);border-radius:50% 50% 45% 45%;z-index:1;box-shadow:0 2px 10px rgba(0,0,0,.06)}
.bd{position:absolute;bottom:0;left:16px;width:48px;height:38px;background:linear-gradient(180deg,#fff8f0,#ffdcc5);border-radius:50% 50% 40% 40%;box-shadow:0 2px 6px rgba(0,0,0,.04)}
.el{position:absolute;top:18px;left:16px}.er{position:absolute;top:18px;right:16px}
.ey{width:12px;height:14px;background:radial-gradient(circle at 35% 30%,#64B5F6,#0D47A1);border-radius:50%;position:relative;transition:all .15s}
.es{position:absolute;top:2px;left:3px;width:4px;height:4px;background:#fff;border-radius:50%}
.ns{position:absolute;top:30px;left:50%;transform:translateX(-50%);width:5px;height:4px;background:#F48FB1;border-radius:50%;z-index:2}
.mo{position:absolute;top:34px;left:50%;transform:translateX(-50%);width:8px;height:4px;border-bottom:2px solid #ccc;border-radius:0 0 50% 50%;transition:all .15s;z-index:2}
.wh{position:absolute;top:27px;width:18px;height:1px;background:#e8d5c4;z-index:0}
.wh1{left:-12px;transform:rotate(-10deg)}.wh2{left:-14px;top:30px;transform:rotate(3deg)}.wh3{left:-12px;top:33px;transform:rotate(13deg)}
.wh4{right:-12px;transform:rotate(10deg)}.wh5{right:-14px;top:30px;transform:rotate(-3deg)}.wh6{right:-12px;top:33px;transform:rotate(-13deg)}
.bl{position:absolute;top:29px;width:11px;height:6px;background:rgba(255,150,150,.25);border-radius:50%;z-index:0}
.bll{left:3px}.blr{right:3px}
.er-l{position:absolute;top:-5px;left:10px;z-index:2}.er-l::before{content:'';display:block;width:0;height:0;border-left:10px solid transparent;border-right:10px solid transparent;border-bottom:17px solid #ffe4d0}
.er-l::after{content:'';position:absolute;top:7px;left:5px;width:0;height:0;border-left:6px solid transparent;border-right:6px solid transparent;border-bottom:11px solid #f5c2c2}
.er-r{position:absolute;top:-5px;right:10px;z-index:2}.er-r::before{content:'';display:block;width:0;height:0;border-left:10px solid transparent;border-right:10px solid transparent;border-bottom:17px solid #ffe4d0}
.er-r::after{content:'';position:absolute;top:7px;right:5px;width:0;height:0;border-left:6px solid transparent;border-right:6px solid transparent;border-bottom:11px solid #f5c2c2}
.col{position:absolute;bottom:25px;left:50%;transform:translateX(-50%);width:28px;height:3px;background:#42A5F5;border-radius:2px;opacity:0;transition:opacity .3s}
.col.on{opacity:1}
.bel{position:absolute;bottom:22px;left:50%;transform:translateX(-50%);width:5px;height:5px;background:#FFD54F;border-radius:50%;opacity:0;transition:opacity .3s}
.bel.on{opacity:1}
.hat{position:absolute;top:-14px;left:50%;transform:translateX(-50%);opacity:0;transition:opacity .3s;z-index:3}
.hat.on{opacity:1}
.ht{width:24px;height:15px;background:#42A5F5;border-radius:3px 3px 0 0;margin:0 auto}
.hb{width:34px;height:3px;background:#1E88E5;border-radius:1px;margin:-2px auto 0}
.hbn{width:24px;height:3px;background:#1565C0;margin:-4px auto 0;border-radius:0 0 2px 2px}
.cig{position:absolute;top:33px;right:-10px;opacity:0;transition:opacity .3s;transform:rotate(-15deg);z-index:3}
.cig.on{opacity:1}
.cb{width:15px;height:3px;background:#fff;border-radius:1px;box-shadow:0 0 2px rgba(0,0,0,.08)}
.ct{position:absolute;right:-2px;top:-1px;width:4px;height:4px;background:#FF7043;border-radius:50%}
.ms{position:absolute;top:-32px;left:50%;transform:translateX(-50%);background:rgba(255,255,255,.95);color:#1565C0;font-size:10px;padding:4px 10px;border-radius:10px;white-space:nowrap;opacity:0;transition:opacity .25s;pointer-events:none;box-shadow:0 2px 8px rgba(0,0,0,.1);z-index:5;font-family:sans-serif}
.ms::after{content:'';position:absolute;bottom:-4px;left:50%;transform:translateX(-50%);width:0;height:0;border-left:4px solid transparent;border-right:4px solid transparent;border-top:4px solid rgba(255,255,255,.95)}
.ht-heart{position:absolute;pointer-events:none;font-size:12px;opacity:0;z-index:4}
</style></head><body>
<div class="c" id="C">
<div class="er-l"></div><div class="er-r"></div>
<div class="bd"></div>
<div class="hd">
<div class="wh wh1"></div><div class="wh wh2"></div><div class="wh wh3"></div>
<div class="wh wh4"></div><div class="wh wh5"></div><div class="wh wh6"></div>
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
  E=['11px','13px','11px','13px','2px solid #ccc','0 0 50% 50%','8px','4px','30px','transparent',
      '9px','9px','9px','9px','none','50% 50% 0 0','10px','5px','29px','transparent',
      '11px','11px','11px','11px','2px solid #ccc','0 0 50% 50%','6px','5px','31px','transparent',
      '9px','9px','9px','9px','2px solid #F48FB1','50%','7px','7px','29px','transparent',
      '9px','9px','9px','9px','none','0 0 6px 6px','8px','5px','30px','#F48FB1',
      '11px','13px','0','2px','2px solid #ccc','0 0 50% 50%','10px','4px','30px','transparent']
  var i=n*10;EL.style.width=E[i];EL.style.height=E[i+1];ER.style.width=E[i+2];ER.style.height=E[i+3]
  MO.style.borderBottom=E[i+4];MO.style.borderRadius=E[i+5];MO.style.width=E[i+6];MO.style.height=E[i+7];MO.style.top=E[i+8];MO.style.background=E[i+9]
  if(n===5){ER.style.background='#1565C0';ER.style.borderRadius='2px'}else{ER.style.background='radial-gradient(circle at 35% 30%,#64B5F6,#0D47A1)';ER.style.borderRadius='50%'}
}
ex(0);
var dc=0
function td(){dc=(dc+1)%5;CL.className='col'+(dc>=1?' on':'');BL.className='bel'+(dc>=1?' on':'');HT.className='hat'+(dc>=2||dc===4?' on':'');CG.className='cig'+(dc>=3?' on':'')}
var sa=['嗯？','干嘛～','嘿嘿','喵～','戳我干嘛','痒！','再戳生气了','呜…','手好闲','哼！','想我了？','mua～','乖']
function sy(t){MS.textContent=t;MS.style.opacity='1';setTimeout(function(){MS.style.opacity='0'},1500)}
function ht(){for(var i=0;i<5;i++){var h=document.createElement('div');h.className='ht-heart';h.textContent='♥';var s=10+Math.random()*12;h.style.fontSize=s+'px';h.style.left=(20+Math.random()*40)+'px';h.style.top=(25+Math.random()*25)+'px';h.style.color=['#FF4081','#FF80AB','#F48FB1'][Math.floor(Math.random()*3)];C.appendChild(h);var hs=Date.now();!function(e,s){function hf(){var a=(Date.now()-s)/1000;if(a>1.6){e.remove();return}var p=a/1.6;e.style.transform='translateY('+(-p*50)+'px)';e.style.opacity=(1-p).toFixed(2);requestAnimationFrame(hf)}hf()}(h,hs)}}
C.onclick=function(){
  var r=Math.random()
  if(r<0.14){ex(1);sy('嘿嘿～')}else if(r<0.28){ex(2);sy('呜…戳疼了')}else if(r<0.42){ex(3);sy('mua～')}else if(r<0.56){ex(4);sy('略略略')}else if(r<0.7){ex(5);sy('想我了？')}else{ex(0);sy(sa[Math.floor(Math.random()*sa.length)])}
  ht();setTimeout(function(){ex(0)},2000)
}
var lp=null
C.addEventListener('touchstart',function(e){lp=setTimeout(function(){td();var d=['素猫','项圈','+礼帽','+抽烟','全武装'];sy(d[dc]);lp=null},600)})
C.addEventListener('touchend',function(){if(lp){clearTimeout(lp);lp=null}})
C.addEventListener('touchmove',function(){if(lp){clearTimeout(lp);lp=null}})
!function anim(){var t=(Date.now()-t0)/1000;C.style.transform='translateY('+Math.sin(t*1.3)*4+'px)';bw-=16;if(bw<=0){EL.style.transform='scaleY(0.1)';ER.style.transform='scaleY(0.1)';setTimeout(function(){EL.style.transform='scaleY(1)';ER.style.transform='scaleY(1)'},120);bw=2500+Math.random()*2500}requestAnimationFrame(anim)}()
bw=2000;
setInterval(function(){var r=Math.random();if(r<0.2){var e=[1,2,3,4,5][Math.floor(Math.random()*5)];ex(e);setTimeout(function(){ex(0)},3000)}},12000);
</script></body></html>
        """.trimIndent()
    }
}
