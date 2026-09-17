package com.aaronsedna.hopecards.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aaronsedna.hopecards.model.AppSettings
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.ui.screens.HomeScreen
import com.aaronsedna.hopecards.ui.theme.HopeCardsTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CardDrawResponsivenessTest {
    @get:Rule val rule = createComposeRule()

    @Test fun returnTapAfterFourHundredMillisecondsIsNotDropped() {
        var draws = 0
        var completed = 0
        rule.setContent {
            HopeCardsTheme(ThemeName.CLASSIC) {
                HomeScreen(
                    verse = Verse("test", "hope", "A verse for the tap test.", "John 3:16", "BSB", emptyList(), Translation.BSB),
                    favorite = false,
                    settings = AppSettings(enableHaptics = false),
                    onNextVerse = { draws++ },
                    onFavorite = {},
                    onShare = {},
                    onCompletedCard = { completed++ },
                    onChangeTranslation = {},
                )
            }
        }
        rule.mainClock.autoAdvance = false
        rule.onNodeWithText("Draw a Card").performClick()
        rule.mainClock.advanceTimeBy(400)
        rule.onNodeWithText("Return to Deck").performClick()
        rule.mainClock.advanceTimeBy(400)
        rule.runOnIdle {
            assertEquals("One new card per draw", 1, draws)
            assertEquals("Return tap should complete, not disappear during spring settling", 1, completed)
        }
        rule.onNodeWithText("Draw a Card").assertExists()
    }
}
