package com.example.walletapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
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
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                WalletThemePreferences.setMode(
                                    context,
                                    "light"
                                )
                            }
                        ) {
                            Text("☀️ روشن")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                WalletThemePreferences.setMode(
                                    context,
                                    "light"
                                )
                            }
                        ) {
                            Text("☀️ روشن")
                        }
                    }

                    if (mode == "dark") {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                WalletThemePreferences.setMode(
                                    context,
                                    "dark"
                                )
                            }
                        ) {
                            Text("🌙 تاریک")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                WalletThemePreferences.setMode(
                                    context,
                                    "dark"
                                )
                            }
                        ) {
                            Text("🌙 تاریک")
                        }
                    }

                    if (mode == "system") {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                WalletThemePreferences.setMode(
                                    context,
                                    "system"
                                )
                            }
                        ) {
                            Text("📱 خودکار")
                        }
                    } else {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                WalletThemePreferences.setMode(
                                    context,
                                    "system"
                                )
                            }
                        ) {
                            Text("📱 خودکار")
                        }
                    }
                }
            }

            item {
                PreviewCard(refresh)
            }

            item {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    onClick = {
                        WalletThemePreferences.resetColors(
                            context
                        )
                        refresh++
                    }
                ) {
                    Text("بازگردانی رنگ‌ها")
                }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
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

            TextField(
                value = hexText,
                onValueChange = {
                    hexText = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("کد رنگ HEX")
                },
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

        Card(
            colors = CardDefaults.cardColors(
                containerColor = card
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
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

                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = button
                    )
                ) {
                    Text("ثبت تراکنش")
                }

                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    "رنگ اصلی: ${primary.toHex()}",
                    color = secondaryText
                )
            }
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
