package com.funnyenglish.backend.service

import com.funnyenglish.backend.dto.CreateGameRequest
import com.funnyenglish.backend.dto.CreateUserRequest
import com.funnyenglish.backend.model.GameResult
import com.funnyenglish.backend.model.GameStatus
import com.funnyenglish.backend.model.GameType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ServiceIntegrationTests {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    lateinit var gameService: GameService

    @Autowired
    lateinit var chatService: ChatService

    @Test
    fun `user lifecycle`() {
        val user = userService.getOrCreateUser(
            CreateUserRequest(telegramId = 100001, username = "u1", firstName = "U", lastName = "")
        )
        assertNotNull(user)
        assertEquals(100001, user.telegramId)
        assertEquals("u1", user.username)
        assertEquals(0, user.gamesPlayed)
        assertEquals(0, user.gamesWon)

        // Second create should update, not duplicate
        val user2 = userService.getOrCreateUser(
            CreateUserRequest(telegramId = 100001, username = "u1_updated", firstName = "U", lastName = "")
        )
        assertEquals("u1_updated", user2.username)

        val found = userService.getUserByTelegramId(100001)!!
        assertEquals("u1_updated", found.username)
    }

    @Test
    fun `game lifecycle and stats`() {
        val user = userService.getOrCreateUser(
            CreateUserRequest(telegramId = 100002, username = "gamer", firstName = "G", lastName = "")
        )

        val game = gameService.createGame(
            CreateGameRequest(gameType = GameType.TIC_TAC_TOE_PVE, playerXId = user.telegramId, playerOId = null, difficulty = 2)
        )
        assertEquals(GameStatus.IN_PROGRESS, game.status)
        assertEquals(GameResult.UNKNOWN, game.result)

        val userGames = gameService.getUserGames(user.telegramId)
        assertTrue(userGames.isNotEmpty())
        assertEquals(GameStatus.IN_PROGRESS, userGames[0].status)

        // Finish as X won
        val finished = gameService.finishGame(game.id, GameResult.X_WON)!!
        assertEquals(GameStatus.FINISHED, finished.status)
        assertEquals(GameResult.X_WON, finished.result)

        // Check user stats
        val stats = userService.getUserStats()
        assertTrue(stats.total >= 1)

        val updatedUser = userService.getUserByTelegramId(100002)!!
        assertEquals(1, updatedUser.gamesPlayed)
        assertEquals(1, updatedUser.gamesWon)
    }

    @Test
    fun `chat message lifecycle`() {
        val msg = chatService.saveMessage(
            com.funnyenglish.backend.dto.SendMessageRequest(userId = 100003, userName = "chatter", content = "Test msg", isFromUser = true)
        )
        assertEquals("Test msg", msg.content)
        assertTrue(msg.isFromUser)

        val userMessages = chatService.getUserMessages(100003)
        assertTrue(userMessages.isNotEmpty())
        assertEquals("Test msg", userMessages[0].content)
    }

    @Test
    fun `game stats counts`() {
        val user = userService.getOrCreateUser(
            CreateUserRequest(telegramId = 100004, username = "stats", firstName = "S", lastName = "")
        )
        gameService.createGame(CreateGameRequest(GameType.TIC_TAC_TOE_PVE, user.telegramId, null, 1))
        gameService.createGame(CreateGameRequest(GameType.TIC_TAC_TOE_PVP, user.telegramId, 100005, 1))

        val userGames = gameService.getUserGames(user.telegramId)
        assertEquals(2, userGames.size)
        assertTrue(userGames.all { it.status == GameStatus.IN_PROGRESS })
    }

    @Test
    fun `abort game`() {
        val user = userService.getOrCreateUser(
            CreateUserRequest(telegramId = 100006, username = "abort", firstName = "A", lastName = "")
        )
        val game = gameService.createGame(CreateGameRequest(GameType.TIC_TAC_TOE_PVE, user.telegramId, null, 1))
        val aborted = gameService.abortGame(game.id)!!
        assertEquals(GameStatus.ABORTED, aborted.status)
    }
}
