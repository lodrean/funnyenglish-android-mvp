package com.funnyenglish.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funnyenglish.core.designsystem.components.ShimmerBox
import com.funnyenglish.core.designsystem.theme.Primary
import com.funnyenglish.core.designsystem.theme.Secondary
import com.funnyenglish.core.designsystem.theme.Tertiary
import com.funnyenglish.core.presentation.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    onNavigateTo: (String) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HomeEvent.NavigateTo -> onNavigateTo(event.route)
        }
    }

    HomeContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeState,
    onAction: (HomeAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FunnyEnglish") },
                actions = {
                    IconButton(
                        onClick = { onAction(HomeAction.OnRefresh) },
                        enabled = !state.isRefreshing
                    ) {
                        if (state.isRefreshing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Обновить")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (state.isLoading) {
                // Shimmer placeholders
                items(6) { index ->
                    AnimatedListItem(index = index) {
                        when (index) {
                            0 -> ShimmerBox(modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp))
                            1 -> ShimmerBox(modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(16.dp))
                            2 -> ShimmerBox(modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(20.dp))
                            3 -> ShimmerBox(modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(8.dp))
                            4 -> ShimmerBox(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(16.dp))
                            5 -> ShimmerBox(modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(16.dp))
                        }
                    }
                }
            } else {
                // Greeting + Archie avatar placeholder
                item {
                    AnimatedListItem(index = 0) {
                        ArchieHeader(state.userName)
                    }
                }

                // Stats row
                item {
                    AnimatedListItem(index = 1) {
                        StatsRow(streak = state.streakDays, xp = state.totalXp)
                    }
                }

                // Daily Word card
                item {
                    AnimatedListItem(index = 2) {
                        DailyWordCard(
                            word = state.dailyWord,
                            definition = state.dailyWordDefinition,
                            onClick = { onAction(HomeAction.OnDailyWordClick) }
                        )
                    }
                }

                // Feature cards grid
                item {
                    AnimatedListItem(index = 3) {
                        Text(
                            text = "Чем займёмся?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                item {
                    AnimatedListItem(index = 4) {
                        FeatureGrid(onAction = onAction)
                    }
                }

                // Motivational quote
                item {
                    AnimatedListItem(index = 5) {
                        MotivationCard()
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchieHeader(userName: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = Tertiary.copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "🤖",
                    fontSize = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "Привет, $userName! 👋",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Я Арчи — твой AI-напарник",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsRow(streak: Int, xp: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Default.Star,
            value = "$streak",
            label = "дней streak",
            color = Tertiary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Default.Star,
            value = "$xp",
            label = "XP очков",
            color = Secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
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
private fun DailyWordCard(
    word: String,
    definition: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 Слово дня",
                    style = MaterialTheme.typography.labelLarge,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = word,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FeatureGrid(onAction: (HomeAction) -> Unit) {
    val features = listOf(
        FeatureItem("Чат с Арчи", Icons.AutoMirrored.Filled.Send, Primary, HomeAction.OnChatClick),
        FeatureItem("Словарь", Icons.Default.Search, Secondary, HomeAction.OnDictionaryClick),
        FeatureItem("Квизы", Icons.Default.ThumbUp, Tertiary, HomeAction.OnQuizClick),
        FeatureItem("Игры", Icons.Default.PlayArrow, Primary, HomeAction.OnGamesClick),
        FeatureItem("Профиль", Icons.Default.Person, Secondary, HomeAction.OnProfileClick)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        features.chunked(2).forEach { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowFeatures.forEach { feature ->
                    FeatureCard(
                        title = feature.title,
                        icon = feature.icon,
                        color = feature.color,
                        onClick = { onAction(feature.action) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class FeatureItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val action: HomeAction
)

@Composable
private fun FeatureCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MotivationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "💡 Мудрость от Арчи",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"The limits of my language mean the limits of my world.\" — Ludwig Wittgenstein",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AnimatedListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 80L)
        visible.value = true
    }
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                slideInVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    initialOffsetY = { it / 4 }
                )
    ) {
        content()
    }
}
