package com.example.maolianzhihe

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.maolianzhihe.databinding.ActivityCustomerServiceBinding
import com.example.maolianzhihe.model.Message
import com.example.maolianzhihe.viewmodel.ChatViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class CustomerServiceActivity : BaseActivity() {
    private lateinit var binding: ActivityCustomerServiceBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatViewModel: ChatViewModel
    private var conversationHistory = mutableListOf<Message>()
    private val isStreaming = AtomicBoolean(false)
    private var currentStreamJob: Job? = null
    private var isUserScrolling = false
    private var lastScrollTime = 0L
    private val streamBuffer = StringBuilder()
    private var lastUpdateTime = 0L
    private val updateInterval = 50L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        initTitleBar("智能客服", showBack = true, showSetting = true)
        setupChatList()
        setupInputArea()
        setupQuickQuestions()
        initBottomNav(R.id.nav_customer)
        addWelcomeMessage()
    }

    private fun setupChatList() {
        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@CustomerServiceActivity).apply { stackFromEnd = true }
            adapter = chatAdapter
            itemAnimator = null
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING,
                        RecyclerView.SCROLL_STATE_SETTLING -> {
                            isUserScrolling = true
                            lastScrollTime = System.currentTimeMillis()
                        }
                        RecyclerView.SCROLL_STATE_IDLE -> recyclerView.postDelayed({
                            if (System.currentTimeMillis() - lastScrollTime > 1000) isUserScrolling = false
                        }, 1000)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy != 0) lastScrollTime = System.currentTimeMillis()
                }
            })
        }
        chatAdapter.submitList(emptyList())
    }

    private fun setupInputArea() {
        binding.ivSend.setOnClickListener { sendMessage() }
        binding.etInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
        binding.etInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.ivSend.isEnabled = s?.toString()?.trim()?.isNotEmpty() == true && !isStreaming.get()
            }
        })
    }

    private fun setupQuickQuestions() {
        binding.tvFee.setOnClickListener { if (!isStreaming.get()) askQuickQuestion("如何计算报关费用？") }
        binding.tvSize.setOnClickListener { if (!isStreaming.get()) askQuickQuestion("集装箱尺寸规格有哪些？") }
        binding.tvContract.setOnClickListener { if (!isStreaming.get()) askQuickQuestion("外贸合同范本有哪些注意事项？") }
    }

    private fun addWelcomeMessage() {
        val messages = chatAdapter.getAllMessages().toMutableList()
        messages.add(ChatMessage("您好！我是外贸智能助手。我可以帮助您解答关于外贸流程、国际贸易术语、海关政策、单证制作等问题。请问有什么可以帮您的？", ChatType.TYPE_AI))
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

        binding.etInput.setText("")
        val messages = chatAdapter.getAllMessages().toMutableList()
        messages.add(ChatMessage(question, ChatType.TYPE_USER))
        chatAdapter.submitList(messages)
        scrollToBottom()
        startStreamingResponse(question)
    }

    private fun startStreamingResponse(question: String) {
        if (!isStreaming.compareAndSet(false, true)) return
        currentStreamJob?.cancel()
        streamBuffer.clear()
        lastUpdateTime = System.currentTimeMillis()
        chatAdapter.startStreaming(ChatType.TYPE_AI)
        scrollToBottom()

        currentStreamJob = lifecycleScope.launch {
            disableInput()
            chatViewModel.streamAnswer(
                question = question,
                history = conversationHistory,
                onChunk = { chunk ->
                    streamBuffer.append(chunk)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime >= updateInterval || streamBuffer.length >= 10) {
                        lastUpdateTime = currentTime
                        runOnUiThread { updateStreamingBuffer() }
                    }
                },
                onComplete = {
                    runOnUiThread {
                        if (streamBuffer.isNotEmpty()) updateStreamingBuffer()
                        chatAdapter.finishStreaming()
                        isStreaming.set(false)
                        enableInput()
                        val aiMessage = chatAdapter.getAllMessages().lastOrNull { it.type == ChatType.TYPE_AI }
                        if (aiMessage != null) {
                            conversationHistory.add(Message("user", question))
                            conversationHistory.add(Message("assistant", aiMessage.content))
                            if (conversationHistory.size > 20) conversationHistory = conversationHistory.takeLast(20).toMutableList()
                        }
                        scrollToBottom()
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        showError("请求失败: ${error.message}")
                        chatAdapter.finishStreaming()
                        isStreaming.set(false)
                        enableInput()
                    }
                }
            )
        }
    }

    private fun updateStreamingBuffer() {
        if (streamBuffer.isEmpty()) return
        val content = streamBuffer.toString()
        streamBuffer.clear()
        chatAdapter.appendStreamingContent(content)
        chatAdapter.flushBuffer()
        if (!isUserScrolling) scrollToBottom()
    }

    private fun disableInput() {
        binding.etInput.isEnabled = false
        binding.etInput.hint = "AI正在思考中..."
        binding.ivSend.isEnabled = false
        binding.tvFee.isEnabled = false
        binding.tvSize.isEnabled = false
        binding.tvContract.isEnabled = false
    }

    private fun enableInput() {
        binding.etInput.isEnabled = true
        binding.etInput.hint = "输入您的问题..."
        binding.ivSend.isEnabled = binding.etInput.text.toString().trim().isNotEmpty()
        binding.tvFee.isEnabled = true
        binding.tvSize.isEnabled = true
        binding.tvContract.isEnabled = true
    }

    private fun showError(message: String) {
        chatAdapter.finishStreaming()
        val messages = chatAdapter.getAllMessages().toMutableList()
        messages.add(ChatMessage("抱歉，出现错误: $message", ChatType.TYPE_AI))
        chatAdapter.submitList(messages)
        scrollToBottom()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun scrollToBottom() {
        binding.rvChat.post {
            if (chatAdapter.itemCount > 0) binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentStreamJob?.cancel()
        isStreaming.set(false)
        chatAdapter.destroy()
    }
}
