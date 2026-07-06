package com.funnyenglish.feature.chat

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val showBatteryWarning: Boolean = false,
    val showModelDownloadDialog: Boolean = false,
    val isDownloading: Boolean = false,
    val modelDownloadProgress: Float? = null,
    val error: String? = null,
    val modelVariant: ModelVariant? = null
)
