package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.AppDatabase
import com.example.data.db.LevelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class GameRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val levelDao = db.levelDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("color_connect_prefs", Context.MODE_PRIVATE)

    val allLevelProgress: Flow<List<LevelEntity>> = levelDao.getAllLevels()

    private val _coinsFlow = MutableStateFlow(prefs.getInt("coins", 50))
    val coinsFlow = _coinsFlow.asStateFlow()

    private val _hintsFlow = MutableStateFlow(prefs.getInt("hints", 3))
    val hintsFlow = _hintsFlow.asStateFlow()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var musicEnabled: Boolean
        get() = prefs.getBoolean("music_enabled", true)
        set(value) = prefs.edit().putBoolean("music_enabled", value).apply()

    var vibrationEnabled: Boolean
        get() = prefs.getBoolean("vibration_enabled", true)
        set(value) = prefs.edit().putBoolean("vibration_enabled", value).apply()

    var darkModeEnabled: Boolean
        get() = prefs.getBoolean("dark_mode_enabled", true)
        set(value) = prefs.edit().putBoolean("dark_mode_enabled", value).apply()

    var language: String
        get() = prefs.getString("game_language", "en") ?: "en"
        set(value) = prefs.edit().putString("game_language", value).apply()

    var lastPlayedLevel: Int
        get() = prefs.getInt("last_played_level", 1)
        set(value) = prefs.edit().putInt("last_played_level", value).apply()

    fun addCoins(amount: Int) {
        val newCoins = (_coinsFlow.value + amount).coerceAtLeast(0)
        _coinsFlow.value = newCoins
        prefs.edit().putInt("coins", newCoins).apply()
    }

    fun addHints(amount: Int) {
        val newHints = (_hintsFlow.value + amount).coerceAtLeast(0)
        _hintsFlow.value = newHints
        prefs.edit().putInt("hints", newHints).apply()
    }

    fun spendHint(): Boolean {
        if (_hintsFlow.value > 0) {
            val newHints = _hintsFlow.value - 1
            _hintsFlow.value = newHints
            prefs.edit().putInt("hints", newHints).apply()
            return true
        } else if (_coinsFlow.value >= 25) {
            addCoins(-25)
            return true
        }
        return false
    }

    fun buyHint(): Boolean {
        if (_coinsFlow.value >= 25) {
            addCoins(-25)
            val newHints = _hintsFlow.value + 1
            _hintsFlow.value = newHints
            prefs.edit().putInt("hints", newHints).apply()
            return true
        }
        return false
    }

    suspend fun saveLevelProgress(
        levelId: Int,
        stars: Int,
        moves: Int,
        timeSeconds: Int
    ) = withContext(Dispatchers.IO) {
        levelDao.insertOrUpdate(
            LevelEntity(
                levelId = levelId,
                completed = true,
                stars = stars,
                bestMoves = moves,
                timeSpentSeconds = timeSeconds
            )
        )
        lastPlayedLevel = (levelId + 1).coerceAtMost(100)
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        levelDao.clearAll()
        _coinsFlow.value = 50
        _hintsFlow.value = 3
        prefs.edit()
            .putInt("coins", 50)
            .putInt("hints", 3)
            .putInt("last_played_level", 1)
            .apply()
    }
}
