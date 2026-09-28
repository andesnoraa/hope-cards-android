package com.aaronsedna.hopecards.ui.screens

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.stateDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.aaronsedna.hopecards.audio.QuizSoundPlayer
import com.aaronsedna.hopecards.audio.QuizVibration
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.data.BibleQuizRepository
import com.aaronsedna.hopecards.data.QuizRoundHistory
import com.aaronsedna.hopecards.model.BibleReferenceFormatter
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.QuizQuestion
import com.aaronsedna.hopecards.model.QuizSession
import com.aaronsedna.hopecards.model.QuizStopwatch
import com.aaronsedna.hopecards.model.formatQuizCountdownTime
import com.aaronsedna.hopecards.model.QUIZ_COUNTDOWN_DURATION_MILLIS
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.model.formatQuizElapsedTime
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.interfaceFontFor
import com.aaronsedna.hopecards.ui.quizString
import com.aaronsedna.hopecards.ui.QuizReviewContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

private val SessionSaver = listSaver<QuizSession, Any>(
    save = { listOf(ArrayList(it.questionIds), ArrayList(it.answers), it.position, it.selectedIndex, it.finished, it.timedOut) },
    restore = { values ->
        @Suppress("UNCHECKED_CAST")
        QuizSession(values[0] as List<String>, values[1] as List<Int>, values[2] as Int, values[3] as Int, values[4] as Boolean,
            values.getOrNull(5) as? Boolean ?: false)
    },
)

