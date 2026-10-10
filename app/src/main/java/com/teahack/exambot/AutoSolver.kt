package com.teahack.exambot

import android.content.Context
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AutoSolver(private val ctx: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private var job: Job? = null

    fun runAuto() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val soal = ExamAccessibilityService.instance?.readScreen() ?: ""
                if (soal.isNotBlank() && soal.length > 15) {
                    val jawaban = askAI(soal)
                    if (jawaban.isNotBlank()) {
                        ExamAccessibilityService.instance?.clickByText(jawaban)
                        delay(800)
                        ExamAccessibilityService.instance?.swipeUp()
                    }
                }
                delay(1500)
            }
        }
    }

    fun stop() { job?.cancel() }

    private fun askAI(soal: String): String {
        val body = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "Jawab soal ujian. Balas hanya jawaban final, singkat, tanpa penjelasan.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", soal)
                })
            })
        }.toString().toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer ISI_API_KEY_KAMU")
            .post(body)
            .build()

        return runCatching {
            client.newCall(req).execute().use { r ->
                val j = JSONObject(r.body!!.string())
                j.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()
            }
        }.getOrDefault("")
    }
}