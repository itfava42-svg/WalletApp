package com.example.walletapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// NOTE: the UI text is Persian (Vazirmatn in the web mock). To use the real
// Vazirmatn font, download the variable font from Google Fonts, drop the
// .ttf files into app/src/main/res/font/, and replace FontFamily.Default
// below with FontFamily(Font(R.font.vazirmatn_regular), Font(R.font.vazirmatn_bold, FontWeight.Bold)).
val AppFontFamily = FontFamily.Default

// Custom app-specific text styles matching DESIGN.md tokens.
// (Material3's default Typography type names are reused where they line up;
// these extra ones are exposed via AppTypography below for bespoke sizes.)
object AppTextStyles {
    val displayLg = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 48.sp)
    val headlineMd = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 36.sp)
    val headlineSm = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 30.sp)
    val bodyLg = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val bodyMd = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp)
    val labelMd = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp)
    val numeralXl = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp)
}

val AppTypography = Typography(
    displayLarge = AppTextStyles.displayLg,
    headlineMedium = AppTextStyles.headlineMd,
    headlineSmall = AppTextStyles.headlineSm,
    bodyLarge = AppTextStyles.bodyLg,
    bodyMedium = AppTextStyles.bodyMd,
    labelMedium = AppTextStyles.labelMd,
)