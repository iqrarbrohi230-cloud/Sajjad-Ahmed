package com.example.ui.gameplay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.data.model.CellPos
import com.example.data.model.GameColor
import com.example.game.BoardState

@Composable
fun PuzzleBoardView(
    boardState: BoardState,
    activeColorId: Int?,
    onStartDrag: (CellPos) -> Unit,
    onDragMove: (CellPos) -> Unit,
    onEndDrag: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridSize = boardState.level.size
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    var currentTouchOffset by remember { mutableStateOf<Offset?>(null) }
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = colors.primary.copy(alpha = 0.3f))
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A))
            .border(2.dp, colors.outline.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(10.dp)
            .pointerInput(gridSize) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val cellW = size.width / gridSize
                        val cellH = size.height / gridSize
                        val cx = (offset.x / cellW).toInt().coerceIn(0, gridSize - 1)
                        val cy = (offset.y / cellH).toInt().coerceIn(0, gridSize - 1)
                        currentTouchOffset = offset
                        onStartDrag(CellPos(cx, cy))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentTouchOffset = change.position
                        val cellW = size.width / gridSize
                        val cellH = size.height / gridSize
                        val cx = (change.position.x / cellW).toInt().coerceIn(0, gridSize - 1)
                        val cy = (change.position.y / cellH).toInt().coerceIn(0, gridSize - 1)
                        onDragMove(CellPos(cx, cy))
                    },
                    onDragEnd = {
                        currentTouchOffset = null
                        onEndDrag()
                    },
                    onDragCancel = {
                        currentTouchOffset = null
                        onEndDrag()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellW = size.width / gridSize
            val cellH = size.height / gridSize

            // 1. Draw Grid Lines
            val gridColor = Color(0xFF1E293B)
            for (i in 1 until gridSize) {
                // Vertical lines
                drawLine(
                    color = gridColor,
                    start = Offset(i * cellW, 0f),
                    end = Offset(i * cellW, size.height),
                    strokeWidth = 2.dp.toPx()
                )
                // Horizontal lines
                drawLine(
                    color = gridColor,
                    start = Offset(0f, i * cellH),
                    end = Offset(size.width, i * cellH),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 2. Draw Paths
            val pathStrokeWidth = cellW * 0.38f
            val glowStrokeWidth = cellW * 0.58f

            for ((colorId, pathPoints) in boardState.paths) {
                if (pathPoints.size >= 2) {
                    val gameColor = GameColor.fromId(colorId)
                    val composePath = Path().apply {
                        val firstCenter = Offset(
                            pathPoints.first().x * cellW + cellW / 2f,
                            pathPoints.first().y * cellH + cellH / 2f
                        )
                        moveTo(firstCenter.x, firstCenter.y)
                        for (i in 1 until pathPoints.size) {
                            val pt = pathPoints[i]
                            val center = Offset(pt.x * cellW + cellW / 2f, pt.y * cellH + cellH / 2f)
                            lineTo(center.x, center.y)
                        }
                    }

                    // Outer Glow
                    drawPath(
                        path = composePath,
                        color = gameColor.glowColor,
                        style = Stroke(
                            width = glowStrokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Main Path Line
                    drawPath(
                        path = composePath,
                        color = gameColor.primaryColor,
                        style = Stroke(
                            width = pathStrokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Inner highlight
                    drawPath(
                        path = composePath,
                        color = gameColor.lightColor.copy(alpha = 0.5f),
                        style = Stroke(
                            width = pathStrokeWidth * 0.35f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // 3. Draw Endpoints (Colored Dots)
            for (pair in boardState.level.pairs) {
                val gameColor = GameColor.fromId(pair.colorId)
                val isConnected = boardState.connectedColors.contains(pair.colorId)
                val isActive = activeColorId == pair.colorId

                drawEndpoint(
                    pos = pair.start,
                    gameColor = gameColor,
                    cellW = cellW,
                    cellH = cellH,
                    isConnected = isConnected,
                    isActive = isActive,
                    pulse = pulseScale
                )

                drawEndpoint(
                    pos = pair.end,
                    gameColor = gameColor,
                    cellW = cellW,
                    cellH = cellH,
                    isConnected = isConnected,
                    isActive = isActive,
                    pulse = pulseScale
                )
            }
        }
    }
}

private fun DrawScope.drawEndpoint(
    pos: CellPos,
    gameColor: GameColor,
    cellW: Float,
    cellH: Float,
    isConnected: Boolean,
    isActive: Boolean,
    pulse: Float
) {
    val center = Offset(pos.x * cellW + cellW / 2f, pos.y * cellH + cellH / 2f)
    val baseRadius = cellW * 0.34f
    val radius = if (isActive) baseRadius * pulse else baseRadius

    // Outer glow
    drawCircle(
        color = gameColor.glowColor,
        radius = radius * 1.45f,
        center = center
    )

    // Solid Main Circle
    drawCircle(
        color = gameColor.primaryColor,
        radius = radius,
        center = center
    )

    // Inner Core Accent
    val innerRadius = if (isConnected) radius * 0.45f else radius * 0.35f
    drawCircle(
        color = Color.White.copy(alpha = if (isConnected) 0.9f else 0.55f),
        radius = innerRadius,
        center = center
    )
}
