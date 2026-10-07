package com.example.walletapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.walletapp.ui.models.ToolAction
import com.example.walletapp.ui.models.ToolItem
import com.example.walletapp.ui.models.toolsGrid
import com.example.walletapp.ui.models.toolsTopRow
import com.example.walletapp.ui.components.DashboardBottomBar
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppIconBadge
import com.example.walletapp.ui.components.appNavItems
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.OnSurface
import com.example.walletapp.ui.theme.OnSurfaceVariant
import com.example.walletapp.ui.theme.AppTextStyles
import com.example.walletapp.ui.theme.Primary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    selectedNavIndex: Int,
    onNavSelect: (Int) -> Unit,
    onOpenSettingsList: () -> Unit,
    onOpenReminder: () -> Unit,
    onOpenLoanManagement: () -> Unit,
    onOpenCheckManagement: () -> Unit,
    onOpenOperations: () -> Unit,
    onOpenReports: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMessage(text: String) {
        scope.launch {
            snackbarHostState.showSnackbar(text)
        }
    }

    fun handleClick(action: ToolAction, title: String) {
        when (action) {
            ToolAction.OPEN_SETTINGS_LIST -> onOpenSettingsList()
            ToolAction.REMINDER -> onOpenReminder()
            ToolAction.LOAN_MANAGEMENT -> onOpenLoanManagement()
            ToolAction.CHECK_MANAGEMENT -> onOpenCheckManagement()
            ToolAction.OPERATIONS -> onOpenOperations()
            ToolAction.REPORTS -> onOpenReports()
            ToolAction.PROFILE -> showMessage("صفحه پروفایل هنوز فعال نشده است.")
            ToolAction.PERSONALIZATION -> showMessage("شخصی‌سازی هنوز فعال نشده است.")
            ToolAction.SAVINGS -> showMessage("بخش پس‌انداز هنوز فعال نشده است.")
            ToolAction.PRICES -> showMessage("بخش قیمت‌ها هنوز فعال نشده است.")
            ToolAction.FINANCIAL_CALENDAR -> showMessage("تقویم مالی هنوز فعال نشده است.")
            else -> showMessage(title)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            DashboardBottomBar(
                items = appNavItems,
                selectedIndex = selectedNavIndex,
                onSelect = onNavSelect
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppDimens.PagePadding),
        ) {
            Spacer(Modifier.height(AppDimens.ItemGap))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                toolsTopRow.forEach { item ->
                    TopIconButton(
                        item = item,
                        onClick = {
                            handleClick(item.action, item.title)
                        },
                    )
                }
            }

            Spacer(Modifier.height(AppDimens.SectionGap))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ابزارها",
                        style = AppTextStyles.headlineSm
                    )

                    Spacer(Modifier.width(AppDimens.IntroGap))

                    Icon(
                        Icons.Filled.Apps,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(AppDimens.IconSmall)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Primary),
                )
            }

            Spacer(Modifier.height(AppDimens.ItemGap))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.CardGap),
                verticalArrangement = Arrangement.spacedBy(AppDimens.CardGap),
                contentPadding = PaddingValues(bottom = AppDimens.SectionGap),
                modifier = Modifier.fillMaxSize(),
            ) {
                toolsGrid.forEach { item ->
                    val fullWidth = item.action == ToolAction.PRICES

                    item(
                        span = {
                            GridItemSpan(if (fullWidth) maxLineSpan else 1)
                        },
                    ) {
                        ToolGridCard(
                            item = item,
                            onClick = {
                                if (item.action == ToolAction.REPORTS) {
                                    onOpenReports()
                                } else {
                                    handleClick(item.action, item.title)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopIconButton(
    item: ToolItem,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIconBadge(icon = item.icon, contentDescription = item.title, onClick = onClick, size = 52.dp, iconSize = 24.dp)

        Spacer(Modifier.height(AppDimens.IntroGap))

        Text(
            item.title,
            style = AppTextStyles.titleSm,
            color = OnSurface
        )
    }
}

@Composable
private fun ToolGridCard(
    item: ToolItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp),
        contentPadding = PaddingValues(AppDimens.CardPadding),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppDimens.CardPadding)
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(AppDimens.IconLarge),
            )

            Column(
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    item.title,
                    style = AppTextStyles.titleSm,
                    color = OnSurface,
                )

                Spacer(Modifier.height(AppDimens.TightGap))

                Text(
                    item.description,
                    style = AppTextStyles.labelMd,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}
