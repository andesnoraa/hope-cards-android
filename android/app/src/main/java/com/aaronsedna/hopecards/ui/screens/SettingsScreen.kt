package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import com.aaronsedna.hopecards.model.AppSettings
import com.aaronsedna.hopecards.model.ThemeName
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.components.ResponsiveScrollColumn
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
    onEnableReminder: () -> Unit,
    onBackup: () -> Unit,
    onExport: () -> Unit,
    onRestore: () -> Unit,
) {
    val colors = LocalHopeColors.current
    var themePicker by remember { mutableStateOf(false) }
    var translationPicker by remember { mutableStateOf(false) }
    var reminderPicker by remember { mutableStateOf(false) }
    var pickerHour by remember { mutableStateOf("08") }
    var pickerMinute by remember { mutableStateOf("00") }
    var pickerIsPm by remember { mutableStateOf(false) }
    var enableReminderAfterSave by remember { mutableStateOf(false) }

    fun openReminderPicker(enableAfterSave: Boolean) {
        val hour12 = settings.dailyHopeReminderHour % 12
        pickerHour = (if (hour12 == 0) 12 else hour12).toString().padStart(2, '0')
        pickerMinute = settings.dailyHopeReminderMinute.toString().padStart(2, '0')
        pickerIsPm = settings.dailyHopeReminderHour >= 12
        enableReminderAfterSave = enableAfterSave
        reminderPicker = true
    }

    ResponsiveScrollColumn(
        Modifier.padding(horizontal = 24.dp).padding(top = 22.dp, bottom = 40.dp),
    ) {
        Text(
            "Customize your Hope Cards experience.",
            color = colors.textSecondary,
            fontFamily = Poppins,
            fontSize = 16.sp,
            lineHeight = 28.sp,
            modifier = Modifier.padding(bottom = 28.dp),
        )

        SettingsHeader("APPEARANCE", AppIconGlyph.SparklesOutline)
        SettingsRow(
            title = "Theme",
            subtitle = settings.themeName.label,
            onClick = { themePicker = true },
            trailing = { ThemePreview(settings.themeName); Chevron() },
        )
        Divider()
        SettingsRow(
            title = "Draw Button",
            subtitle = "Show a button for drawing cards, or tap the deck instead.",
            trailing = { HopeSwitch(settings.showDrawButton) { value -> onUpdate { it.copy(showDrawButton = value) } } },
        )
        Divider()
        SettingsRow(
            title = "Bible Translation",
            subtitle = "${settings.preferredTranslation.label} · ${settings.preferredTranslation.displayName}",
            onClick = { translationPicker = true },
            trailing = { Chevron() },
        )

        SettingsHeader("DAILY HOPE", AppIconGlyph.NotificationsOutline, Modifier.padding(top = 26.dp))
        SettingsRow(
            title = "Background Music",
            subtitle = "Play peaceful music with Daily Hope.",
            trailing = { HopeSwitch(settings.dailyHopeMusicEnabled) { value -> onUpdate { it.copy(dailyHopeMusicEnabled = value) } } },
        )
        Divider()
        SettingsRow(
            title = "Daily Reminder",
            subtitle = "Receive a daily verse reminder.",
            trailing = {
                HopeSwitch(
                    checked = settings.dailyHopeReminderEnabled,
                    modifier = Modifier.testTag("daily_reminder_switch"),
                    onChecked = { value ->
                        if (value) openReminderPicker(enableAfterSave = true)
                        else onUpdate { it.copy(dailyHopeReminderEnabled = false) }
                    },
                )
            },
        )
        Divider()
        SettingsRow(
            title = "Reminder Time",
            subtitle = formatTime(settings.dailyHopeReminderHour, settings.dailyHopeReminderMinute),
            onClick = { openReminderPicker(enableAfterSave = false) },
            trailing = { Chevron() },
        )

        SettingsHeader("INTERACTION", AppIconGlyph.HandLeftOutline, Modifier.padding(top = 26.dp))
        SettingsRow(
            title = "Haptic Feedback",
            subtitle = "Use gentle vibration when drawing and saving cards.",
            trailing = { HopeSwitch(settings.enableHaptics) { value -> onUpdate { it.copy(enableHaptics = value) } } },
        )

        SettingsHeader("BACKUP & RESTORE", AppIconGlyph.CloudUploadOutline, Modifier.padding(top = 26.dp))
        SettingsRow(
            title = "Backup Data",
            subtitle = "Create a device backup of favorites, journal entries, and settings.",
            onClick = onBackup,
            trailing = { AppIcon(AppIconGlyph.CheckmarkCircleOutline, null, colors.textTertiary) },
        )
        Divider()
        SettingsRow(
            title = "Export Backup",
            subtitle = "Save or share a copy outside Hope Cards. Backup files are not encrypted.",
            onClick = onExport,
            trailing = { AppIcon(AppIconGlyph.CloudUploadOutline, null, colors.textTertiary) },
        )
        Divider()
        SettingsRow(
            title = "Restore Backup",
            subtitle = "Replace this device’s Hope Cards data from a backup file.",
            onClick = onRestore,
            trailing = { AppIcon(AppIconGlyph.RefreshOutline, null, colors.textTertiary) },
        )
    }

    if (themePicker) {
        SelectorDialog(
            title = "Theme",
            subtitle = "Choose a style that helps you pause and reflect.",
            onDismiss = { themePicker = false },
        ) {
            ThemeName.entries.forEach { theme ->
                PickerRow(theme.label, theme.description, theme == settings.themeName) {
                    onUpdate { it.copy(themeName = theme) }
                    themePicker = false
                }
            }
        }
    }
    if (translationPicker) {
        BibleTranslationDialog(
            selected = settings.preferredTranslation,
            onDismiss = { translationPicker = false },
            onSelect = { translation ->
                onUpdate { it.copy(preferredTranslation = translation) }
                translationPicker = false
            },
        )
    }
    if (reminderPicker) {
        ReminderTimeDialog(
            hour = pickerHour,
            minute = pickerMinute,
            isPm = pickerIsPm,
            onHourChange = { pickerHour = sanitizeTimePart(it) },
            onMinuteChange = { pickerMinute = sanitizeTimePart(it) },
            onStepHour = { amount ->
                val current = pickerHour.toIntOrNull()?.coerceIn(1, 12) ?: 12
                pickerHour = (((current - 1 + amount + 12) % 12) + 1).toString().padStart(2, '0')
            },
            onStepMinute = { amount ->
                val current = pickerMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                pickerMinute = ((current + amount + 60) % 60).toString().padStart(2, '0')
            },
            onPeriodChange = { pickerIsPm = it },
            onDismiss = {
                reminderPicker = false
                enableReminderAfterSave = false
            },
            onSave = {
                val hour12 = pickerHour.toIntOrNull()?.coerceIn(1, 12) ?: 12
                val minute = pickerMinute.toIntOrNull()?.coerceIn(0, 59) ?: 0
                val hour24 = when {
                    pickerIsPm && hour12 != 12 -> hour12 + 12
                    !pickerIsPm && hour12 == 12 -> 0
                    else -> hour12
                }
                onUpdate { it.copy(dailyHopeReminderHour = hour24, dailyHopeReminderMinute = minute) }
                if (enableReminderAfterSave) onEnableReminder()
                reminderPicker = false
                enableReminderAfterSave = false
            },
        )
    }
}

