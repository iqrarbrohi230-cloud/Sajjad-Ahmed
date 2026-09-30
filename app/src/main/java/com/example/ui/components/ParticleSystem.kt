package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color
)

@Composable
fun ParticleExplosion(
    modifier: Modifier = Modifier,
    particleCount: Int = 45,
    colors: List<Color> = listOf(
        Color(0xFFFF334B),
        Color(0xFF0084FF),
        Color(0xFF00E676),
        Color(0xFFFFD600),
        Color(0xFFFF6D00),
        Color(0xFFD500F9)
    )
) {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        List(particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 450f + 150f
            Particle(
                x = 0f,
                y = 0f,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed - 100f,
                size = Random.nextFloat() * 10f + 6f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val t = progress.value
        val gravity = 350f * t * t
        val alpha = (1f - t).coerceIn(0f, 1f)

        particles.forEach { p ->
            val curX = centerX + p.vx * t
            val curY = centerY + p.vy * t + gravity
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size * (1f - t * 0.5f),
                center = Offset(curX, curY)
            )
        }
    }
}
