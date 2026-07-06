package com.funnyenglish

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OnboardingPages = listOf(
    OnboardingPage(
        emoji = "👋",
        title = "Добро пожаловать!",
        description = "Я — Арчи, твой игровой компаньон по изучению английского. Будем играть, болтать и учиться вместе!"
    ),
    OnboardingPage(
        emoji = "🎮",
        title = "Играй и учись",
        description = "Крестики-нолики, шахматы и квизы — всё на английском! Прокачивай vocabulary через игру."
    ),
    OnboardingPage(
        emoji = "💬",
        title = "Чат с Арчи",
        description = "Задавай любые вопросы по английскому. Я отвечу с характером и немного сарказма 😏"
    ),
    OnboardingPage(
        emoji = "🏆",
        title = "Отслеживай прогресс",
        description = "Зарабатывай XP, открывай достижения и поддерживай streak. Давай начнём!"
    )
)

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val page = OnboardingPages[currentPage]
    val isLastPage = currentPage == OnboardingPages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut() + slideOutHorizontally { -it }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = page.emoji,
                                fontSize = 56.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                OnboardingPages.forEachIndexed { index, _ ->
                    Card(
                        shape = CircleShape,
                        modifier = Modifier.size(if (index == currentPage) 12.dp else 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        )
                    ) {}
                }
            }

            // Buttons
            if (isLastPage) {
                Button(
                    onClick = {
                        context.getSharedPreferences("funnyenglish", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("has_seen_onboarding", true)
                            .apply()
                        onFinished()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Начать! 🚀",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = { currentPage++ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Дальше →",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            if (!isLastPage) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        context.getSharedPreferences("funnyenglish", android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("has_seen_onboarding", true)
                            .apply()
                        onFinished()
                    }
                ) {
                    Text("Пропустить")
                }
            }
        }
    }
}
