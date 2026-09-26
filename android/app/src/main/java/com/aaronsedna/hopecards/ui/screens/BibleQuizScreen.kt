package com.aaronsedna.hopecards.ui.screens

import android.content.res.Configuration
import android.content.Context
import android.os.Build
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.stateDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aaronsedna.hopecards.audio.QuizSoundPlayer
import androidx.annotation.StringRes
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.model.BibleReferenceFormatter
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.QuizQuestion
import com.aaronsedna.hopecards.model.QuizSession
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.interfaceFontFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/** Localize only the quiz; the rest of the app intentionally retains its English UI. */
@Composable
private fun quizString(translation: Translation, @StringRes id: Int, vararg args: Any): String {
    val context = LocalContext.current
    val language = QuizLanguage.forTranslation(translation)
    val configuration = LocalConfiguration.current
    val resources = remember(context, configuration, language) {
        val config = Configuration(configuration)
        config.setLocale(Locale.forLanguageTag(language.code))
        context.createConfigurationContext(config).resources
    }
    return resources.getString(id, *args)
}

private val SessionSaver = listSaver<QuizSession, Any>(
    save = { listOf(ArrayList(it.questionIds), ArrayList(it.answers), it.position, it.selectedIndex, it.finished) },
    restore = { values ->
        @Suppress("UNCHECKED_CAST")
        QuizSession(values[0] as List<String>, values[1] as List<Int>, values[2] as Int, values[3] as Int, values[4] as Boolean)
    },
)

@Composable
fun BibleQuizRoute(
    translation: Translation,
    onComplete: (() -> Boolean, () -> Unit) -> Unit = { _, proceed -> proceed() },
    hapticsEnabled: Boolean = true,
    onExitHandlerChanged: (((() -> Unit) -> Unit)?) -> Unit = {},
    onChangeTranslation: () -> Unit,
) {
    val context = LocalContext.current
    val language = QuizLanguage.forTranslation(translation)
    var retry by remember { mutableIntStateOf(0) }
    val loaded by produceState<Result<List<QuizQuestion>>?>(null, language, retry) {
        value = null
        value = withContext(Dispatchers.IO) {
            runCatching { BibleQuizRepository(context).load(language) }
        }
    }
    when {
        loaded == null -> Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            CircularProgressIndicator(color = LocalHopeColors.current.text)
            QuizText(quizString(translation, R.string.quiz_loading), translation)
        }
        loaded?.isFailure == true -> QuizMessage(translation, R.string.quiz_error, R.string.quiz_retry, { retry++ })
        else -> BibleQuizScreen(loaded!!.getOrThrow(), translation, onComplete, hapticsEnabled, onExitHandlerChanged, onChangeTranslation)
    }
}

@Composable
private fun QuizMessage(translation: Translation, @StringRes message: Int, @StringRes action: Int, onAction: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)) {
        QuizText(quizString(translation, message), translation)
        QuizButton(quizString(translation, action), translation, onAction)
    }
}

