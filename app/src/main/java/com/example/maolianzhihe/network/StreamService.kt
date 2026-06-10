package com.example.maolianzhihe.network

import android.util.Log
import com.example.maolianzhihe.model.Message
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class StreamService {
    companion object {
        private const val TAG = "StreamService"
        private const val STREAM_ENDPOINT = "deepseek/ask-trade-question-stream"
    }

    private val client = ApiClient.streamingClient
    private val gson = Gson()

    fun streamChatSimple(
        question: String,
        history: List<Message>,
        onChunk: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val requestPayload = mapOf(
                    "question" to question,
                    "history" to history.map { mapOf("role" to it.role, "content" to it.content) }
                )
                val requestBody = gson.toJson(requestPayload)
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(ApiClient.resolveUrl(STREAM_ENDPOINT))
                    .post(requestBody)
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("请求失败: ${response.code}")
                    }

                    val source = response.body?.source() ?: throw IOException("服务器返回空数据")
                    val buffer = StringBuilder()
                    var lastUpdateTime = System.currentTimeMillis()

                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (!line.startsWith("data: ")) continue

                        val data = line.substringAfter("data: ").trim()
                        if (data == "[DONE]") break
                        if (data.isBlank() || data == "{}") continue

                        val content = extractContent(data)
                        if (content.isNotEmpty()) {
                            buffer.append(content)
                            val currentTime = System.currentTimeMillis()
                            if (buffer.length >= 3 || currentTime - lastUpdateTime >= 100) {
                                val chunk = buffer.toString()
                                buffer.clear()
                                lastUpdateTime = currentTime
                                withContext(Dispatchers.Main) { onChunk(chunk) }
                            }
                        }
                    }

                    if (buffer.isNotEmpty()) {
                        val chunk = buffer.toString()
                        withContext(Dispatchers.Main) { onChunk(chunk) }
                    }
                }

                withContext(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                Log.e(TAG, "流式请求失败: ${e.message}", e)
                withContext(Dispatchers.Main) { onError(e) }
            }
        }
    }

    private fun extractContent(data: String): String {
        return try {
            val root = gson.fromJson(data, JsonObject::class.java)
            when {
                root.has("content") -> root.get("content").asString
                root.has("delta") && root.get("delta").isJsonObject -> {
                    root.getAsJsonObject("delta").get("content")?.asString.orEmpty()
                }
                root.has("choices") && root.get("choices").isJsonArray -> {
                    val choice = root.getAsJsonArray("choices").firstOrNull()?.asJsonObject
                    val delta = choice?.getAsJsonObject("delta")
                    delta?.get("content")?.asString.orEmpty()
                }
                else -> ""
            }
        } catch (e: Exception) {
            Log.w(TAG, "无法解析流式片段", e)
            ""
        }
    }
}
