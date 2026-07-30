package com.denxhinjo.fabinventory.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.denxhinjo.fabinventory.R

// Same two-typeface pairing as the site: Space Grotesk for headings/display
// (--font-display), Inter for body/UI text (--font-sans). Both are bundled as
// their single variable-font files, with each weight declared as a distinct
// FontVariation.Settings instance rather than separate static font files.
@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val SpaceGroteskFontFamily = FontFamily(
    variableFont(R.font.space_grotesk_variable, FontWeight.Normal),
    variableFont(R.font.space_grotesk_variable, FontWeight.Medium),
    variableFont(R.font.space_grotesk_variable, FontWeight.SemiBold),
    variableFont(R.font.space_grotesk_variable, FontWeight.Bold),
)

val InterFontFamily = FontFamily(
    variableFont(R.font.inter_variable, FontWeight.Normal),
    variableFont(R.font.inter_variable, FontWeight.Medium),
    variableFont(R.font.inter_variable, FontWeight.SemiBold),
    variableFont(R.font.inter_variable, FontWeight.Bold),
)

val FabInventoryTypography = Typography().let { base ->
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold),
        displayMedium = base.displayMedium.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold),
        displaySmall = base.displaySmall.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold),
        headlineMedium = base.headlineMedium.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.copy(fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = InterFontFamily, fontSize = 16.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = InterFontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = InterFontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.copy(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium),
        labelSmall = base.labelSmall.copy(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium),
    )
}
