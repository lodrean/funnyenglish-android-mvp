package com.funnyenglish.feature.chat

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.File

class LocalAiRepository(private val context: Context) {

    private var llmInference: LlmInference? = null
    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(extraBufferCapacity = 64)
    val partialResults: Flow<Pair<String, Boolean>> = _partialResults.asSharedFlow()

    val isModelLoaded: Boolean
        get() = llmInference != null

    fun initModel(modelPath: String): Result<Unit> {
        return try {
            val file = File(modelPath)
            if (!file.exists()) {
                return Result.failure(Exception("Model file not found at $modelPath"))
            }

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .setTopK(40)
                .setTemperature(0.7f)
                .setResultListener { partialResult, done ->
                    _partialResults.tryEmit(partialResult to done)
                }
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.Default) {
        val inference = llmInference ?: throw IllegalStateException("Model not initialized")
        inference.generateResponse(prompt)
    }

    fun generateResponseAsync(prompt: String) {
        val inference = llmInference ?: return
        inference.generateResponseAsync(prompt)
    }

    companion object {
        const val MODEL_FILENAME = "gemma-2b-it-cpu-int4.bin"
    }
}
