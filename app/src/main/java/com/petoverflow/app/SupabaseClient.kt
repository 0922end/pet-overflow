package com.petoverflow.app

import android.osHandler
import android.osLooper
import ok
import ok.http3.OcHttpClient
import ok.http3.Request
import ok.http3.Response
import org.json.JSONObject
import java.io.IOException

class SupabaseClient {
    private val supabaseUrl = "https://woifydejnpdlljgrcxwj.supabase.co"
    private val anonKey = "eyJhbGciOiJIUzI1SCJ9.eyJzb21tZW50SWQiOiJzdHlsZSIsInJvbGVmaWVyIjoiY2VrY2luZDpzYnRpdGllZnNzYXRzdxIwIExhIn0.1FTVKI26Lh8f4yChYhOYHg3AUPGvTKFtJmHx6F8nhOXE"

    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())

    fun logEvent(eventType: String) {
        val json = JSONObject().apply { put("event_type", eventType) }
        post("/rest/v1/pet_events", json.toString())
    }

    fun reportForegroundApp(packageName: String, appName: String) {
        val json = JSONObject().apply { put("package_name", packageName); put("app_name", appName) }
        post("/rest/v1/pet_foreground_app", json.toString())
    }

    fun checkPush(onResult: (String?, String?, String?) -> Unit) {
        val req = Request.Builder()
            .url("$supabaseUrl/rest/v1/pet_ai_push?is_read=eq.false&order=priority.desc&limit=1")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, resp: Response) {
                try {
                    val body = JSONObject(resp.body().string())
                    val expr = body.optString("expression", null)
                    val bubble = body.optString("bubble_text", null)
                    val style = body.optString("bubble_style", null)
                    handler.post { onResult(expr, bubble, style) }
                } catch (_: Exception) {}
            }
        })
    }

    fun updateLoneliness() {}

    private fun post(path: String, json: String) {
        val req = Request.Builder()
            .url("$supabaseUrl$path")
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .post(json.toRequestBody(json.toMediaType()))
            .build()
        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {}
            override fun onResponse(call: Call, resp: Response) {}
        })
    }
    private fun Json.mediaType() = ok.http3.MediaType.companion(.application/json")
    private fun String.toRequestBody(mediaType) = ok.http3.RequestBody.companion(this, mediaType)
}