package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppTopBar
import com.example.walletapp.ui.components.AppIconButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppIconBadge
import com.example.walletapp.ui.models.SettingsMenu
import com.example.walletapp.ui.models.ToolAction
import com.example.walletapp.ui.models.ToolItem
import com.example.walletapp.ui.models.ToolSection
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles
import com.example.walletapp.ui.theme.OnSurface
import com.example.walletapp.ui.theme.OnSurfaceVariant
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsListScreen(
    onBack: () -> Unit,
    onOpenSmsLearning: () -> Unit,
    onOpenThemeBuilder: () -> Unit,
    onOpenTransactionSync: () -> Unit,
    onOpenBankList: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMessage(text: String) {
        scope.launch { snackbarHostState.showSnackbar(text) }
    }

    fun handleClick(item: ToolItem) {
        when (item.action) {
            ToolAction.SMS_LEARNING -> onOpenSmsLearning()
            ToolAction.THEME_BUILDER -> onOpenThemeBuilder()
            ToolAction.TRANSACTION_SYNC -> onOpenTransactionSync()
            ToolAction.BANK_LIST -> onOpenBankList()
            ToolAction.SHARE -> {
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    putExtra(
                        android.content.Intent.EXTRA_TEXT,
                        "کیف پول — بهترین برنامه مدیریت مالی"
                    )
                    type = "text/plain"
                }
                context.startActivity(
                    android.content.Intent.createChooser(
                        shareIntent,
                        "معرفی به دوستان"
                    )
                )
            }
            else -> showMessage("${item.title} — به‌زودی")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(centerAligned = true,
                title = {
                    Text(
                        "تنظیمات",
                        style = AppTextStyles.titleLg,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    AppIconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppDimens.PagePadding),
            contentPadding = PaddingValues(vertical = AppDimens.PagePadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
        ) {
            SettingsMenu.sections.forEach { section ->
                item { SettingsSectionHeader(section) }
                items(section.items) { toolItem ->
                    SettingsRow(toolItem, onClick = { handleClick(toolItem) })
                }
                item { Spacer(Modifier.height(AppDimens.CardGap)) }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(section: ToolSection) {
    Text(
        section.title,
        style = AppTextStyles.labelMd,
        color = OnSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = AppDimens.ItemGap, bottom = AppDimens.TightGap),
    )
}

@Composable
private fun SettingsRow(
    item: ToolItem,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconBadge(
                icon = item.icon,
                size = AppDimens.IconBox,
                iconSize = AppDimens.Icon,
            )

            Spacer(Modifier.width(AppDimens.CardGap))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontWeight = FontWeight.SemiBold,
                    style = AppTextStyles.bodyLg,
                    color = OnSurface,
                )
                Text(
                    item.description,
                    style = AppTextStyles.labelMd,
                    color = OnSurfaceVariant,
                )
            }
        }
    }
}