@Composable
fun BibleQuizScreen(
    questions: List<QuizQuestion>, translation: Translation,
    onComplete: (() -> Boolean, () -> Unit) -> Unit = { _, proceed -> proceed() },
    hapticsEnabled: Boolean = true,
    onExitHandlerChanged: (((() -> Unit) -> Unit)?) -> Unit = {},
    onChangeTranslation: () -> Unit,
) {
    val colors = LocalHopeColors.current
    val language = QuizLanguage.forTranslation(translation)
    val haptics = LocalHapticFeedback.current
    val byId = remember(questions) { questions.associateBy { it.id } }
    var savedSession by rememberSaveable(language, stateSaver = SessionSaver) { mutableStateOf(QuizSession()) }
    // A future content update may retire IDs; do not restore a broken or mismatched round.
    val session = savedSession.takeIf { it.isValidFor(questions) } ?: QuizSession()
    var completing by remember { mutableStateOf(false) }
    var completionRecorded by rememberSaveable(language, session.questionIds) { mutableStateOf(false) }
    val activeScreen = remember { mutableStateOf(true) }
    DisposableEffect(Unit) { onDispose { activeScreen.value = false } }
    var confirmEnd by rememberSaveable { mutableStateOf(false) }
    var review by rememberSaveable(language) { mutableStateOf(false) }
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { context.getSharedPreferences("bible-quiz", Context.MODE_PRIVATE) }
    var soundEnabled by remember(preferences) { mutableStateOf(preferences.getBoolean("sound", false)) }
    val sound = remember(soundEnabled, context) { if (soundEnabled) runCatching { QuizSoundPlayer(context) }.getOrNull() else null }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(sound, lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) sound?.stop() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); sound?.release() }
    }
    val success = Color(0xFF237447)
    val successSurface = Color(0xFFEAF5EE)
    val wrongSurface = Color(0xFFFBEDEC)
    val listState = rememberLazyListState()
    LaunchedEffect(session.questionIds, session.position, session.finished, review) { listState.scrollToItem(0) }
    val feedbackRequester = remember { BringIntoViewRequester() }

    // A completed round remains visible until the user chooses to leave it.
    // Review and Back to results never consume this one-time exit opportunity.
    val leaveCompletedQuiz: (() -> Unit) -> Unit = { proceed ->
        if (!completing) {
            if (!session.finished || completionRecorded) proceed()
            else {
                completionRecorded = true
                completing = true
                val completedRound = session
                val isCurrent = { activeScreen.value && savedSession == completedRound }
                onComplete(isCurrent) {
                    if (isCurrent()) proceed()
                    completing = false
                }
            }
        }
    }
    val currentExitHandler by rememberUpdatedState(leaveCompletedQuiz)
    DisposableEffect(onExitHandlerChanged) {
        onExitHandlerChanged { proceed -> currentExitHandler(proceed) }
        onDispose { onExitHandlerChanged(null) }
    }

    if (confirmEnd) AlertDialog(
        onDismissRequest = { confirmEnd = false },
        containerColor = colors.surface,
        title = { QuizText(quizString(translation, R.string.quiz_exit_title), translation, heading = true) },
        confirmButton = {
            TextButton(onClick = { savedSession = QuizSession(); confirmEnd = false }) {
                QuizText(quizString(translation, R.string.quiz_end_round), translation)
            }
        },
        dismissButton = {
            TextButton(onClick = { confirmEnd = false }) { QuizText(quizString(translation, R.string.quiz_keep_playing), translation) }
        },
    )

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth().testTag("bible_quiz"),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (session.started) item(key = "language") {
                QuizTranslation(translation) { leaveCompletedQuiz(onChangeTranslation) }
            }
            when {
                !session.started -> {
                    item {
                        Column(Modifier.padding(top = 8.dp, bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuizText(quizString(translation, R.string.quiz_intro), translation, heading = true)
                            QuizText(quizString(translation, R.string.quiz_description), translation, color = colors.textSecondary)
                        }
                    }
                    item {
                        Surface(color = colors.surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, colors.divider)) {
                            Column(Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                                QuizTranslation(translation) { leaveCompletedQuiz(onChangeTranslation) }
                                HorizontalDivider(color = colors.divider)
                                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("quiz_sound")
                                    .toggleable(soundEnabled, role = Role.Switch) {
                                        soundEnabled = it
                                        preferences.edit().putBoolean("sound", it).apply()
                                    }, verticalAlignment = Alignment.CenterVertically) {
                                    QuizText(quizString(translation, R.string.quiz_enable_sound), translation, small = true, modifier = Modifier.weight(1f))
                                    Switch(checked = soundEnabled, onCheckedChange = null,
                                        colors = SwitchDefaults.colors(checkedTrackColor = colors.buttonBackground,
                                            checkedThumbColor = colors.buttonText, uncheckedTrackColor = colors.switchOff,
                                            uncheckedThumbColor = colors.surface, uncheckedBorderColor = Color.Transparent))
                                }
                            }
                        }
                    }
                    item { QuizButton(quizString(translation, R.string.quiz_start), translation, { review = false; completionRecorded = false; savedSession = QuizSession.start(questions) }, tag = "quiz_start") }
                }
                session.finished -> {
                    item {
                        Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (review) QuizText(quizString(translation, R.string.quiz_review), translation, heading = true)
                            if (!review) {
                                Surface(color = colors.surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, colors.divider)) {
                                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        QuizText(quizString(translation, R.string.quiz_complete), translation, small = true, color = colors.textSecondary)
                                        Text(quizString(translation, R.string.quiz_score, session.score(byId), session.questionIds.size),
                                            modifier = Modifier.testTag("quiz_score").semantics { heading() },
                                            color = colors.text, fontFamily = interfaceFontFor(translation),
                                            fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                QuizButton(quizString(translation, R.string.quiz_review), translation, { review = true }, tag = "quiz_review")
                                TextButton(onClick = { leaveCompletedQuiz { review = false; completionRecorded = false; savedSession = QuizSession.start(questions) } }, enabled = !completing,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("quiz_restart")) {
                                    QuizText(quizString(translation, R.string.quiz_play_again), translation)
                                }
                            }
                        }
                    }
                    if (review) itemsIndexed(session.questionIds, key = { _, id -> id }) { index, id ->
                        val q = byId.getValue(id)
                        Surface(color = colors.surface, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, colors.divider)) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                QuizText("${index + 1}. ${q.question}", translation)
                                val correct = session.answers[index] == q.correctIndex
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    AppIcon(if (correct) AppIconGlyph.CheckmarkCircleOutline else AppIconGlyph.Close, null, if (correct) success else colors.danger)
                                    QuizText(quizString(translation, R.string.quiz_your_answer, q.options[session.answers[index]]), translation, small = true, modifier = Modifier.weight(1f), color = if (correct) success else colors.danger)
                                }
                                if (!correct) QuizText(quizString(translation, R.string.quiz_correct_answer, q.options[q.correctIndex]), translation, small = true, color = success)
                                QuizExplanation(q, translation)
                            }
                        }
                    }
                    if (review) item {
                        QuizButton(quizString(translation, R.string.quiz_back_results), translation, { review = false }, tag = "quiz_back_results")
                    }
                }
                else -> {
                    val q = byId.getValue(session.questionIds[session.position])
                    item(key = "progress") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val progress = quizString(translation, R.string.quiz_progress, session.position + 1, session.questionIds.size)
                            QuizText(progress, translation, small = true)
                            LinearProgressIndicator(
                                progress = { session.answers.size.toFloat() / session.questionIds.size },
                                modifier = Modifier.fillMaxWidth().semantics { contentDescription = progress },
                                color = colors.text, trackColor = colors.divider,
                            )
                        }
                    }
                    item(key = "question") { QuizText(q.question, translation, heading = true, modifier = Modifier.testTag("quiz_question")) }
                    item(key = "options") {
                        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            q.options.forEachIndexed { index, answer ->
                                val selected = session.selectedIndex == index
                                val correct = session.checked && index == q.correctIndex
                                val wrong = session.checked && selected && !correct
                                val answerState = if (correct) quizString(translation, R.string.quiz_correct)
                                    else if (wrong) quizString(translation, R.string.quiz_incorrect) else ""
                                Surface(
                                    modifier = Modifier.fillMaxWidth().testTag("quiz_option_$index")
                                        .semantics { if (answerState.isNotEmpty()) stateDescription = answerState }
                                        .selectable(selected, enabled = !session.checked, role = Role.RadioButton) { savedSession = session.choose(index) },
                                    shape = RoundedCornerShape(18.dp),
                                    color = when { correct -> successSurface; wrong -> wrongSurface; selected -> colors.accentSoft; else -> colors.surface },
                                    border = BorderStroke(if (correct || selected) 2.dp else 1.dp, if (wrong) colors.danger else if (correct) success else if (selected) colors.text else colors.divider),
                                ) {
                                    Row(Modifier.heightIn(min = 58.dp).padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = CircleShape, color = if (correct) success else if (wrong) colors.danger else colors.background) {
                                            Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                                                if (correct || wrong) AppIcon(if (correct) AppIconGlyph.Checkmark else AppIconGlyph.Close, null, Color.White, size = 20.dp)
                                                else Text(('A' + index).toString(), color = colors.text, fontSize = 13.sp)
                                            }
                                        }
                                        QuizText(answer, translation, modifier = Modifier.weight(1f), color = if (correct) success else if (wrong) colors.danger else colors.text)
                                    }
                                }
                            }
                        }
                    }
                    if (session.checked) item(key = "feedback") {
                        LaunchedEffect(q.id) { feedbackRequester.bringIntoView() }
                        Surface(color = colors.surface, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, colors.divider), modifier = Modifier.bringIntoViewRequester(feedbackRequester).testTag("quiz_feedback").semantics { liveRegion = LiveRegionMode.Polite }) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                QuizText(quizString(translation, if (session.selectedIndex == q.correctIndex) R.string.quiz_correct else R.string.quiz_correct_answer, q.options[q.correctIndex]), translation, small = true, color = success)
                                QuizExplanation(q, translation)
                            }
                        }
                    }
                    item(key = "action") {
                        QuizButton(
                            quizString(translation, if (!session.checked) R.string.quiz_check else if (session.position == session.questionIds.lastIndex) R.string.quiz_results else R.string.quiz_next),
                            translation,
                            {
                                if (session.checked) savedSession = session.advance()
                                else {
                                    savedSession = session.submit()
                                    if (savedSession.checked) {
                                        val correct = session.selectedIndex == q.correctIndex
                                        sound?.play(correct)
                                        if (!correct && hapticsEnabled) {
                                            haptics.performHapticFeedback(if (Build.VERSION.SDK_INT >= 30) HapticFeedbackType.Reject else HapticFeedbackType.LongPress)
                                        }
                                    }
                                }
                            },
                            enabled = session.selectedIndex >= 0 && !completing,
                            tag = "quiz_action",
                        )
                    }
                    item {
                        TextButton(onClick = { confirmEnd = true }, modifier = Modifier.fillMaxWidth()) {
                            QuizText(quizString(translation, R.string.quiz_end_round), translation, small = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizTranslation(translation: Translation, onChange: () -> Unit) {
    val colors = LocalHopeColors.current
    val language = QuizLanguage.forTranslation(translation)
    Surface(onClick = onChange, color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().testTag("quiz_change_translation"), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.heightIn(min = 52.dp).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuizText(quizString(translation, R.string.quiz_selected_edition, language.nativeName, translation.label),
                translation, small = true, modifier = Modifier.weight(1f), color = colors.textSecondary)
            AppIcon(AppIconGlyph.ChevronDown, quizString(translation, R.string.quiz_change_translation), colors.textSecondary, size = 18.dp)
        }
    }
}

@Composable
private fun QuizExplanation(question: QuizQuestion, translation: Translation) {
    // Keep the explanation and its citation together; allow natural wrapping for long languages.
    val reference = BibleReferenceFormatter.formatFull(question.reference, translation)
    QuizText("${question.explanation} ($reference)", translation, small = true)
}

@Composable
private fun QuizText(text: String, translation: Translation, modifier: Modifier = Modifier, heading: Boolean = false, small: Boolean = false, color: Color = LocalHopeColors.current.text) {
    Text(
        text = text,
        modifier = if (heading) modifier.semantics { heading() } else modifier,
        color = color,
        fontFamily = interfaceFontFor(translation),
        fontSize = if (heading) 23.sp else if (small) 14.sp else 16.sp,
        lineHeight = if (heading) 32.sp else if (small) 21.sp else 25.sp,
        fontWeight = if (heading) FontWeight.SemiBold else FontWeight.Normal,
    )
}

@Composable
private fun QuizButton(label: String, translation: Translation, onClick: () -> Unit, enabled: Boolean = true, tag: String = "") {
    val colors = LocalHopeColors.current
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(tag),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colors.buttonBackground, contentColor = colors.buttonText),
    ) {
        Text(label, fontFamily = interfaceFontFor(translation), fontSize = 16.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
    }
}
