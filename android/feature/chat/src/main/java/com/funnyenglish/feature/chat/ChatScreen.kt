package com.funnyenglish.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChatContent(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    state: ChatState,
    onAction: (ChatAction) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Чат с Арчи")
                        if (state.isLoading) {
                            Spacer(modifier = Modifier.width(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                inputText = state.inputText,
                onInputChange = { onAction(ChatAction.InputChanged(it)) },
                onSend = { onAction(ChatAction.SendMessage) },
                enabled = !state.isLoading
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }
            }
        }
    }

    // Battery warning dialog
    if (state.showBatteryWarning) {
        AlertDialog(
            onDismissRequest = { onAction(ChatAction.DismissBatteryWarning) },
            title = { Text("⚠️ Низкий заряд батареи") },
            text = {
                Text(
                    "Локальная AI-модель сильно нагружает процессор и быстро разряжает телефон. " +
                    "Рекомендую подключить зарядку или дождаться уровня заряда выше 30%."
                )
            },
            confirmButton = {
                TextButton(onClick = { onAction(ChatAction.DismissBatteryWarning) }) {
                    Text("Понятно")
                }
            }
        )
    }

    // Model download dialog
    if (state.showModelDownloadDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDownloading) onAction(ChatAction.DismissModelDialog)
            },
            title = { Text("📥 Загрузка AI-модели") },
            text = {
                Column {
                    Text(
                        "Для работы Арчи офлайн нужно загрузить модель Gemma 2B (~2.5GB). " +
                        "Это разовая загрузка."
                    )
                    if (state.isDownloading || state.modelDownloadProgress != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { state.modelDownloadProgress ?: 0f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${((state.modelDownloadProgress ?: 0f) * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    state.error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onAction(ChatAction.DownloadModel) },
                    enabled = !state.isDownloading
                ) {
                    Text(if (state.isDownloading) "Загрузка..." else "Загрузить")
                }
            },
            dismissButton = {
                if (!state.isDownloading) {
                    TextButton(onClick = { onAction(ChatAction.DismissModelDialog) }) {
                        Text("Позже")
                    }
                }
            }
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🤖", fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                if (message.isLoading) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(8.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Напиши Арчи...") },
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = enabled && inputText.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
