package com.example.maolianzhihe.data.repository

import com.example.maolianzhihe.model.Message
import com.example.maolianzhihe.network.StreamService

class ChatRepository(
    private val streamService: StreamService = StreamService()
) {
    fun streamAnswer(
        question: String,
        history: List<Message>,
        onChunk: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        streamService.streamChatSimple(
            question = question,
            history = history,
            onChunk = onChunk,
            onComplete = onComplete,
            onError = onError
        )
    }
}
