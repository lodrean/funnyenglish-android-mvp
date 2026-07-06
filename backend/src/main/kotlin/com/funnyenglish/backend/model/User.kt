package com.funnyenglish.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val telegramId: Long,

    @Column(nullable = false)
    val username: String? = null,

    @Column(nullable = false)
    val firstName: String? = null,

    @Column(nullable = false)
    val lastName: String? = null,

    @Column(nullable = false)
    val registeredAt: Instant = Instant.now(),

    @Column(nullable = false)
    val lastActivityAt: Instant = Instant.now(),

    @Column(nullable = false)
    val gamesPlayed: Int = 0,

    @Column(nullable = false)
    val gamesWon: Int = 0,

    @Column(nullable = false)
    val isActive: Boolean = true
)
