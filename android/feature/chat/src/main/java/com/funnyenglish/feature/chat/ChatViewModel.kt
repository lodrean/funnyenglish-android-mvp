package com.funnyenglish.feature.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.util.UUID

class ChatViewModel(
    private val context: Context,
    private val localAi: LocalAiRepository,
    private val modelPathResolver: ModelPathResolver,
    private val modelDownloader: ModelDownloader,
    private val chatHistory: ChatHistoryRepository
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
        loadHistory()
        try {
            checkModelStatus()
        } catch (e: Throwable) {
            Log.e(TAG, "checkModelStatus crashed in init", e)
            _state.update {
                it.copy(error = "🚫 AI-чат недоступен на этом устройстве.")
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = chatHistory.loadMessages()
            if (history.isNotEmpty()) {
                _state.update { it.copy(messages = history) }
            }
        }
    }

    private fun saveHistory() {
        viewModelScope.launch {
            chatHistory.saveMessages(_state.value.messages)
        }
    }

    fun onAction(action: ChatAction) {
        when (action) {
            is ChatAction.InputChanged -> _state.update { it.copy(inputText = action.text) }
            ChatAction.SendMessage -> sendMessage()
            ChatAction.DismissBatteryWarning -> _state.update { it.copy(showBatteryWarning = false) }
            ChatAction.DismissModelDialog -> _state.update { it.copy(showModelDownloadDialog = false) }
            ChatAction.DownloadModel -> downloadModel()
            ChatAction.ClearHistory -> clearHistory()
        }
    }

    private fun checkModelStatus() {
        if (!localAi.isModelLoaded) {
            val modelPath = modelPathResolver.resolveModelPath()
            if (modelPath != null) {
                initModel(modelPath)
                return
            }

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
        val handler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Unhandled exception in initModel coroutine", throwable)
            _state.update {
                it.copy(
                    modelDownloadProgress = null,
                    error = "🚫 AI-чат недоступен на этом устройстве."
                )
            }
        }
        viewModelScope.launch(handler) {
            _state.update { it.copy(modelDownloadProgress = 0.95f) }
            val result = try {
                localAi.initModel(modelPath)
            } catch (e: Throwable) {
                Result.failure(e)
            }
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

                    val currentVariant = _state.value.modelVariant
                    val isUnsupportedDevice = error is UnsatisfiedLinkError ||
                            error is NoClassDefFoundError ||
                            errorMsg.contains("libllm_inference_engine_jni", ignoreCase = true) ||
                            errorMsg.contains("dlopen failed", ignoreCase = true)

                    if (isUnsupportedDevice) {
                        _state.update {
                            it.copy(
                                modelDownloadProgress = null,
                                showModelDownloadDialog = false,
                                error = "🚫 AI-чат не поддерживается на этом устройстве.\n" +
                                        "Требуется процессор ARM (arm64-v8a или armeabi-v7a)."
                            )
                        }
                        return@launch
                    }

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
        if (_state.value.isLoading) return
        if (!localAi.isModelLoaded) {
            _state.update { it.copy(showModelDownloadDialog = true) }
            return
        }

        // Battery warning — non-blocking, just show a snackbar-style warning
        val batteryLow = try {
            BatteryMonitor.isBatteryLow(context) && !BatteryMonitor.isCharging(context)
        } catch (e: Exception) {
            Log.e(TAG, "Battery check failed", e)
            false
        }

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isFromUser = true
        )
        val loadingId = "loading-${UUID.randomUUID()}"
        val loadingMessage = ChatMessage(
            id = loadingId,
            text = "",
            isFromUser = false,
            isLoading = true
        )

        _state.update {
            it.copy(
                messages = it.messages + userMessage + loadingMessage,
                inputText = "",
                isLoading = true,
                showBatteryWarning = batteryLow
            )
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Generating response for: $text")
                val prompt = buildPrompt(text)

                val response = withContext(Dispatchers.IO) {
                    withTimeout(120_000) { // 2 minutes timeout for CPU inference
                        localAi.generateResponse(prompt)
                    }
                }

                val cleanedResponse = response.replace(prompt, "").trim()
                Log.d(TAG, "Response received: ${cleanedResponse.take(100)}")

                _state.update { state ->
                    val filtered = state.messages.filter { !it.isLoading }
                    state.copy(
                        messages = filtered + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = cleanedResponse.ifBlank { "Хм, я что-то запутался... 🤔" },
                            isFromUser = false
                        ),
                        isLoading = false
                    )
                }
                saveHistory()
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "Response generation timed out", e)
                _state.update { state ->
                    val filtered = state.messages.filter { !it.isLoading }
                    state.copy(
                        messages = filtered + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = "⏳ Арчи думает слишком долго... Попробуй задать вопрос покороче или подключи зарядку.",
                            isFromUser = false
                        ),
                        isLoading = false
                    )
                }
                saveHistory()
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "OOM during inference", e)
                _state.update { state ->
                    val filtered = state.messages.filter { !it.isLoading }
                    state.copy(
                        messages = filtered + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = "😰 Не хватает памяти для работы модели. Закрой другие приложения и попробуй снова.",
                            isFromUser = false
                        ),
                        isLoading = false
                    )
                }
                saveHistory()
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response", e)
                _state.update { state ->
                    val filtered = state.messages.filter { !it.isLoading }
                    state.copy(
                        messages = filtered + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = "Ой, ошибка: ${e.message}",
                            isFromUser = false
                        ),
                        isLoading = false
                    )
                }
                saveHistory()
            } catch (e: Throwable) {
                // UnsatisfiedLinkError, NoClassDefFoundError, etc.
                Log.e(TAG, "Critical error generating response", e)
                _state.update { state ->
                    val filtered = state.messages.filter { !it.isLoading }
                    state.copy(
                        messages = filtered + ChatMessage(
                            id = UUID.randomUUID().toString(),
                            text = "🚫 AI-чат недоступен на этом устройстве.",
                            isFromUser = false
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            chatHistory.clear()
            _state.update {
                it.copy(
                    messages = listOf(
                        ChatMessage(
                            id = "welcome",
                            text = "Привет! Я Арчи 🤖\nЯ работаю прямо на твоём телефоне — без интернета!\n\nДля начала нужно загрузить AI-модель.",
                            isFromUser = false
                        )
                    )
                )
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
