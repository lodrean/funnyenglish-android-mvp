package com.funnyenglish.feature.chat

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.chatDataStore: DataStore<Preferences> by preferencesDataStore(name = "chat_history")

class ChatHistoryRepository(private val context: Context) {

    private val messagesKey = stringPreferencesKey("messages")

    suspend fun saveMessages(messages: List<ChatMessage>) {
        val json = Json.encodeToString(messages.filter { !it.isLoading })
        context.chatDataStore.edit { preferences ->
            preferences[messagesKey] = json
        }
    }

    suspend fun loadMessages(): List<ChatMessage> {
        val preferences = context.chatDataStore.data.first()
        val json = preferences[messagesKey] ?: return emptyList()
        return try {
            Json.decodeFromString<List<ChatMessage>>(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun clear() {
        context.chatDataStore.edit { it.remove(messagesKey) }
    }
}
