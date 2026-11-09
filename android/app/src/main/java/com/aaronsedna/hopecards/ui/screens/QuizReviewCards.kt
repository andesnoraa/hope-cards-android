package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.QuizReviewContent
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.interfaceFontFor

@Composable
private fun outcomeInk(outcome: QuizReviewContent.Outcome): Color {
    val dark = LocalHopeColors.current.text.luminance() > .5f
    return when (outcome) {
        QuizReviewContent.Outcome.CORRECT -> if (dark) Color(0xFF8CD4AE) else Color(0xFF206641)
        QuizReviewContent.Outcome.INCORRECT -> if (dark) Color(0xFFF1AAA1) else Color(0xFF97352D)
        QuizReviewContent.Outcome.UNANSWERED -> if (dark) Color(0xFF9CC8EC) else Color(0xFF285B85)
    }
}

@Composable
internal fun QuizResultsSummary(content: QuizReviewContent, translation: Translation, timeTaken: String?, timedOut: Boolean) {
    val colors = LocalHopeColors.current
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        ReviewText(content.title, translation, heading = true)
        Spacer(Modifier.height(20.dp))
        Text(content.resources.getString(if (timedOut) R.string.quiz_time_up else R.string.quiz_complete),
            Modifier.then(if (timedOut) Modifier.testTag("quiz_time_up") else Modifier),
            color = colors.textSecondary, fontFamily = interfaceFontFor(translation), fontSize = 13.sp, lineHeight = 21.sp)
        Text(buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 64.sp, fontWeight = FontWeight.SemiBold)) { append(content.correctCount.toString()) }
            withStyle(SpanStyle(fontSize = 30.sp, color = colors.textSecondary)) { append(" / ${content.questionCount}") }
        }, modifier = Modifier.testTag("quiz_score").clearAndSetSemantics { heading(); text = AnnotatedString(content.score) },
            fontFamily = interfaceFontFor(translation), color = colors.text, lineHeight = 80.sp)
        timeTaken?.let { ReviewText(it, translation, Modifier.testTag("quiz_elapsed_result"), small = true,
            color = colors.textSecondary, centered = true) }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuizReviewContent.Outcome.entries.forEach { outcome ->
                val count = content.answers.count { it.outcome == outcome }
                val label = content.resources.getString(when (outcome) {
                    QuizReviewContent.Outcome.CORRECT -> R.string.quiz_result_correct
                    QuizReviewContent.Outcome.INCORRECT -> R.string.quiz_incorrect
                    QuizReviewContent.Outcome.UNANSWERED -> R.string.quiz_unanswered
                })
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReviewMark(outcome, null)
                        Text(count.toString(), color = outcomeInk(outcome), fontFamily = interfaceFontFor(translation),
                            fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(label, color = colors.textSecondary, fontFamily = interfaceFontFor(translation),
                        fontSize = 12.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
                }
            }
        }
        Spacer(Modifier.height(22.dp))
        HorizontalDivider(color = colors.divider)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuizReviewSummary(content: QuizReviewContent, translation: Translation) {
    val colors = LocalHopeColors.current
    FlowRow(Modifier.fillMaxWidth().testTag("quiz_review_summary"),
        horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(content.scoreRatio, color = colors.text, fontFamily = interfaceFontFor(translation),
            fontSize = 22.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 16.dp).clearAndSetSemantics { text = AnnotatedString(content.score) })
        content.totalTime?.let {
            Text(it, Modifier.width(IntrinsicSize.Max).padding(top = 4.dp),
                color = colors.textSecondary, fontFamily = interfaceFontFor(translation),
                fontSize = 13.sp, lineHeight = 24.sp)
        }
    }
}

@Composable
internal fun QuizReviewFilters(content: QuizReviewContent, translation: Translation,
    selected: QuizReviewContent.Outcome?, onSelect: (QuizReviewContent.Outcome?) -> Unit) {
    val colors = LocalHopeColors.current
    Column {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            listOf(null, QuizReviewContent.Outcome.INCORRECT, QuizReviewContent.Outcome.UNANSWERED).forEach { filter ->
                val label = content.resources.getString(when (filter) {
                    null -> R.string.quiz_review_all
                    QuizReviewContent.Outcome.INCORRECT -> R.string.quiz_incorrect
                    else -> R.string.quiz_unanswered
                })
                val count = content.answers.count { filter == null || it.outcome == filter }
                val active = filter == selected
                Column(Modifier.width(IntrinsicSize.Max).testTag("quiz_review_filter_${filter?.name ?: "ALL"}")
                    .selectable(active, role = Role.Tab, onClick = { onSelect(filter) })) {
                    Text("$label $count", Modifier.heightIn(min = 48.dp).padding(vertical = 12.dp),
                        color = if (active) colors.text else colors.textSecondary, fontFamily = interfaceFontFor(translation),
                        fontSize = 14.sp, lineHeight = 24.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                    HorizontalDivider(thickness = 2.dp, color = if (active) colors.text else Color.Transparent)
                }
            }
        }
        HorizontalDivider(color = colors.divider)
    }
}

