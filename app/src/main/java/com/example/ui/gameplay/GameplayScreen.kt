package com.example.ui.gameplay

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameViewModel
import com.example.ui.components.CoinPill
import com.example.ui.localization.GameStrings
import com.example.ui.navigation.Screen

@Composable
fun GameplayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val boardState by viewModel.boardState.collectAsState()
    val activeColorId by viewModel.activeDrawingColorId.collectAsState()
    val showWinDialog by viewModel.showWinDialog.collectAsState()
    val winCoins by viewModel.winCoinsEarned.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val hints by viewModel.hints.collectAsState()
    val lang by viewModel.language.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

    val colors = MaterialTheme.colorScheme

    var showBuyHintDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateTo(Screen.LevelSelect)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.surfaceVariant,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                ) {
                    IconButton(onClick = { viewModel.navigateTo(Screen.LevelSelect) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = GameStrings.get("back", lang),
                            tint = colors.onSurface
                        )
                    }
                }

                // Level & Size Tag
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${GameStrings.get("level", lang)} ${boardState.level.id}",
                        color = colors.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${boardState.level.size}x${boardState.level.size} • ${boardState.level.difficultyCategory}",
                        color = colors.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Theme Toggle in gameplay
                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (darkModeEnabled) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = if (darkModeEnabled) GameStrings.get("night_mode", lang) else GameStrings.get("light_mode", lang),
                                tint = if (darkModeEnabled) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    CoinPill(coins = coins)
                }
            }

            // Stats Card (Moves & Flow Coverage)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${GameStrings.get("moves", lang)}: ${boardState.movesCount} / ${boardState.level.parMoves}",
                            color = colors.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${GameStrings.get("pipe", lang)}: ${boardState.connectedColors.size}/${boardState.level.pairs.size} (${boardState.coveragePercent}%)",
                            color = colors.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val animatedProgress by animateFloatAsState(
                        targetValue = (boardState.coveragePercent / 100f).coerceIn(0f, 1f),
                        label = "coverageProgress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = colors.primary,
                        trackColor = colors.surfaceVariant
                    )
                }
            }

            // 2D Puzzle Canvas Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentAlignment = Alignment.Center
            ) {
                PuzzleBoardView(
                    boardState = boardState,
                    activeColorId = activeColorId,
                    onStartDrag = { pos -> viewModel.startDrag(pos) },
                    onDragMove = { pos -> viewModel.dragTo(pos) },
                    onEndDrag = { viewModel.endDrag() }
                )
            }

            // Controls Bar (Restart, Undo, Hint)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Restart Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                ) {
                    IconButton(
                        onClick = { viewModel.restartLevel() },
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = GameStrings.get("restart", lang),
                            tint = colors.onSurfaceVariant
                        )
                    }
                }

                // Undo Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                ) {
                    IconButton(
                        onClick = { viewModel.undo() },
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = GameStrings.get("undo", lang),
                            tint = colors.onSurfaceVariant
                        )
                    }
                }

                // Hint Button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.primaryContainer.copy(alpha = if (darkModeEnabled) 0.35f else 0.5f),
                    border = BorderStroke(1.5.dp, colors.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .height(54.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (hints > 0) {
                                    viewModel.useHint()
                                } else {
                                    showBuyHintDialog = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = GameStrings.get("hints", lang),
                                tint = colors.primary
                            )
                        }
                        Text(
                            text = "$hints",
                            color = colors.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Watch Ad for Free Hint with tips button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF59E0B).copy(alpha = if (darkModeEnabled) 0.2f else 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .height(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.showRewardedAd() }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = GameStrings.get("watch_ad", lang),
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+1",
                            color = Color(0xFFF59E0B),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Buy Hint Dialog
        if (showBuyHintDialog) {
            AlertDialog(
                onDismissRequest = { showBuyHintDialog = false },
                containerColor = colors.surface,
                title = {
                    Text(
                        text = GameStrings.get("hints", lang),
                        color = colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = GameStrings.get("need_hints", lang),
                        color = colors.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                showBuyHintDialog = false
                                viewModel.showRewardedAd()
                            }
                        ) {
                            Text(
                                text = GameStrings.get("watch_ad", lang),
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = {
                                viewModel.buyHint()
                                showBuyHintDialog = false
                            }
                        ) {
                            Text(
                                text = GameStrings.get("buy", lang),
                                color = colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBuyHintDialog = false }) {
                        Text(
                            text = GameStrings.get("cancel", lang),
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            )
        }

        // Win Dialog
        if (showWinDialog) {
            WinDialog(
                levelId = boardState.level.id,
                stars = boardState.stars,
                moves = boardState.movesCount,
                parMoves = boardState.level.parMoves,
                coinsEarned = winCoins,
                lang = lang,
                onWatchAdBonus = { viewModel.showRewardedAd() },
                onNextLevel = { viewModel.nextLevel() },
                onRetry = { viewModel.restartLevel() },
                onHome = { viewModel.navigateTo(Screen.Home) }
            )
        }
    }
}
