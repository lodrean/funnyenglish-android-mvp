package com.funnyenglish

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class FunnyEnglishUiAutomatorTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // Clear onboarding prefs only
        context.getSharedPreferences("funnyenglish", android.content.Context.MODE_PRIVATE)
            .edit().clear().apply()

        // Launch app fresh
        val intent = context.packageManager.getLaunchIntentForPackage("com.funnyenglish")
        intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK or android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg("com.funnyenglish").depth(0)), 5000)
    }

    @Test
    fun onboardingIsDisplayed() {
        assertTrue(device.findObject(By.text("Добро пожаловать!")) != null)
    }

    @Test
    fun navigateThroughOnboardingAndAllTabs() {
        // Skip onboarding
        val skipBtn = device.findObject(By.text("Пропустить"))
        skipBtn?.click()
        device.wait(Until.gone(By.text("Пропустить")), 3000)

        // Check Home
        assertTrue(device.findObject(By.textContains("Streak")) != null)

        // Navigate to Games
        device.findObject(By.text("Игры"))?.click()
        device.wait(Until.hasObject(By.text("Крестики-нолики")), 3000)
        assertTrue(device.findObject(By.text("Крестики-нолики")) != null)

        // Navigate to Quiz
        device.findObject(By.text("Квиз"))?.click()
        device.wait(Until.hasObject(By.textContains("Вопрос")), 3000)
        assertTrue(device.findObject(By.textContains("Вопрос")) != null)

        // Navigate to Chat
        device.findObject(By.text("Чат"))?.click()
        device.wait(Until.hasObject(By.text("Чат с Арчи")), 3000)
        assertTrue(device.findObject(By.text("Чат с Арчи")) != null)

        // Navigate to Profile
        device.findObject(By.text("Профиль"))?.click()
        device.wait(Until.hasObject(By.text("Профиль")), 3000)
        assertTrue(device.findObject(By.text("Профиль")) != null)
    }

    @Test
    fun playTicTacToe() {
        device.findObject(By.text("Пропустить"))?.click()
        device.wait(Until.gone(By.text("Пропустить")), 3000)

        device.findObject(By.text("Игры"))?.click()
        device.wait(Until.hasObject(By.text("Крестики-нолики")), 3000)

        assertTrue(device.findObject(By.textContains("Твой ход")) != null)

        device.findObject(By.text("Новая игра"))?.click()
        device.wait(Until.hasObject(By.textContains("Твой ход")), 2000)
        assertTrue(device.findObject(By.textContains("Твой ход")) != null)
    }

    @Test
    fun quizAnswerAndNavigate() {
        device.findObject(By.text("Пропустить"))?.click()
        device.wait(Until.gone(By.text("Пропустить")), 3000)

        device.findObject(By.text("Квиз"))?.click()
        device.wait(Until.hasObject(By.textContains("Вопрос")), 3000)

        assertTrue(device.findObject(By.textContains("Вопрос")) != null)

        // Click first answer option
        device.findObject(By.textStartsWith("A."))?.click()
        device.wait(Until.hasObject(By.textContains("Вопрос")), 2000)
    }
}