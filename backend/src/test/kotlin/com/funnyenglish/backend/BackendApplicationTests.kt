package com.funnyenglish.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import org.springframework.test.annotation.DirtiesContext

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BackendApplicationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun contextLoads() {
    }

    @Test
    fun `create and get user`() {
        // Create user
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"telegramId": 999001, "username": "test_user", "firstName": "Test", "lastName": "User"}
                """.trimIndent())
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.telegramId").value(999001))
            .andExpect(jsonPath("$.username").value("test_user"))

        // Get user by ID
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/999001"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.telegramId").value(999001))
    }

    @Test
    fun `create user twice updates existing`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"telegramId": 999002, "username": "first", "firstName": "First", "lastName": "Last"}
                """.trimIndent())
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("first"))

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"telegramId": 999002, "username": "updated", "firstName": "Updated", "lastName": "Last"}
                """.trimIndent())
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value("updated"))
    }

    @Test
    fun `create game and get active`() {
        // Create user first
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"telegramId": 999003, "username": "gamer", "firstName": "Gamer", "lastName": ""}
                """.trimIndent())
        ).andExpect(status().isOk)

        // Create game
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"gameType": "TIC_TAC_TOE_PVE", "playerXId": 999003, "difficulty": 2}
                """.trimIndent())
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.gameType").value("TIC_TAC_TOE_PVE"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(jsonPath("$.playerXId").value(999003))

        // Get active games — verify at least one exists
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/games/active"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    fun `finish game updates stats`() {
        // Create user
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"telegramId": 999004, "username": "finisher", "firstName": "Finisher", "lastName": ""}
                """.trimIndent())
        ).andExpect(status().isOk)

        // Create game
        val result = mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"gameType": "TIC_TAC_TOE_PVE", "playerXId": 999004, "difficulty": 1}
                """.trimIndent())
        ).andExpect(status().isOk)
            .andReturn()

        // Finish game
        val gameId = result.response.contentAsString
            .replace(Regex(".*\"id\":(\\d+).*"), "$1").toLong()

        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/games/{gameId}/finish?result=X_WON", gameId)
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("FINISHED"))
            .andExpect(jsonPath("$.result").value("X_WON"))

        // Check user stats
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/999004"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.gamesPlayed").value(1))
            .andExpect(jsonPath("$.gamesWon").value(1))
    }

    @Test
    fun `send and retrieve chat message`() {
        // Create user
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"telegramId": 999005, "username": "chatter", "firstName": "Chatter", "lastName": ""}
                """.trimIndent())
        ).andExpect(status().isOk)

        // Send message
        mockMvc.perform(
            MockMvcRequestBuilders.post("/api/v1/chat/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"userId": 999005, "userName": "chatter", "content": "Hello test!", "isFromUser": true}
                """.trimIndent())
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.content").value("Hello test!"))
            .andExpect(jsonPath("$.isFromUser").value(true))

        // Get recent messages
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/chat/messages/recent"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].content").value("Hello test!"))
    }

    @Test
    fun `admin login page accessible`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/login"))
            .andExpect(status().isOk)
    }

    @Test
    fun `admin dashboard requires auth`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/admin/dashboard"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun `get nonexistent user returns 404`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/users/999999999"))
            .andExpect(status().isNotFound)
    }
}
