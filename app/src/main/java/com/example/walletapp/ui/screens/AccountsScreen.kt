package com.example.walletapp.ui.screens

import com.example.walletapp.data.toPersianCurrency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import android.widget.Toast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.Account
import com.example.walletapp.data.AccountManager
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.AccountType
import com.example.walletapp.data.AccountLedger
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.ui.components.DashboardBottomBar
import com.example.walletapp.ui.components.appNavItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    selectedNavIndex: Int = 1,
    onNavSelect: (Int) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        AccountStore.attachContext(context)
        TransactionStore.attachContext(context)
    }

    val accounts = AccountStore.accounts

    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var accountPendingDelete by remember { mutableStateOf<Account?>(null) }

    val totalBalance = AccountLedger.totalBalance(
        accounts = accounts,
        transactions = TransactionStore.transactions,
    )

    val activeAccounts = accounts.count { it.isActive }
    val defaultAccount = accounts.firstOrNull { it.isDefault }

    Scaffold(
        topBar = {
    CenterAlignedTopAppBar(
        title = { Text("حساب‌ها", fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن حساب", tint = MaterialTheme.colorScheme.primary)
            }
        },
    )
},

bottomBar = {
            DashboardBottomBar(
                items = appNavItems,
                selectedIndex = selectedNavIndex,
                onSelect = onNavSelect,
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                },
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "افزودن حساب",
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),

            contentPadding = PaddingValues(
                top = 14.dp,
                bottom = 100.dp,
            ),

            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF005663),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {

                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = "موجودی کل",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                )

                                Spacer(
                                    Modifier.height(7.dp)
                                )

                                Text(
                                    text = formatAmount(totalBalance),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                )

                                Spacer(
                                    Modifier.height(2.dp)
                                )

                                Text(
                                    text = "ریال",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(
                                            alpha = 0.55f
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }

                        Spacer(
                            Modifier.height(18.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {

                            AccountSummaryStat(
                                modifier = Modifier.weight(1f),
                                value = accounts.size.toString(),
                                label = "کل حساب‌ها",
                            )

                            AccountSummaryStat(
                                modifier = Modifier.weight(1f),
                                value = activeAccounts.toString(),
                                label = "حساب فعال",
                            )

                            AccountSummaryStat(
                                modifier = Modifier.weight(1f),
                                value = if (defaultAccount != null) "✓" else "—",
                                label = "پیش‌فرض",
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "حساب‌های من",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )

                        Spacer(
                            Modifier.height(2.dp)
                        )

                        Text(
                            text = "حساب‌های بانکی و مالی شما",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = "${accounts.size} حساب",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (accounts.isEmpty()) {

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {

                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(22.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }

                            Spacer(
                                Modifier.height(16.dp)
                            )

                            Text(
                                text = "هنوز حسابی ثبت نشده است",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            Spacer(
                                Modifier.height(6.dp)
                            )

                            Text(
                                text = "اولین حساب مالی خود را اضافه کنید.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Spacer(
                                Modifier.height(18.dp)
                            )

                            Button(
                                onClick = {
                                    showAddDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                )

                                Spacer(
                                    Modifier.width(7.dp)
                                )

                                Text("افزودن حساب")
                            }
                        }
                    }
                }

            } else {

                items(
                    items = accounts,
                    key = { it.id },
                ) { account ->

                    AccountCard(
                        account = account,
                        onEdit = {
                            editingAccount = account
                        },
                        onDelete = {
                            accountPendingDelete = account
                        },
                        onSetDefault = {
                            AccountManager.setDefault(account.id)
                        },
                        onToggleActive = {
                            AccountManager.setActive(
                                id = account.id,
                                active = !account.isActive,
                            )
                        }
                    )
                }
            }
        }
    }

    accountPendingDelete?.let { account ->

        AlertDialog(
            onDismissRequest = {
                accountPendingDelete = null
            },
            title = {
                Text(
                    "حذف حساب",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    "آیا از حذف حساب «${account.name}» مطمئن هستید؟"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {

                        val deleted =
                            AccountManager.delete(account.id)

                        accountPendingDelete = null

                        if (!deleted) {

                            Toast.makeText(
                                context,
                                "این حساب دارای تراکنش است و برای حفظ تاریخچه مالی حذف نمی‌شود.",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        accountPendingDelete = null
                    }
                ) {
                    Text("انصراف")
                }
            }
        )
    }

    if (showAddDialog) {

        AccountDialog(
            title = "افزودن حساب",
            initialAccount = null,
            onDismiss = {
                showAddDialog = false
            },
            onSave = { name, type, bank, number, cardLast4, openingBalance, isDefault ->

                AccountManager.add(
                    name = name,
                    type = type,
                    bankName = bank,
                    accountNumber = number,
                    cardLast4 = cardLast4,
                    openingBalance = openingBalance,
                    isDefault = isDefault,
                )

                showAddDialog = false
            }
        )
    }

    editingAccount?.let { account ->

        AccountDialog(
            title = "ویرایش حساب",
            initialAccount = account,
            onDismiss = {
                editingAccount = null
            },
            onSave = { name, type, bank, number, cardLast4, openingBalance, isDefault ->

                AccountManager.update(
                    account.copy(
                        name = name,
                        type = type,
                        bankName = bank,
                        accountNumber = number,
                        cardLast4 = cardLast4,
                        openingBalance = openingBalance,
                        isDefault = isDefault,
                    )
                )

                editingAccount = null
            }
        )
    }
}

@Composable
private fun AccountSummaryStat(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
            )
            .padding(
                horizontal = 10.dp,
                vertical = 9.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        Spacer(
            Modifier.height(2.dp)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
        )
    }
}
@Composable
private fun AccountCard(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
    onToggleActive: () -> Unit,
) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }

    val balance = AccountLedger.balanceOf(
        accountId = account.id,
        transactions = TransactionStore.transactions,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (account.isActive) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(17.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = accountIcon(account.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(
                    Modifier.width(12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                ) {

                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(
                        Modifier.height(3.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        if (account.bankName.isNotBlank()) {
                            Text(
                                text = account.bankName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        if (account.cardLast4.isNotBlank()) {

                            if (account.bankName.isNotBlank()) {
                                Text(
                                    text = "  •  ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Text(
                                text = "•••• ${account.cardLast4}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Box {

                    IconButton(
                        onClick = {
                            menuExpanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "گزینه‌ها",
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("ویرایش")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )

                        if (!account.isDefault) {

                            DropdownMenuItem(
                                text = {
                                    Text("تعیین به عنوان پیش‌فرض")
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onSetDefault()
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (account.isActive) {
                                        "غیرفعال کردن حساب"
                                    } else {
                                        "فعال کردن حساب"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onToggleActive()
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("حذف")
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {

                Column {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        if (account.isDefault) {
                            AccountBadge(
                                text = "پیش‌فرض",
                                icon = Icons.Filled.Star,
                            )
                        }

                        if (!account.isActive) {
                            AccountBadge(
                                text = "غیرفعال",
                                icon = Icons.Filled.AccountBalance,
                            )
                        }
                    }

                    if (account.accountNumber.isNotBlank()) {
                        Spacer(
                            Modifier.height(7.dp)
                        )

                        Text(
                            text = "شماره حساب: ${account.accountNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                ) {

                    Text(
                        text = formatAmount(balance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color =
                            if (account.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )

                    Text(
                        text = "ریال",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp),
            )
            .padding(
                horizontal = 8.dp,
                vertical = 5.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Spacer(
            Modifier.width(4.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}
@Composable
private fun AccountDialog(
    title: String,
    initialAccount: Account?,
    onDismiss: () -> Unit,
    onSave: (
        String,
        AccountType,
        String,
        String,
        String,
        Long,
        Boolean
    ) -> Unit
) {

    var name by remember {
        mutableStateOf(initialAccount?.name ?: "")
    }

    var bank by remember {
        mutableStateOf(initialAccount?.bankName ?: "")
    }

    var accountNumber by remember {
        mutableStateOf(initialAccount?.accountNumber ?: "")
    }

    var cardLast4 by remember {
        mutableStateOf(initialAccount?.cardLast4 ?: "")
    }

    var openingBalanceText by remember {
        mutableStateOf(
            initialAccount?.openingBalance?.toString() ?: ""
        )
    }

    var selectedType by remember {
        mutableStateOf(
            initialAccount?.type ?: AccountType.BANK
        )
    }

    var isDefault by remember {
        mutableStateOf(
            initialAccount?.isDefault ?: false
        )
    }

    var typeMenuExpanded by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("نام حساب")
                    },
                    singleLine = true
                )

                Box {

                    OutlinedButton(
                        onClick = {
                            typeMenuExpanded = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            accountTypeTitle(selectedType)
                        )
                    }

                    DropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = {
                            typeMenuExpanded = false
                        }
                    ) {

                        AccountType.entries.forEach { type ->

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        accountTypeTitle(type)
                                    )
                                },
                                onClick = {
                                    selectedType = type
                                    typeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = bank,
                    onValueChange = {
                        bank = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("نام بانک")
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = {
                        accountNumber = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("شماره حساب")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = cardLast4,
                    onValueChange = {
                        if (it.length <= 4 && it.all(Char::isDigit)) {
                            cardLast4 = it
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("۴ رقم آخر کارت")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value = openingBalanceText,
                    onValueChange = {
                        openingBalanceText = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("موجودی اولیه")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "حساب پیش‌فرض"
                    )

                    Spacer(
                        Modifier.weight(1f)
                    )

                    androidx.compose.material3.Switch(
                        checked = isDefault,
                        onCheckedChange = {
                            isDefault = it
                        }
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                enabled = name.isNotBlank(),
                onClick = {

                    val openingBalance = openingBalanceText
                        .replace(",", "")
                        .trim()
                        .toLongOrNull() ?: 0L

                    onSave(
                        name.trim(),
                        selectedType,
                        bank.trim(),
                        accountNumber.trim(),
                        cardLast4.trim(),
                        openingBalance,
                        isDefault
                    )
                }
            ) {
                Text("ذخیره")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("انصراف")
            }
        }
    )
}

private fun accountTypeTitle(
    type: AccountType
): String {

    return when (type) {
        AccountType.BANK -> "حساب بانکی"
        AccountType.CASH -> "پول نقد"
        AccountType.CARD -> "کارت بانکی"
        AccountType.SAVINGS -> "حساب پس‌انداز"
        AccountType.WALLET -> "کیف پول"
    }
}

private fun accountIcon(
    type: AccountType
): androidx.compose.ui.graphics.vector.ImageVector {

    return when (type) {
        AccountType.BANK -> Icons.Filled.AccountBalance
        AccountType.CASH -> Icons.Filled.AccountBalance
        AccountType.CARD -> Icons.Filled.CreditCard
        AccountType.SAVINGS -> Icons.Filled.Savings
        AccountType.WALLET -> Icons.Filled.AccountBalance
    }
}

private fun formatAmount(
    amount: Long
): String {

    return amount.toPersianCurrency()
}











