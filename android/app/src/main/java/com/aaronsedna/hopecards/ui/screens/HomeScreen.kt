package com.aaronsedna.hopecards.ui.screens

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.model.AppSettings
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Verse
import com.aaronsedna.hopecards.ui.components.CardBack
import com.aaronsedna.hopecards.ui.components.VerseCardFace
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.LocalHopeThemeName
import com.aaronsedna.hopecards.ui.theme.Poppins
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private const val CARD_WIDTH = 345f
private const val CARD_HEIGHT = 540f
private const val STACK_WIDTH = 355f
private const val STACK_HEIGHT = 550f

@Composable
fun HomeScreen(
    verse: Verse,
    favorite: Boolean,
    settings: AppSettings,
    onNextVerse: () -> Unit,
    onFavorite: () -> Unit,
    onShare: () -> Unit,
    onCompletedCard: (Activity) -> Unit,
) {
    val colors = LocalHopeColors.current
    val activity = LocalActivity.current ?: return
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var revealed by remember { mutableStateOf(false) }
    val rotation = remember { Animatable(0f) }
    val cardTranslationY = remember { Animatable(0f) }
    val cardScale = remember { Animatable(1f) }
    val deckInteractionSource = remember { MutableInteractionSource() }
    val interactionSource = remember { MutableInteractionSource() }
    val buttonPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        if (buttonPressed) .96f else 1f,
        spring(dampingRatio = .7f, stiffness = 260f),
        label = "drawButtonScale",
    )
    val cardSpring = spring<Float>(dampingRatio = .86f, stiffness = 170f)

    fun flip() {
        if (settings.enableHaptics) {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
        scope.launch {
            if (revealed) {
                revealed = false
                launch { rotation.animateTo(0f, cardSpring) }
                launch { cardTranslationY.animateTo(0f, cardSpring) }
                launch { cardScale.animateTo(1f, cardSpring) }
                onCompletedCard(activity)
            } else {
                onNextVerse()
                revealed = true
                launch { cardTranslationY.animateTo(-20f, cardSpring) }
                launch { cardScale.animateTo(1.02f, cardSpring) }
                delay(120)
                rotation.animateTo(180f, cardSpring)
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        val buttonSpace = if (settings.showDrawButton) 76f else 0f
        val targetWidth = min(350f, min(maxWidth.value * .92f, maxWidth.value - 16f)).coerceAtLeast(190f)
        val availableHeight = (maxHeight.value - 24f - 20f - buttonSpace).coerceAtLeast(280f)
        val scale = min(targetWidth / STACK_WIDTH, availableHeight / STACK_HEIGHT).coerceAtMost(1f)
        val stackWidth = (STACK_WIDTH * scale).dp
        val stackHeight = (STACK_HEIGHT * scale).dp

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Box(Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                // Matches the released app while the deck itself keeps a descriptive click label.
            }
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(stackWidth, stackHeight)
                    .clickable(
                        interactionSource = deckInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = if (revealed) "Return card to deck" else "Draw a card",
                        onClick = ::flip,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                StackCard(
                    Modifier.requiredSize(CARD_WIDTH.dp, CARD_HEIGHT.dp)
                        .offset(x = (-4 * scale).dp, y = (-6 * scale).dp)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin.Center
                            scaleX = scale
                            scaleY = scale
                            rotationZ = -1f
                        },
                )
                StackCard(
                    Modifier.requiredSize(CARD_WIDTH.dp, CARD_HEIGHT.dp)
                        .offset(x = (-2 * scale).dp, y = (-2 * scale).dp)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin.Center
                            scaleX = scale
                            scaleY = scale
                            rotationZ = -.6f
                        },
                )
                Box(
                    Modifier.requiredSize(CARD_WIDTH.dp, CARD_HEIGHT.dp)
                        .graphicsLayer {
                            translationY = cardTranslationY.value * density * scale
                            scaleX = scale * cardScale.value
                            scaleY = scale * cardScale.value
                            rotationY = rotation.value
                            cameraDistance = 14f * density
                        },
                ) {
                    if (rotation.value <= 90f) {
                        CardBack(Modifier.fillMaxSize())
                    } else {
                        VerseCardFace(
                            verse = verse,
                            favorite = favorite,
                            onFavorite = onFavorite,
                            onShare = onShare,
                            modifier = Modifier.fillMaxSize().graphicsLayer { rotationY = 180f },
                        )
                    }
                }
            }
            if (settings.showDrawButton) {
                Spacer(Modifier.height(20.dp))
                Surface(
                    onClick = ::flip,
                    interactionSource = interactionSource,
                    modifier = Modifier.width(stackWidth).height(56.dp).graphicsLayer {
                        scaleX = buttonScale
                        scaleY = buttonScale
                    },
                    color = colors.buttonBackground,
                    contentColor = colors.buttonText,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, colors.buttonBorder),
                    shadowElevation = 5.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            if (revealed) "Return to Deck" else "Draw a Card",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            letterSpacing = .5.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StackCard(modifier: Modifier) {
    val colors = LocalHopeColors.current
    val leatherBound = LocalHopeThemeName.current in setOf(ThemeName.VINTAGE_HERITAGE, ThemeName.SERENITY)
    Surface(
        modifier = modifier,
        color = colors.cardBack,
        shape = RoundedCornerShape(if (leatherBound) 24.dp else 34.dp),
        border = BorderStroke(1.5.dp, colors.cardBackAccent),
        shadowElevation = 2.dp,
    ) {}
}
