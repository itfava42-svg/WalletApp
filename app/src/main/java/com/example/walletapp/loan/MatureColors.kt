package com.example.walletapp.loan

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


object MatureColors {

    val Primary = Color(0xFF2C3E50)

    val PrimaryLight = Color(0xFF5D7B93)

    val PrimaryDark = Color(0xFF1A2634)


    val Secondary = Color(0xFFE8A87C)

    val SecondaryLight = Color(0xFFF5D6B8)

    val SecondaryDark = Color(0xFFC98B5F)


    val Surface = Color(0xFFF8F6F3)

    val CardBackground = Color.White


    val TextPrimary = Color(0xFF1A1A1A)

    val TextSecondary = Color(0xFF5A5A5A)

    val TextHint = Color(0xFF9A9A9A)


    val Success = Color(0xFF6B9F8A)


    val PrimaryGradient =
        Brush.horizontalGradient(
            colors = listOf(
                Primary,
                PrimaryLight
            )
        )
}
