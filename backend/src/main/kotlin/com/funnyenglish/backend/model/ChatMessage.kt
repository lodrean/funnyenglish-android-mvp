package com.funnyenglish.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "chat_messages")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val userName: String? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(nullable = false)
    val isFromUser: Boolean = true,

    @Column(nullable = false)
    val sentAt: Instant = Instant.now(),

    @Column
    val aiModel: String? = null
)
