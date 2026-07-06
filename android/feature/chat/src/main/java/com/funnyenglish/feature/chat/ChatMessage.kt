package com.funnyenglish.feature.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val isLoading: Boolean = false
)
