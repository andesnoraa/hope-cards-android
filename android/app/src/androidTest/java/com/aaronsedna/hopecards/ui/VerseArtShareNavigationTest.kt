package com.aaronsedna.hopecards.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.aaronsedna.hopecards.MainActivity
import com.aaronsedna.hopecards.data.AppRepository
import com.aaronsedna.hopecards.model.Destination
import com.aaronsedna.hopecards.model.Translation
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class VerseArtShareNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun cancelledShareReturnsToArtworkAndBothBackControlsReturnToGallery(): Unit = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val repository = AppRepository(instrumentation.targetContext)
        repository.initialize()
        val original = repository.currentSettings()
        try {
            repository.updateSettings { it.copy(preferredTranslation = Translation.BSB) }
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { it.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
                compose.waitUntil(15_000) {
                    runCatching { compose.onNodeWithContentDescription("Open navigation").assertIsDisplayed() }.isSuccess
                }
                scenario.onActivity { ViewModelProvider(it)[HopeCardsViewModel::class.java].navigate(Destination.VERSE_ART) }
                compose.waitUntil(15_000) { runCatching { compose.onNodeWithTag("art-category-peace").assertIsDisplayed() }.isSuccess }
                compose.onNodeWithTag("art-category-peace").performClick()
                compose.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-peace"))
                compose.onNodeWithTag("art-open-peace").performClick()
                compose.onNodeWithTag("art-share").performScrollTo().performClick()
                // Samsung's full-height Sharesheet stops the activity; other devices only pause it.
                compose.waitUntil(5_000) { scenario.state != Lifecycle.State.RESUMED }
                compose.waitUntil(5_000) {
                    instrumentation.uiAutomation.rootInActiveWindow?.packageName?.toString() in
                        setOf("com.android.intentresolver", "android")
                }
                back()
                compose.waitUntil(5_000) { scenario.state == Lifecycle.State.RESUMED }
                compose.onNodeWithTag("art-detail-image").assertIsDisplayed()
                // Background/foreground alone must never finish the artwork or display an ad.
                scenario.moveToState(Lifecycle.State.CREATED)
                scenario.moveToState(Lifecycle.State.RESUMED)
                compose.onNodeWithTag("art-detail-image").assertIsDisplayed()
                compose.onNodeWithContentDescription("Back").performClick()
                compose.onNodeWithTag("art-gallery").assertIsDisplayed()
                compose.onNodeWithTag("art-gallery").performScrollToNode(hasTestTag("art-open-peace"))
                compose.onNodeWithTag("art-open-peace").performClick()
                scenario.recreate()
                compose.waitUntil(15_000) { runCatching { compose.onNodeWithTag("art-detail-image").assertIsDisplayed() }.isSuccess }
                compose.onNodeWithTag("art-detail-image").assertIsDisplayed()
                back()
                compose.waitUntil(5_000) {
                    runCatching { compose.onNodeWithTag("art-gallery").assertIsDisplayed() }.isSuccess
                }
            }
        } finally { repository.replaceSettings(original) }
    }

    private fun back() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("input keyevent KEYCODE_BACK").use {
            java.io.FileInputStream(it.fileDescriptor).use { stream -> stream.readBytes() }
        }
    }
}
