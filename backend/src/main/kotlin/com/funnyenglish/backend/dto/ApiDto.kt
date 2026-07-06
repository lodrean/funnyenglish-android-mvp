package com.funnyenglish.backend.dto

import com.funnyenglish.backend.model.GameType
import com.funnyenglish.backend.model.GameResult
import com.funnyenglish.backend.model.GameStatus
import java.time.Instant

// User DTOs
data class UserDto(
    val id: Long,
    val telegramId: Long,
    val username: String?,
    val firstName: String?,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val isActive: Boolean,
    val lastActivityAt: Instant
)

data class CreateUserRequest(
    val telegramId: Long,
    val username: String?,
    val firstName: String?,
    val lastName: String?
)

// Game DTOs
data class GameSessionDto(
    val id: Long,
    val gameType: GameType,
    val status: GameStatus,
    val result: GameResult,
    val playerXId: Long,
    val playerOId: Long?,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val movesCount: Int
)

data class CreateGameRequest(
    val gameType: GameType,
    val playerXId: Long,
    val playerOId: Long?,
    val difficulty: Int?
)

data class MakeMoveRequest(
    val gameId: Long,
    val position: String,
    val playerId: Long
)

// Chat DTOs
data class ChatMessageDto(
    val id: Long,
    val userId: Long,
    val userName: String?,
    val content: String,
    val isFromUser: Boolean,
    val sentAt: Instant
)

data class SendMessageRequest(
    val userId: Long,
    val userName: String?,
    val content: String,
    val isFromUser: Boolean = true
)

// Stats DTO
data class DashboardStats(
    val totalUsers: Long,
    val activeUsersToday: Long,
    val activeUsersWeek: Long,
    val totalGames: Long,
    val activeGames: Long,
    val totalMessages: Long,
    val recentGames: List<GameSessionDto>,
    val recentUsers: List<UserDto>,
    val recentMessages: List<ChatMessageDto>
)
