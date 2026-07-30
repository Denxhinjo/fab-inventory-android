package com.denxhinjo.fabinventory.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FabInventoryTypography = Typography(
    titleLarge = Typography().titleLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold),
    titleMedium = Typography().titleMedium.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold),
    bodyLarge = Typography().bodyLarge.copy(fontFamily = FontFamily.Default, fontSize = 16.sp),
    bodyMedium = Typography().bodyMedium.copy(fontFamily = FontFamily.Default),
    labelLarge = Typography().labelLarge.copy(fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium),
)
