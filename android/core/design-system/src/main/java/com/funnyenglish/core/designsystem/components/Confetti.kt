package com.funnyenglish.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val CONFETTI_COUNT = 60
private const val ANIMATION_DURATION = 2000

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    active: Boolean,
    onFinished: () -> Unit = {}
) {
    if (!active) return

    val particles = remember { List(CONFETTI_COUNT) { ConfettiParticle.random() } }
    val progresses = remember { List(CONFETTI_COUNT) { Animatable(0f) } }

    LaunchedEffect(active) {
        progresses.forEachIndexed { index, animatable ->
            launch {
                delay(index * 15L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = ANIMATION_DURATION)
                )
            }
        }
        delay(ANIMATION_DURATION.toLong() + 500)
        onFinished()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEachIndexed { index, particle ->
                val progress = progresses[index].value
                if (progress > 0f) {
                    drawConfetti(particle, progress, size.width, size.height)
                }
            }
        }
    }
}

private fun DrawScope.drawConfetti(
    particle: ConfettiParticle,
    progress: Float,
    canvasWidth: Float,
    canvasHeight: Float
) {
    val x = particle.startX + (particle.velocityX * progress * canvasWidth * 0.5f)
    val y = particle.startY + (particle.velocityY * progress * canvasHeight * 0.8f) + (progress * progress * canvasHeight * 0.3f)
    val rotation = particle.rotation + (progress * particle.rotationSpeed * 360f)
    val size = particle.size * (1f - progress * 0.3f)

    val alpha = if (progress > 0.8f) 1f - ((progress - 0.8f) / 0.2f) else 1f

    drawContext.canvas.save()
    drawContext.canvas.translate(x, y)
    drawContext.canvas.rotate(rotation)
    drawRect(
        color = particle.color.copy(alpha = alpha),
        topLeft = Offset(-size / 2, -size / 4),
        size = androidx.compose.ui.geometry.Size(size, size / 2)
    )
    drawContext.canvas.restore()
}

private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color
) {
    companion object {
        fun random(): ConfettiParticle {
            val colors = listOf(
                Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D),
                Color(0xFF95E1D3), Color(0xFFF38181), Color(0xFFAA96DA),
                Color(0xFFFCBAD3), Color(0xFFA8D8EA)
            )
            return ConfettiParticle(
                startX = Random.nextFloat() * 1000f,
                startY = -50f,
                velocityX = (Random.nextFloat() - 0.5f) * 2f,
                velocityY = Random.nextFloat() * 0.5f + 0.5f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 4f,
                size = Random.nextFloat() * 12f + 8f,
                color = colors.random()
            )
        }
    }
}
