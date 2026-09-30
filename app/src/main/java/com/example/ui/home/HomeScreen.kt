package com.example.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameViewModel
import com.example.ui.components.CoinPill
import com.example.ui.components.GameButton
import com.example.ui.localization.GameStrings
import com.example.ui.navigation.Screen

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val coins by viewModel.coins.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val musicEnabled by viewModel.musicEnabled.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()
    val lang by viewModel.language.collectAsState()
    val levelProgressList by viewModel.levelProgressList.collectAsState()

    val colors = MaterialTheme.colorScheme

    val totalStars = levelProgressList.sumOf { it.stars }
    val completedCount = levelProgressList.count { it.completed }
    val nextPlayLevel = (levelProgressList.maxOfOrNull { it.levelId }?.plus(1) ?: 1).coerceAtMost(100)

    val infiniteTransition = rememberInfiniteTransition(label = "pulseButton")
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btnPulse"
    )

    val bgBrush = if (darkModeEnabled) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF090D16),
                Color(0xFF0F172A),
                Color(0xFF0A0F1D)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF1F5F9),
                Color(0xFFE2E8F0),
                Color(0xFFF8FAFC)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar with Stats & Quick Audio + Theme Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoinPill(coins = coins)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick Light / Night Mode toggle
                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = if (darkModeEnabled) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = if (darkModeEnabled) GameStrings.get("night_mode", lang) else GameStrings.get("light_mode", lang),
                                tint = if (darkModeEnabled) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Sound toggle
                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleSound() },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = GameStrings.get("sound", lang),
                                tint = if (soundEnabled) colors.primary else colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Music toggle
                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleMusic() },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = if (musicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = GameStrings.get("music", lang),
                                tint = if (musicEnabled) colors.primary else colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Settings Icon
                    Surface(
                        shape = CircleShape,
                        color = colors.surfaceVariant,
                        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                    ) {
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.Settings) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = GameStrings.get("settings", lang),
                                tint = colors.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Hero Art & Title Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Generated Hero Game Art
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(180.dp)
                        .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = colors.primary)
                        .clip(RoundedCornerShape(24.dp)),
                    border = BorderStroke(1.5.dp, colors.primary.copy(alpha = 0.5f)),
                    color = colors.surface
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hero_color_connect_1790761731979),
                        contentDescription = "Color Connect Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "COLOR CONNECT",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = colors.onSurface
                )

                Text(
                    text = "2D PUZZLE ADVENTURE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = colors.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Banner (Stars & Levels Completed)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$totalStars Stars",
                                color = colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(16.dp)
                                .background(colors.outline.copy(alpha = 0.5f))
                        )

                        Text(
                            text = "$completedCount / 100 Levels",
                            color = colors.onSurfaceVariant,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ad with tips banner
                com.example.ui.ads.TipAdBanner(
                    lang = lang,
                    onWatchAdClicked = { viewModel.showRewardedAd() },
                    modifier = Modifier.fillMaxWidth(0.92f)
                )
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Play Button (Pulsing)
                GameButton(
                    text = "${GameStrings.get("play", lang)} (Lv $nextPlayLevel)",
                    onClick = { viewModel.navigateTo(Screen.Gameplay(nextPlayLevel)) },
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .scale(buttonPulse),
                    icon = Icons.Default.PlayArrow,
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF2563EB), Color(0xFF06B6D4), Color(0xFF3B82F6))
                    )
                )

                // Level Select Button
                GameButton(
                    text = GameStrings.get("select_level", lang),
                    onClick = { viewModel.navigateTo(Screen.LevelSelect) },
                    modifier = Modifier.fillMaxWidth(0.88f),
                    icon = Icons.Default.GridView,
                    gradient = if (darkModeEnabled) {
                        Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                    },
                    textColor = colors.onSurface
                )

                // Settings Button
                GameButton(
                    text = GameStrings.get("settings", lang),
                    onClick = { viewModel.navigateTo(Screen.Settings) },
                    modifier = Modifier.fillMaxWidth(0.88f),
                    icon = Icons.Default.Settings,
                    gradient = if (darkModeEnabled) {
                        Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
                    },
                    textColor = colors.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
