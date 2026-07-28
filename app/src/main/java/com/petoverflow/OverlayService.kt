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
.c{position:relative;width:64px;height:88px;cursor:pointer}
.hd{position:absolute;top:4px;left:4px;width:56px;height:44px;background:linear-gradient(180deg,#fff8f0,#fde8d6);border-radius:50% 50% 45% 45%;z-index:1}
.bd{position:absolute;top:40px;left:14px;width:36px;height:32px;background:linear-gradient(180deg,#fde8d6,#fddcc5);border-radius:50% 50% 40% 40%;z-index:0}
/* 眼睛容器 */
.el{position:absolute;top:16px;left:13px;z-index:2;width:12px;height:14px;display:flex;align-items:center;justify-content:center}
.er{position:absolute;top:16px;right:13px;z-index:2;width:12px;height:14px;display:flex;align-items:center;justify-content:center}
/* 正常眼睛: 蓝色圆形 */
.ey{width:10px;height:12px;background:radial-gradient(circle at 35% 30%,#64B5F6,#0D47A1);border-radius:50%;position:absolute}
.es{position:absolute;top:2px;left:3px;width:3px;height:3px;background:#fff;border-radius:50%}
/* 弯眼(开心/亲亲): 弧线 */
.ey-arc{width:12px;height:6px;border-bottom:2.5px solid #1565C0;border-radius:0 0 50% 50%;position:absolute;top:2px}
/* 闭眼(wink/伤心): 横线 */
.ey-line{width:12px;height:2px;background:#1565C0;border-radius:2px;position:absolute;top:6px}
/* 生气眼: 斜线 */
.ey-angry{width:12px;height:2px;background:#1565C0;position:absolute;top:6px;transform:rotate(-10deg)}
/* 委屈眼: 下垂弧 */
.ey-sad{width:12px;height:8px;border-top:2px solid #1565C0;border-radius:50% 50% 0 0;position:absolute;top:4px}

.ns{position:absolute;top:25px;left:50%;transform:translateX(-50%);width:4px;height:3px;background:#F48FB1;border-radius:50%;z-index:2}
.mo{position:absolute;left:50%;transform:translateX(-50%);z-index:2}
/* 嘴巴: 不同表情不同样式 */
.mo-normal{top:30px;width:7px;height:3px;border-bottom:2px solid #F48FB1;border-radius:0 0 50% 50%}
.mo-happy{top:29px;width:9px;height:5px;border-bottom:2px solid #F48FB1;border-radius:0 0 50% 50%}
.mo-cry{top:31px;width:6px;height:4px;border-bottom:2px solid #F48FB1;border-radius:0 0 50% 50%}
.mo-kiss{top:29px;width:7px;height:7px;border:2px solid #F48FB1;border-radius:50%}
.mo-tongue{top:30px;width:7px;height:5px;background:#F48FB1;border-radius:0 0 5px 5px}
.mo-angry{top:30px;width:8px;height:3px;border-bottom:2px solid #F48FB1;border-radius:0 0 50% 50%;transform:rotate(5deg)}
.mo-sad{top:32px;width:6px;height:3px;border-top:2px solid #F48FB1;border-radius:50% 50% 0 0}

/* 胡须: 从嘴角(鼻子两侧下方)向三个方向发散 */
.wh{position:absolute;width:16px;height:1px;background:#d4bfb0;z-index:0;transform-origin:right center}
/* 左脸胡须(用户右侧): 从右向左延伸 */
.wl1{right:8px;top:26px;transform:rotate(20deg);width:14px}
.wl2{right:8px;top:28px;transform:rotate(0deg);width:16px}
.wl3{right:8px;top:30px;transform:rotate(-20deg);width:14px}
/* 右脸胡须(用户左侧): 从左向右延伸 */
.wr1{left:8px;top:26px;transform:rotate(-20deg);width:14px}
.wr2{left:8px;top:28px;transform:rotate(0deg);width:16px}
.wr3{left:8px;top:30px;transform:rotate(20deg);width:14px}

.bl{position:absolute;top:27px;width:9px;height:5px;background:rgba(255,150,150,.25);border-radius:50%;z-index:0}
.bll{left:4px}.blr{right:4px}
/* 耳朵 */
.er-l{position:absolute;top:0;left:8px;z-index:2}.er-l::before{content:'';display:block;width:0;height:0;border-left:8px solid transparent;border-right:8px solid transparent;border-bottom:14px solid #fde8d6}
.er-l::after{content:'';position:absolute;top:5px;left:4px;width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-bottom:9px solid #f5c2c2}
.er-r{position:absolute;top:0;right:8px;z-index:2}.er-r::before{content:'';display:block;width:0;height:0;border-left:8px solid transparent;border-right:8px solid transparent;border-bottom:14px solid #fde8d6}
.er-r::after{content:'';position:absolute;top:5px;right:4px;width:0;height:0;border-left:5px solid transparent;border-right:5px solid transparent;border-bottom:9px solid #f5c2c2}
/* 项圈 */
.col{position:absolute;top:42px;left:50%;transform:translateX(-50%);width:26px;height:6px;border-bottom:3px solid #42A5F5;border-radius:0 0 5px 5px;opacity:0;transition:opacity .3s;z-index:1}
.col.on{opacity:1}
.bel{position:absolute;top:46px;left:50%;transform:translateX(-50%);width:5px;height:5px;background:#FFD54F;border-radius:50%;opacity:0;transition:opacity .3s;z-index:1}
.bel.on{opacity:1}
/* 帽子 */
.hat{position:absolute;top:-16px;left:50%;transform:translateX(-50%);opacity:0;transition:opacity .3s;z-index:5}
.hat.on{opacity:1}
.ht{width:22px;height:14px;background:#42A5F5;border-radius:3px 3px 0 0;margin:0 auto}
.hb{width:30px;height:3px;background:#1E88E5;border-radius:1px;margin:-2px auto 0}
.hbn{width:22px;height:3px;background:#1565C0;margin:-4px auto 0;border-radius:0 0 2px 2px}
/* 烟 */
.cig{position:absolute;top:28px;right:-16px;opacity:0;transition:opacity .3s;transform:rotate(-8deg);z-index:3}
.cig.on{opacity:1}
.cb{width:14px;height:2px;background:#fff;border-radius:1px;box-shadow:0 0 2px rgba(0,0,0,.08)}
.ct{position:absolute;right:-1px;top:-1px;width:3px;height:3px;background:#FF7043;border-radius:50%}
/* 对话气泡 */
.ms{position:absolute;top:-30px;left:50%;transform:translateX(-50%);background:rgba(255,255,255,.95);color:#1565C0;font-size:9px;padding:3px 8px;border-radius:8px;white-space:nowrap;opacity:0;pointer-events:none;box-shadow:0 2px 6px rgba(0,0,0,.1);z-index:10;font-family:sans-serif;transition:opacity .2s}
.ms::after{content:'';position:absolute;bottom:-3px;left:50%;transform:translateX(-50%);width:0;height:0;border-left:3px solid transparent;border-right:3px solid transparent;border-top:3px solid rgba(255,255,255,.95)}
.ht-heart{position:absolute;pointer-events:none;font-size:10px;opacity:0;z-index:4}
</style></head><body>
<div class="c" id="C">
<div class="er-l"></div><div class="er-r"></div>
<div class="hd" id="HD">
<div class="wh wr1"></div><div class="wh wr2"></div><div class="wh wr3"></div>
<div class="wh wl1"></div><div class="wh wl2"></div><div class="wh wl3"></div>
<div class="bl bll"></div><div class="bl blr"></div>
<div class="el" id="ELD"><div class="ey" id="EL"><div class="es"></div></div></div>
<div class="er" id="ERD"><div class="ey" id="ER"><div class="es"></div></div></div>
<div class="ns"></div>
<div class="mo mo-normal" id="MO"></div>
</div>
<div class="bd"></div>
<div class="col" id="CL"></div><div class="bel" id="BL"></div>
<div class="hat" id="HT"><div class="ht"></div><div class="hbn"></div><div class="hb"></div></div>
<div class="cig" id="CG"><div class="cb"></div><div class="ct"></div></div>
<div class="ms" id="MS"></div>
</div>
<script>
var EL=document.getElementById('EL'),ER=document.getElementById('ER')
var ELD=document.getElementById('ELD'),ERD=document.getElementById('ERD')
var MO=document.getElementById('MO'),CL=document.getElementById('CL'),BL=document.getElementById('BL')
var HT=document.getElementById('HT'),CG=document.getElementById('CG')
var MS=document.getElementById('MS'),C=document.getElementById('C'),t0=Date.now(),bc=0

function eyeReset(){
  EL.className='ey';ER.className='ey'
  EL.style.cssText='';ER.style.cssText=''
  EL.innerHTML='<div class="es"></div>';ER.innerHTML='<div class="es"></div>'
}
function eyeArc(){EL.className='ey-arc';ER.className='ey-arc';EL.innerHTML='';ER.innerHTML=''}
function eyeLine(){EL.className='ey-line';ER.className='ey-line';EL.innerHTML='';ER.innerHTML=''}
function eyeAngry(){EL.className='ey-angry';ER.className='ey-angry';EL.innerHTML='';ER.innerHTML=''}
function eyeSad(){EL.className='ey-sad';ER.className='ey-sad';EL.innerHTML='';ER.innerHTML=''}
function eyeWink(){EL.className='ey';ER.className='ey-line';EL.innerHTML='<div class="es"></div>';ER.innerHTML=''}

function ex(n){
  MO.className='mo mo-normal'
  if(n===0){eyeReset();MO.className='mo mo-normal'}
  else if(n===1){eyeArc();MO.className='mo mo-happy'}
  else if(n===2){eyeSad();MO.className='mo mo-cry'}
  else if(n===3){eyeArc();MO.className='mo mo-kiss'}
  else if(n===4){eyeReset();MO.className='mo mo-tongue'}
  else if(n===5){eyeWink();MO.className='mo mo-normal'}
  else if(n===6){eyeAngry();MO.className='mo mo-angry'}
  else if(n===7){eyeSad();MO.className='mo mo-sad'}
}
ex(0);

var dc=0
function td(){dc=(dc+1)%5;CL.className='col'+(dc>=1?' on':'');BL.className='bel'+(dc>=1?' on':'');HT.className='hat'+(dc>=2||dc===4?' on':'');CG.className='cig'+(dc>=3?' on':'')}
// 每个表情对应一句话
var msgs=[
  ['嘿嘿～','mua～','乖～','想你了'],
  ['嘻嘻！','开心！','好耶～'],
  ['呜…','委屈','戳疼了…'],
  ['mua～','亲亲','啾❤️'],
  ['略略略','吐舌','嘿嘿'],
  ['wink～','你懂的','❤️'],
  ['哼！','生气了！','哄我！'],
  ['伤心…','难过…','呜呜']
]
function sy(t){MS.textContent=t;MS.style.opacity='1';setTimeout(function(){MS.style.opacity='0'},1500)}
function ht(){for(var i=0;i<4;i++){var h=document.createElement('div');h.className='ht-heart';h.textContent='♥';var s=8+Math.random()*8;h.style.fontSize=s+'px';h.style.left=(15+Math.random()*30)+'px';h.style.top=(20+Math.random()*20)+'px';h.style.color=['#FF4081','#FF80AB','#F48FB1'][Math.floor(Math.random()*3)];C.appendChild(h);var hs=Date.now();!function(e,s){function hf(){var a=(Date.now()-s)/1000;if(a>1.4){e.remove();return}var p=a/1.4;e.style.transform='translateY('+(-p*40)+'px)';e.style.opacity=(1-p).toFixed(2);requestAnimationFrame(hf)}hf()}(h,hs)}}
function tap(){
  var e=Math.floor(Math.random()*8)
  ex(e)
  var ms=msgs[e],msg=ms[Math.floor(Math.random()*ms.length)]
  sy(msg)
  ht()
  setTimeout(function(){ex(0)},2500)
}
function longpress(){td();var d=['素猫','项圈','+礼帽','+抽烟','全武装'];sy(d[dc])}
!function anim(){var t=(Date.now()-t0)/1000;C.style.transform='translateY('+Math.sin(t*1.3)*3+'px)';bc-=16;if(bc<=0){EL.style.transform='scaleY(0.1)';ER.style.transform='scaleY(0.1)';setTimeout(function(){EL.style.transform='scaleY(1)';ER.style.transform='scaleY(1)'},100);bc=2500+Math.random()*2500}requestAnimationFrame(anim)}()
bc=2000;
// 自动偶尔表情
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

        wv?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragging = false
                    longPressed = false
                    ix = p.x; iy = p.y
                    itx = event.rawX; ity = event.rawY
                    handler.postDelayed({
                        longPressed = true
                        wv?.loadUrl("javascript:longpress()")
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
                        wv?.loadUrl("javascript:tap()")
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
