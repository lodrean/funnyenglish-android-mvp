package com.funnyenglish.backend.repository

import com.funnyenglish.backend.model.ChatMessage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long> {
    fun findByUserIdOrderBySentAtDesc(userId: Long): List<ChatMessage>
    fun findTop50ByOrderBySentAtDesc(): List<ChatMessage>
}
