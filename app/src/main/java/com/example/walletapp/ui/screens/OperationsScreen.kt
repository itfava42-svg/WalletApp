package com.example.walletapp.ui.screens

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.example.walletapp.ui.components.AppCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
            TopAppBar(
                title = {
                    Text(
                        "عملیات",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "عملیات مالی",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "مدیریت عملیات و خدمات مالی برنامه",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(operations) { operation ->
                    AppCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
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
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            androidx.compose.material3.Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Icon(
                                    imageVector = operation.icon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(28.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Column {
                                Text(
                                    text = operation.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

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
