package com.funnyenglish.feature.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funnyenglish.core.designsystem.components.ConfettiOverlay
import android.view.HapticFeedbackConstants
import androidx.compose.ui.platform.LocalView
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val view = LocalView.current

    QuizContent(
        state = state,
        onAction = { action ->
            if (action is QuizAction.SelectAnswer) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            if (action is QuizAction.NextQuestion || action is QuizAction.Restart) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
            viewModel.onAction(action)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizContent(
    state: QuizState,
    onAction: (QuizAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Квиз") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (state.isFinished) {
                var showConfetti by rememberSaveable { mutableStateOf(true) }
                if (state.score >= state.questions.size * 5) {
                    ConfettiOverlay(
                        active = showConfetti,
                        onFinished = { showConfetti = false }
                    )
                }
                QuizResult(
                    score = state.score,
                    total = state.questions.size * 10,
                    onRestart = { onAction(QuizAction.Restart) }
                )
            } else {
                val question = state.questions[state.currentQuestionIndex]
                val progress = (state.currentQuestionIndex + 1).toFloat() / state.questions.size

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Вопрос ${state.currentQuestionIndex + 1} / ${state.questions.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(
                        targetState = question.word,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) +
                             slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium), initialOffsetY = { it / 2 }))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(200)) +
                                    slideOutVertically(animationSpec = tween(200), targetOffsetY = { -it / 4 })
                                )
                        },
                        label = "question"
                    ) { animatedWord ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = animatedWord,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        question.options.forEachIndexed { index, option ->
                            val backgroundColor = when {
                                state.selectedAnswer == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                index == state.selectedAnswer && state.isCorrect == true ->
                                    MaterialTheme.colorScheme.secondaryContainer
                                index == state.selectedAnswer && state.isCorrect == false ->
                                    MaterialTheme.colorScheme.errorContainer
                                option == question.correctTranslation && state.isCorrect == false ->
                                    MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }

                            val scale by animateFloatAsState(
                                targetValue = if (index == state.selectedAnswer) 1.05f else 1f,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                label = "answerScale"
                            )
                            Card(
                                onClick = {
                                    if (state.selectedAnswer == null) {
                                        onAction(QuizAction.SelectAnswer(index))
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(scale),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                enabled = state.selectedAnswer == null
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.selectedAnswer != null) {
                        val feedback = if (state.isCorrect == true) "✅ Правильно! +10 XP" else "❌ Неправильно"
                        Text(
                            text = feedback,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.isCorrect == true) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { onAction(QuizAction.NextQuestion) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Дальше →")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizResult(
    score: Int,
    total: Int,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Квиз завершён!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$score / $total XP",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRestart) {
            Text("Пройти ещё раз")
        }
    }
}
