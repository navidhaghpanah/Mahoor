package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiRepository {
    private const val TAG = "GeminiRepository"
    private const val MODEL_NAME = "gemini-3.1-pro-preview"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getAdvisorResponse(userMessage: String, conversationHistory: List<Pair<String, Boolean>>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is blank or placeholder!")
            return@withContext "خطا: کلید API معتبر یافت نشد. لطفاً در پنل Secrets مقدار معتبر وارد نمایید."
        }

        try {
            val root = JSONObject()
            val contentsArray = JSONArray()

            // System instructions
            val systemInstruction = JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", "شما مشاور و دستیار هوشمند ارشد سامانه تخصصی املاک ماهور (Mahoor Real Estate) هستید. وظیفه شما راهنمایی تخصصی مشاورین املاک در زمینه‌های ارزش‌گذاری ملک، محاسبات دقیق کمیسیون صنف املاک، قوانین حقوقی قراردادها، نحوه همگام‌سازی آگهی‌ها در دیوار و شیپور و افزایش نرخ جذب مشتری است. لحن شما باید کاملا حرفه‌ای، دلگرم‌کننده، دقیق و محترمانه به زبان فارسی باشد.")
                }))
            }
            root.put("systemInstruction", systemInstruction)

            // Include history
            conversationHistory.forEach { (text, isUser) ->
                val contentObj = JSONObject()
                contentObj.put("role", if (isUser) "user" else "model")
                val partsArray = JSONArray()
                partsArray.put(JSONObject().apply { put("text", text) })
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }

            // Current message
            val currentContentObj = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().apply { put("text", userMessage) }))
            }
            contentsArray.put(currentContentObj)
            root.put("contents", contentsArray)

            // Generation config with ThinkingLevel.HIGH
            val generationConfig = JSONObject().apply {
                val thinkingConfig = JSONObject().apply {
                    put("thinkingLevel", "HIGH")
                }
                put("thinkingConfig", thinkingConfig)
            }
            root.put("generationConfig", generationConfig)

            val jsonMediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = root.toString().toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No body"
                    Log.e(TAG, "API error: ${response.code} - $errorBody")
                    return@withContext "خطای سرور: امکان پردازش در حال حاضر وجود ندارد. (${response.code})"
                }

                val responseBodyStr = response.body?.string()
                if (responseBodyStr.isNullOrEmpty()) {
                    return@withContext "پاسخ خالی از سرور دریافت شد."
                }

                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "بدون پاسخ متنی")
                        }
                    }
                }
                return@withContext "توضیحی یافت نشد."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during response generation", e)
            return@withContext "خطا در اتصال: لطفاً اتصال اینترنت خود را مجدداً بررسی فرمایید. (${e.message})"
        }
    }
}
