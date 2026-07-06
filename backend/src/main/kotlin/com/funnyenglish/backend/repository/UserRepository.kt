package com.funnyenglish.backend.repository

import com.funnyenglish.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByTelegramId(telegramId: Long): User?
    fun findByIsActiveTrue(): List<User>
    fun countByRegisteredAtAfter(after: Instant): Long
    fun countByLastActivityAtAfter(after: Instant): Long
}