/** Flat answer groups, with spoken outcome descriptions retained for screen readers. */
@Composable
internal fun QuizReviewCard(answer: QuizReviewContent.Answer, translation: Translation) {
    val colors = LocalHopeColors.current
    Column(Modifier.fillMaxWidth().testTag("quiz_review_card_${answer.id}").padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            ReviewText(answer.prompt, translation, Modifier.weight(1f), heading = true)
            answer.duration?.let {
                Text(it, Modifier.width(IntrinsicSize.Max).padding(top = 3.dp).testTag("quiz_review_time_${answer.id}")
                    .semantics { contentDescription = requireNotNull(answer.timeTaken) },
                    color = colors.textSecondary, fontFamily = interfaceFontFor(translation), fontSize = 12.sp, lineHeight = 21.sp)
            }
        }
        answer.passage?.let { ReviewText(it, translation, small = true) }
        if (answer.outcome == QuizReviewContent.Outcome.UNANSWERED) {
            ReviewAnswerRow(answer.status, translation, answer.outcome, answer.status, answer.id)
        }
        answer.selectedValue?.let { ReviewAnswerRow(it, translation, answer.outcome, answer.status, answer.id,
            caption = if (answer.outcome == QuizReviewContent.Outcome.INCORRECT) answer.selectedLabel else null) }
        answer.correctAnswer?.let {
            ReviewAnswerRow(answer.correctValue, translation,
                if (answer.outcome == QuizReviewContent.Outcome.UNANSWERED) answer.outcome else QuizReviewContent.Outcome.CORRECT,
                null, null, showMark = answer.outcome != QuizReviewContent.Outcome.UNANSWERED, caption = answer.correctLabel)
        }
        if (answer.explanation.isNotBlank()) ReviewText(answer.explanation, translation, small = true, color = colors.textSecondary)
        ReviewText(answer.shortReference, translation, small = true, color = colors.textSecondary)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = colors.divider)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewAnswerRow(text: String, translation: Translation, outcome: QuizReviewContent.Outcome,
    description: String?, id: String?, showMark: Boolean = true, caption: String? = null) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (showMark) ReviewMark(outcome, description, Modifier.padding(top = 3.dp)
            .then(if (id != null) Modifier.testTag("quiz_review_status_$id") else Modifier))
        else Spacer(Modifier.width(20.dp))
        FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text, Modifier.width(IntrinsicSize.Max), color = outcomeInk(outcome), fontFamily = interfaceFontFor(translation), fontSize = 16.sp,
                lineHeight = 25.sp, fontWeight = FontWeight.SemiBold)
            caption?.let { Text(it, Modifier.width(IntrinsicSize.Max).padding(top = 3.dp), color = LocalHopeColors.current.textSecondary,
                fontFamily = interfaceFontFor(translation), fontSize = 12.sp, lineHeight = 21.sp) }
        }
    }
}

@Composable
private fun ReviewMark(outcome: QuizReviewContent.Outcome, description: String?, modifier: Modifier = Modifier) {
    val ink = outcomeInk(outcome)
    Canvas(modifier.size(20.dp).semantics { if (description != null) contentDescription = description }) {
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) = drawLine(ink,
            Offset(size.width * x1, size.height * y1), Offset(size.width * x2, size.height * y2),
            strokeWidth = 2.3.dp.toPx(), cap = StrokeCap.Round)
        when (outcome) {
            QuizReviewContent.Outcome.CORRECT -> { line(.12f, .52f, .4f, .8f); line(.4f, .8f, .9f, .18f) }
            QuizReviewContent.Outcome.INCORRECT -> { line(.2f, .2f, .8f, .8f); line(.2f, .8f, .8f, .2f) }
            QuizReviewContent.Outcome.UNANSWERED -> line(.15f, .5f, .85f, .5f)
        }
    }
}

@Composable
private fun ReviewText(text: String, translation: Translation, modifier: Modifier = Modifier,
    color: Color = LocalHopeColors.current.text, heading: Boolean = false, small: Boolean = false,
    strong: Boolean = false, centered: Boolean = false) {
    Text(text, modifier.fillMaxWidth().then(if (heading) Modifier.semantics { heading() } else Modifier), color = color,
        fontFamily = interfaceFontFor(translation), fontWeight = when {
            heading || strong -> FontWeight.SemiBold
            else -> FontWeight.Normal
        }, fontSize = if (heading) 18.sp else if (small) 14.sp else 16.sp,
        lineHeight = if (heading) 27.sp else if (small) 22.sp else 25.sp,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start)
}
