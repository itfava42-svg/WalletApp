package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppTopBar

import com.example.walletapp.ui.components.AppIconButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppIconBadge
import com.example.walletapp.ui.theme.AppDimens
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private data class OperationItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OperationsScreen(
    onBack: () -> Unit,
    onOpenCheckManagement: () -> Unit,
    onOpenLoanManagement: () -> Unit,
    onOpenTransfer: () -> Unit,
) {
    val operations = listOf(
        OperationItem(
            title = "انتقال بین حساب‌ها",
            description = "انتقال وجه بین حساب‌های خود",
            icon = Icons.Filled.AccountBalance,
        ),
        OperationItem(
            title = "طلب و بدهی",
            description = "مدیریت طلب‌ها و بدهی‌های خود را به راحتی",
            icon = Icons.Filled.AttachMoney,
        ),
        OperationItem(
            title = "دُنگ",
            description = "تقسیم هزینه‌های گروهی و محاسبه سهم هر نفر",
            icon = Icons.Filled.Groups,
        ),
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(centerAligned = false, 
                title = {
                    Text(
                        "عملیات",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    AppIconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppDimens.PagePadding),
        ) {
            Spacer(modifier = Modifier.height(AppDimens.CardGap))

            Text(
                text = "عملیات مالی",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(AppDimens.IntroGap))

            Text(
                text = "مدیریت عملیات و خدمات مالی برنامه",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(AppDimens.SectionGap))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = AppDimens.SectionGap),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.CardGap),
                verticalArrangement = Arrangement.spacedBy(AppDimens.CardGap),
            ) {
                items(operations) { operation ->
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(AppDimens.OperationCardHeight),
                        onClick = {
                            when (operation.title) {
                                "انتقال بین حساب‌ها" -> onOpenTransfer()
                                "طلب و بدهی" -> {
                                    // Existing debt/credit action will be wired here.
                                }
                                "دُنگ" -> {
                                    // Existing bill-split action will be wired here.
                                }
                            }
                        },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(AppDimens.CardPadding),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            AppIconBadge(icon = operation.icon, size = AppDimens.FeatureIconBox, iconSize = AppDimens.FeatureIcon)

                            Column {
                                Text(
                                    text = operation.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                Spacer(modifier = Modifier.height(AppDimens.TightGap))

                                Text(
                                    text = operation.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
