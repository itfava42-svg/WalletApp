package com.example.walletapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppOutlinedButton
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.ui.components.AppTextField
import com.example.walletapp.ui.theme.WalletThemePreferences

private data class ColorSetting(
    val key: String,
    val title: String,
    val description: String,
)

private val colorSettings = listOf(
    ColorSetting(
        "pageBackground",
        "پس‌زمینه صفحات",
        "رنگ پس‌زمینه تمام صفحات برنامه"
    ),
    ColorSetting(
        "cardBackground",
        "کاشی‌ها و کارت‌ها",
        "رنگ کارت‌ها، کاشی‌ها و پنل‌ها"
    ),
    ColorSetting(
        "primary",
        "رنگ اصلی",
        "رنگ اصلی رابط کاربری"
    ),
    ColorSetting(
        "accent",
        "رنگ تأکیدی",
        "رنگ عناصر ثانویه و تأکیدی"
    ),
    ColorSetting(
        "primaryText",
        "متن اصلی",
        "رنگ عنوان‌ها و نوشته‌های اصلی"
    ),
    ColorSetting(
        "secondaryText",
        "متن فرعی",
        "رنگ توضیحات و نوشته‌های فرعی"
    ),
    ColorSetting(
        "button",
        "دکمه‌ها",
        "رنگ دکمه‌های اصلی"
    ),
    ColorSetting(
        "border",
        "حاشیه‌ها",
        "رنگ خطوط و حاشیه‌ها"
    ),
)

