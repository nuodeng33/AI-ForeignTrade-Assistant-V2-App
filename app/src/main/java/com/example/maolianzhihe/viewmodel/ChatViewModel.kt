package com.example.maolianzhihe.viewmodel

import androidx.lifecycle.ViewModel
import com.example.maolianzhihe.data.repository.ChatRepository
import com.example.maolianzhihe.model.Message

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {
    fun streamAnswer(
        question: String,
        history: List<Message>,
        onChunk: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        repository.streamAnswer(question, history, onChunk, onComplete, onError)
    }
}
