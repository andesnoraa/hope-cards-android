package com.aaronsedna.hopecards.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReflectionCopyInstrumentedTest {
    @Test
    fun everyCategoryUsesACalmStatementInsteadOfAQuestion() {
        assertEquals(14, HopeCardsViewModel.prompts.size)
        HopeCardsViewModel.prompts.values.forEach { invitation ->
            assertTrue(invitation.isNotBlank())
            assertFalse(invitation.contains('?'))
        }
    }
}
