package com.funnyenglish.feature.chat

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val isLoading: Boolean = false
)
