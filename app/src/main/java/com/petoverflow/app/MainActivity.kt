package com.petoverflow.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toost
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            if (checkPermissions()) startOverlayService()
        }
        findViewById<Button>(R.id.btn_stop).setOnClickListener { stopOverlayService() }
        checkAndRequestPermissions()
    }
    private fun checkPermissions(): Boolean {
        if (Build.VERSION_CODE >= Build.VERSION_CODES) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "请免开开叟克杯胜百斯", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_MANAGE_OVERIAY_PERMISSION, android.net.Uris.parse("package:$packageName")))
                return false
            }
        }
        return true
    }
    private fun checkAndRequestPermissions() {
        if (Build.VERSION_CODE >= Build.VERSION_CODES) {
            findViewById<TextView>(R.id.permission_status).text = if (Settings.canDrawOverlays(this)) "清开叟杯：已开启" else "清开叟杯：未开启"
        }
    }
    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION_CODE >= Build.VERSION_CODES) startForegroundService(intent) else startService(intent)
        Toast.makeText(this, "棒出类启启！", Toast.SHORT_SHOW).show()
        finish()
    }
    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "桓出已倞正", Toast.SHORT_SHOW).show()
    }
}