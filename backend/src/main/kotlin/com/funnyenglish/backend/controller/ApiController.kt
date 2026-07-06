package com.funnyenglish.backend.controller

import com.funnyenglish.backend.dto.*
import com.funnyenglish.backend.service.ChatService
import com.funnyenglish.backend.service.GameService
import com.funnyenglish.backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class ApiController(
    private val userService: UserService,
    private val gameService: GameService,
    private val chatService: ChatService
) {

    // --- Users ---
    @PostMapping("/users")
    fun createOrUpdateUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.getOrCreateUser(request))
    }

    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<UserDto>> {
        return ResponseEntity.ok(userService.getAllUsers())
    }

    @GetMapping("/users/{telegramId}")
    fun getUser(@PathVariable telegramId: Long): ResponseEntity<UserDto> {
        return userService.getUserByTelegramId(telegramId)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/users/{telegramId}/activity")
    fun updateActivity(@PathVariable telegramId: Long): ResponseEntity<Void> {
        userService.updateActivity(telegramId)
        return ResponseEntity.ok().build()
    }

    // --- Games ---
    @PostMapping("/games")
    fun createGame(@RequestBody request: CreateGameRequest): ResponseEntity<GameSessionDto> {
        return ResponseEntity.ok(gameService.createGame(request))
    }

    @GetMapping("/games")
    fun getAllGames(): ResponseEntity<List<GameSessionDto>> {
        return ResponseEntity.ok(gameService.getAllGames())
    }

    @GetMapping("/games/active")
    fun getActiveGames(): ResponseEntity<List<GameSessionDto>> {
        return ResponseEntity.ok(gameService.getActiveGames())
    }

    @PostMapping("/games/move")
    fun makeMove(@RequestBody request: MakeMoveRequest): ResponseEntity<GameSessionDto> {
        return gameService.makeMove(request)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    @PostMapping("/games/{gameId}/finish")
    fun finishGame(
        @PathVariable gameId: Long,
        @RequestParam result: String
    ): ResponseEntity<GameSessionDto> {
        val gameResult = try {
            enumValueOf<com.funnyenglish.backend.model.GameResult>(result)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
        return gameService.finishGame(gameId, gameResult)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    // --- Chat ---
    @PostMapping("/chat/messages")
    fun sendMessage(@RequestBody request: SendMessageRequest): ResponseEntity<ChatMessageDto> {
        return ResponseEntity.ok(chatService.saveMessage(request))
    }

    @GetMapping("/chat/messages/recent")
    fun getRecentMessages(): ResponseEntity<List<ChatMessageDto>> {
        return ResponseEntity.ok(chatService.getRecentMessages())
    }
}
