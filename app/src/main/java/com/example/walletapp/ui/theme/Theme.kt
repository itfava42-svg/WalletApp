package com.example.walletapp.ui.theme

import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private const val PREFS_NAME = "wallet_theme_preferences"

object WalletThemePreferences {

    private var initialized = false

    var selectedMode by mutableStateOf("system")
        private set

    private val colorMap = mutableStateMapOf<String, Color>()

    private val defaults = mapOf(
        "pageBackground" to Color(0xFFF5F6FA),
        "cardBackground" to Color(0xFFFFFFFF),
        "primary" to Color(0xFF2563EB),
        "accent" to Color(0xFF3B82F6),
        "primaryText" to Color(0xFF0F172A),
        "secondaryText" to Color(0xFF64748B),
        "button" to Color(0xFF2563EB),
        "border" to Color(0xFFE2E8F0),
    )

    fun initialize(context: Context) {
        if (initialized) return

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        selectedMode =
            prefs.getString("selected_mode", "system") ?: "system"

        defaults.forEach { (key, defaultColor) ->
            val saved = prefs.getString(key, null)

            colorMap[key] =
                if (saved != null) {
                    parseColor(saved) ?: defaultColor
                } else {
                    defaultColor
                }
        }

        initialized = true
    }

    fun getColor(key: String): Color {
        return colorMap[key] ?: defaults[key] ?: Color.White
    }

    fun setColor(
        context: Context,
        key: String,
        color: Color,
    ) {
        colorMap[key] = color

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(key, color.toHex())
            .apply()
    }

    fun setMode(
        context: Context,
        mode: String,
    ) {
        selectedMode = mode

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString("selected_mode", mode)
            .apply()
    }

    fun resetColors(context: Context) {
        defaults.forEach { (key, color) ->
            colorMap[key] = color
        }

        val editor = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()

        defaults.forEach { (key, color) ->
            editor.putString(key, color.toHex())
        }

        editor.apply()
    }

    private fun parseColor(value: String): Color? {
        return try {
            val hex = value.removePrefix("#")
            val argb =
                when (hex.length) {
                    6 -> "FF$hex"
                    8 -> hex
                    else -> return null
                }

            Color(
                android.graphics.Color.parseColor("#$argb")
            )
        } catch (_: Exception) {
            null
        }
    }
}

private fun Color.toHex(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)

    return "#%02X%02X%02X".format(r, g, b)
}

private fun createColorScheme(
    dark: Boolean,
): ColorScheme {

    val pageBackground =
        WalletThemePreferences.getColor("pageBackground")

    val cardBackground =
        WalletThemePreferences.getColor("cardBackground")

    val primary =
        WalletThemePreferences.getColor("primary")

    val accent =
        WalletThemePreferences.getColor("accent")

    val primaryText =
        WalletThemePreferences.getColor("primaryText")

    val secondaryText =
        WalletThemePreferences.getColor("secondaryText")

    val button =
        WalletThemePreferences.getColor("button")

    val border =
        WalletThemePreferences.getColor("border")

    return if (dark) {

        darkColorScheme(
            primary = button,
            onPrimary = Color.White,

            primaryContainer = primary,
            onPrimaryContainer = Color.White,

            secondary = accent,
            onSecondary = Color.White,

            secondaryContainer = accent.copy(alpha = 0.25f),
            onSecondaryContainer = Color.White,

            tertiary = accent,
            onTertiary = Color.White,

            background = pageBackground,
            onBackground = primaryText,

            surface = cardBackground,
            onSurface = primaryText,

            surfaceVariant = cardBackground,
            onSurfaceVariant = secondaryText,

            outline = border,
            outlineVariant = border,

            error = Color(0xFFE53935),
            onError = Color.White,
        )

    } else {

        lightColorScheme(
            primary = button,
            onPrimary = Color.White,

            primaryContainer = primary.copy(alpha = 0.14f),
            onPrimaryContainer = primary,

            secondary = accent,
            onSecondary = Color.White,

            secondaryContainer = accent.copy(alpha = 0.14f),
            onSecondaryContainer = primary,

            tertiary = accent,
            onTertiary = Color.White,

            background = pageBackground,
            onBackground = primaryText,

            surface = cardBackground,
            onSurface = primaryText,

            surfaceVariant = cardBackground,
            onSurfaceVariant = secondaryText,

            outline = border,
            outlineVariant = border,

            error = Color(0xFFD32F2F),
            onError = Color.White,
        )
    }
}

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp),
)

@Composable
fun WalletAppTheme(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    WalletThemePreferences.initialize(context)

    val darkTheme =
        when (WalletThemePreferences.selectedMode) {
            "dark" -> true
            "light" -> false
            else -> androidx.compose.foundation.isSystemInDarkTheme()
        }

    val colorScheme =
        createColorScheme(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
