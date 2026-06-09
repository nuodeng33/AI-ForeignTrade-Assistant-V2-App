package com.example.maolianzhihe.network

import android.util.Log
import com.example.maolianzhihe.model.Message
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class StreamService {

    companion object {
        private const val TAG = "StreamService"
        private const val BASE_URL = "http://192.168.42.94:1337/api/"
        private const val STREAM_ENDPOINT = "deepseek/ask-trade-question-stream"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 改进的流式请求实现 - 真正的逐字显示
     */
    fun streamChat(
        question: String,
        history: List<Message>
    ): Flow<String> = channelFlow {
        Log.d(TAG, "开始流式请求，问题: $question")

        // 构建请求体
        val requestBody = """
            {
                "question": "${escapeJson(question)}",
                "history": ${history.toJson()}
            }
        """.trimIndent().toRequestBody("application/json".toMediaType())

        // 构建请求
        val request = Request.Builder()
            .url("${BASE_URL}${STREAM_ENDPOINT}")
            .post(requestBody)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        var response: Response? = null

        try {
            response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("请求失败: ${response.code}")
            }

            Log.d(TAG, "流式连接建立成功")

            response.body?.let { body ->
                val source = body.source()
                var buffer = StringBuilder()

                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break

                    if (line.startsWith("data: ")) {
                        val data = line.substring(6).trim()

                        if (data == "[DONE]") {
                            Log.d(TAG, "流式传输完成")
                            break
                        }

                        if (data.isNotEmpty() && data != "{}") {
                            try {
                                // 解析JSON数据
                                val content = extractContentFromJson(data)
                                if (content.isNotEmpty()) {
                                    // 逐字发送，制造真正的流式效果
                                    for (char in content) {
                                        delay(20) // 20毫秒延迟，模拟打字效果
                                        send(char.toString())
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "解析数据失败", e)
                            }
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "流式请求失败: ${e.message}", e)
            throw e
        } finally {
            response?.close()
            Log.d(TAG, "流式连接关闭")
        }
    }

    /**
     * 使用回调方式的流式请求 - 真正的逐字效果
     */
    fun streamChatWithCallback(
        question: String,
        history: List<Message>,
        onChunk: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "开始流式请求，问题: $question")

                // 构建请求体
                val requestBody = """
                    {
                        "question": "${escapeJson(question)}",
                        "history": ${history.toJson()}
                    }
                """.trimIndent().toRequestBody("application/json".toMediaType())

                // 构建请求
                val request = Request.Builder()
                    .url("${BASE_URL}${STREAM_ENDPOINT}")
                    .post(requestBody)
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .build()

                val call = client.newCall(request)
                val response = call.execute()

                if (!response.isSuccessful) {
                    throw IOException("请求失败: ${response.code}")
                }

                Log.d(TAG, "流式连接建立成功")

                response.body?.let { body ->
                    val source = body.source()

                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break

                        if (line.startsWith("data: ")) {
                            val data = line.substring(6).trim()

                            if (data == "[DONE]") {
                                Log.d(TAG, "流式传输完成")
                                break
                            }

                            if (data.isNotEmpty() && data != "{}") {
                                try {
                                    // 解析JSON数据
                                    val content = extractContentFromJson(data)
                                    if (content.isNotEmpty()) {
                                        // 逐字发送，制造真正的流式效果
                                        for (char in content) {
                                            withContext(Dispatchers.Main) {
                                                onChunk(char.toString())
                                            }
                                            delay(20) // 20毫秒延迟，模拟打字效果
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "解析数据失败", e)
                                }
                            }
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    onComplete()
                }

            } catch (e: Exception) {
                Log.e(TAG, "流式请求失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    /**
     * 简化的流式请求 - 批量发送但保持流畅
     */
    fun streamChatSimple(
        question: String,
        history: List<Message>,
        onChunk: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "开始流式请求，问题: $question")

                // 构建请求体
                val requestBody = """
                    {
                        "question": "${escapeJson(question)}",
                        "history": ${history.toJson()}
                    }
                """.trimIndent().toRequestBody("application/json".toMediaType())

                // 构建请求
                val request = Request.Builder()
                    .url("${BASE_URL}${STREAM_ENDPOINT}")
                    .post(requestBody)
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .build()

                val call = client.newCall(request)
                val response = call.execute()

                if (!response.isSuccessful) {
                    throw IOException("请求失败: ${response.code}")
                }

                Log.d(TAG, "流式连接建立成功")

                response.body?.let { body ->
                    val source = body.source()
                    var lastUpdateTime = System.currentTimeMillis()
                    val buffer = StringBuilder()

                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break

                        if (line.startsWith("data: ")) {
                            val data = line.substring(6).trim()

                            if (data == "[DONE]") {
                                // 发送缓冲区剩余内容
                                if (buffer.isNotEmpty()) {
                                    withContext(Dispatchers.Main) {
                                        onChunk(buffer.toString())
                                    }
                                    buffer.clear()
                                }
                                Log.d(TAG, "流式传输完成")
                                break
                            }

                            if (data.isNotEmpty() && data != "{}") {
                                try {
                                    // 解析JSON数据
                                    val content = extractContentFromJson(data)
                                    if (content.isNotEmpty()) {
                                        buffer.append(content)

                                        // 如果缓冲区内容足够多，或者已经超过100毫秒，发送一次
                                        val currentTime = System.currentTimeMillis()
                                        if (buffer.length >= 3 || currentTime - lastUpdateTime >= 100) {
                                            withContext(Dispatchers.Main) {
                                                onChunk(buffer.toString())
                                            }
                                            buffer.clear()
                                            lastUpdateTime = currentTime
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "解析数据失败", e)
                                }
                            }
                        }
                    }

                    // 发送最后的内容
                    if (buffer.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            onChunk(buffer.toString())
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    onComplete()
                }

            } catch (e: Exception) {
                Log.e(TAG, "流式请求失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

    /**
     * 从JSON中提取content字段
     */
    private fun extractContentFromJson(json: String): String {
        return try {
            // 查找content字段
            val contentKey = "\"content\":\""
            val startIndex = json.indexOf(contentKey)
            if (startIndex != -1) {
                val contentStart = startIndex + contentKey.length
                var contentEnd = json.indexOf("\"", contentStart)
                if (contentEnd == -1) {
                    contentEnd = json.length
                }
                val content = json.substring(contentStart, contentEnd)
                // 处理转义字符
                content.replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\")
                    .replace("\\t", "\t")
                    .replace("\\r", "\r")
                    // 处理Unicode转义序列
                    .replace("\\u0027", "'")
                    .replace("\\u0022", "\"")
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "提取内容失败", e)
            ""
        }
    }

    /**
     * 转义JSON特殊字符 - 修复版
     */
    private fun escapeJson(text: String): String {
        val result = StringBuilder()
        for (char in text) {
            when (char) {
                '\\' -> result.append("\\\\")
                '\"' -> result.append("\\\"")
                '\n' -> result.append("\\n")
                '\r' -> result.append("\\r")
                '\t' -> result.append("\\t")
                '\b' -> result.append("\\b")  // 退格
                '\u000C' -> result.append("\\f")  // 换页
                else -> result.append(char)
            }
        }
        return result.toString()
    }

    /**
     * 将历史记录转为JSON字符串
     */
    private fun List<Message>.toJson(): String {
        return if (this.isEmpty()) {
            "[]"
        } else {
            this.joinToString(",", "[", "]") { message ->
                """{"role":"${message.role}","content":"${escapeJson(message.content)}"}"""
            }
        }
    }
}