private val colorPalette = listOf(
    Color(0xFF2E7D32),
    Color(0xFF1565C0),
    Color(0xFF6A1B9A),
    Color(0xFFEF6C00),
    Color(0xFFC62828),
    Color(0xFF00838F),
    Color(0xFF5D4037),
    Color(0xFF37474F),
    Color(0xFF000000),
    Color(0xFFFFFFFF),
    Color(0xFFF8F9FA),
    Color(0xFFE0E0E0),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBuilderScreen(
    onBack: () -> Unit,
) {
    val context =
        androidx.compose.ui.platform.LocalContext.current

    WalletThemePreferences.initialize(context)

    var refresh by remember {
        mutableStateOf(0)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "🎨 تم‌ساز",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            item {
                AppCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentPadding = PaddingValues(16.dp),
                ) {
                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Palette,
                                contentDescription = null
                            )

                            Spacer(
                                Modifier.size(8.dp)
                            )

                            Text(
                                "شخصی‌سازی کامل ظاهر",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(
                            Modifier.height(8.dp)
                        )

                        Text(
                            "رنگ هر بخش را جداگانه انتخاب کنید. تغییرات ذخیره می‌شوند."
                        )
                    }
                }

            item {
                Text(
                    "🎨 رنگ‌های برنامه",
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(colorSettings.size) { index ->

                val setting = colorSettings[index]

                ColorSettingCard(
                    setting = setting,
                    refresh = refresh,
                    onChanged = {
                        refresh++
                    }
                )
            }

            item {
                Text(
                    "🌓 حالت نمایش",
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                val mode =
                    WalletThemePreferences.selectedMode

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    if (mode == "light") {
                        AppPrimaryButton(
                            modifier = Modifier.weight(1f),
                            text = "☀️ روشن",
                            onClick = {
                                WalletThemePreferences.setMode(context, "light")
                            }
                        )
                    } else {
                        AppOutlinedButton(
                            modifier = Modifier.weight(1f),
                            text = "☀️ روشن",
                            onClick = {
                                WalletThemePreferences.setMode(context, "light")
                            }
                        )
                    }

                    if (mode == "dark") {
                        AppPrimaryButton(
                            modifier = Modifier.weight(1f),
                            text = "🌙 تاریک",
                            onClick = {
                                WalletThemePreferences.setMode(context, "dark")
                            }
                        )
                    } else {
                        AppOutlinedButton(
                            modifier = Modifier.weight(1f),
                            text = "🌙 تاریک",
                            onClick = {
                                WalletThemePreferences.setMode(context, "dark")
                            }
                        )
                    }

                    if (mode == "system") {
                        AppPrimaryButton(
                            modifier = Modifier.weight(1f),
                            text = "📱 خودکار",
                            onClick = {
                                WalletThemePreferences.setMode(context, "system")
                            }
                        )
                    } else {
                        AppOutlinedButton(
                            modifier = Modifier.weight(1f),
                            text = "📱 خودکار",
                            onClick = {
                                WalletThemePreferences.setMode(context, "system")
                            }
                        )
                    }
                }
            }

            item {
                PreviewCard(refresh)
            }

            item {
                AppOutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    text = "بازگردانی رنگ‌ها",
                    onClick = {
                        WalletThemePreferences.resetColors(context)
                        refresh++
                    }
                )
            }

            item {
                Spacer(
                    Modifier.height(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorSettingCard(
    setting: ColorSetting,
    refresh: Int,
    onChanged: () -> Unit,
) {
    val context =
        androidx.compose.ui.platform.LocalContext.current

    val currentColor =
        WalletThemePreferences.getColor(setting.key)

    var hexText by remember(
        setting.key,
        currentColor,
        refresh
    ) {
        mutableStateOf(
            currentColor.toHex()
        )
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentPadding = PaddingValues(14.dp),
    ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            currentColor,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(12.dp)
                        )
                )

                Spacer(
                    Modifier.size(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        setting.title,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        setting.description,
                        style =
                            MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(
                Modifier.height(10.dp)
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {

                colorPalette.forEach { color ->

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {

                                WalletThemePreferences.setColor(
                                    context,
                                    setting.key,
                                    color
                                )

                                onChanged()
                            }
                    )
                }
            }

            Spacer(
                Modifier.height(10.dp)
            )

            AppTextField(
                value = hexText,
                onValueChange = {
                    hexText = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = "کد رنگ HEX",
                placeholder = {
                    Text("#2E7D32")
                },
                trailingIcon = {

                    Text(
                        "اعمال",
                        modifier = Modifier
                            .clickable {

                                parseHexColor(
                                    hexText
                                )?.let { color ->

                                    WalletThemePreferences.setColor(
                                        context,
                                        setting.key,
                                        color
                                    )

                                    onChanged()
                                }
                            }
                            .padding(12.dp),
                        color =
                            MaterialTheme.colorScheme.primary,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            )
        }
    }

@Composable
private fun PreviewCard(
    refresh: Int,
) {
    val page =
        WalletThemePreferences.getColor(
            "pageBackground"
        )

    val card =
        WalletThemePreferences.getColor(
            "cardBackground"
        )

    val primary =
        WalletThemePreferences.getColor(
            "primary"
        )

    val accent =
        WalletThemePreferences.getColor(
            "accent"
        )

    val text =
        WalletThemePreferences.getColor(
            "primaryText"
        )

    val secondaryText =
        WalletThemePreferences.getColor(
            "secondaryText"
        )

    val button =
        WalletThemePreferences.getColor(
            "button"
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                page,
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp)
    ) {

        Text(
            "👁 پیش‌نمایش",
            fontWeight = FontWeight.Bold,
            color = text
        )

        Spacer(
            Modifier.height(10.dp)
        )

        AppCard(
            containerColor = card,
            contentPadding = PaddingValues(14.dp),
        ) {

                Text(
                    "موجودی کل",
                    color = secondaryText
                )

                Text(
                    "۲۸۵,۴۰۰,۰۰۰ تومان",
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = text
                )

                Spacer(
                    Modifier.height(10.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            accent,
                            RoundedCornerShape(8.dp)
                        )
                )

                Spacer(
                    Modifier.height(10.dp)
                )


                Text(
                    "رنگ اصلی: ${primary.toHex()}",
                    color = secondaryText
                )
            }
        }
}

private fun parseHexColor(
    value: String
): Color? {
    return try {

        val hex =
            value.trim().removePrefix("#")

        val argb =
            when (hex.length) {
                6 -> "FF$hex"
                8 -> hex
                else -> return null
            }

        Color(
            android.graphics.Color.parseColor(
                "#$argb"
            )
        )

    } catch (_: Exception) {
        null
    }
}

private fun Color.toHex(): String {
    val r =
        (red * 255)
            .toInt()
            .coerceIn(0, 255)

    val g =
        (green * 255)
            .toInt()
            .coerceIn(0, 255)

    val b =
        (blue * 255)
            .toInt()
            .coerceIn(0, 255)

    return "#%02X%02X%02X".format(r, g, b)
}

