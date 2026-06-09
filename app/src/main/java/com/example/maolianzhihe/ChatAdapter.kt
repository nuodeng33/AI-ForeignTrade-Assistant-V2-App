package com.example.maolianzhihe

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.maolianzhihe.databinding.ItemChatAiBinding
import com.example.maolianzhihe.databinding.ItemChatUserBinding

class ChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_AI = 0
        private const val VIEW_TYPE_USER = 1
        private const val BUFFER_FLUSH_THRESHOLD = 3 // 每3个字符刷新一次
    }

    private var messages: List<ChatMessage> = emptyList()

    // 流式输出缓冲区
    private var streamingIndex = -1
    private val streamingBuffer = StringBuilder()
    private var isStreaming = false

    // 使用Handler在主线程更新UI
    private val mainHandler = Handler(Looper.getMainLooper())

    // 提交完整列表（用于用户消息等）
    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        streamingIndex = -1
        streamingBuffer.clear()
        isStreaming = false
        notifyDataSetChanged()
    }

    // 开始流式输出
    fun startStreaming(type: ChatType = ChatType.TYPE_AI) {
        if (isStreaming) finishStreaming()

        val newList = messages.toMutableList()
        newList.add(ChatMessage("", type))
        messages = newList
        streamingIndex = messages.size - 1
        isStreaming = true

        notifyItemInserted(streamingIndex)
    }

    // 追加流式内容
    fun appendStreamingContent(content: String) {
        if (!isStreaming || streamingIndex < 0) return

        streamingBuffer.append(content)

        // 当缓冲区达到阈值时刷新
        if (streamingBuffer.length >= BUFFER_FLUSH_THRESHOLD) {
            flushBuffer()
        }
    }

    // 完成流式输出
    fun finishStreaming() {
        if (streamingBuffer.isNotEmpty()) {
            flushBuffer()
        }
        isStreaming = false
        streamingIndex = -1
        streamingBuffer.clear()
    }

    // 刷新缓冲区 - 使用Handler确保在主线程更新
    fun flushBuffer() {
        if (streamingIndex >= 0 && streamingIndex < messages.size && streamingBuffer.isNotEmpty()) {
            val currentMsg = messages[streamingIndex]
            val newContent = currentMsg.content + streamingBuffer.toString()

            val newList = messages.toMutableList()
            newList[streamingIndex] = ChatMessage(newContent, currentMsg.type)
            messages = newList

            // 在主线程更新UI
            mainHandler.post {
                notifyItemChanged(streamingIndex)
            }

            streamingBuffer.clear()
        }
    }

    // 获取所有消息（用于保存历史记录）
    fun getAllMessages(): List<ChatMessage> {
        return messages
    }

    // 清空所有消息
    fun clear() {
        messages = emptyList()
        streamingIndex = -1
        streamingBuffer.clear()
        isStreaming = false
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            ChatType.TYPE_AI -> VIEW_TYPE_AI
            ChatType.TYPE_USER -> VIEW_TYPE_USER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_AI -> AiViewHolder(
                ItemChatAiBinding.inflate(inflater, parent, false)
            )
            VIEW_TYPE_USER -> UserViewHolder(
                ItemChatUserBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("未知的消息类型")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        // 设置固定最小高度
        holder.itemView.minimumHeight = 60

        when (holder) {
            is AiViewHolder -> holder.bind(message)
            is UserViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    // AI消息ViewHolder
    class AiViewHolder(private val binding: ItemChatAiBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvAiContent.text = message.content

            // 如果是光标，添加闪烁动画
            if (message.content == "▎") {
                binding.tvAiContent.alpha = 0.5f
                startCursorBlink(binding.tvAiContent)
            } else {
                binding.tvAiContent.alpha = 1.0f
                binding.tvAiContent.clearAnimation()
            }
        }

        private fun startCursorBlink(textView: android.widget.TextView) {
            textView.animate()
                .alpha(0.2f)
                .setDuration(500)
                .withEndAction {
                    textView.animate()
                        .alpha(0.8f)
                        .setDuration(500)
                        .withEndAction {
                            if (textView.text == "▎") {
                                startCursorBlink(textView)
                            }
                        }
                        .start()
                }
                .start()
        }
    }

    // 用户消息ViewHolder
    class UserViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.tvUserContent.text = message.content
        }
    }

    // 清理资源
    fun destroy() {
        mainHandler.removeCallbacksAndMessages(null)
    }
}