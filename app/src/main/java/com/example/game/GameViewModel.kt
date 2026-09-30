package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioManager
import com.example.data.db.LevelEntity
import com.example.data.levels.PuzzleLevels
import com.example.data.model.CellPos
import com.example.data.model.PuzzleLevel
import com.example.data.repository.GameRepository
import com.example.haptics.VibrationManager
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)
    val audioManager = AudioManager()
    val vibrationManager = VibrationManager(application)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private var puzzleEngine: PuzzleEngine = PuzzleEngine(PuzzleLevels.getLevel(1))

    private val _boardState = MutableStateFlow(puzzleEngine.getBoardState())
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    private val _activeDrawingColorId = MutableStateFlow<Int?>(null)
    val activeDrawingColorId: StateFlow<Int?> = _activeDrawingColorId.asStateFlow()

    private val _showWinDialog = MutableStateFlow(false)
    val showWinDialog: StateFlow<Boolean> = _showWinDialog.asStateFlow()

    private val _showRewardedAdDialog = MutableStateFlow(false)
    val showRewardedAdDialog: StateFlow<Boolean> = _showRewardedAdDialog.asStateFlow()

    private val _activeAdTip = MutableStateFlow(com.example.data.model.PuzzleTipsCatalog.getTip(0))
    val activeAdTip: StateFlow<com.example.data.model.PuzzleTip> = _activeAdTip.asStateFlow()

    private val _winCoinsEarned = MutableStateFlow(0)
    val winCoinsEarned: StateFlow<Int> = _winCoinsEarned.asStateFlow()

    private val _soundEnabled = MutableStateFlow(repository.soundEnabled)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _musicEnabled = MutableStateFlow(repository.musicEnabled)
    val musicEnabled: StateFlow<Boolean> = _musicEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(repository.vibrationEnabled)
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _darkModeEnabled = MutableStateFlow(repository.darkModeEnabled)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    private val _language = MutableStateFlow(repository.language)
    val language: StateFlow<String> = _language.asStateFlow()

    val coins: StateFlow<Int> = repository.coinsFlow
    val hints: StateFlow<Int> = repository.hintsFlow

    val levelProgressList: StateFlow<List<LevelEntity>> = repository.allLevelProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        audioManager.isSoundEnabled = repository.soundEnabled
        audioManager.isMusicEnabled = repository.musicEnabled
        vibrationManager.isVibrationEnabled = repository.vibrationEnabled

        if (repository.musicEnabled) {
            audioManager.startBackgroundMusic()
        }
    }

    fun navigateTo(screen: Screen) {
        audioManager.playClick()
        if (screen is Screen.Gameplay) {
            loadLevel(screen.levelId)
        }
        _currentScreen.value = screen
    }

    fun loadLevel(levelId: Int) {
        val level = PuzzleLevels.getLevel(levelId)
        puzzleEngine.reset(level)
        _activeDrawingColorId.value = null
        _showWinDialog.value = false
        _winCoinsEarned.value = 0
        _boardState.value = puzzleEngine.getBoardState()
    }

    fun startDrag(pos: CellPos) {
        val colorId = puzzleEngine.startDrag(pos)
        _activeDrawingColorId.value = colorId
        if (colorId != null) {
            audioManager.playDotSelect(colorId)
            vibrationManager.vibrateTick()
        }
        _boardState.value = puzzleEngine.getBoardState()
    }

    fun dragTo(pos: CellPos) {
        val success = puzzleEngine.dragTo(
            targetPos = pos,
            onConnected = {
                audioManager.playConnect()
                vibrationManager.vibrateConnect()
            },
            onStep = { stepIdx ->
                audioManager.playStep(stepIdx)
                vibrationManager.vibrateTick()
            }
        )

        val updatedState = puzzleEngine.getBoardState()
        _boardState.value = updatedState

        if (updatedState.isCompleted && !_showWinDialog.value) {
            handleLevelComplete(updatedState)
        }
    }

    fun endDrag() {
        puzzleEngine.endDrag()
        _activeDrawingColorId.value = null
        val updatedState = puzzleEngine.getBoardState()
        _boardState.value = updatedState
        if (updatedState.isCompleted && !_showWinDialog.value) {
            handleLevelComplete(updatedState)
        }
    }

    private fun handleLevelComplete(state: BoardState) {
        val stars = state.stars
        val coinsGained = stars * 10 + 5
        _winCoinsEarned.value = coinsGained
        _showWinDialog.value = true

        audioManager.playWin()
        vibrationManager.vibrateWin()

        viewModelScope.launch {
            repository.saveLevelProgress(
                levelId = state.level.id,
                stars = stars,
                moves = state.movesCount,
                timeSeconds = 0
            )
            repository.addCoins(coinsGained)
        }
    }

    fun restartLevel() {
        audioManager.playClick()
        val curLevel = _boardState.value.level
        loadLevel(curLevel.id)
    }

    fun undo() {
        if (puzzleEngine.undo()) {
            audioManager.playClick()
            vibrationManager.vibrateTick()
            _boardState.value = puzzleEngine.getBoardState()
        }
    }

    fun useHint() {
        if (repository.spendHint()) {
            audioManager.playHint()
            vibrationManager.vibrateConnect()
            puzzleEngine.applyHint()
            val state = puzzleEngine.getBoardState()
            _boardState.value = state
            if (state.isCompleted && !_showWinDialog.value) {
                handleLevelComplete(state)
            }
        } else {
            audioManager.playError()
            vibrationManager.vibrateError()
        }
    }

    fun buyHint(): Boolean {
        val success = repository.buyHint()
        if (success) {
            audioManager.playClick()
            vibrationManager.vibrateConnect()
        } else {
            audioManager.playError()
        }
        return success
    }

    fun showRewardedAd(tip: com.example.data.model.PuzzleTip? = null) {
        audioManager.playClick()
        _activeAdTip.value = tip ?: com.example.data.model.PuzzleTipsCatalog.getRandomTip()
        _showRewardedAdDialog.value = true
    }

    fun dismissRewardedAd() {
        _showRewardedAdDialog.value = false
    }

    fun claimAdReward() {
        _showRewardedAdDialog.value = false
        repository.addHints(1)
        repository.addCoins(25)
        audioManager.playWin()
        vibrationManager.vibrateWin()
    }

    fun nextLevel() {
        audioManager.playClick()
        val nextId = (_boardState.value.level.id + 1).coerceAtMost(100)
        navigateTo(Screen.Gameplay(nextId))
    }

    fun toggleSound() {
        val next = !_soundEnabled.value
        _soundEnabled.value = next
        repository.soundEnabled = next
        audioManager.isSoundEnabled = next
        if (next) audioManager.playClick()
    }

    fun toggleMusic() {
        val next = !_musicEnabled.value
        _musicEnabled.value = next
        repository.musicEnabled = next
        audioManager.isMusicEnabled = next
    }

    fun toggleVibration() {
        val next = !_vibrationEnabled.value
        _vibrationEnabled.value = next
        repository.vibrationEnabled = next
        vibrationManager.isVibrationEnabled = next
        if (next) vibrationManager.vibrateTick()
    }

    fun toggleDarkMode() {
        val next = !_darkModeEnabled.value
        _darkModeEnabled.value = next
        repository.darkModeEnabled = next
        audioManager.playClick()
        if (_vibrationEnabled.value) vibrationManager.vibrateTick()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        repository.language = lang
        audioManager.playClick()
    }

    fun resetAllProgress() {
        audioManager.playClick()
        viewModelScope.launch {
            repository.resetAllProgress()
            loadLevel(1)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.stopBackgroundMusic()
    }
}