@Composable
fun BibleTranslationDialog(
    selected: Translation,
    onDismiss: () -> Unit,
    onSelect: (Translation) -> Unit,
) {
    SelectorDialog(
        title = "Bible Translation",
        subtitle = "Choose the translation used throughout Hope Cards.",
        onDismiss = onDismiss,
    ) {
        Translation.entries.forEach { translation ->
            PickerRow(
                "${translation.label} · ${translation.displayName}",
                translation.language,
                translation == selected,
            ) { onSelect(translation) }
        }
    }
}

@Composable
private fun ReminderTimeDialog(
    hour: String,
    minute: String,
    isPm: Boolean,
    onHourChange: (String) -> Unit,
    onMinuteChange: (String) -> Unit,
    onStepHour: (Int) -> Unit,
    onStepMinute: (Int) -> Unit,
    onPeriodChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val colors = LocalHopeColors.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 38.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp).testTag("reminder_time_dialog"),
                shape = RoundedCornerShape(28.dp),
                color = colors.surface,
                shadowElevation = 14.dp,
            ) {
                Column(Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Reminder Time",
                            color = colors.text,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            lineHeight = 30.sp,
                            letterSpacing = (-.35).sp,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = colors.accentSoft,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AppIcon(AppIconGlyph.Close, "Close reminder time selector", colors.text, size = 21.dp)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TimeUnit(hour, onHourChange, { onStepHour(1) }, { onStepHour(-1) }, "hour")
                        Text(
                            ":",
                            color = colors.accent,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp,
                        )
                        TimeUnit(minute, onMinuteChange, { onStepMinute(1) }, { onStepMinute(-1) }, "minute")
                        Column(
                            modifier = Modifier.width(64.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            PeriodButton("AM", selected = !isPm) { onPeriodChange(false) }
                            PeriodButton("PM", selected = isPm) { onPeriodChange(true) }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Button(
                            onClick = onSave,
                            modifier = Modifier.testTag("reminder_time_save"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.buttonText),
                        ) {
                            Text("Save", fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeUnit(
    value: String,
    onValueChange: (String) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    label: String,
) {
    val colors = LocalHopeColors.current
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(onClick = onIncrease, color = Color.Transparent, modifier = Modifier.size(48.dp, 40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                AppIcon(AppIconGlyph.ChevronUp, "Increase reminder $label", colors.accent, size = 24.dp)
            }
        }
        Surface(color = colors.background, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(76.dp, 62.dp)) {
            Box(contentAlignment = Alignment.Center) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(
                        color = colors.text,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Surface(onClick = onDecrease, color = Color.Transparent, modifier = Modifier.size(48.dp, 40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                AppIcon(AppIconGlyph.ChevronDown, "Decrease reminder $label", colors.accent, size = 24.dp)
            }
        }
    }
}

@Composable
private fun PeriodButton(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalHopeColors.current
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(42.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) colors.text else colors.background,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (selected) colors.buttonText else colors.cardMuted,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun SelectorDialog(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalHopeColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().widthIn(max = 600.dp).heightIn(max = 700.dp),
                shape = RoundedCornerShape(28.dp),
                color = colors.surface,
                shadowElevation = 14.dp,
            ) {
                Column(Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f).padding(end = 16.dp)) {
                            Text(
                                title,
                                color = colors.text,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                lineHeight = 30.sp,
                                letterSpacing = (-.35).sp,
                            )
                            Text(
                                subtitle,
                                color = colors.textSecondary,
                                fontFamily = Poppins,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(top = 5.dp),
                            )
                        }
                        Surface(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = colors.accentSoft,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                AppIcon(AppIconGlyph.Close, "Close selector", colors.text, size = 21.dp)
                            }
                        }
                    }
                    Column(
                        Modifier.fillMaxWidth().padding(top = 18.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        content = content,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(label: String, icon: AppIconGlyph, modifier: Modifier = Modifier) {
    val colors = LocalHopeColors.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = CircleShape,
            color = colors.accent,
            shadowElevation = 3.dp,
        ) { Box(contentAlignment = Alignment.Center) { AppIcon(icon, null, Color.White, size = 20.dp) } }
        Text(label, color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(start = 14.dp))
        Box(Modifier.padding(start = 18.dp).height(1.dp).weight(1f).background(colors.accentLine))
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit,
) {
    val colors = LocalHopeColors.current
    Row(
        Modifier.fillMaxWidth().padding(start = 56.dp).heightIn(min = 70.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 18.dp)) {
            Text(title, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Text(subtitle, color = colors.textSecondary, fontFamily = Poppins, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 6.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { trailing() }
    }
}

@Composable
private fun Divider() {
    val colors = LocalHopeColors.current
    HorizontalDivider(Modifier.padding(start = 56.dp), color = colors.divider, thickness = 1.dp)
}

@Composable
private fun HopeSwitch(
    checked: Boolean,
    modifier: Modifier = Modifier,
    onChecked: (Boolean) -> Unit,
) {
    val colors = LocalHopeColors.current
    Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = onChecked,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = colors.accent,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = colors.switchOff,
            uncheckedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
private fun ThemePreview(theme: ThemeName) {
    val colors = LocalHopeColors.current
    val palette = when (theme) {
        ThemeName.CLASSIC -> listOf(0xFFF8F6F2, 0xFF1A2747, 0xFFC89B3C)
        ThemeName.ROSE_DAWN -> listOf(0xFFFAF3F1, 0xFF6E2E42, 0xFFB96F73)
        ThemeName.OLIVE_GROVE -> listOf(0xFFF5F4EA, 0xFF35482F, 0xFF8C7A33)
        ThemeName.SERENITY -> listOf(0xFFFCFBF7, 0xFF183B32, 0xFFC5A35A)
        ThemeName.STILL_WATER -> listOf(0xFFF4F8FA, 0xFF234D63, 0xFF5F9EC1)
        ThemeName.MIDNIGHT -> listOf(0xFFF7F4EE, 0xFF121212, 0xFFB98F3B)
        ThemeName.COASTAL_LINEN -> listOf(0xFFF4F7F5, 0xFF164E52, 0xFFC07D52)
        ThemeName.QUIET_LAVENDER -> listOf(0xFFF8F5FA, 0xFF4C365A, 0xFF8D6A9F)
        ThemeName.VINTAGE_HERITAGE -> listOf(0xFFF7EBD2, 0xFF542C24, 0xFFC69A45)
    }
    Box(Modifier.size(58.dp, 24.dp)) {
        palette.forEachIndexed { index, value ->
            Box(
                Modifier.padding(start = (index * 17).dp).size(24.dp)
                    .background(Color(value), CircleShape)
                    .border(1.dp, colors.divider, CircleShape),
            )
        }
    }
}

@Composable
private fun Chevron() {
    AppIcon(AppIconGlyph.ChevronForward, null, LocalHopeColors.current.textTertiary, size = 24.dp)
}

@Composable
private fun PickerRow(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalHopeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = if (selected) colors.accentSoft else colors.surface,
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(subtitle, color = colors.textSecondary, fontFamily = Poppins, fontSize = 13.sp, lineHeight = 18.sp)
            }
            if (selected) {
                AppIcon(AppIconGlyph.Checkmark, "Selected", colors.accent, Modifier.padding(start = 12.dp), 22.dp)
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("h:mm a")).lowercase()

private fun sanitizeTimePart(value: String): String = value.filter(Char::isDigit).take(2)
