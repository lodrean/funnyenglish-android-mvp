package com.funnyenglish.feature.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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
                    text = "Привет! Я Арчи 🤖\nЯ работаю прямо на твоём телефоне — без интернета!\n\nДля начала нужно загрузить AI-модель.",
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
            // Check if any model already exists on disk
            val modelPath = modelPathResolver.resolveModelPath()
            if (modelPath != null) {
                initModel(modelPath)
                return
            }

            // No model found — auto-select variant and show dialog
            val variant = ModelVariant.autoSelect()
            _state.update {
                it.copy(
                    showModelDownloadDialog = true,
                    modelVariant = variant
                )
            }
        }
    }

    private fun downloadModel() {
        if (_state.value.isDownloading) return

        val variant = _state.value.modelVariant ?: ModelVariant.autoSelect()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isDownloading = true,
                    modelDownloadProgress = 0f,
                    error = null,
                    modelVariant = variant
                )
            }

            val result = modelDownloader.download(variant = variant) { progress ->
                _state.update { it.copy(modelDownloadProgress = progress) }
            }

            result
                .onSuccess { downloadResult ->
                    _state.update {
                        it.copy(
                            isDownloading = false,
                            modelDownloadProgress = null,
                            modelVariant = downloadResult.variant
                        )
                    }
                    initModel(downloadResult.path)
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
                            showModelDownloadDialog = false
                        )
                    }
                }
                .onFailure { error ->
                    val errorMsg = error.message ?: ""
                    Log.e(TAG, "initModel failed: $errorMsg", error)

                    // Check if this is a GPU/OpenCL error and we haven't tried CPU yet
                    val currentVariant = _state.value.modelVariant
                    val isGpuError = errorMsg.contains("OpenCL", ignoreCase = true) ||
                            errorMsg.contains("clSetPerfHint", ignoreCase = true) ||
                            errorMsg.contains("GPU", ignoreCase = true)

                    if (isGpuError && currentVariant == ModelVariant.GPU) {
                        Log.d(TAG, "GPU model failed, falling back to CPU variant")
                        deleteModelFile(ModelVariant.GPU)
                        _state.update {
                            it.copy(
                                modelDownloadProgress = null,
                                modelVariant = ModelVariant.CPU,
                                error = "GPU-ускорение не поддерживается на этом устройстве. " +
                                        "Переключаюсь на CPU-версию (${ModelVariant.CPU.sizeLabel}). " +
                                        "Нажмите «Загрузить» для скачивания."
                            )
                        }
                        return@launch
                    }

                    // Generic failure — delete the file and show error
                    deleteModelFile(currentVariant)

                    val userFriendlyError = when {
                        errorMsg.contains("file not found", ignoreCase = true) -> {
                            "Файл модели не найден. Нажмите «Загрузить», чтобы скачать модель."
                        }
                        else -> {
                            "Не удалось запустить модель: $errorMsg. " +
                                    "Файл модели был удалён, попробуйте загрузить заново."
                        }
                    }

                    _state.update {
                        it.copy(
                            modelDownloadProgress = null,
                            showModelDownloadDialog = true,
                            error = userFriendlyError
                        )
                    }
                }
        }
    }

    private fun deleteModelFile(variant: ModelVariant?) {
        try {
            variant?.let {
                val modelFile = File(context.filesDir, it.fileName)
                if (modelFile.exists()) {
                    modelFile.delete()
                    Log.d(TAG, "Deleted model file: ${modelFile.absolutePath}")
                }
                val tempFile = File(context.filesDir, "${it.fileName}.tmp")
                if (tempFile.exists()) {
                    tempFile.delete()
                    Log.d(TAG, "Deleted temp file: ${tempFile.absolutePath}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete model file", e)
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

    companion object {
        private const val TAG = "ChatViewModel"
    }
}