@Composable
fun BibleQuizRoute(
    translation: Translation,
    onComplete: (() -> Boolean, () -> Unit) -> Unit = { _, proceed -> proceed() },
    hapticsEnabled: Boolean = true,
    onExitHandlerChanged: (((() -> Unit) -> Unit)?) -> Unit = {},
) {
    val context = LocalContext.current
    val language = QuizLanguage.forTranslation(translation)
    val roundHistory = remember(context) { QuizRoundHistory(context) }
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
        else -> BibleQuizScreen(loaded!!.getOrThrow(), translation, onComplete, hapticsEnabled, onExitHandlerChanged,
            onStartRound = { roundHistory.nextRound(language, loaded!!.getOrThrow()) })
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
    onStartRound: (() -> QuizSession)? = null,
    onWrongAnswerHaptic: (() -> Unit)? = null,
    elapsedRealtimeMillis: () -> Long = SystemClock::elapsedRealtime,
    onCountdownCue: ((Int) -> Unit)? = null,
) {
    val colors = LocalHopeColors.current
    val language = QuizLanguage.forTranslation(translation)
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
    var showCertificates by rememberSaveable(language) { mutableStateOf(false) }
    val context = LocalContext.current.applicationContext
    val reviewContent = remember(context, translation, session, questions) {
        if (session.finished) QuizReviewContent(context, translation, session, questions) else null
    }
    val vibration = remember(context) { QuizVibration(context) }
    val preferences = remember(context) { context.getSharedPreferences("bible-quiz", Context.MODE_PRIVATE) }
    var soundEnabled by remember(preferences) { mutableStateOf(preferences.getBoolean("sound", true)) }
    var timerEnabled by remember(preferences) { mutableStateOf(preferences.getBoolean("timer", false)) }
    val currentClock by rememberUpdatedState(elapsedRealtimeMillis)
    // The setting is available on newer Android versions; monotonic rollback also handles older devices.
    val bootCount = remember(context) { runCatching { Settings.Global.getInt(context.contentResolver, "boot_count", 0) }.getOrDefault(0) }
    val stopwatchSaver = remember(bootCount) {
        listSaver<QuizStopwatch, Any>(
            save = {
                val checkpoint = it.checkpoint(currentClock(), bootCount)
                listOf(checkpoint.enabled, checkpoint.running, checkpoint.anchorMillis,
                    checkpoint.elapsedBeforeAnchorMillis, checkpoint.bootCount)
            },
            restore = { values ->
                QuizStopwatch(values[0] as Boolean, values[1] as Boolean, values[2] as Long,
                    values[3] as Long, values[4] as Int).restore(currentClock(), bootCount)
            },
        )
    }
    var stopwatch by rememberSaveable(language, stateSaver = stopwatchSaver) { mutableStateOf(QuizStopwatch()) }
    var completedQuestionTimeMillis by rememberSaveable(language) { mutableLongStateOf(0L) }
    var lastCountdownCue by rememberSaveable(language) { mutableIntStateOf(6) }
    val startNewRound = {
        val nextRound = onStartRound?.invoke() ?: QuizSession.start(questions)
        review = false
        completionRecorded = false
        savedSession = nextRound
        stopwatch = QuizStopwatch.start(timerEnabled, currentClock(), bootCount)
        completedQuestionTimeMillis = 0L
        lastCountdownCue = 6
    }
    val sound = remember(soundEnabled, context) { if (soundEnabled) runCatching { QuizSoundPlayer(context) }.getOrNull() else null }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(sound, vibration, lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) { sound?.stop(); vibration.stop() } }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); sound?.release(); vibration.stop() }
    }
    val success = Color(0xFF237447)
    val successSurface = Color(0xFFEAF5EE)
    val wrongSurface = Color(0xFFFBEDEC)
    val listState = rememberLazyListState()
    LaunchedEffect(session.questionIds, session.position, session.finished, review) { listState.scrollToItem(0) }
    LaunchedEffect(session.questionIds, session.position, session.checked, session.finished) {
        if (session.started && session.checked && !session.finished) {
            // Question and options occupy the first two items. Compose feedback even when
            // the pinned action was tapped while the end of a long question was offscreen.
            listState.animateScrollToItem(2)
        }
    }

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
            TextButton(onClick = { savedSession = QuizSession(); stopwatch = QuizStopwatch(); completedQuestionTimeMillis = 0L; confirmEnd = false }) {
                QuizText(quizString(translation, R.string.quiz_end_round), translation)
            }
        },
        dismissButton = {
            TextButton(onClick = { confirmEnd = false }) { QuizText(quizString(translation, R.string.quiz_keep_playing), translation) }
        },
    )

    val playing = session.started && !session.finished
    val timeLimitMillis = QUIZ_COUNTDOWN_DURATION_MILLIS
    fun finishIfExpired(now: Long): Boolean {
        // Consult saved state directly: stale click handlers cannot reopen a timed-out round.
        val active = savedSession
        if (!active.started || active.finished) return active.timedOut
        if (active.checked) return false
        val limit = QUIZ_COUNTDOWN_DURATION_MILLIS
        if (!stopwatch.enabled || !stopwatch.running || stopwatch.elapsedMillis(now, bootCount) < limit) return false
        if (soundEnabled && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && lastCountdownCue != 0) {
            lastCountdownCue = 0
            if (onCountdownCue != null) onCountdownCue(0) else sound?.playCountdown(ended = true)
        }
        val next = active.expireQuestion()
        savedSession = next
        if (next.finished) {
            stopwatch = stopwatch.finish(now, bootCount).copy(elapsedBeforeAnchorMillis = limit)
        } else {
            completedQuestionTimeMillis += limit
            stopwatch = QuizStopwatch.start(stopwatch.enabled, now, bootCount)
            lastCountdownCue = 6
        }
        confirmEnd = false
        review = false
        return true
    }
    if (showCertificates) QuizCertificateScreen(translation) { showCertificates = false }
    val elapsedMillis by produceState(stopwatch.elapsedMillis(currentClock(), bootCount), stopwatch, playing, lifecycle) {
        value = stopwatch.elapsedMillis(currentClock(), bootCount)
        if (playing && stopwatch.running) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    value = stopwatch.elapsedMillis(currentClock(), bootCount)
                    delay(1_000L)
                }
            }
        }
    }
    LaunchedEffect(playing, session.position, session.checked, stopwatch, soundEnabled, elapsedMillis) {
        // A delayed frame must not play an old warning after the deadline has passed.
        val currentElapsedMillis = stopwatch.elapsedMillis(currentClock(), bootCount)
        val remainingSeconds = ((timeLimitMillis - currentElapsedMillis).coerceAtLeast(0L) + 999L) / 1_000L
        if (playing && savedSession == session && !session.checked && stopwatch.running && soundEnabled && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) &&
            remainingSeconds in 1L..5L && remainingSeconds < lastCountdownCue) {
            lastCountdownCue = remainingSeconds.toInt()
            if (onCountdownCue != null) onCountdownCue(lastCountdownCue) else sound?.playCountdown(ended = false)
        }
    }
    LaunchedEffect(session.questionIds, session.position, session.checked, session.finished, stopwatch, elapsedMillis) {
        if (playing && savedSession == session && stopwatch.enabled && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) finishIfExpired(currentClock())
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize()) {
            if (playing) {
                Column(Modifier.padding(horizontal = 24.dp)) {
                    val progress = quizString(translation, R.string.quiz_progress, session.position + 1, session.questionIds.size)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuizText(progress, translation, small = true, modifier = Modifier.weight(1f), color = colors.textSecondary)
                        if (stopwatch.enabled) {
                            val remaining = (timeLimitMillis - stopwatch.elapsedMillis(currentClock(), bootCount)).coerceAtLeast(0L)
                            val duration = formatQuizCountdownTime(remaining)
                            val description = quizString(translation, R.string.quiz_time_remaining, duration)
                            val urgent = remaining <= 5_000L && !session.checked
                            Surface(color = if (urgent) wrongSurface else colors.accentSoft, shape = RoundedCornerShape(12.dp)) {
                                Text(duration, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("quiz_timer_display").semantics { contentDescription = description },
                                    color = if (urgent) colors.danger else colors.text, fontFamily = interfaceFontFor(translation),
                                    fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        IconButton(onClick = { confirmEnd = true }, modifier = Modifier.testTag("quiz_end_round")) {
                            AppIcon(AppIconGlyph.Close, quizString(translation, R.string.quiz_end_round), colors.textSecondary, size = 20.dp)
                        }
                    }
                    LinearProgressIndicator(
                        progress = { session.answers.size.toFloat() / session.questionIds.size },
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = progress },
                        color = colors.text, trackColor = colors.divider,
                    )
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth().weight(1f).testTag("bible_quiz"),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                when {
                    !session.started -> {
                        item {
                            Column(Modifier.padding(bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                QuizText(quizString(translation, R.string.quiz_intro), translation, heading = true)
                                QuizText(quizString(translation, R.string.quiz_description), translation, color = colors.textSecondary)
                            }
                        }
                        item {
                            Surface(color = colors.surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, colors.divider)) {
                                Column(Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
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
                                    HorizontalDivider(color = colors.divider)
                                    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("quiz_timer")
                                        .toggleable(timerEnabled, role = Role.Switch) {
                                            timerEnabled = it
                                            preferences.edit().putBoolean("timer", it).apply()
                                        }, verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Column(Modifier.weight(1f).padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                            QuizText(quizString(translation, R.string.quiz_enable_timer), translation, small = true)
                                            QuizText(quizString(translation, R.string.quiz_timer_description,
                                                (QUIZ_COUNTDOWN_DURATION_MILLIS / 1_000L).toInt()), translation, small = true, color = colors.textSecondary)
                                        }
                                        Switch(checked = timerEnabled, onCheckedChange = null,
                                            colors = SwitchDefaults.colors(checkedTrackColor = colors.buttonBackground,
                                                checkedThumbColor = colors.buttonText, uncheckedTrackColor = colors.switchOff,
                                                uncheckedThumbColor = colors.surface, uncheckedBorderColor = Color.Transparent))
                                    }
                                }
                            }
                        }
                        item {
                            QuizCertificateButton(translation) { showCertificates = true }
                        }
                    }
                    session.finished -> {
                        item {
                            Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                if (review) {
                                    QuizReviewSummary(requireNotNull(reviewContent), translation)
                                    QuizText(reviewContent.reviewTitle, translation, heading = true)
                                }
                                if (!review) {
                                    Surface(color = colors.surface, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, colors.divider)) {
                                        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            QuizText(quizString(translation, if (session.timedOut) R.string.quiz_time_up else R.string.quiz_complete),
                                                translation, small = true, color = colors.textSecondary,
                                                modifier = if (session.timedOut) Modifier.testTag("quiz_time_up") else Modifier)
                                            Text(quizString(translation, R.string.quiz_score, session.score(byId), session.questionIds.size),
                                                modifier = Modifier.testTag("quiz_score").semantics { heading() },
                                                color = colors.text, fontFamily = interfaceFontFor(translation),
                                                fontSize = 30.sp, lineHeight = 40.sp, fontWeight = FontWeight.SemiBold)
                                            if (stopwatch.enabled) QuizText(
                                                quizString(translation, R.string.quiz_elapsed_time, formatQuizElapsedTime(completedQuestionTimeMillis + stopwatch.elapsedMillis(currentClock(), bootCount))),
                                                translation, small = true, color = colors.textSecondary, modifier = Modifier.testTag("quiz_elapsed_result"))
                                        }
                                    }
                                    QuizButton(quizString(translation, R.string.quiz_review), translation, { review = true }, tag = "quiz_review")
                                    QuizResultActions(translation, session, questions)
                                    QuizCertificateButton(translation, enabled = !completing) { showCertificates = true }
                                    TextButton(onClick = { leaveCompletedQuiz(startNewRound) }, enabled = !completing,
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("quiz_restart")) {
                                        QuizText(quizString(translation, R.string.quiz_play_again), translation)
                                    }
                                }
                            }
                        }
                        if (review) itemsIndexed(requireNotNull(reviewContent).answers, key = { _, answer -> answer.id }) { _, answer ->
                            QuizReviewCard(answer, reviewContent.explanationLabel, translation)
                        }
                        if (review) item {
                            QuizResultActions(translation, session, questions)
                        }
                        if (review) item {
                            QuizButton(quizString(translation, R.string.quiz_back_results), translation, { review = false }, tag = "quiz_back_results")
                        }
                    }
                    else -> {
                        val q = byId.getValue(session.questionIds[session.position])
                        item(key = "question-${q.id}") { QuizQuestionText(q.question, translation) }
                        // Replacing the question must cancel any press still held on its options.
                        item(key = "options-${q.id}") {
                            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                q.options.forEachIndexed { index, answer ->
                                    val selected = session.selectedIndex == index
                                    val correct = session.checked && index == q.correctIndex
                                    val wrong = session.checked && selected && !correct
                                    val answerState = if (correct) quizString(translation, R.string.quiz_correct)
                                        else if (wrong) quizString(translation, R.string.quiz_incorrect) else ""
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().testTag("quiz_option_$index")
                                            .semantics { if (answerState.isNotEmpty()) stateDescription = answerState }
                                            .selectable(selected, enabled = !session.checked, role = Role.RadioButton) {
                                                if (savedSession == session && !finishIfExpired(currentClock())) savedSession = session.choose(index)
                                            },
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
                        if (session.checked) item(key = "feedback-${q.id}") {
                            QuizAnswerFeedback(q, translation, session.selectedIndex == q.correctIndex)
                        }
                    }
                }
            }
            if (!session.started) {
                Surface(color = colors.background) {
                    Column {
                        HorizontalDivider(color = colors.divider)
                        Box(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                            QuizButton(quizString(translation, R.string.quiz_start), translation, startNewRound, tag = "quiz_start")
                        }
                    }
                }
            }
            if (playing) {
                val q = byId.getValue(session.questionIds[session.position])
                // The action stays reachable while long questions, answers and feedback scroll above it.
                Surface(color = colors.background) {
                    Column {
                        HorizontalDivider(color = colors.divider)
                        Box(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                            QuizButton(
                                quizString(translation, if (!session.checked) R.string.quiz_check else if (session.position == session.questionIds.lastIndex) R.string.quiz_results else R.string.quiz_next),
                                translation,
                                {
                                    if (savedSession != session || finishIfExpired(currentClock())) Unit
                                    else if (session.checked) {
                                        val next = session.advance()
                                        if (!next.finished) {
                                            completedQuestionTimeMillis += stopwatch.elapsedMillis(currentClock(), bootCount)
                                            stopwatch = QuizStopwatch.start(stopwatch.enabled, currentClock(), bootCount)
                                            lastCountdownCue = 6
                                        }
                                        savedSession = next
                                    }
                                    else {
                                        savedSession = session.submit()
                                        if (savedSession.checked) {
                                            stopwatch = stopwatch.finish(currentClock(), bootCount)
                                            val correct = session.selectedIndex == q.correctIndex
                                            sound?.play(correct)
                                            if (!correct && hapticsEnabled) {
                                                if (onWrongAnswerHaptic != null) onWrongAnswerHaptic() else vibration.wrongAnswer()
                                            }
                                        }
                                    }
                                },
                                enabled = session.selectedIndex >= 0 && !completing,
                                tag = "quiz_action",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizCertificateButton(translation: Translation, enabled: Boolean = true, onClick: () -> Unit) {
    val colors = LocalHopeColors.current
    val foreground = if (enabled) colors.text else colors.text.copy(alpha = 0.38f)
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).testTag("quiz_certificates"),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (enabled) colors.accentLine else colors.divider),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.accentSoft,
            contentColor = colors.text,
            disabledContainerColor = colors.accentSoft.copy(alpha = 0.45f),
            disabledContentColor = foreground,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(quizString(translation, R.string.quiz_certificate_generate),
            modifier = Modifier.fillMaxWidth(), color = foreground,
            textAlign = TextAlign.Center,
            fontFamily = interfaceFontFor(translation), fontSize = 16.sp,
            lineHeight = 25.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuizQuestionText(question: String, translation: Translation) {
    val passageStart = question.indexOf("\n\n")
    val content = buildAnnotatedString {
        if (passageStart < 0) append(question)
        else {
            append(question.substring(0, passageStart + 2))
            withStyle(ParagraphStyle(lineHeight = 27.sp)) {
                withStyle(SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal)) {
                    append(question.substring(passageStart + 2))
                }
            }
        }
    }
    Text(content, Modifier.testTag("quiz_question").semantics { heading() },
        color = LocalHopeColors.current.text, fontFamily = interfaceFontFor(translation),
        fontSize = 21.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuizAnswerFeedback(question: QuizQuestion, translation: Translation, correct: Boolean, modifier: Modifier = Modifier) {
    val colors = LocalHopeColors.current
    val success = Color(0xFF237447)
    Surface(color = colors.surface, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, colors.divider),
        modifier = modifier.fillMaxWidth().testTag("quiz_feedback").semantics { liveRegion = LiveRegionMode.Polite }) {
        Column(Modifier.heightIn(min = 58.dp).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)) {
            // Keep short references beside the result. Long translations and large fonts wrap naturally.
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(quizString(translation, if (correct) R.string.quiz_correct else R.string.quiz_incorrect),
                    modifier = Modifier.padding(end = 16.dp).alignByBaseline(),
                    color = if (correct) success else colors.danger, fontFamily = interfaceFontFor(translation),
                    fontSize = 16.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold)
                Text(BibleReferenceFormatter.formatFull(question.reference, translation),
                    modifier = Modifier.alignByBaseline(), color = colors.textSecondary,
                    fontFamily = interfaceFontFor(translation), fontSize = 14.sp, lineHeight = 21.sp)
            }
            if (!correct) QuizText(quizString(translation, R.string.quiz_correct_answer, question.options[question.correctIndex]),
                translation, small = true, color = success)
            if (question.explanation.isNotBlank()) QuizText(question.explanation, translation, small = true)
        }
    }
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
