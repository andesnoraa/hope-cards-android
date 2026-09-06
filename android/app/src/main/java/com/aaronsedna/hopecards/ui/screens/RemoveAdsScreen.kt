package com.aaronsedna.hopecards.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aaronsedna.hopecards.model.BillingState
import com.aaronsedna.hopecards.ui.components.AppIcon
import com.aaronsedna.hopecards.ui.components.AppIconGlyph
import com.aaronsedna.hopecards.ui.components.ResponsiveScrollColumn
import com.aaronsedna.hopecards.ui.theme.LocalHopeColors
import com.aaronsedna.hopecards.ui.theme.Poppins

private data class AdFreeBenefit(
    val title: String,
    val icon: AppIconGlyph,
)

private val adFreeBenefits = listOf(
    AdFreeBenefit(
        title = "No banner or full-screen ads",
        icon = AppIconGlyph.EyeOffOutline,
    ),
    AdFreeBenefit(
        title = "More space for every verse",
        icon = AppIconGlyph.HeartOutline,
    ),
    AdFreeBenefit(
        title = "One purchase · No subscription",
        icon = AppIconGlyph.CheckmarkCircleOutline,
    ),
)

@Composable
fun RemoveAdsScreen(
    billing: BillingState,
    privacyOptionsRequired: Boolean,
    onPurchase: () -> Unit,
    onRestore: () -> Unit,
    onPrivacyOptions: () -> Unit,
) {
    val colors = LocalHopeColors.current

    ResponsiveScrollColumn(
        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 18.dp, bottom = 36.dp),
        maxContentWidth = 560.dp,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                modifier = Modifier.size(82.dp),
                shape = CircleShape,
                color = colors.accentSoft,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppIcon(AppIconGlyph.EyeOffOutline, null, colors.accent, size = 40.dp)
                }
            }
            Text(
                text = "A quieter Hope Cards",
                color = colors.text,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 18.dp),
            )
            Text(
                text = "Every feature stays free. Remove ads once for calmer reading and reflection.",
                color = colors.textSecondary,
                fontFamily = Poppins,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.accentLine),
            shadowElevation = 2.dp,
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                adFreeBenefits.forEachIndexed { index, benefit ->
                    BenefitRow(benefit)
                    if (index < adFreeBenefits.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 50.dp),
                            color = colors.divider,
                        )
                    }
                }
            }
        }

        PurchaseAction(billing = billing, onPurchase = onPurchase)

        if (!billing.isAdFree) {
            TextButton(onClick = onRestore, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AppIcon(AppIconGlyph.RefreshOutline, null, colors.accent, size = 19.dp)
                Text(
                    text = "Restore purchase",
                    modifier = Modifier.padding(start = 7.dp),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }

        if (privacyOptionsRequired && !billing.isAdFree) {
            TextButton(onClick = onPrivacyOptions, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                AppIcon(AppIconGlyph.ShieldCheckmarkOutline, null, colors.accent, size = 18.dp)
                Text(
                    text = "Ad privacy choices",
                    modifier = Modifier.padding(start = 7.dp),
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(benefit: AdFreeBenefit) {
    val colors = LocalHopeColors.current
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 66.dp).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = colors.accentSoft,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppIcon(benefit.icon, null, colors.accent, size = 20.dp)
            }
        }
        Text(
            text = benefit.title,
            color = colors.text,
            fontFamily = Poppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PurchaseAction(
    billing: BillingState,
    onPurchase: () -> Unit,
) {
    val colors = LocalHopeColors.current

    when {
        billing.loading -> Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp),
            color = colors.surface,
            shape = RoundedCornerShape(29.dp),
            border = BorderStroke(1.dp, colors.accentLine),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = colors.accent,
                    strokeWidth = 2.dp,
                )
                Text(
                    text = "Checking Google Play…",
                    color = colors.text,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        }

        billing.isAdFree -> Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.accentSoft,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, colors.accentLine),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppIcon(AppIconGlyph.CheckmarkCircleOutline, null, colors.accent, size = 28.dp)
                Text(
                    text = "You’re enjoying Hope Cards ad-free",
                    color = colors.text,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onPurchase,
                modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).testTag("remove_ads_purchase_button"),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonBackground,
                    contentColor = colors.buttonText,
                ),
            ) {
                Text(
                    text = billing.price?.let { "Remove ads forever  ·  $it" } ?: "Remove ads forever",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                text = if (billing.canPurchase) {
                    "One-time purchase · No subscription"
                } else {
                    "Google Play will show your local one-time price"
                },
                color = colors.textTertiary,
                fontFamily = Poppins,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
