package com.example.walletapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.walletapp.R

/**
 * Application font family.
 *
 * Vazirmatn is used consistently across the application so Persian text
 * remains readable and visually consistent, including SMS completion UI.
 */
val AppFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

// Custom app-specific text styles matching DESIGN.md tokens.
object AppTextStyles {
    val displayLg = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 48.sp
    )

    val headlineMd = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 36.sp
    )

    val headlineSm = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 30.sp
    )

    val bodyLg = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )

    val bodyMd = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    )

    val labelMd = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp
    )

    val numeralXl = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    )
}

val AppTypography = Typography(
    displayLarge = AppTextStyles.displayLg,
    headlineMedium = AppTextStyles.headlineMd,
    headlineSmall = AppTextStyles.headlineSm,
    bodyLarge = AppTextStyles.bodyLg,
    bodyMedium = AppTextStyles.bodyMd,
    labelMedium = AppTextStyles.labelMd,
)
