package com.example.walletapp.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.Account
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "حساب‌ها",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "مدیریت موجودی و حساب‌های مالی",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                }
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "افزودن حساب"
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),

            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 100.dp
            ),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {

                        Text(
                            "موجودی کل",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(
                            Modifier.height(8.dp)
                        )

                        Text(
                            formatAmount(totalBalance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(
                            Modifier.height(4.dp)
                        )

                        Text(
                            "ریال",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        "حساب‌های من",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "${accounts.size} حساب",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (accounts.isEmpty()) {

                item {

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(
                                Modifier.height(12.dp)
                            )

                            Text(
                                "هنوز حسابی ثبت نشده است",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                Modifier.height(6.dp)
                            )

                            Text(
                                "اولین حساب مالی خود را اضافه کنید.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(
                                Modifier.height(16.dp)
                            )

                            Button(
                                onClick = {
                                    showAddDialog = true
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null
                                )

                                Spacer(
                                    Modifier.width(6.dp)
                                )

                                Text("افزودن حساب")
                            }
                        }
                    }
                }

            } else {

                items(
                    accounts,
                    key = { it.id }
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
                            AccountStore.setDefault(account.id)
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
                    fontWeight = FontWeight.Bold
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
                            AccountStore.delete(account.id)

                        accountPendingDelete = null

                        if (!deleted) {

                            Toast.makeText(
                                context,
                                "این حساب دارای تراکنش است و برای حفظ تاریخچه مالی حذف نمی‌شود.",
                                Toast.LENGTH_LONG
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

                AccountStore.add(
                    name = name,
                    type = type,
                    bankName = bank,
                    accountNumber = number,
                    cardLast4 = cardLast4,
                    openingBalance = openingBalance,
                    isDefault = isDefault
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

                AccountStore.update(
                    account.copy(
                        name = name,
                        type = type,
                        bankName = bank,
                        accountNumber = number,
                        cardLast4 = cardLast4,
                        openingBalance = openingBalance,
                        isDefault = isDefault
                    )
                )

                editingAccount = null
            }
        )
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
) {

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    accountIcon(account.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(
                Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        account.name,
                        fontWeight = FontWeight.Bold
                    )

                    if (account.isDefault) {

                        Spacer(
                            Modifier.width(6.dp)
                        )

                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "حساب پیش‌فرض",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (account.bankName.isNotBlank()) {

                    Spacer(
                        Modifier.height(3.dp)
                    )

                    Text(
                        account.bankName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (account.cardLast4.isNotBlank()) {

                    Spacer(
                        Modifier.height(2.dp)
                    )

                    Text(
                        "•••• ${account.cardLast4}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    formatAmount(
                        AccountLedger.balanceOf(
                            accountId = account.id,
                            transactions = TransactionStore.transactions,
                        )
                    ),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "ریال",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {

                IconButton(
                    onClick = {
                        menuExpanded = true
                    }
                ) {

                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "گزینه‌ها"
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
                                contentDescription = null
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
                                    contentDescription = null
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
                            Text("حذف")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null
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

    return "%,d".format(amount)
}





