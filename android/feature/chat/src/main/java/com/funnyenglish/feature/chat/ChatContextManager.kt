package com.funnyenglish.feature.chat

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Manages conversation context for LLM inference with sliding-window overflow handling.
 *
 * Responsibilities:
 * - Maintains persistent conversation history via [ChatHistoryRepository]
 * - Builds prompts in Gemma 2B IT chat format with full context
 * - Auto-trims oldest message pairs when context window overflows
 * - Supports token-aware trimming via [LocalAiRepository.sizeInTokens] when model is loaded
 * - Filters out UI-only artifacts (loading messages, welcome screen)
 * - Handles incomplete turns gracefully (orphaned user messages from crashes)
 *
 * Token budget constraints (tied to [LocalAiRepository.MAX_TOKENS] = 512):
 *   - The entire prompt + model completion must fit within 512 tokens.
 *   - We reserve ~130 tokens for the model response.
 *   - Therefore the prompt is capped at ~380 tokens.
 */
class ChatContextManager(
    private val chatHistoryRepository: ChatHistoryRepository,
    private val localAi: LocalAiRepository? = null,
    private val maxContextMessages: Int = 5,          // 5 user-model pairs max
    private val maxContextChars: Int = 1800,          // rough char limit
    private val maxContextTokens: Int = 380,          // hard token limit for the prompt
) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    suspend fun load() {
        val loaded = chatHistoryRepository.loadMessages()
            .filter { !it.isLoading && it.id != "welcome" && it.text.isNotBlank() }
        _messages.value = loaded
        Log.d(TAG, "Loaded ${loaded.size} messages from persistent storage")
    }

    suspend fun addUserMessage(text: String): ChatMessage {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isFromUser = true
        )
        appendAndSave(msg)
        return msg
    }

    suspend fun addModelMessage(text: String): ChatMessage {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isFromUser = false
        )
        appendAndSave(msg)
        return msg
    }

    suspend fun clear() {
        _messages.value = emptyList()
        chatHistoryRepository.clear()
        Log.d(TAG, "Conversation context cleared")
    }

    /**
     * Builds a prompt in Gemma 2B IT chat format including sanitized conversation history.
     *
     * Incomplete turns (orphaned user messages without model responses, e.g. from a previous crash)
     * are filtered out to keep the prompt structure valid.
     *
     * Trimming strategy (applied in order):
     *   1. Drop oldest message pairs if count exceeds [maxContextMessages].
     *   2. Hard character cap ([maxContextChars]).
     *   3. Token cap ([maxContextTokens]) — requires loaded model.
     *   4. Nuclear fallback: drop entire history, keep only current turn.
     */
    fun buildPrompt(systemPrompt: String, newUserMessage: String): String {
        val history = sanitizeHistory(_messages.value)
            .takeLast(maxContextMessages * 2)

        val sb = StringBuilder()

        // Past conversation in Gemma format
        for (msg in history) {
            if (msg.isFromUser) {
                sb.append("<start_of_turn>user\n${msg.text}<end_of_turn>\n")
            } else {
                sb.append("<start_of_turn>model\n${msg.text}<end_of_turn>\n")
            }
        }

        // Current turn with system prompt
        sb.append("<start_of_turn>user\n")
        sb.append(systemPrompt)
        sb.append("\n\n")
        sb.append(newUserMessage)
        sb.append("<end_of_turn>\n")
        sb.append("<start_of_turn>model\n")

        var prompt = sb.toString()

        // 1. Character-based overflow guard
        if (prompt.length > maxContextChars) {
            Log.w(TAG, "Prompt exceeds $maxContextChars chars (${prompt.length}), trimming history")
            prompt = trimByChars(prompt, systemPrompt, newUserMessage)
        }

        // 2. Token-aware overflow guard (optional, requires loaded model)
        if (localAi?.isModelLoaded == true) {
            try {
                val tokens = localAi.sizeInTokens(prompt)
                if (tokens > maxContextTokens) {
                    Log.w(TAG, "Prompt exceeds $maxContextTokens tokens ($tokens), aggressive trimming")
                    prompt = aggressiveTrim(prompt, systemPrompt, newUserMessage)

                    // Re-check after aggressive trim
                    val trimmedTokens = localAi.sizeInTokens(prompt)
                    if (trimmedTokens > maxContextTokens) {
                        Log.e(TAG, "Still over limit ($trimmedTokens > $maxContextTokens), dropping all history")
                        prompt = buildCurrentTurn(systemPrompt, newUserMessage)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Token counting failed, keeping char-trimmed prompt", e)
            }
        }

        return prompt
    }

    /**
     * Removes UI artifacts and incomplete turns from raw history.
     */
    private fun sanitizeHistory(raw: List<ChatMessage>): List<ChatMessage> {
        val filtered = raw.filter { !it.isLoading && it.id != "welcome" && it.text.isNotBlank() }
        if (filtered.isEmpty()) return emptyList()

        // Drop orphaned user message at the end (previous crash without model response)
        return if (filtered.last().isFromUser) {
            Log.d(TAG, "Dropping orphaned user message from previous session")
            filtered.dropLast(1)
        } else {
            filtered
        }
    }

    private suspend fun appendAndSave(message: ChatMessage) {
        val current = _messages.value.toMutableList()
        current.add(message)

        // Sliding-window overflow: trim oldest pairs
        val relevant = current.filter { !it.isLoading && it.id != "welcome" }
        if (relevant.size > maxContextMessages * 2) {
            val excess = relevant.size - maxContextMessages * 2
            Log.d(TAG, "Context overflow: trimming $excess oldest messages")
            // Remove from the beginning of the full list (preserving order)
            var toRemove = excess
            val iter = current.listIterator()
            while (iter.hasNext() && toRemove > 0) {
                val msg = iter.next()
                if (!msg.isLoading && msg.id != "welcome") {
                    iter.remove()
                    toRemove--
                }
            }
        }

        _messages.value = current
        chatHistoryRepository.saveMessages(current)
    }

    private fun trimByChars(prompt: String, systemPrompt: String, newUserMessage: String): String {
        val currentTurn = buildCurrentTurn(systemPrompt, newUserMessage)
        val maxHistoryChars = maxContextChars - currentTurn.length
        if (maxHistoryChars <= 0) return currentTurn

        val historyEnd = prompt.lastIndexOf(currentTurn)
        if (historyEnd <= 0) return currentTurn

        val historyPart = prompt.substring(0, historyEnd)
        val trimmed = historyPart.takeLast(maxHistoryChars)
        val cleanStart = trimmed.indexOf("<start_of_turn>")
        val cleanHistory = if (cleanStart > 0) trimmed.substring(cleanStart) else trimmed
        return cleanHistory + currentTurn
    }

    private fun aggressiveTrim(prompt: String, systemPrompt: String, newUserMessage: String): String {
        // Halve the context window and try again
        val currentTurn = buildCurrentTurn(systemPrompt, newUserMessage)
        val historyEnd = prompt.lastIndexOf(currentTurn)
        if (historyEnd <= 0) return currentTurn

        val historyPart = prompt.substring(0, historyEnd)
        val half = historyPart.length / 2
        val trimmed = historyPart.takeLast(half)
        val cleanStart = trimmed.indexOf("<start_of_turn>")
        val cleanHistory = if (cleanStart > 0) trimmed.substring(cleanStart) else trimmed
        return cleanHistory + currentTurn
    }

    private fun buildCurrentTurn(systemPrompt: String, newUserMessage: String): String {
        return buildString {
            append("<start_of_turn>user\n")
            append(systemPrompt)
            append("\n\n")
            append(newUserMessage)
            append("<end_of_turn>\n")
            append("<start_of_turn>model\n")
        }
    }

    companion object {
        private const val TAG = "ChatContextManager"
    }
}
