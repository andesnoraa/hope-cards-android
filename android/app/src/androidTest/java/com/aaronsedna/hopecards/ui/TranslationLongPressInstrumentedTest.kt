package com.aaronsedna.hopecards.ui

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.platform.testTag
import com.aaronsedna.hopecards.ui.components.changeTranslationOnLongPress
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TranslationLongPressInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun normalTapDoesNothingAndLongPressOpensSelector() {
        var requests = 0
        composeRule.setContent {
            Text(
                text = "Berean Standard Bible (BSB)",
                modifier = Modifier
                    .testTag("translation")
                    .changeTranslationOnLongPress(hapticsEnabled = false) { requests += 1 },
            )
        }

        composeRule.onNodeWithTag("translation").performClick()
        composeRule.runOnIdle { assertEquals(0, requests) }

        composeRule.onNodeWithTag("translation").performTouchInput { longClick() }
        composeRule.runOnIdle { assertEquals(1, requests) }
    }
}
