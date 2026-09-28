package com.aaronsedna.hopecards.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.export.CertificateDetails
import com.aaronsedna.hopecards.export.CertificateParticipant
import com.aaronsedna.hopecards.export.GeneratedCertificate
import com.aaronsedna.hopecards.export.QuizCertificates
import com.aaronsedna.hopecards.model.QuizLanguage
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.forQuizTranslation
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.interfaceFontFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** An independent participation-certificate form. It never changes or infers a quiz result. */
@Composable
internal fun QuizCertificateScreen(translation: Translation, onClose: () -> Unit) {
    val context = LocalContext.current
    val resources = remember(context, translation) { context.forQuizTranslation(translation).resources }
    val colors = LocalHopeColors.current
    val font = interfaceFontFor(translation)
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val bibleLanguage = QuizLanguage.forTranslation(translation)
    val language = bibleLanguage.code
    val preferences = remember(context) { context.getSharedPreferences("bible-quiz", Context.MODE_PRIVATE) }
    val availableLanguages = remember {
        QuizLanguage.entries.filter { candidate -> Translation.entries.any { QuizLanguage.forTranslation(it) == candidate } }
    }
    var outputLanguageCode by rememberSaveable(language) {
        mutableStateOf(preferences.getString("certificate_language", null)
            ?.takeIf { saved -> availableLanguages.any { it.code == saved } }
            ?: if (preferences.getBoolean("certificate_english", false)) QuizLanguage.ENGLISH.code else language)
    }
    val outputLanguage = availableLanguages.first { it.code == outputLanguageCode }
    val outputTranslation = outputLanguage.certificateTranslation()
    val outputResources = remember(context, outputTranslation) { context.forQuizTranslation(outputTranslation).resources }
    // DatePicker represents calendar dates at UTC midnight, independent of the device timezone.
    var dateMillis by rememberSaveable(language) { mutableLongStateOf(LocalDate.now().toEpochDay() * 86_400_000L) }
    val date = remember(dateMillis, outputLanguage) { formatCertificateDate(dateMillis, outputLanguage) }
    var datePickerOpen by rememberSaveable(language) { mutableStateOf(false) }
    var languagePickerOpen by rememberSaveable(language) { mutableStateOf(false) }
    var competition by rememberSaveable(language) { mutableStateOf(outputResources.getString(R.string.quiz_certificate_competition_default)) }
    var competitionEdited by rememberSaveable(language) { mutableStateOf(false) }
    var organizer by rememberSaveable(language) { mutableStateOf("") }
    var names by rememberSaveable(language) { mutableStateOf("") }
    var submitted by rememberSaveable(language) { mutableStateOf(false) }
    var generatedNames by rememberSaveable(language) { mutableStateOf<List<String>>(arrayListOf()) }
    var generatedPaths by rememberSaveable(language) { mutableStateOf<List<String>>(arrayListOf()) }
    var generatedTranslationId by rememberSaveable(language) { mutableStateOf(outputTranslation.id) }
    var busy by remember { mutableStateOf(false) }
    var completed by remember { mutableIntStateOf(0) }
    var error by rememberSaveable(language) { mutableStateOf(false) }
    // Spreadsheet columns often introduce tabs. Normalize them only for export; keep the
    // original editable draft intact. Blank lines separate names and duplicates stay distinct.
    val participants = remember(names) {
        val tabs = Regex("\\t+")
        names.lineSequence().map { it.replace(tabs, " ").trim() }.filter(String::isNotEmpty)
            .map(::CertificateParticipant).toList()
    }
    val competitionError = when {
        competition.isBlank() -> R.string.quiz_certificate_error_required
        competition.trim().length > 120 -> R.string.quiz_certificate_error_title_length
        else -> null
    }
    val organizerError = R.string.quiz_certificate_error_organizer_length.takeIf { organizer.trim().length > 120 }
    val namesError = when {
        participants.isEmpty() -> R.string.quiz_certificate_error_required
        participants.size > 50 -> R.string.quiz_certificate_error_count
        participants.any { it.name.length > 100 } -> R.string.quiz_certificate_error_name_length
        else -> null
    }
    val valid = listOf(competitionError, organizerError, namesError).all { it == null }
    val visibleValidationError = if (submitted && !valid) listOfNotNull(competitionError, organizerError, namesError).firstOrNull() else null
    val certificates = remember(generatedNames, generatedPaths) {
        generatedNames.zip(generatedPaths) { name, path -> GeneratedCertificate(name, File(path)) }
    }
    val filesAvailable = certificates.isNotEmpty() && certificates.all { it.file.isFile }
    val missingFiles = certificates.isNotEmpty() && !filesAvailable
    val sharingMessage = remember(context, generatedTranslationId) {
        QuizCertificates.shareMessage(context, Translation.fromId(generatedTranslationId))
    }
    var sharingMessageCopied by remember(sharingMessage) { mutableStateOf(false) }
    val selectLanguage: (QuizLanguage) -> Unit = { selected ->
        if (!busy) {
            outputLanguageCode = selected.code
            preferences.edit().putString("certificate_language", selected.code).remove("certificate_english").apply()
            if (!competitionEdited) competition = context.forQuizTranslation(selected.certificateTranslation())
                .getString(R.string.quiz_certificate_competition_default)
            languagePickerOpen = false
        }
    }
    val share: (List<GeneratedCertificate>) -> Unit = { selected ->
        try {
            context.startActivity(Intent.createChooser(
                QuizCertificates.shareIntent(context, Translation.fromId(generatedTranslationId), selected),
                resources.getString(R.string.quiz_certificate_title),
            ))
            error = false
        } catch (_: Exception) { error = true }
    }
    val generate: () -> Unit = {
        submitted = true
        error = false
        if (valid && !busy) {
            focus.clearFocus()
            val details = CertificateDetails(competition.trim(), date.trim(), organizer.trim())
            val people = participants.toList()
            val certificateTranslation = outputTranslation
            // Set immediately before dispatch, so two rapid taps cannot start overlapping batches.
            busy = true
            completed = 0
            scope.launch {
                try {
                    val result = QuizCertificates.generate(context.applicationContext, certificateTranslation, details, people) { done, _ ->
                        withContext(Dispatchers.Main.immediate) { completed = done }
                    }
                    generatedNames = ArrayList(result.map { it.participantName })
                    generatedPaths = ArrayList(result.map { it.file.absolutePath })
                    generatedTranslationId = certificateTranslation.id
                } catch (cancel: CancellationException) { throw cancel }
                catch (_: Exception) { error = true }
                finally { busy = false }
            }
        }
    }

    Dialog(onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(color = colors.background, modifier = Modifier.fillMaxSize().testTag("certificate_screen")) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(resources.getString(R.string.quiz_certificate_generate), color = colors.text,
                        fontFamily = font, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 27.sp,
                        modifier = Modifier.weight(1f).semantics { heading() })
                    IconButton(onClick = onClose, modifier = Modifier.testTag("certificate_close")) {
                        AppIcon(AppIconGlyph.Close, resources.getString(R.string.quiz_export_close), colors.text, size = 23.dp)
                    }
                }
                HorizontalDivider(color = colors.divider)
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp).testTag("certificate_content"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(shape = RoundedCornerShape(16.dp), color = colors.accentSoft) {
                        Text(resources.getString(R.string.quiz_certificate_intro), fontFamily = font,
                            color = colors.text, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(16.dp))
                    }
                    if (filesAvailable) {
                        Text(resources.getString(R.string.quiz_certificate_language_ready,
                            QuizLanguage.forTranslation(Translation.fromId(generatedTranslationId)).nativeName),
                            color = colors.textSecondary, fontFamily = font, fontSize = 13.sp, lineHeight = 20.sp,
                            modifier = Modifier.testTag("certificate_output_language"))
                        Text(resources.getString(R.string.quiz_certificate_ready, certificates.size),
                            color = colors.text, fontFamily = font, fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp, modifier = Modifier.testTag("certificate_ready").semantics {
                                heading(); liveRegion = LiveRegionMode.Polite
                            })
                        certificates.forEachIndexed { index, certificate ->
                            Surface(shape = RoundedCornerShape(18.dp), color = colors.surface,
                                border = BorderStroke(1.dp, colors.divider), modifier = Modifier.fillMaxWidth().testTag("certificate_person_$index")) {
                                Row(Modifier.padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(certificate.participantName, fontFamily = font, fontSize = 16.sp, lineHeight = 24.sp,
                                        color = colors.text, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { share(listOf(certificate)) }, modifier = Modifier.testTag("certificate_share_$index")) {
                                        AppIcon(AppIconGlyph.ShareOutline, resources.getString(R.string.quiz_certificate_share_one, certificate.participantName),
                                            colors.text, size = 22.dp)
                                    }
                                }
                            }
                        }
                        Surface(shape = RoundedCornerShape(18.dp), color = colors.accentSoft,
                            modifier = Modifier.fillMaxWidth().testTag("certificate_sharing_message_card")) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(resources.getString(R.string.quiz_certificate_share_message),
                                    color = colors.text, fontFamily = font, fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp, modifier = Modifier.semantics { heading() })
                                Text(sharingMessage, color = colors.text,
                                    fontFamily = interfaceFontFor(Translation.fromId(generatedTranslationId)),
                                    fontSize = 12.sp, lineHeight = 18.sp,
                                    modifier = Modifier.fillMaxWidth().testTag("certificate_sharing_message"))
                                Text(resources.getString(R.string.quiz_certificate_share_hint), color = colors.textSecondary,
                                    fontFamily = font, fontSize = 12.sp, lineHeight = 18.sp)
                                TextButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText(
                                        resources.getString(R.string.quiz_certificate_share_message), sharingMessage))
                                    sharingMessageCopied = true
                                }, modifier = Modifier.testTag("certificate_copy_message")) {
                                    Text(resources.getString(if (sharingMessageCopied) R.string.quiz_certificate_share_copied
                                        else R.string.quiz_certificate_share_copy), color = colors.text,
                                        fontFamily = font, fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                                }
                            }
                        }
                    } else {
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CertificateSelectionField(outputLanguage.nativeName, resources.getString(R.string.quiz_certificate_language_label),
                                "certificate_language", translation, outputTranslation, busy) {
                                focus.clearFocus(); languagePickerOpen = true
                            }
                            Text(resources.getString(R.string.quiz_certificate_language_hint), fontFamily = font,
                                color = colors.textSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                        CertificateField(competition, { competition = it; competitionEdited = true }, resources.getString(R.string.quiz_certificate_competition_label),
                            "certificate_competition", translation, busy, if (submitted) competitionError?.let(resources::getString) else null)
                        CertificateSelectionField(date, resources.getString(R.string.quiz_certificate_date_label),
                            "certificate_date", translation, outputTranslation, busy) {
                            focus.clearFocus(); datePickerOpen = true
                        }
                        CertificateField(organizer, { organizer = it }, resources.getString(R.string.quiz_certificate_organizer_label),
                            "certificate_organizer", translation, busy, if (submitted) organizerError?.let(resources::getString) else null)
                        CertificateField(names, { names = it }, resources.getString(R.string.quiz_certificate_participants_label),
                            "certificate_participants", translation, busy, if (submitted) namesError?.let(resources::getString) else null,
                            helper = resources.getString(R.string.quiz_certificate_participants_hint) + "\n" +
                                resources.getString(R.string.quiz_certificate_counter, participants.size, 50), multiline = true)
                    }
                }
                Surface(color = colors.background, shadowElevation = 6.dp) {
                    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (error || missingFiles) Text(resources.getString(R.string.quiz_certificate_error), color = colors.danger,
                            fontFamily = font, fontSize = 13.sp, lineHeight = 19.sp,
                            modifier = Modifier.testTag("certificate_error").semantics { liveRegion = LiveRegionMode.Polite })
                        else if (visibleValidationError != null) Text(resources.getString(visibleValidationError), color = colors.danger,
                            fontFamily = font, fontSize = 13.sp, lineHeight = 19.sp,
                            modifier = Modifier.testTag("certificate_validation_error").semantics { liveRegion = LiveRegionMode.Polite })
                        Button(onClick = { if (filesAvailable) share(certificates) else generate() }, enabled = !busy,
                            shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.buttonBackground, contentColor = colors.buttonText),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp).testTag(if (filesAvailable) "certificate_share_all" else "certificate_generate")) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (busy) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp, color = colors.buttonText)
                                Text(when {
                                    busy -> resources.getString(R.string.quiz_certificate_generating, completed, participants.size)
                                    filesAvailable -> resources.getString(R.string.quiz_certificate_share_all)
                                    else -> resources.getString(R.string.quiz_certificate_generate)
                                }, fontFamily = font, fontSize = 15.sp, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                            }
                        }
                        if (filesAvailable) TextButton(onClick = {
                            generatedNames = arrayListOf(); generatedPaths = arrayListOf(); error = false; sharingMessageCopied = false
                        }, modifier = Modifier.fillMaxWidth().testTag("certificate_edit")) {
                            Text(resources.getString(R.string.quiz_certificate_edit), fontFamily = font, color = colors.text)
                        }
                    }
                }
            }
        }
    }
    if (languagePickerOpen && !busy) AlertDialog(
        onDismissRequest = { languagePickerOpen = false },
        modifier = Modifier.testTag("certificate_language_picker"),
        containerColor = colors.background,
        title = { Text(resources.getString(R.string.quiz_certificate_language_label), fontFamily = font, color = colors.text) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableLanguages.forEach { option ->
                    CertificateLanguageOption(option.nativeName, option == outputLanguage, false,
                        option.certificateTranslation(), "certificate_language_${option.code}") { selectLanguage(option) }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { languagePickerOpen = false }, modifier = Modifier.testTag("certificate_language_cancel")) {
                Text(resources.getString(R.string.quiz_certificate_date_cancel), fontFamily = font, color = colors.text)
            }
        },
    )
    if (datePickerOpen && !busy) CertificateDatePicker(dateMillis, translation,
        onDismiss = { datePickerOpen = false }, onDateSelected = { dateMillis = it; datePickerOpen = false })
}

