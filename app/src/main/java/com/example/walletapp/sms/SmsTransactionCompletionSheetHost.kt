package com.example.walletapp.sms

import android.net.Uri

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp

import com.example.walletapp.data.LoanEntry
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsTransactionCompletionSheetHost(
    transaction: Transaction,
    detectedBank: String,
    sender: String?,
    initialCategory: String?,
    activeLoans: List<LoanEntry>,
    receiptUri: Uri?,
    onPickReceipt: () -> Unit,
    onTakePhoto: () -> Unit,
    onComplete: (
        selectedType: TransactionType,
        category: String?,
        tags: List<String>,
        description: String,
        receiptUri: Uri?,
        selectedBank: String,
        selectedAccountId: String?,
        learnBank: Boolean,
        loan: LoanEntry?,
        installment: SmsLoanInstallmentChoice?,
    ) -> Unit,
    onIgnoreSender: () -> Unit,
    onAddBankAccount: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(transaction.id) {

        android.util.Log.d(
            "TEMP_SHEET_STATE",
            "LaunchedEffect START id=${transaction.id} state=${sheetState.currentValue}"
        )

        runCatching {
            sheetState.show()

            android.util.Log.d(
                "TEMP_SHEET_STATE",
                "sheetState.show() COMPLETED id=${transaction.id} state=${sheetState.currentValue}"
            )
        }.onFailure { error ->
            android.util.Log.e(
                "TEMP_SHEET_STATE",
                "sheetState.show() FAILED id=${transaction.id}",
                error
            )
        }
    }

    android.util.Log.d(
        "TEMP_SHEET_STATE",
        "Host COMPOSING id=${transaction.id} state=${sheetState.currentValue}"
    )

    BackHandler(enabled = true) {
        onDismiss()
    }

    DisposableEffect(transaction.id) {

        android.util.Log.d(
            "TEMP_SHEET_MODAL",
            "Modal lifecycle ENTER id=${transaction.id}"
        )

        onDispose {
            android.util.Log.d(
                "TEMP_SHEET_MODAL",
                "Modal lifecycle DISPOSE id=${transaction.id}"
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            android.util.Log.d(
                "TEMP_SHEET_MODAL",
                "onDismissRequest CALLED id=${transaction.id}"
            )
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        android.util.Log.d(
            "TEMP_SHEET_MODAL",
            "REAL CONTENT TEST composed id=${transaction.id} state=${sheetState.currentValue}"
        )

        SmsTransactionCompletionBottomSheetContent(
            transaction = transaction,
            detectedBank = detectedBank,
            sender = sender,
            initialCategory = initialCategory,
            activeLoans = activeLoans,
            receiptUri = receiptUri,
            onPickReceipt = onPickReceipt,
            onTakePhoto = onTakePhoto,
            onComplete = onComplete,
            onIgnoreSender = onIgnoreSender,
            onAddBankAccount = onAddBankAccount,
        )
    }
}














