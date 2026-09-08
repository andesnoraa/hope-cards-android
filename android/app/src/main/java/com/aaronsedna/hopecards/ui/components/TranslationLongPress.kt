package com.aaronsedna.hopecards.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role

/** A quiet, long-press-only affordance for displayed Bible translation names. */
@Composable
fun Modifier.changeTranslationOnLongPress(
    hapticsEnabled: Boolean,
    onLongPress: () -> Unit,
): Modifier {
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    return combinedClickable(
        interactionSource = interactionSource,
        indication = null,
        role = Role.Button,
        onClickLabel = null,
        onLongClickLabel = "Change Bible translation",
        onLongClick = {
            if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onLongPress()
        },
        onClick = {},
    )
}