private fun QuizLanguage.certificateTranslation(): Translation = Translation.entries.first { QuizLanguage.forTranslation(it) == this }

internal fun formatCertificateDate(utcDateMillis: Long, language: QuizLanguage): String =
    DateFormat.getDateInstance(DateFormat.LONG, Locale.forLanguageTag(language.code)).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(utcDateMillis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CertificateDatePicker(dateMillis: Long, translation: Translation,
    onDismiss: () -> Unit, onDateSelected: (Long) -> Unit) {
    val context = LocalContext.current
    val localizedContext = remember(context, translation) { context.forQuizTranslation(translation) }
    val resources = localizedContext.resources
    val colors = LocalHopeColors.current
    val font = interfaceFontFor(translation)
    // Localize the calendar's month names, navigation and accessibility labels with the form.
    CompositionLocalProvider(LocalContext provides localizedContext,
        LocalConfiguration provides resources.configuration) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(onDismissRequest = onDismiss, modifier = Modifier.testTag("certificate_date_picker"),
            colors = DatePickerDefaults.colors(containerColor = colors.background),
            confirmButton = {
                TextButton(onClick = { state.selectedDateMillis?.let(onDateSelected) },
                    enabled = state.selectedDateMillis != null, modifier = Modifier.testTag("certificate_date_confirm")) {
                    Text(resources.getString(R.string.quiz_certificate_date_confirm), fontFamily = font, color = colors.text)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, modifier = Modifier.testTag("certificate_date_cancel")) {
                    Text(resources.getString(R.string.quiz_certificate_date_cancel), fontFamily = font, color = colors.text)
                }
            }) {
            val typography = MaterialTheme.typography
            MaterialTheme(typography = typography.copy(
                headlineLarge = typography.headlineLarge.copy(fontFamily = font),
                headlineMedium = typography.headlineMedium.copy(fontFamily = font),
                titleLarge = typography.titleLarge.copy(fontFamily = font),
                titleSmall = typography.titleSmall.copy(fontFamily = font),
                bodyLarge = typography.bodyLarge.copy(fontFamily = font),
                bodyMedium = typography.bodyMedium.copy(fontFamily = font),
                bodySmall = typography.bodySmall.copy(fontFamily = font),
                labelLarge = typography.labelLarge.copy(fontFamily = font),
                labelMedium = typography.labelMedium.copy(fontFamily = font),
                labelSmall = typography.labelSmall.copy(fontFamily = font),
            )) {
                DatePicker(state = state, modifier = Modifier.verticalScroll(rememberScrollState()),
                    title = { Text(resources.getString(R.string.quiz_certificate_date_picker_title), fontFamily = font,
                        color = colors.textSecondary, modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)) },
                    headline = {
                        Text(state.selectedDateMillis?.let { formatCertificateDate(it, QuizLanguage.forTranslation(translation)) }.orEmpty(),
                            fontFamily = font, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold,
                            color = colors.text, maxLines = 2,
                            modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp))
                    },
                    colors = DatePickerDefaults.colors(containerColor = colors.background,
                        selectedDayContainerColor = colors.buttonBackground, selectedDayContentColor = colors.buttonText,
                        todayContentColor = colors.text, todayDateBorderColor = colors.text))
            }
        }
    }
}

