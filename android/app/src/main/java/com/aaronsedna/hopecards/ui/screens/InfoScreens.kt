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
import androidx.compose.ui.platform.LocalContext
import com.aaronsedna.hopecards.BuildConfig
import com.aaronsedna.hopecards.R
import com.aaronsedna.hopecards.model.Translation
import com.aaronsedna.hopecards.ui.LocalAppTranslation
import com.aaronsedna.hopecards.ui.appString
import com.aaronsedna.hopecards.ui.forTranslation
import com.aaronsedna.hopecards.ui.components.ResponsiveScrollColumn
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins

@Composable
fun PrivacyScreen(onPrivacyOptions: (() -> Unit)?) {
    val sections = listOf(
        appString(R.string.privacy_information) to listOf(
            appString(R.string.privacy_information_1), appString(R.string.privacy_information_2),
        ),
        appString(R.string.privacy_device) to listOf(
            appString(R.string.privacy_device_1), appString(R.string.privacy_device_2), appString(R.string.privacy_device_3),
        ),
        appString(R.string.privacy_permissions) to listOf(
            appString(R.string.privacy_permissions_1), appString(R.string.privacy_permissions_2),
        ),
        appString(R.string.privacy_sharing) to listOf(
            appString(R.string.privacy_sharing_1), appString(R.string.privacy_sharing_2), appString(R.string.privacy_sharing_3),
        ),
        appString(R.string.privacy_advertising) to listOf(
            appString(R.string.privacy_advertising_1), appString(R.string.privacy_advertising_2),
        ),
        appString(R.string.privacy_purchase) to listOf(
            appString(R.string.privacy_purchase_1),
        ),
        appString(R.string.privacy_children) to listOf(appString(R.string.privacy_children_1)),
        appString(R.string.privacy_updates) to listOf(appString(R.string.privacy_updates_1)),
    )
    val colors = LocalHopeColors.current
    ResponsiveScrollColumn(
        Modifier.padding(horizontal = 24.dp).padding(top = 32.dp, bottom = 42.dp),
        maxContentWidth = 840.dp,
    ) {
        Text(appString(R.string.privacy_title), color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-.6).sp)
        Text(appString(R.string.privacy_intro), color = colors.textSecondary, fontFamily = Poppins, fontSize = 17.sp, lineHeight = 26.sp, modifier = Modifier.padding(top = 12.dp))
        Text(appString(R.string.privacy_effective), color = colors.textTertiary, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 14.dp, bottom = 30.dp))
        sections.forEach { (title, paragraphs) ->
            HorizontalDivider(color = colors.divider)
            Column(Modifier.padding(vertical = 32.dp)) {
                Text(title, color = colors.text, fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-.35).sp)
                Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    paragraphs.forEach { paragraph ->
                        Text(paragraph, color = colors.cardText, fontFamily = Poppins, fontSize = 15.sp, lineHeight = 23.sp)
                    }
                    if (title == appString(R.string.privacy_advertising) && onPrivacyOptions != null) {
                        Text("${appString(R.string.ad_privacy_choices)}  ↗", color = colors.accent, fontFamily = Poppins, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, modifier = Modifier.clickable(onClick = onPrivacyOptions).padding(vertical = 5.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AboutScreen() {
    val colors = LocalHopeColors.current
    val context = LocalContext.current
    val translation = LocalAppTranslation.current
    val features = context.forTranslation(translation).resources.getStringArray(R.array.about_feature_list).toList()
    val developedWith = appString(R.string.about_developed_with)
    val by = appString(R.string.about_by)
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
            Text(appString(R.string.about_tagline), color = colors.textSecondary, fontFamily = Poppins, fontSize = 17.sp, lineHeight = 25.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 300.dp).padding(top = 9.dp))
            Text(appString(R.string.version_format, displayedVersion), color = colors.textTertiary, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 14.dp))
        }
        AboutSection(appString(R.string.about_features)) {
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
        AboutSection(appString(R.string.about_why)) {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .padding(vertical = 6.dp)
                        .width(2.dp)
                        .background(colors.accent),
                )
                Column(Modifier.padding(start = 18.dp)) {
                    Text(appString(R.string.about_story_1), color = colors.cardText, fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 25.sp, letterSpacing = (-.1).sp)
                    Text(appString(R.string.about_story_2), color = colors.textSecondary, fontFamily = Poppins, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
        AboutSection(appString(R.string.about_credits), bottomPadding = 20.dp) {
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
                        Text(appString(R.string.about_public_domain), color = colors.textSecondary, fontFamily = Poppins, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
                if (index != Translation.entries.lastIndex) HorizontalDivider(thickness = .5.dp, color = colors.divider)
            }
        }
        HorizontalDivider(thickness = .5.dp, color = colors.divider)
        Text(
            buildAnnotatedString {
                append("$developedWith ")
                // Force text presentation so Samsung does not replace the heart with an emoji.
                withStyle(SpanStyle(color = colors.danger)) { append("♥︎") }
                append(" $by ")
                withStyle(SpanStyle(color = colors.text, fontWeight = FontWeight.SemiBold)) { append("Aaronsedna") }
            },
            color = colors.textTertiary,
            fontFamily = Poppins,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        )
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
