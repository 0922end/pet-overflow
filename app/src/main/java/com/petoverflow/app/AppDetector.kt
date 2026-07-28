package com.petoverflow.app

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Handler
import android.os.Looper

class AppDetector(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var lastApp = ""
    private var isRunning = false

    fun startListening() {
        isRunning = true
        handler.postDelayed(checkRunnable, 3000)
    }

    fun stopListening() {
        isRunning = false
        handler.removeCallbacks(checkRunnable)
    }

    private val checkRunnable = object : Runnable {
        override fun run() {
            if (!isRunning) return
            try {
                val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - 60000, System.currentTimeMillis())
                if (stats != null && stats.isNotEmpty()) {
                    val latest = stats.maxBy { it.lastTimeUsed }
                    val current = latest?.packageName ?: ""
                    if (current != lastApp && lastApp.isNotEmpty()) {
                        onAppSwitch(current)
                    }
                    lastApp = current
                }
            } catch (_: Exception) {}
            handler.postDelayed(this, 3000)
        }
    }

    private fun onAppSwitch(package: String) {
        supabaseClient().reportForegroundApp(package, package)
    }
}
