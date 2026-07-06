package com.funnyenglish.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val isLoading: Boolean = false,
)