@Composable
private fun CertificateSelectionField(value: String, label: String, tag: String,
    translation: Translation, valueTranslation: Translation, busy: Boolean, onClick: () -> Unit) {
    val colors = LocalHopeColors.current
    Surface(onClick = onClick, enabled = !busy, shape = RoundedCornerShape(14.dp),
        color = colors.background, border = BorderStroke(1.dp, colors.divider),
        modifier = Modifier.fillMaxWidth().testTag(tag)) {
        Row(Modifier.heightIn(min = 72.dp).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, fontFamily = interfaceFontFor(translation), fontSize = 12.sp, lineHeight = 18.sp, color = colors.textSecondary)
                Text(value, fontFamily = interfaceFontFor(valueTranslation), fontSize = 16.sp, lineHeight = 24.sp,
                    color = colors.text, modifier = Modifier.testTag("${tag}_value"))
            }
            AppIcon(AppIconGlyph.ChevronDown, null, colors.textSecondary, size = 22.dp)
        }
    }
}

@Composable
private fun CertificateLanguageOption(label: String, selected: Boolean, busy: Boolean,
    translation: Translation, tag: String, onSelect: () -> Unit) {
    val colors = LocalHopeColors.current
    Surface(shape = RoundedCornerShape(14.dp), color = if (selected) colors.accentSoft else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.text else colors.divider)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp)
            .selectable(selected = selected, enabled = !busy, role = Role.RadioButton, onClick = onSelect)
            .testTag(tag).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RadioButton(selected = selected, enabled = !busy, onClick = null,
                colors = RadioButtonDefaults.colors(selectedColor = colors.text, unselectedColor = colors.textSecondary))
            Text(label, fontFamily = interfaceFontFor(translation), fontSize = 15.sp,
                color = colors.text, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CertificateField(value: String, onValueChange: (String) -> Unit, label: String, tag: String,
    translation: Translation, busy: Boolean, error: String?, helper: String? = null, multiline: Boolean = false) {
    val colors = LocalHopeColors.current
    val font = interfaceFontFor(translation)
    val focus = LocalFocusManager.current
    OutlinedTextField(value = value, onValueChange = onValueChange, enabled = !busy,
        modifier = Modifier.fillMaxWidth().testTag(tag), shape = RoundedCornerShape(14.dp),
        label = { Text(label, fontFamily = font, fontSize = 14.sp) },
        textStyle = TextStyle(fontFamily = font, fontSize = 16.sp, lineHeight = 24.sp),
        singleLine = !multiline, minLines = if (multiline) 4 else 1, maxLines = if (multiline) 8 else 1,
        isError = error != null, supportingText = if (error != null || helper != null) {
            { Text(error ?: helper.orEmpty(), fontFamily = font, fontSize = 12.sp, lineHeight = 18.sp,
                modifier = Modifier.testTag("${tag}_hint")) }
        } else null,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = if (multiline) ImeAction.Default else ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focus.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = colors.text, unfocusedTextColor = colors.text,
            focusedBorderColor = colors.text, unfocusedBorderColor = colors.divider,
            focusedLabelColor = colors.text, unfocusedLabelColor = colors.textSecondary,
            cursorColor = colors.text, errorBorderColor = colors.danger, errorLabelColor = colors.danger,
            errorSupportingTextColor = colors.danger))
}
