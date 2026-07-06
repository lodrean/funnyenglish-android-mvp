package com.funnyenglish.feature.chat

sealed interface ChatAction {
    data class InputChanged(val text: String) : ChatAction
    data object SendMessage : ChatAction
    data object DismissBatteryWarning : ChatAction
    data object DismissModelDialog : ChatAction
    data object DownloadModel : ChatAction
    data object ClearHistory : ChatAction
}
