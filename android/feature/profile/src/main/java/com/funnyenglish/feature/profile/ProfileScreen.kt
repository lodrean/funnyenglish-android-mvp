package com.funnyenglish.feature.profile

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    ProfileContent(
        state = state,
        onToggleDarkMode = viewModel::toggleDarkMode
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileState,
    onToggleDarkMode: (Boolean) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Профиль") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User header
            item {
                UserHeader(userName = state.userName, levelTitle = state.levelTitle)
            }

            // Stats cards
            item {
                StatsRow(streak = state.streakDays, xp = state.xp, words = state.wordsLearned)
            }

            // Level progress
            item {
                LevelCard(level = state.level, xp = state.xp, xpToNext = state.xpToNextLevel)
            }

            // Settings
            item {
                SettingsCard(
                    isDarkMode = state.isDarkMode,
                    onToggleDarkMode = onToggleDarkMode
                )
            }

            // Achievements
            item {
                Text(
                    text = "Достижения",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                AchievementsGrid(achievements = state.achievements)
            }
        }
    }
}

@Composable
private fun UserHeader(userName: String, levelTitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(80.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎓",
                    fontSize = 36.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = userName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = levelTitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatsRow(streak: Int, xp: Int, words: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("🔥", streak.toString(), "Streak", Modifier.weight(1f))
        StatCard("⭐", xp.toString(), "XP", Modifier.weight(1f))
        StatCard("📚", words.toString(), "Слов", Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LevelCard(level: Int, xp: Int, xpToNext: Int) {
    val progress = if (xpToNext > 0) xp.toFloat() / xpToNext else 1f
    val remaining = maxOf(0, xpToNext - xp)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Уровень $level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$xp / $xpToNext XP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ещё $remaining XP до уровня ${level + 1}!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsCard(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🌙 Тёмная тема",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Переключить оформление приложения",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isDarkMode,
                onCheckedChange = onToggleDarkMode
            )
        }
    }
}

@Composable
private fun AchievementsGrid(achievements: List<Achievement>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        achievements.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { achievement ->
                    val isUnlocked = achievement.isUnlocked
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isUnlocked) {
                                Text(achievement.icon, fontSize = 24.sp)
                            } else {
                                Text(
                                    "🔒",
                                    fontSize = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = achievement.name,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (isUnlocked) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
