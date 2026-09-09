package com.example.walletapp.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
    val operations = listOf(        OperationItem(
            title = "انتقال بین حساب‌ها",
            description = "انتقال وجه بین حساب‌های خودتان",
            icon = Icons.Filled.AccountBalance,
        ),
        OperationItem(
            title = "ط·ظ„ط¨ ظˆ ط¨ط¯ظ‡غŒ",
            description = "ظ…ط¯غŒط±غŒطھ ط·ظ„ط¨â€Œظ‡ط§ ظˆ ط¨ط¯ظ‡غŒâ€Œظ‡ط§غŒ ط®ظˆط¯ ط±ط§ ط¨ظ‡ ط±ط§ط­طھغŒ",
            icon = Icons.Filled.AttachMoney,
        ),
        OperationItem(
            title = "ط¯ظڈظ†ع¯",
            description = "طھظ‚ط³غŒظ… ظ‡ط²غŒظ†ظ‡â€Œظ‡ط§غŒ ع¯ط±ظˆظ‡غŒ ظˆ ظ…ط­ط§ط³ط¨ظ‡ ط³ظ‡ظ… ظ‡ط± ظ†ظپط±",
            icon = Icons.Filled.Groups,
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("ط¹ظ…ظ„غŒط§طھ")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "ط¨ط§ط²ع¯ط´طھ",
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
                text = "ط¹ظ…ظ„غŒط§طھ ظ…ط§ظ„غŒ",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ظ…ط¯غŒط±غŒطھ ط¹ظ…ظ„غŒط§طھ ظˆ ط®ط¯ظ…ط§طھ ظ…ط§ظ„غŒ ط¨ط±ظ†ط§ظ…ظ‡",
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

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable {
                                when (operation.title) {                                    "انتقال بین حساب‌ها" -> {
                                        onOpenTransfer()
                                    }

                                    "ط·ظ„ط¨ ظˆ ط¨ط¯ظ‡غŒ" -> {
                                        // Existing debt/credit action will be wired here.
                                    }
                                    "ط¯ظڈظ†ع¯" -> {
                                        // Existing bill-split action will be wired here.
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {

                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Icon(
                                    imageVector = operation.icon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(28.dp),
                                    tint =
                                        MaterialTheme.colorScheme.onPrimaryContainer,
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
                                    color =
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
