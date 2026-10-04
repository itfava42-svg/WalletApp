package com.example.walletapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.walletapp.data.Account
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.ui.components.AppTextField


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransferScreen(
    onBack: () -> Unit,
    onTransferCompleted: () -> Unit,
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AccountStore.attachContext(context)
        TransactionStore.attachContext(context)
    }

    val accounts =
        AccountStore.accounts.filter { it.isActive }

    // Smart defaults: prefer the user's default account as the source and,
    // when transfer history exists, reuse the most recent counterpart as the
    // destination. These are only suggestions; the user can change either.
    val suggestedSourceId = remember(accounts) {
        accounts.firstOrNull { it.isDefault }?.id
            ?: accounts.firstOrNull()?.id
    }

    val suggestedDestinationId = remember(accounts) {
        val sourceId = suggestedSourceId
        val recentCounterpart =
            sourceId?.let { source ->
                TransactionStore.transactions
                    .asSequence()
                    .filter { transaction ->
                        transaction.kind == com.example.walletapp.data.TransactionKind.TRANSFER &&
                            (transaction.sourceAccountId == source || transaction.destinationAccountId == source)
                    }
                    .mapNotNull { transaction ->
                        if (transaction.sourceAccountId == source) {
                            transaction.destinationAccountId
                        } else {
                            transaction.sourceAccountId
                        }
                    }
                    .firstOrNull { candidate ->
                        accounts.any { it.id == candidate && it.id != sourceId }
                    }
            }

        recentCounterpart
            ?: accounts.firstOrNull { it.id != sourceId }?.id
    }

    var sourceAccountId by remember(suggestedSourceId) {
        mutableStateOf(suggestedSourceId)
    }

    var destinationAccountId by remember(suggestedDestinationId, sourceAccountId) {
        mutableStateOf(
            suggestedDestinationId?.takeIf { it != sourceAccountId }
                ?: accounts.firstOrNull { it.id != sourceAccountId }?.id
        )
    }

    var amountText by remember {
        mutableStateOf("")
    }

    var note by remember {
        mutableStateOf("")
    }

    var selectingSource by remember {
        mutableStateOf(false)
    }

    var selectingDestination by remember {
        mutableStateOf(false)
    }

    val sourceAccount =
        accounts.firstOrNull { it.id == sourceAccountId }

    val destinationAccount =
        accounts.firstOrNull { it.id == destinationAccountId }

    val amount =
        amountText
            .replace(",", "")
            .replace("٬", "")
            .trim()
            .map { ch ->
                when (ch) {
                    in '۰'..'۹' -> ('0'.code + (ch.code - '۰'.code)).toChar()
                    in '٠'..'٩' -> ('0'.code + (ch.code - '٠'.code)).toChar()
                    else -> ch
                }
            }
            .joinToString("")
            .toLongOrNull()

    fun submitTransfer() {

        if (sourceAccountId == null) {
            Toast.makeText(
                context,
                "لطفاً حساب مبدأ را انتخاب کنید.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        if (destinationAccountId == null) {
            Toast.makeText(
                context,
                "لطفاً حساب مقصد را انتخاب کنید.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        if (sourceAccountId == destinationAccountId) {
            Toast.makeText(
                context,
                "حساب مبدأ و مقصد باید متفاوت باشند.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        if (amount == null || amount <= 0L) {
            Toast.makeText(
                context,
                "مبلغ انتقال را به‌درستی وارد کنید.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        val sourceId = sourceAccountId
        val destinationId = destinationAccountId

        if (sourceId == null || destinationId == null) {
            Toast.makeText(
                context,
                "حساب مبدأ و مقصد را انتخاب کنید.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        try {

            TransactionStore.addTransfer(
                sourceAccountId = sourceId,
                destinationAccountId = destinationId,
                amount = amount,
                note = note.trim(),
            )

            Toast.makeText(
                context,
                "انتقال با موفقیت ثبت شد.",
                Toast.LENGTH_SHORT,
            ).show()

            onTransferCompleted()

        } catch (error: IllegalArgumentException) {

            Toast.makeText(
                context,
                error.message ?: "ثبت انتقال انجام نشد.",
                Toast.LENGTH_LONG,
            ).show()

        } catch (error: Exception) {

            Toast.makeText(
                context,
                "خطا در ثبت انتقال.",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "انتقال بین حساب‌ها",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                    )

                    Spacer(
                        modifier = Modifier.size(12.dp)
                    )

                    Column {
                        Text(
                            "انتقال داخلی",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.onPrimaryContainer,
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            "انتقال وجه بین حساب‌های خودتان بدون ثبت به‌عنوان هزینه",
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            if (accounts.isEmpty()) {

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "برای انتقال، حداقل دو حساب فعال ثبت کنید.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error,
                    )
                }

            } else {

                AccountSelectorCard(
                    title = "حساب مبدأ",
                    account = sourceAccount,
                    onClick = {
                        selectingSource = true
                    },
                )

                AccountSelectorCard(
                    title = "حساب مقصد",
                    account = destinationAccount,
                    onClick = {
                        selectingDestination = true
                    },
                )

                AppTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText =
                            input.filter {
                                it.isDigit() ||
                                    it == ',' ||
                                    it == '٬'
                            }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = "مبلغ انتقال",
                    placeholder = { Text("مثلاً 2,000,000") },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                        ),
                )

                AppTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "توضیحات",
                    placeholder = { Text("اختیاری") },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                AppPrimaryButton(
                    text = "ثبت انتقال",
                    onClick = { submitTransfer() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled =
                        sourceAccountId != null &&
                            destinationAccountId != null &&
                            amount != null &&
                            amount > 0L &&
                            sourceAccountId != destinationAccountId,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }

    if (selectingSource) {
        AccountSelectionDialog(
            title = "انتخاب حساب مبدأ",
            accounts = accounts,
            selectedAccountId = sourceAccountId,
            excludedAccountId = destinationAccountId,
            onSelect = {
                sourceAccountId = it
                selectingSource = false
            },
            onDismiss = {
                selectingSource = false
            },
        )
    }

    if (selectingDestination) {
        AccountSelectionDialog(
            title = "انتخاب حساب مقصد",
            accounts = accounts,
            selectedAccountId = destinationAccountId,
            excludedAccountId = sourceAccountId,
            onSelect = {
                destinationAccountId = it
                selectingDestination = false
            },
            onDismiss = {
                selectingDestination = false
            },
        )
    }
}

@Composable
private fun AccountSelectorCard(
    title: String,
    account: Account?,
    onClick: () -> Unit,
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    account?.name ?: "انتخاب نشده",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun AccountSelectionDialog(
    title: String,
    accounts: List<Account>,
    selectedAccountId: String?,
    excludedAccountId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                accounts
                    .filter { it.id != excludedAccountId }
                    .forEach { account ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(account.id)
                                }
                                .padding(vertical = 8.dp),
                        ) {
                            RadioButton(
                                selected =
                                    selectedAccountId == account.id,
                                onClick = {
                                    onSelect(account.id)
                                },
                            )

                            Spacer(
                                modifier = Modifier.size(8.dp)
                            )

                            Text(
                                account.name,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text("انصراف")
            }
        },
    )
}



