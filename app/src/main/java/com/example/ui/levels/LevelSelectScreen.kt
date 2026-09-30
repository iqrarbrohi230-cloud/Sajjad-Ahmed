package com.example.ui.levels

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameViewModel
import com.example.ui.components.CoinPill
import com.example.ui.localization.GameStrings
import com.example.ui.navigation.Screen

@Composable
fun LevelSelectScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val levelProgressList by viewModel.levelProgressList.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val lang by viewModel.language.collectAsState()
    val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

    val colors = MaterialTheme.colorScheme

    var selectedPackIndex by remember { mutableIntStateOf(0) }

    val packs = listOf(
        GameStrings.get("beginner_pack", lang) to (1..25),
        GameStrings.get("easy_pack", lang) to (26..50),
        GameStrings.get("medium_pack", lang) to (51..75),
        GameStrings.get("hard_pack", lang) to (76..100)
    )

    val progressMap = remember(levelProgressList) {
        levelProgressList.associateBy { it.levelId }
    }

    val maxUnlockedLevel = remember(levelProgressList) {
        val completedMax = levelProgressList.filter { it.completed }.maxOfOrNull { it.levelId } ?: 0
        (completedMax + 1).coerceAtMost(100)
    }

    BackHandler {
        viewModel.navigateTo(Screen.Home)
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
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
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = GameStrings.get("back", lang),
                            tint = colors.onSurface
                        )
                    }
                }

                Text(
                    text = GameStrings.get("select_level", lang),
                    color = colors.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                CoinPill(coins = coins)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pack Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedPackIndex,
                containerColor = colors.surface,
                contentColor = colors.primary,
                edgePadding = 0.dp,
                divider = {}
            ) {
                packs.forEachIndexed { index, (title, _) ->
                    Tab(
                        selected = selectedPackIndex == index,
                        onClick = { selectedPackIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = if (selectedPackIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedPackIndex == index) colors.primary else colors.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of 25 levels for the selected pack
            val currentLevels = packs[selectedPackIndex].second.toList()

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(currentLevels) { levelId ->
                    val isUnlocked = levelId <= maxUnlockedLevel
                    val entity = progressMap[levelId]
                    val isCompleted = entity?.completed == true
                    val stars = entity?.stars ?: 0
                    val isCurrent = levelId == maxUnlockedLevel

                    LevelCard(
                        levelId = levelId,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        stars = stars,
                        isCurrent = isCurrent,
                        isDark = darkModeEnabled,
                        onClick = {
                            if (isUnlocked) {
                                viewModel.navigateTo(Screen.Gameplay(levelId))
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tip Ad Banner at bottom of Level Select
            com.example.ui.ads.TipAdBanner(
                lang = lang,
                onWatchAdClicked = { viewModel.showRewardedAd() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LevelCard(
    levelId: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    stars: Int,
    isCurrent: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    val borderColor = when {
        isCurrent -> colors.primary
        isCompleted -> colors.secondary.copy(alpha = 0.7f)
        isUnlocked -> colors.outline.copy(alpha = 0.6f)
        else -> colors.outline.copy(alpha = 0.25f)
    }

    val backgroundColor = when {
        isCurrent -> colors.primaryContainer.copy(alpha = if (isDark) 0.6f else 0.4f)
        isCompleted -> colors.surface
        isUnlocked -> colors.surface
        else -> colors.surfaceVariant.copy(alpha = if (isDark) 0.4f else 0.6f)
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(if (isCurrent) 6.dp else 1.dp, RoundedCornerShape(16.dp), spotColor = colors.primary)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isUnlocked, onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isCurrent) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = colors.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "$levelId",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) colors.primary else colors.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Stars row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= stars) Color(0xFFFFD700) else colors.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}
