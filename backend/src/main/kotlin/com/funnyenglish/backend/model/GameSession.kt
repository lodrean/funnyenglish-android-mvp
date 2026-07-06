package com.funnyenglish.backend.model

import jakarta.persistence.*
import java.time.Instant

enum class GameType {
    TIC_TAC_TOE_PVE,
    TIC_TAC_TOE_PVP,
    CHESS
}

enum class GameStatus {
    IN_PROGRESS,
    FINISHED,
    ABORTED
}

enum class GameResult {
    X_WON,
    O_WON,
    DRAW,
    UNKNOWN
}

@Entity
@Table(name = "game_sessions")
data class GameSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val gameType: GameType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: GameStatus = GameStatus.IN_PROGRESS,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val result: GameResult = GameResult.UNKNOWN,

    @Column(nullable = false)
    val playerXId: Long,

    @Column
    val playerOId: Long? = null,

    @Column(nullable = false)
    val startedAt: Instant = Instant.now(),

    @Column
    val finishedAt: Instant? = null,

    @Column(nullable = false)
    val movesCount: Int = 0,

    @Column
    val difficulty: Int? = null,

    @Column(columnDefinition = "TEXT")
    val boardState: String? = null
)
