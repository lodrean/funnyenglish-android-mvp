package com.funnyenglish.backend.service

import com.funnyenglish.backend.dto.ChatMessageDto
import com.funnyenglish.backend.dto.SendMessageRequest
import com.funnyenglish.backend.model.ChatMessage
import com.funnyenglish.backend.repository.ChatMessageRepository
import org.springframework.stereotype.Service

@Service
class ChatService(private val chatMessageRepository: ChatMessageRepository) {

    fun saveMessage(request: SendMessageRequest): ChatMessageDto {
        val message = ChatMessage(
            userId = request.userId,
            userName = request.userName,
            content = request.content,
            isFromUser = request.isFromUser
        )
        return chatMessageRepository.save(message).toDto()
    }

    fun getUserMessages(userId: Long): List<ChatMessageDto> =
        chatMessageRepository.findByUserIdOrderBySentAtDesc(userId).map { it.toDto() }

    fun getRecentMessages(): List<ChatMessageDto> =
        chatMessageRepository.findTop50ByOrderBySentAtDesc().map { it.toDto() }

    fun getMessageStats(): MessageStats {
        return MessageStats(
            total = chatMessageRepository.count()
        )
    }

    data class MessageStats(val total: Long)
}

fun ChatMessage.toDto(): ChatMessageDto = ChatMessageDto(
    id = id,
    userId = userId,
    userName = userName,
    content = content,
    isFromUser = isFromUser,
    sentAt = sentAt
)
