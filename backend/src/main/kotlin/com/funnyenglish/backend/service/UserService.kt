package com.funnyenglish.backend.service

import com.funnyenglish.backend.dto.CreateUserRequest
import com.funnyenglish.backend.dto.UserDto
import com.funnyenglish.backend.model.User
import com.funnyenglish.backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class UserService(private val userRepository: UserRepository) {

    fun getOrCreateUser(request: CreateUserRequest): UserDto {
        val existing = userRepository.findByTelegramId(request.telegramId)
        if (existing != null) {
            val updated = existing.copy(
                lastActivityAt = Instant.now(),
                username = request.username ?: existing.username,
                firstName = request.firstName ?: existing.firstName
            )
            return userRepository.save(updated).toDto()
        }
        val user = User(
            telegramId = request.telegramId,
            username = request.username ?: "",
            firstName = request.firstName ?: "",
            lastName = request.lastName ?: ""
        )
        return userRepository.save(user).toDto()
    }

    fun getAllUsers(): List<UserDto> = userRepository.findAll().map { it.toDto() }

    fun getActiveUsers(): List<UserDto> = userRepository.findByIsActiveTrue().map { it.toDto() }

    fun getUserByTelegramId(telegramId: Long): UserDto? =
        userRepository.findByTelegramId(telegramId)?.toDto()

    fun updateActivity(telegramId: Long) {
        userRepository.findByTelegramId(telegramId)?.let {
            userRepository.save(it.copy(lastActivityAt = Instant.now()))
        }
    }

    fun getUserStats(): UserStats {
        val now = Instant.now()
        return UserStats(
            total = userRepository.count(),
            activeToday = userRepository.countByLastActivityAtAfter(now.minus(1, ChronoUnit.DAYS)),
            activeWeek = userRepository.countByLastActivityAtAfter(now.minus(7, ChronoUnit.DAYS))
        )
    }

    data class UserStats(val total: Long, val activeToday: Long, val activeWeek: Long)
}

fun User.toDto(): UserDto = UserDto(
    id = id,
    telegramId = telegramId,
    username = username,
    firstName = firstName,
    gamesPlayed = gamesPlayed,
    gamesWon = gamesWon,
    isActive = isActive,
    lastActivityAt = lastActivityAt
)
