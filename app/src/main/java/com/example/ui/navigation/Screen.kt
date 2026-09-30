package com.example.ui.navigation

sealed class Screen {
    object Home : Screen()
    object LevelSelect : Screen()
    data class Gameplay(val levelId: Int) : Screen()
    object Settings : Screen()
}
