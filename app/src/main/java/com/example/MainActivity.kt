package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameViewModel
import com.example.ui.ads.RewardedAdDialog
import com.example.ui.gameplay.GameplayScreen
import com.example.ui.home.HomeScreen
import com.example.ui.levels.LevelSelectScreen
import com.example.ui.navigation.Screen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.ColorConnectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val darkModeEnabled by viewModel.darkModeEnabled.collectAsState()

            ColorConnectTheme(darkTheme = darkModeEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ColorConnectApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ColorConnectApp(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val showRewardedAd by viewModel.showRewardedAdDialog.collectAsState()
    val activeAdTip by viewModel.activeAdTip.collectAsState()
    val lang by viewModel.language.collectAsState()

    Crossfade(
        targetState = currentScreen,
        animationSpec = tween(durationMillis = 250),
        label = "screenTransition"
    ) { screen ->
        when (screen) {
            is Screen.Home -> HomeScreen(viewModel = viewModel)
            is Screen.LevelSelect -> LevelSelectScreen(viewModel = viewModel)
            is Screen.Gameplay -> GameplayScreen(viewModel = viewModel)
            is Screen.Settings -> SettingsScreen(viewModel = viewModel)
        }
    }

    if (showRewardedAd) {
        RewardedAdDialog(
            tip = activeAdTip,
            lang = lang,
            onRewardClaimed = { viewModel.claimAdReward() },
            onDismiss = { viewModel.dismissRewardedAd() }
        )
    }
}
