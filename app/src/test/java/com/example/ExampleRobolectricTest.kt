package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.levels.PuzzleLevels
import com.example.data.model.PuzzleTipsCatalog
import com.example.data.repository.GameRepository
import com.example.game.GameViewModel
import com.example.game.PuzzleEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Color Connect", appName)
    }

    @Test
    fun `puzzle levels catalog contains 100 levels`() {
        val levels = PuzzleLevels.getAllLevels()
        assertEquals(100, levels.size)
        val level1 = PuzzleLevels.getLevel(1)
        assertNotNull(level1)
        assertTrue(level1.pairs.isNotEmpty())
    }

    @Test
    fun `puzzle engine handles hints and win`() {
        val level1 = PuzzleLevels.getLevel(1)
        val engine = PuzzleEngine(level1)
        val initial = engine.getBoardState()
        assertEquals(false, initial.isCompleted)

        // Apply hint until complete
        for (i in level1.pairs.indices) {
            engine.applyHint()
        }
        val finalState = engine.getBoardState()
        assertTrue(finalState.isCompleted)
    }

    @Test
    fun `repository and viewModel toggle night mode and light mode`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repo = GameRepository(app)
        val initial = repo.darkModeEnabled
        repo.darkModeEnabled = !initial
        assertEquals(!initial, repo.darkModeEnabled)
        repo.darkModeEnabled = initial

        val vm = GameViewModel(app)
        val vmInitial = vm.darkModeEnabled.value
        vm.toggleDarkMode()
        assertEquals(!vmInitial, vm.darkModeEnabled.value)
    }

    @Test
    fun `ads with tips catalog and claim ad reward awards hints and coins`() {
        assertTrue(PuzzleTipsCatalog.tips.isNotEmpty())
        val tip = PuzzleTipsCatalog.getRandomTip()
        assertNotNull(tip.fallbackTitle)
        assertNotNull(tip.fallbackDesc)

        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GameViewModel(app)
        val initialHints = vm.hints.value
        val initialCoins = vm.coins.value

        vm.showRewardedAd(tip)
        assertEquals(true, vm.showRewardedAdDialog.value)
        assertEquals(tip.fallbackTitle, vm.activeAdTip.value.fallbackTitle)

        vm.claimAdReward()
        assertEquals(false, vm.showRewardedAdDialog.value)
        assertEquals(initialHints + 1, vm.hints.value)
        assertEquals(initialCoins + 25, vm.coins.value)
    }
}
