package com.example.walletapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletapp.ui.models.SettingsMenu
import com.example.walletapp.ui.models.ToolAction
import com.example.walletapp.ui.models.ToolItem
import com.example.walletapp.ui.models.ToolSection
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "تنظیمات",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SettingsMenu.sections.forEach { section ->
                item { SettingsSectionHeader(section) }
                items(section.items) { toolItem ->
                    SettingsRow(toolItem, onClick = { handleClick(toolItem) })
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(section: ToolSection) {
    Text(
        section.title,
        style = MaterialTheme.typography.labelLarge,
        color = OnSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsRow(
    item: ToolItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurface,
                )
                Text(
                    item.description,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                )
            }
        }
    }
}
