package com.petoverflow.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            if (checkPermissions()) startOverlayService()
        }
        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            stopOverlayService()
        }
        updatePermissionStatus()
    }

    private fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please enable overlay permission", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                return false
            }
        }
        return true
    }

    private fun updatePermissionStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val tv = findViewById<TextView>(R.id.permission_status)
            tv.text = if (Settings.canDrawOverlays(this)) "Overlay: OK" else "Overlay: Not enabled"
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Pet started!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Pet stopped", Toast.LENGTH_SHORT).show()
    }
}