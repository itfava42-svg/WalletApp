package com.example.walletapp.sms

import android.net.Uri

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
    

BackHandler(enabled = true) {
        onDismiss()
    }


    android.util.Log.d(
        "SMS_HOST_COMPOSE_DIAG",
        "BEFORE_MODAL_BOTTOM_SHEET transactionId=${transaction.id}"
    )

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
    ) {
        android.util.Log.d(
            "SMS_HOST_COMPOSE_DIAG",
            "MODAL_CONTENT_ENTER transactionId=${transaction.id}"
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


























