package com.funnyenglish.core.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class FunnyEnglishApi(private val client: HttpClient) {

    companion object {
        // 10.0.2.2 is the emulator's alias for host localhost
        const val BASE_URL = "http://10.0.2.2:8082"
    }

    suspend fun getUsers(): List<UserDto> =
        client.get("$BASE_URL/api/v1/users").body()

    suspend fun getGames(): List<GameDto> =
        client.get("$BASE_URL/api/v1/games").body()

    suspend fun getChatMessages(): List<ChatMessageDto> =
        client.get("$BASE_URL/api/v1/chat").body()

    suspend fun sendChatMessage(message: String): ChatMessageDto =
        client.post("$BASE_URL/api/v1/chat") {
            contentType(ContentType.Application.Json)
            setBody(ChatRequest(message))
        }.body()
}

@Serializable
data class UserDto(
    val id: Long,
    val telegramId: String,
    val username: String? = null,
    val firstName: String? = null,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val isActive: Boolean = true
)

@Serializable
data class GameDto(
    val id: Long,
    val gameType: String,
    val status: String,
    val result: String? = null,
    val playerXId: String,
    val playerOId: String? = null,
    val movesCount: Int = 0
)

@Serializable
data class ChatMessageDto(
    val id: Long,
    val userId: String,
    val userName: String? = null,
    val content: String,
    val isFromUser: Boolean,
    val sentAt: String? = null
)

@Serializable
data class ChatRequest(val message: String)
