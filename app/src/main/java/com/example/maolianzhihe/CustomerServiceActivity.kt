package com.example.maolianzhihe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maolianzhihe.databinding.ActivityCustomerServiceBinding
import com.example.maolianzhihe.model.Message
import com.example.maolianzhihe.network.StreamService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class CustomerServiceActivity : BaseActivity() {

    private lateinit var binding: ActivityCustomerServiceBinding
    private lateinit var chatAdapter: ChatAdapter
    private val streamService = StreamService()
    private var conversationHistory = mutableListOf<Message>()
    private val isStreaming = AtomicBoolean(false)
    private var currentStreamJob: Job? = null

    // 滚动控制
    private var isUserScrolling = false
    private var lastScrollTime = 0L

    // 流式输出缓冲区
    private val streamBuffer = StringBuilder()
    private var lastUpdateTime = 0L
    private val UPDATE_INTERVAL = 50L // 50ms更新一次

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化标题栏
        initTitleBar("智能客服", showBack = true, showSetting = true)

        // 初始化聊天列表
        setupChatList()

        // 初始化输入框和发送按钮
        setupInputArea()

        // 初始化快捷问题
        setupQuickQuestions()

        // 初始化底部导航
        initBottomNav(R.id.nav_customer)

        // 添加欢迎消息
        addWelcomeMessage()
    }

    private fun setupChatList() {
        chatAdapter = ChatAdapter()

        // 使用简单的配置
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@CustomerServiceActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
            itemAnimator = null // 禁用动画减少抖动

            // 滚动监听
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING,
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                            isUserScrolling = true
                            lastScrollTime = System.currentTimeMillis()
                        }
                        RecyclerView.SCROLL_STATE_IDLE -> {
                            // 停止滚动1秒后，重置用户滚动标志
                            recyclerView.postDelayed({
                                if (System.currentTimeMillis() - lastScrollTime > 1000) {
                                    isUserScrolling = false
                                }
                            }, 1000)
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy != 0) {
                        lastScrollTime = System.currentTimeMillis()
                    }
                }
            })
        }

        // 提交初始空列表
        chatAdapter.submitList(emptyList())
    }

    private fun setupInputArea() {
        // 发送按钮点击事件
        binding.ivSend.setOnClickListener {
            sendMessage()
        }

        // 输入框回车发送
        binding.etInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        // 输入框内容监听，控制发送按钮状态
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.ivSend.isEnabled = s?.toString()?.trim()?.isNotEmpty() == true && !isStreaming.get()
            }
        })
    }

    private fun setupQuickQuestions() {
        binding.tvFee.setOnClickListener {
            if (!isStreaming.get()) {
                askQuickQuestion("如何计算报关费用？")
            }
        }

        binding.tvSize.setOnClickListener {
            if (!isStreaming.get()) {
                askQuickQuestion("集装箱尺寸规格有哪些？")
            }
        }

        binding.tvContract.setOnClickListener {
            if (!isStreaming.get()) {
                askQuickQuestion("外贸合同范本有哪些注意事项？")
            }
        }
    }

    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            "您好！我是外贸智能助手。我可以帮助您解答关于外贸流程、国际贸易术语、海关政策、单证制作等问题。请问有什么可以帮您的？",
            ChatType.TYPE_AI
        )

        val messages = chatAdapter.getAllMessages().toMutableList()
        messages.add(welcomeMessage)
        chatAdapter.submitList(messages)
        scrollToBottom()
    }

    private fun askQuickQuestion(question: String) {
        binding.etInput.setText(question)
        sendMessage()
    }

    private fun sendMessage() {
        val question = binding.etInput.text.toString().trim()
        if (question.isEmpty() || isStreaming.get()) return

        // 清空输入框
        binding.etInput.setText("")

        // 添加用户消息
        val messages = chatAdapter.getAllMessages().toMutableList()
        messages.add(ChatMessage(question, ChatType.TYPE_USER))
        chatAdapter.submitList(messages)

        // 滚动到底部
        scrollToBottom()

        // 开始流式响应
        startStreamingResponse(question)
    }

    /**
     * 开始流式响应 - 使用缓冲方式
     */
    private fun startStreamingResponse(question: String) {
        if (!isStreaming.compareAndSet(false, true)) {
            return
        }

        // 取消之前的流式任务
        currentStreamJob?.cancel()

        // 清空缓冲区
        streamBuffer.clear()
        lastUpdateTime = System.currentTimeMillis()

        // 开始AI流式输出
        chatAdapter.startStreaming(ChatType.TYPE_AI)

        // 滚动到底部
        scrollToBottom()

        currentStreamJob = lifecycleScope.launch {
            try {
                // 禁用输入和发送按钮
                disableInput()

                // 使用简化的流式请求
                streamService.streamChatSimple(
                    question = question,
                    history = conversationHistory,
                    onChunk = { chunk ->
                        // 添加到缓冲区
                        streamBuffer.append(chunk)

                        // 检查是否需要更新UI
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastUpdateTime >= UPDATE_INTERVAL || streamBuffer.length >= 10) {
                            lastUpdateTime = currentTime

                            runOnUiThread {
                                updateStreamingBuffer()
                            }
                        }
                    },
                    onComplete = {
                        runOnUiThread {
                            // 发送缓冲区剩余内容
                            if (streamBuffer.isNotEmpty()) {
                                updateStreamingBuffer()
                            }

                            // ✅ 完成流式输出
                            chatAdapter.finishStreaming()
                            isStreaming.set(false)
                            enableInput()

                            // 更新历史记录
                            val allMessages = chatAdapter.getAllMessages()
                            val aiMessage = allMessages.lastOrNull { it.type == ChatType.TYPE_AI }

                            if (aiMessage != null) {
                                conversationHistory.add(Message("user", question))
                                conversationHistory.add(Message("assistant", aiMessage.content))

                                // 限制历史记录长度
                                if (conversationHistory.size > 20) {
                                    conversationHistory = conversationHistory.takeLast(20).toMutableList()
                                }

                                // 显示完成提示
                                Toast.makeText(this@CustomerServiceActivity, "回答完成", Toast.LENGTH_SHORT).show()
                            }

                            // 滚动到底部
                            scrollToBottom()
                        }
                    },
                    onError = { error ->
                        runOnUiThread {
                            showError("请求失败: ${error.message}")

                            // ✅ 完成流式输出
                            chatAdapter.finishStreaming()

                            isStreaming.set(false)
                            enableInput()
                        }
                    }
                )

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showError("请求失败: ${e.message}")

                    // ✅ 完成流式输出
                    chatAdapter.finishStreaming()

                    isStreaming.set(false)
                    enableInput()
                }
            }
        }
    }

    /**
     * 更新流式缓冲区
     */
    /**
     * 更新流式缓冲区
     */
    private fun updateStreamingBuffer() {
        if (streamBuffer.isEmpty()) return

        // 获取缓冲区内容并清空
        val content = streamBuffer.toString()
        streamBuffer.clear()

        // 更新适配器
        chatAdapter.appendStreamingContent(content)

        // 强制刷新缓冲区（确保即使缓冲区未满也更新）
        chatAdapter.flushBuffer()

        // 条件滚动（只在用户没有手动滚动时）
        if (!isUserScrolling) {
            scrollToBottom()
        }
    }

    /**
     * 禁用输入
     */
    private fun disableInput() {
        runOnUiThread {
            binding.etInput.isEnabled = false
            binding.etInput.hint = "AI正在思考中..."
            binding.ivSend.isEnabled = false
            binding.tvFee.isEnabled = false
            binding.tvSize.isEnabled = false
            binding.tvContract.isEnabled = false
        }
    }

    /**
     * 启用输入
     */
    private fun enableInput() {
        runOnUiThread {
            binding.etInput.isEnabled = true
            binding.etInput.hint = "输入您的问题..."
            binding.ivSend.isEnabled = true
            binding.tvFee.isEnabled = true
            binding.tvSize.isEnabled = true
            binding.tvContract.isEnabled = true
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            // 完成当前的流式输出
            chatAdapter.finishStreaming()

            // 显示错误消息
            val messages = chatAdapter.getAllMessages().toMutableList()
            messages.add(ChatMessage("抱歉，出现错误: $message", ChatType.TYPE_AI))
            chatAdapter.submitList(messages)

            // 滚动到底部
            scrollToBottom()

            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun scrollToBottom() {
        binding.rvChat.post {
            if (chatAdapter.itemCount > 0) {
                binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentStreamJob?.cancel()
        isStreaming.set(false)
        chatAdapter.destroy()
    }
}