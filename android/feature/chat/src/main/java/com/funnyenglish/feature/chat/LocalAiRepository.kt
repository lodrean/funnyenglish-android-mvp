package com.funnyenglish.feature.chat

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalAiRepository(private val context: Context) {

    @Volatile
    private var llmInference: LlmInference? = null

    val isModelLoaded: Boolean
        get() = llmInference != null

    fun initModel(modelPath: String): Result<Unit> {
        return try {
            val file = File(modelPath)
            if (!file.exists()) {
                return Result.failure(Exception("Model file not found at $modelPath"))
            }

            // Validate file size to catch incomplete/corrupted downloads.
            // Actual sizes from GitHub releases: CPU ~1.35GB, GPU ~1.35GB
            val minExpectedSize = 1_200_000_000L // ~1.2GB minimum for both variants
            if (file.length() < minExpectedSize) {
                return Result.failure(
                    Exception("Model file appears incomplete (${file.length()} bytes, expected >$minExpectedSize). Please re-download.")
                )
            }

            android.util.Log.d("LocalAiRepository", "Loading model from $modelPath (${file.length() / 1024 / 1024}MB)")

            // Total token budget: prompt + completion must fit within this limit.
            // The model file's seq_size_T is driven by this value; 512 is the
            // safe ceiling for this device (OnePlus 7 Pro, 6 GB RAM).
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(MAX_TOKENS)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            android.util.Log.d("LocalAiRepository", "Model loaded successfully")
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.Default) {
        val inference = llmInference ?: throw IllegalStateException("Model not initialized")
        inference.generateResponse(prompt)
    }

    fun generateResponseAsync(prompt: String, onResult: (String, Boolean) -> Unit) {
        val inference = llmInference ?: return
        inference.generateResponseAsync(
            prompt,
            ProgressListener { partialResult, done ->
                onResult(partialResult, done)
            }
        )
    }

    /**
     * Returns the number of tokens the given text would consume.
     * Requires the model to be loaded. Falls back to rough character heuristic on failure.
     */
    fun sizeInTokens(text: String): Int {
        val inference = llmInference ?: throw IllegalStateException("Model not initialized")
        return try {
            inference.sizeInTokens(text)
        } catch (e: Exception) {
            // Fallback: ~4 chars per token for Latin, ~2 for Cyrillic — use conservative 3
            (text.length / 3).coerceAtLeast(1)
        }
    }

    companion object {
        const val MODEL_FILENAME_GPU = "gemma-2b-it-gpu-int4.bin"
        const val MODEL_FILENAME_CPU = "gemma-2b-it-cpu-int4.bin"
        const val MAX_TOKENS = 512
    }
}
