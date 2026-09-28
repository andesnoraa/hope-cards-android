package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.QuizReviewContent
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.interfaceFontFor

private val reviewSuccess = Color(0xFF206641)
private val reviewIncorrect = Color(0xFF97352D)
private val reviewMuted = Color(0xFF5B6578)
private val reviewSuccessSurface = Color(0xFFEDF6EF)
private val reviewIncorrectSurface = Color(0xFFFCF0EC)
private val reviewNeutralSurface = Color(0xFFF8F6F2)

/** Mirrors the PDF's summary and card hierarchy, while retaining scalable native text. */
@Composable
internal fun QuizReviewSummary(content: QuizReviewContent, translation: Translation) {
    Surface(color = Color(0xFF1A2747), shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().testTag("quiz_review_summary")) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = Color(0xFFC89B3C), shape = RoundedCornerShape(2.dp), modifier = Modifier.width(32.dp).height(3.dp)) {}
            ReviewText(content.title, translation, color = Color.White, heading = true)
            Text(content.score, fontFamily = interfaceFontFor(translation), fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp, lineHeight = 34.sp, color = Color(0xFFF5EAC8))
            ReviewText(content.edition, translation, color = Color.White, small = true)
        }
    }
}

@Composable
internal fun QuizReviewCard(answer: QuizReviewContent.Answer, explanationLabel: String, translation: Translation) {
    val colors = LocalHopeColors.current
    val statusColor = when (answer.outcome) {
        QuizReviewContent.Outcome.CORRECT -> reviewSuccess
        QuizReviewContent.Outcome.INCORRECT -> reviewIncorrect
        QuizReviewContent.Outcome.UNANSWERED -> reviewMuted
    }
    val statusSurface = when (answer.outcome) {
        QuizReviewContent.Outcome.CORRECT -> reviewSuccessSurface
        QuizReviewContent.Outcome.INCORRECT -> reviewIncorrectSurface
        QuizReviewContent.Outcome.UNANSWERED -> reviewNeutralSurface
    }
    Surface(color = colors.surface, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, colors.divider),
        modifier = Modifier.fillMaxWidth().testTag("quiz_review_card_${answer.id}")) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ReviewText(answer.prompt, translation, heading = true)
            answer.passage?.let { ReviewText(it, translation) }
            // Explicit text conveys each outcome without relying on its color or an icon.
            Surface(color = statusSurface, shape = RoundedCornerShape(8.dp)) {
                ReviewText(answer.status, translation, color = statusColor, small = true, strong = true,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fillWidth = false)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ReviewAnswerPanel(answer.selectedAnswer, translation, statusColor, statusSurface)
                ReviewAnswerPanel(answer.correctAnswer, translation, reviewSuccess, reviewSuccessSurface, strong = true)
            }
            if (answer.explanation.isNotBlank()) {
                HorizontalDivider(color = colors.divider)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ReviewText(explanationLabel, translation, small = true, strong = true)
                    ReviewText(answer.explanation, translation, small = true)
                }
            }
            Surface(color = colors.background, shape = RoundedCornerShape(10.dp)) {
                ReviewText(answer.reference, translation, small = true, strong = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp))
            }
        }
    }
}

@Composable
private fun ReviewAnswerPanel(text: String, translation: Translation, ink: Color, background: Color, strong: Boolean = false) {
    Surface(color = background, shape = RoundedCornerShape(10.dp)) {
        ReviewText(text, translation, small = true, strong = strong, color = ink,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp))
    }
}

@Composable
private fun ReviewText(text: String, translation: Translation, modifier: Modifier = Modifier,
    color: Color = LocalHopeColors.current.text, heading: Boolean = false, small: Boolean = false,
    strong: Boolean = false, fillWidth: Boolean = true) {
    // Stable text columns match the PDF and avoid shrink-wrapping a wider measured paragraph.
    val textModifier = if (fillWidth) modifier.fillMaxWidth() else modifier
    Text(text, modifier = if (heading) textModifier.semantics { heading() } else textModifier, color = color,
        fontFamily = interfaceFontFor(translation), fontWeight = when {
            heading -> FontWeight.Bold
            strong -> FontWeight.SemiBold
            else -> FontWeight.Normal
        },
        fontSize = if (heading) 20.sp else if (small) 14.sp else 16.sp,
        lineHeight = if (heading) 29.sp else if (small) 23.sp else 27.sp)
}
