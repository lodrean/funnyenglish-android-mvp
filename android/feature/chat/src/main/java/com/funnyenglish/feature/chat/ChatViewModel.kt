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
    private val chatContext: ChatContextManager
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
        viewModelScope.launch {
            chatContext.load()
            val history = chatContext.messages.value
            if (history.isNotEmpty()) {
                _state.update { it.copy(messages = history) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                checkModelStatus()
            } catch (e: Throwable) {
                Log.e(TAG, "checkModelStatus crashed in init", e)
                _state.update {
                    it.copy(error = "🚫 AI-чат недоступен на этом устройстве.")
                }
            }
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
            // Always use CPU variant to avoid native crashes from broken GPU drivers
            val variant = ModelVariant.CPU
            val modelPath = modelPathResolver.resolveModelPath(variant)
            if (modelPath != null) {
                initModel(modelPath)
                return
            }

            // Clean up old incompatible GPU model if it exists
            deleteModelFile(ModelVariant.GPU)

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
                withContext(Dispatchers.Default) {
                    localAi.initModel(modelPath)
                }
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

        viewModelScope.launch {
            try {
                // Persist user message and update UI
                val userMsg = chatContext.addUserMessage(text)
                val loadingId = "loading-${UUID.randomUUID()}"
                val loadingMessage = ChatMessage(
                    id = loadingId,
                    text = "",
                    isFromUser = false,
                    isLoading = true
                )
                _state.update {
                    it.copy(
                        messages = it.messages + userMsg + loadingMessage,
                        inputText = "",
                        isLoading = true,
                        showBatteryWarning = batteryLow
                    )
                }

                // Memory safety check before heavy inference
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                val memoryInfo = android.app.ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memoryInfo)
                if (memoryInfo.availMem < 400 * 1024 * 1024L) {
                    Log.w(TAG, "Low memory before inference: ${memoryInfo.availMem / 1024 / 1024}MB available")
                    val errorMsg = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "😰 Недостаточно свободной памяти. Закрой другие приложения и попробуй снова.",
                        isFromUser = false
                    )
                    _state.update { state ->
                        state.copy(
                            messages = state.messages.filter { !it.isLoading } + errorMsg,
                            isLoading = false
                        )
                    }
                    return@launch
                }

                Log.d(TAG, "Generating response for: $text")
                val prompt = chatContext.buildPrompt(SYSTEM_PROMPT, text)

                val response = withContext(Dispatchers.IO) {
                    withTimeout(120_000) { // 2 minutes timeout for CPU inference
                        localAi.generateResponse(prompt)
                    }
                }

                val cleanedResponse = sanitizeResponse(response)
                Log.d(TAG, "Response received: ${cleanedResponse.take(100)}")

                // Persist model response and update UI
                val modelMsg = chatContext.addModelMessage(
                    cleanedResponse.ifBlank { "Хм, я что-то запутался... 🤔" }
                )
                _state.update { state ->
                    state.copy(
                        messages = state.messages.filter { !it.isLoading } + modelMsg,
                        isLoading = false
                    )
                }
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e(TAG, "Response generation timed out", e)
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "⏳ Арчи думает слишком долго... Попробуй задать вопрос покороче или подключи зарядку.",
                    isFromUser = false
                )
                _state.update { state ->
                    state.copy(
                        messages = state.messages.filter { !it.isLoading } + errorMsg,
                        isLoading = false
                    )
                }
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "OOM during inference", e)
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "😰 Не хватает памяти для работы модели. Закрой другие приложения и попробуй снова.",
                    isFromUser = false
                )
                _state.update { state ->
                    state.copy(
                        messages = state.messages.filter { !it.isLoading } + errorMsg,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response", e)
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Ой, ошибка: ${e.message}",
                    isFromUser = false
                )
                _state.update { state ->
                    state.copy(
                        messages = state.messages.filter { !it.isLoading } + errorMsg,
                        isLoading = false
                    )
                }
            } catch (e: Throwable) {
                // UnsatisfiedLinkError, NoClassDefFoundError, etc.
                Log.e(TAG, "Critical error generating response", e)
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "🚫 AI-чат недоступен на этом устройстве.",
                    isFromUser = false
                )
                _state.update { state ->
                    state.copy(
                        messages = state.messages.filter { !it.isLoading } + errorMsg,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            chatContext.clear()
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

    private fun sanitizeResponse(raw: String): String {
        var text = raw.trim()

        // Cut at Gemma end-of-turn marker
        text = text.substringBefore("<end_of_turn>").trim()

        // Cut at common hallucination patterns where the model starts generating next turns
        text = text.substringBefore("\nUser:").trim()
        text = text.substringBefore("\nПользователь:").trim()
        text = text.substringBefore("\nАрчи:").trim()
        text = text.substringBefore("\n<start_of_turn>").trim()
        text = text.substringBefore("\nmodel\n").trim()

        // Remove any accidental prompt leakage
        text = text.replace("<start_of_turn>model", "").trim()

        return text.ifBlank { "Хм, я что-то запутался... 🤔" }
    }

    companion object {
        private const val TAG = "ChatViewModel"
        private const val SYSTEM_PROMPT = "You are Archie, a cheerful AI companion for learning English. Speak in a friendly, slightly ironic tone. Keep answers very short (2-4 sentences). If the user writes in English, gently correct mistakes. If in Russian, translate to English and explain briefly."
    }
}
