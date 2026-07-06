package com.funnyenglish.backend.controller

import com.funnyenglish.backend.dto.DashboardStats
import com.funnyenglish.backend.service.ChatService
import com.funnyenglish.backend.service.GameService
import com.funnyenglish.backend.service.UserService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin")
class AdminController(
    private val userService: UserService,
    private val gameService: GameService,
    private val chatService: ChatService
) {

    @GetMapping("/login")
    fun login(): String = "admin/login"

    @GetMapping("/dashboard")
    fun dashboard(model: Model): String {
        val userStats = userService.getUserStats()
        val gameStats = gameService.getGameStats()
        val messageStats = chatService.getMessageStats()

        val stats = DashboardStats(
            totalUsers = userStats.total,
            activeUsersToday = userStats.activeToday,
            activeUsersWeek = userStats.activeWeek,
            totalGames = gameStats.total,
            activeGames = gameStats.active,
            totalMessages = messageStats.total,
            recentGames = gameService.getAllGames().take(10),
            recentUsers = userService.getAllUsers().take(10),
            recentMessages = chatService.getRecentMessages().take(10)
        )

        model.addAttribute("stats", stats)
        return "admin/dashboard"
    }

    @GetMapping("/users")
    fun users(model: Model): String {
        model.addAttribute("users", userService.getAllUsers())
        return "admin/users"
    }

    @GetMapping("/games")
    fun games(model: Model): String {
        model.addAttribute("games", gameService.getAllGames())
        model.addAttribute("activeGames", gameService.getActiveGames())
        return "admin/games"
    }

    @GetMapping("/chat")
    fun chat(model: Model): String {
        model.addAttribute("messages", chatService.getRecentMessages())
        return "admin/chat"
    }
}
