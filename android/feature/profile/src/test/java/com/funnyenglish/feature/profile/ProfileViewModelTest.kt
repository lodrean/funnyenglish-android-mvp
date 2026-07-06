package com.funnyenglish.feature.profile

import app.cash.turbine.test
import com.funnyenglish.core.domain.repository.ThemeRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var themeRepository: ThemeRepository
    private lateinit var viewModel: ProfileViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        themeRepository = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state collects dark mode from repository`() = runTest {
        every { themeRepository.isDarkMode } returns flowOf(true)

        viewModel = ProfileViewModel(themeRepository)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(true, state.isDarkMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state default is false when flow emits false`() = runTest {
        every { themeRepository.isDarkMode } returns flowOf(false)

        viewModel = ProfileViewModel(themeRepository)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(false, state.isDarkMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleDarkMode calls repository setDarkMode`() = runTest {
        every { themeRepository.isDarkMode } returns flowOf(false)

        viewModel = ProfileViewModel(themeRepository)
        viewModel.toggleDarkMode(true)

        coVerify { themeRepository.setDarkMode(true) }
    }
}
