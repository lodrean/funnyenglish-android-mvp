package com.funnyenglish.backend.repository

import com.funnyenglish.backend.model.GameSession
import com.funnyenglish.backend.model.GameStatus
import com.funnyenglish.backend.model.GameType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface GameSessionRepository : JpaRepository<GameSession, Long> {
    fun findByStatus(status: GameStatus): List<GameSession>
    fun findByGameType(gameType: GameType): List<GameSession>
    fun findByPlayerXIdOrPlayerOId(playerXId: Long, playerOId: Long): List<GameSession>
    fun countByStartedAtAfter(after: Instant): Long
    fun countByStatus(status: GameStatus): Long
}
