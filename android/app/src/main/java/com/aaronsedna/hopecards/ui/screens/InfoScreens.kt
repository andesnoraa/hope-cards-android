package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.components.ResponsiveScrollColumn
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins

@Composable
fun PrivacyScreen(onPrivacyOptions: (() -> Unit)?) {
    val sections = listOf(
        "Information We Collect" to listOf(
            "Hope Cards does not ask for your name, email address, phone number, contacts, precise location, photos, or microphone access.",
            "Google Mobile Ads and Google Play may process limited device, consent, advertising, diagnostic, and purchase information to provide ads and verify Remove Ads ownership.",
        ),
        "Data Stored on Your Device" to listOf(
            "Your favorites, private journal entries, selected Bible translation, theme, reminder time, music preference, haptic preference, and other app settings are stored locally on your device.",
            "Journal entries are never sent to Hope Cards or used for advertising. They remain on your device unless you explicitly include them in an exported backup.",
            "Removing the app may remove this data unless you exported a backup.",
        ),
        "Reminders and Permissions" to listOf(
            "If you enable Daily Hope reminders, the app requests notification permission and schedules one battery-conscious reminder on your device.",
            "Background music is included with the app and does not require microphone access.",
        ),
        "Sharing and Backups" to listOf(
            "Sharing a verse or backup opens Android’s sharing controls. Hope Cards does not record who you share with or which service you choose.",
            "Backup files contain favorites, private journal entries, and app settings. They are readable JSON and should be stored somewhere you trust.",
        ),
        "Advertising" to listOf(
            "Hope Cards uses Google Mobile Ads for adaptive banner and occasional full-screen ads. Consent choices are requested through Google’s User Messaging Platform where required.",
            "Your private journal text and saved verse content are not sent for ad targeting.",
        ),
        "Remove Ads Purchase" to listOf(
            "The optional one-time Remove Ads purchase is processed directly by Google Play Billing. Hope Cards checks Play purchase ownership and does not receive your full payment-card details.",
        ),
        "Children’s Privacy" to listOf("Hope Cards is intended for a general audience. We do not knowingly collect personal information directly from children."),
        "Policy Updates" to listOf("We may update this policy when the app or its services change. The effective date above will show when the policy was last revised."),
    )
    val colors = LocalHopeColors.current
    ResponsiveScrollColumn(
        Modifier.padding(horizontal = 24.dp).padding(top = 32.dp, bottom = 42.dp),
        maxContentWidth = 840.dp,
    ) {
        Text("Your privacy matters", color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-.6).sp)
        Text("Hope Cards is designed to keep your verses, favorites, and reflections on your device. It does not require an account.", color = colors.textSecondary, fontFamily = Poppins, fontSize = 17.sp, lineHeight = 26.sp, modifier = Modifier.padding(top = 12.dp))
        Text("Effective 6 September 2026", color = colors.textTertiary, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 14.dp, bottom = 30.dp))
        sections.forEach { (title, paragraphs) ->
            HorizontalDivider(color = colors.divider)
            Column(Modifier.padding(vertical = 32.dp)) {
                Text(title, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-.35).sp)
                Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    paragraphs.forEach { paragraph ->
                        Text(paragraph, color = colors.cardText, fontFamily = Poppins, fontSize = 15.sp, lineHeight = 23.sp)
                    }
                    if (title == "Advertising" && onPrivacyOptions != null) {
                        Text("Ad Privacy Choices  ↗", color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onPrivacyOptions).padding(vertical = 5.dp))
                    }
                }
            }
        }
        Text("© 2026 Hope Cards · All rights reserved", color = colors.textTertiary, fontFamily = Poppins, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
    }
}

@Composable
fun AboutScreen() {
    val colors = LocalHopeColors.current
    val features = listOf("Draw verse cards", "Daily Hope", "Save favorites", "Share verses", "Daily reminders", "Multiple translations", "Custom themes", "Background music", "Haptic feedback", "Backup & restore")
    val displayedVersion = BuildConfig.VERSION_NAME.removeSuffix("-debug")
    ResponsiveScrollColumn(
        Modifier.padding(horizontal = 24.dp).padding(top = 14.dp, bottom = 28.dp),
        maxContentWidth = 840.dp,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(top = 12.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Hope Cards", color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 43.sp, letterSpacing = (-.7).sp, textAlign = TextAlign.Center)
            Text("Discover timely encouragement in Scripture, save meaningful verses, and share hope with others.", color = colors.textSecondary, fontFamily = Poppins, fontSize = 17.sp, lineHeight = 25.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 300.dp).padding(top = 9.dp))
            Text("Version $displayedVersion", color = colors.textTertiary, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 14.dp))
        }
        AboutSection("Features") {
            Column(verticalArrangement = Arrangement.spacedBy(21.dp)) {
                features.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        row.forEach { feature ->
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.Top) {
                                Box(Modifier.padding(top = 8.dp).size(6.dp).background(colors.accent, CircleShape))
                                Text(feature, color = colors.cardText, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(start = 10.dp).weight(1f))
                            }
                        }
                    }
                }
            }
        }
        AboutSection("Why Hope Cards") {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = 6.dp)
                        .width(2.dp)
                        .background(colors.accent),
                )
                Column(Modifier.padding(start = 18.dp)) {
                    Text("Hope Cards was created during a difficult season when Scripture brought strength.", color = colors.cardText, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = (-.1).sp)
                    Text("The vision is simple: to help someone facing a difficult situation find a timely verse, a quiet reminder, and renewed hope.", color = colors.textSecondary, fontFamily = Poppins, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
        AboutSection("Credits", bottomPadding = 20.dp) {
            Translation.entries.forEachIndexed { index, translation ->
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 58.dp).padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        translation.label,
                        color = colors.accent,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = when {
                            translation.label.length > 6 -> 11.sp
                            translation.label.length > 5 -> 12.sp
                            else -> 14.sp
                        },
                        lineHeight = 20.sp,
                        letterSpacing = .2.sp,
                        maxLines = 1,
                        modifier = Modifier.width(52.dp),
                    )
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(translation.attribution, color = colors.cardText, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
                        Text("Public domain", color = colors.textSecondary, fontFamily = Poppins, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
                if (index != Translation.entries.lastIndex) HorizontalDivider(thickness = .5.dp, color = colors.divider)
            }
        }
        HorizontalDivider(thickness = .5.dp, color = colors.divider)
        Text(
            buildAnnotatedString {
                append("Developed with ")
                // Force text presentation so Samsung does not replace the themed heart with a red emoji.
                withStyle(SpanStyle(color = colors.accent)) { append("♥︎") }
                append(" by ")
                withStyle(SpanStyle(color = colors.text, fontWeight = FontWeight.SemiBold)) { append("Aaronsedna") }
            },
            color = colors.textTertiary,
            fontFamily = Poppins,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
        Text("© 2026 Hope Cards · All rights reserved", color = colors.textTertiary, fontFamily = Poppins, fontSize = 11.sp, lineHeight = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
    }
}

@Composable
private fun AboutSection(
    title: String,
    bottomPadding: androidx.compose.ui.unit.Dp = 32.dp,
    content: @Composable () -> Unit,
) {
    val colors = LocalHopeColors.current
    HorizontalDivider(thickness = .5.dp, color = colors.divider)
    Column(Modifier.padding(top = 32.dp, bottom = bottomPadding)) {
        Text(title, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-.35).sp, modifier = Modifier.padding(bottom = 24.dp))
        content()
    }
}
