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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private const val PREFS_NAME = "wallet_theme_preferences"
private const val KEY_THEME = "selected_theme"
private const val KEY_MODE = "selected_mode"

object WalletThemePreferences {

    private var initialized = false

    var selectedTheme by mutableStateOf("default")
        private set

    var selectedMode by mutableStateOf("system")
        private set

    fun initialize(context: Context) {
        if (initialized) return

        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        selectedTheme = prefs.getString(KEY_THEME, "default") ?: "default"
        selectedMode = prefs.getString(KEY_MODE, "system") ?: "system"

        initialized = true
    }

    fun setTheme(context: Context, theme: String) {
        selectedTheme = theme

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(KEY_THEME, theme)
            .apply()
    }

    fun setMode(context: Context, mode: String) {
        selectedMode = mode

        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(KEY_MODE, mode)
            .apply()
    }
}

private val AppColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    error = AppError,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    background = Background,
    onBackground = OnBackground,

    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    outline = Outline,
    outlineVariant = OutlineVariant,

    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,

    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
)

private fun themeColors(
    theme: String,
    dark: Boolean,
): ColorScheme {

    val base = if (dark) {
        darkColorScheme(
            error = AppError,
            onError = OnError,
            errorContainer = ErrorContainer,
            onErrorContainer = OnErrorContainer,
        )
    } else {
        AppColorScheme
    }

    val colors = when (theme) {

        "blue" -> Triple(
            Color(0xFF1565C0),
            Color(0xFF42A5F5),
            Color(0xFF0D47A1)
        )

        "purple" -> Triple(
            Color(0xFF6A1B9A),
            Color(0xFFAB47BC),
            Color(0xFF4A148C)
        )

        "orange" -> Triple(
            Color(0xFFEF6C00),
            Color(0xFFFFA726),
            Color(0xFFE65100)
        )

        else -> Triple(
            Color(0xFF2E7D32),
            Color(0xFF66BB6A),
            Color(0xFF1B5E20)
        )
    }

    val primary = colors.first
    val secondary = colors.second
    val tertiary = colors.third

    return if (dark) {
        base.copy(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            primaryContainer = tertiary,
            secondaryContainer = primary.copy(alpha = 0.55f),
            tertiaryContainer = secondary.copy(alpha = 0.45f),
        )
    } else {
        base.copy(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            primaryContainer = primary.copy(alpha = 0.14f),
            onPrimaryContainer = tertiary,
            secondaryContainer = secondary.copy(alpha = 0.14f),
            onSecondaryContainer = tertiary,
            tertiaryContainer = tertiary.copy(alpha = 0.12f),
            onTertiaryContainer = tertiary,
        )
    }
}

// Corner radii from DESIGN.md ("rounded" tokens)
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun WalletAppTheme(content: @Composable () -> Unit) {

    val context = LocalContext.current

    WalletThemePreferences.initialize(context)

    val darkTheme = when (WalletThemePreferences.selectedMode) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    val colorScheme = themeColors(
        theme = WalletThemePreferences.selectedTheme,
        dark = darkTheme,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
