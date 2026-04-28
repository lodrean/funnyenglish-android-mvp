package com.funnyenglish.feature.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val context: Context,
    private val localAi: LocalAiRepository,
    private val modelPathResolver: ModelPathResolver,
    private val modelDownloader: ModelDownloader
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChatState(
            messages = listOf(
                ChatMessage(
                    id = "welcome",
                    text = "Привет! Я Арчи 🤖\nЯ работаю прямо на твоём телефоне — без интернета!\n\nНо для начала нужно загрузить модель (~1.3GB).",
                    isFromUser = false
                )
            )
        )
    )
    val state: StateFlow<ChatState> = _state.asStateFlow()

    init {
        checkModelStatus()
    }

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.InputChanged -> _state.update { it.copy(inputText = action.text) }
            ChatAction.SendMessage -> sendMessage()
            ChatAction.DismissBatteryWarning -> _state.update { it.copy(showBatteryWarning = false) }
            ChatAction.DismissModelDialog -> _state.update { it.copy(showModelDownloadDialog = false) }
            ChatAction.DownloadModel -> downloadModel()
        }
    }

    private fun checkModelStatus() {
        if (!localAi.isModelLoaded) {
            val modelPath = modelPathResolver.resolveModelPath()
            if (modelPath == null) {
                _state.update { it.copy(showModelDownloadDialog = true) }
            } else {
                initModel(modelPath)
            }
        }
    }

    private fun downloadModel() {
        if (_state.value.isDownloading) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isDownloading = true,
                    modelDownloadProgress = 0f,
                    error = null
                )
            }

            val result = modelDownloader.download { progress ->
                _state.update { it.copy(modelDownloadProgress = progress) }
            }

            result
                .onSuccess { path ->
                    _state.update {
                        it.copy(
                            isDownloading = false,
                            modelDownloadProgress = null
                        )
                    }
                    initModel(path)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isDownloading = false,
                            modelDownloadProgress = null,
                            error = "Ошибка загрузки: ${error.message}. Проверь подключение к интернету и попробуй снова."
                        )
                    }
                }
        }
    }

    private fun initModel(modelPath: String) {
        viewModelScope.launch {
            _state.update { it.copy(modelDownloadProgress = 0.95f) }
            val result = localAi.initModel(modelPath)
            result
                .onSuccess {
                    _state.update {
                        it.copy(
                            modelDownloadProgress = null,
                            showModelDownloadDialog = false,
                            messages = it.messages + ChatMessage(
                                id = UUID.randomUUID().toString(),
                                text = "✅ Модель загружена! Можешь писать мне что угодно.",
                                isFromUser = false
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            modelDownloadProgress = null,
                            error = "Не удалось инициализировать модель: ${error.message}"
                        )
                    }
                }
        }
    }

    private fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank()) return
        if (!localAi.isModelLoaded) {
            _state.update { it.copy(showModelDownloadDialog = true) }
            return
        }

        // Battery warning
        if (BatteryMonitor.isBatteryLow(context) && !BatteryMonitor.isCharging(context)) {
            _state.update { it.copy(showBatteryWarning = true) }
            return
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isFromUser = true
        )
        val loadingMessage = ChatMessage(
            id = "loading",
            text = "",
            isFromUser = false,
            isLoading = true
        )

        _state.update {
            it.copy(
                messages = it.messages + userMessage + loadingMessage,
                inputText = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val prompt = buildPrompt(text)
                val response = localAi.generateResponse(prompt)
                val cleanedResponse = response.replace(prompt, "").trim()

                _state.update { state ->
                    val filtered = state.messages.filter { it.id != "loading" }
                    state.copy(
                        messages = filtered + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = cleanedResponse.ifBlank { "Хм, я что-то запутался... 🤔" },
                            isFromUser = false
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { state ->
                    val filtered = state.messages.filter { it.id != "loading" }
                    state.copy(
                        messages = filtered + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = "Ой, ошибка: ${e.message}",
                            isFromUser = false
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun buildPrompt(userMessage: String): String {
        return """Ты — Арчи, весёлый AI-компаньон для изучения английского. Обращайся на "ты", дружелюбно, с лёгкой иронией. Давай короткие ответы (2-4 предложения). Если пользователь пишет по-английски — исправляй ошибки мягко. Если по-русски — переводи и объясняй.

Пользователь: $userMessage
Арчи:""".trimIndent()
    }
}
