package com.funnyenglish.backend.service

import com.funnyenglish.backend.dto.CreateGameRequest
import com.funnyenglish.backend.dto.GameSessionDto
import com.funnyenglish.backend.dto.MakeMoveRequest
import com.funnyenglish.backend.model.GameResult
import com.funnyenglish.backend.model.GameSession
import com.funnyenglish.backend.model.GameStatus
import com.funnyenglish.backend.repository.GameSessionRepository
import com.funnyenglish.backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class GameService(
    private val gameSessionRepository: GameSessionRepository,
    private val userRepository: UserRepository
) {

    fun createGame(request: CreateGameRequest): GameSessionDto {
        val session = GameSession(
            gameType = request.gameType,
            playerXId = request.playerXId,
            playerOId = request.playerOId,
            difficulty = request.difficulty,
            status = GameStatus.IN_PROGRESS
        )
        return gameSessionRepository.save(session).toDto()
    }

    fun getActiveGames(): List<GameSessionDto> =
        gameSessionRepository.findByStatus(GameStatus.IN_PROGRESS).map { it.toDto() }

    fun getAllGames(): List<GameSessionDto> =
        gameSessionRepository.findAll().map { it.toDto() }

    fun getUserGames(userId: Long): List<GameSessionDto> =
        gameSessionRepository.findByPlayerXIdOrPlayerOId(userId, userId).map { it.toDto() }

    fun makeMove(request: MakeMoveRequest): GameSessionDto? {
        val game = gameSessionRepository.findById(request.gameId).orElse(null) ?: return null
        if (game.status != GameStatus.IN_PROGRESS) return null

        val updated = game.copy(
            movesCount = game.movesCount + 1,
            boardState = (game.boardState ?: "") + "|${request.position}"
        )
        return gameSessionRepository.save(updated).toDto()
    }

    fun finishGame(gameId: Long, result: GameResult): GameSessionDto? {
        val game = gameSessionRepository.findById(gameId).orElse(null) ?: return null
        val finished = game.copy(
            status = GameStatus.FINISHED,
            result = result,
            finishedAt = Instant.now()
        )
        // Update user stats by telegramId (playerXId stores telegramId)
        userRepository.findByTelegramId(game.playerXId)?.let { user ->
            userRepository.save(user.copy(gamesPlayed = user.gamesPlayed + 1))
        }
        game.playerOId?.let { userRepository.findByTelegramId(it)?.let { user ->
            userRepository.save(user.copy(gamesPlayed = user.gamesPlayed + 1))
        }}
        if (result == GameResult.X_WON || result == GameResult.O_WON) {
            val winnerId = if (result == GameResult.X_WON) game.playerXId else game.playerOId
            winnerId?.let { userRepository.findByTelegramId(it)?.let { user ->
                userRepository.save(user.copy(gamesWon = user.gamesWon + 1))
            }}
        }
        return gameSessionRepository.save(finished).toDto()
    }

    fun abortGame(gameId: Long): GameSessionDto? {
        val game = gameSessionRepository.findById(gameId).orElse(null) ?: return null
        return gameSessionRepository.save(game.copy(status = GameStatus.ABORTED, finishedAt = Instant.now())).toDto()
    }

    fun getGameStats(): GameStats {
        val now = Instant.now()
        return GameStats(
            total = gameSessionRepository.count(),
            active = gameSessionRepository.countByStatus(GameStatus.IN_PROGRESS),
            today = gameSessionRepository.countByStartedAtAfter(now.minus(1, ChronoUnit.DAYS))
        )
    }

    data class GameStats(val total: Long, val active: Long, val today: Long)
}

fun GameSession.toDto(): GameSessionDto = GameSessionDto(
    id = id,
    gameType = gameType,
    status = status,
    result = result,
    playerXId = playerXId,
    playerOId = playerOId,
    startedAt = startedAt,
    finishedAt = finishedAt,
    movesCount = movesCount
)
