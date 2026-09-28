package com.example.walletapp.sms

import android.content.Context

import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.LoanEntry
import com.example.walletapp.data.LoanPaymentManager

/**
 * Business-layer handler for completing an SMS-created transaction.
 *
 * IMPORTANT:
 * - This class does NOT create a transaction.
 * - It works only on the already-created SMS transaction.
 * - UI and Activity lifecycle remain outside this class.
 * - The existing financial rules are preserved.
 */
object SmsTransactionCompletionHandler {

    data class CompletionRequest(
        val transaction: Transaction,
        val selectedType: com.example.walletapp.data.TransactionType,
        val isTransfer: Boolean,
        val destinationAccountId: String?,
        val sender: String,
        val rawBody: String?,
        val selectedBank: String,
        val selectedCategory: String?,
        val tags: List<String>,
        val description: String,
        val receiptUri: String?,
        val selectedAccountId: String?,
        val smsAccountNumber: String?,
        val smsCardLast4: String?,
        val learnBank: Boolean,
        val selectedLoan: LoanEntry?,
        val selectedInstallmentNumber: Int?,
    )

    sealed class CompletionResult {

        data object NoChanges : CompletionResult()

        data object AccountRequired : CompletionResult()

        data object InvalidAccount : CompletionResult()

        data object InvalidInstallment : CompletionResult()

        data object InstallmentAlreadyPaid : CompletionResult()

        data object TransactionNotFound : CompletionResult()

        data object InstallmentLinkFailed : CompletionResult()

        data class Success(
            val transaction: Transaction,
        ) : CompletionResult()
    }

    /**
     * Executes the complete business operation.
     *
     * Order intentionally follows the current
     * Legacy SMS completion behavior:
     *
     * 1. Validate completion data
     * 2. Resolve account
     * 3. Save learning rules
     * 4. Link loan installment when required
     * 5. Build updated transaction
     * 6. Persist exactly once
     *
     * This method does NOT:
     * - finish Activity
     * - set Activity result
     * - advance Queue
     * - show UI
     */
    @Synchronized
    fun complete(
        context: Context,
        request: CompletionRequest,
    ): CompletionResult {

        val transaction = request.transaction

        if (!transaction.requiresCompletion) {
            return CompletionResult.TransactionNotFound
        }

        val hasCompletionData =
            request.selectedType != transaction.type ||
            request.selectedCategory != null ||
                request.tags.isNotEmpty() ||
                request.description.isNotBlank() ||
                request.receiptUri != null ||
                request.selectedBank.trim() != transaction.subtitle.trim() ||
            request.selectedAccountId != null
        /*
         * A transaction marked requiresCompletion is already waiting
         * for user confirmation. Confirmation itself is a valid
         * completion even when the user does not change optional fields.
         *
         * Account validation below remains mandatory.
         */
        if (!hasCompletionData && !transaction.requiresCompletion) {
            return CompletionResult.NoChanges
        }
        val metadata =
            SmsTransactionMetadataManager.get(transaction.id)

        val effectiveSender =
            metadata
                ?.sender
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: request.sender.trim()

        val effectiveRawBody =
            metadata
                ?.rawBody
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: request.rawBody
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }

        val resolvedAccountId =
            resolveAccountId(
                context = context,
                selectedBank = request.selectedBank,
                selectedAccountId = request.selectedAccountId,
                smsAccountNumber = request.smsAccountNumber,
                smsCardLast4 = request.smsCardLast4,
            )

        val activeAccounts =
            AccountStore.accounts.filter { it.isActive }

        if (request.isTransfer) {
            val sourceId =
                request.selectedAccountId
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: resolvedAccountId

            val destinationId =
                request.destinationAccountId
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }

            if (sourceId == null || destinationId == null) {
                return CompletionResult.AccountRequired
            }

            if (sourceId == destinationId) {
                return CompletionResult.InvalidAccount
            }

            if (activeAccounts.none { it.id == sourceId } ||
                activeAccounts.none { it.id == destinationId }) {
                return CompletionResult.InvalidAccount
            }
        }

        val explicitAccount =
            request.selectedAccountId
                ?.trim()
                ?.takeIf { it.isNotBlank() }

        /*
         * An explicitly selected active account is authoritative.
         * It must not be rejected merely because its bank differs from
         * the parser's detected/selected bank: the user is allowed to
         * correct a wrong bank detection by choosing the real account.
         */
        if (explicitAccount != null) {
            val selectedAccount =
                activeAccounts.firstOrNull { it.id == explicitAccount }

            if (selectedAccount == null) {
                return CompletionResult.InvalidAccount
            }
        } else if (resolvedAccountId == null) {
            return CompletionResult.AccountRequired
        }

        val installmentLoan =
            if (request.selectedCategory == "قسط") {
                if (request.isTransfer || request.selectedType != com.example.walletapp.data.TransactionType.EXPENSE) {
                    return CompletionResult.InvalidInstallment
                }

                request.selectedLoan
                    ?: return CompletionResult.InvalidInstallment
            } else {
                null
            }

        val installmentNumber =
            if (installmentLoan != null) {
                request.selectedInstallmentNumber
                    ?: return CompletionResult.InvalidInstallment
            } else {
                null
            }

        if (installmentLoan != null) {
            if (
                installmentNumber!! < 1 ||
                installmentNumber > installmentLoan.totalInstallments
            ) {
                return CompletionResult.InvalidInstallment
            }

            if (installmentLoan.isInstallmentPaid(installmentNumber)) {
                return CompletionResult.InstallmentAlreadyPaid
            }
        }

        val updatedTransaction =
            buildUpdatedTransaction(
                request = request,
                resolvedAccountId = resolvedAccountId,
            )

        /*
         * Persist the transaction first. LoanPaymentManager links the
         * existing transaction to the installment, so doing the link
         * first could leave a loan payment behind if transaction
         * persistence failed.
         */
        TransactionStore.update(updatedTransaction)

        if (installmentLoan != null) {
            val payment =
                LoanPaymentManager.linkExistingTransactionToInstallment(
                    context = context,
                    loan = installmentLoan,
                    installmentNumber = installmentNumber!!,
                    transactionId = transaction.id,
                )

            if (payment == null) {
                TransactionStore.restore(transaction)
                return CompletionResult.InstallmentLinkFailed
            }
        }

        /*
         * Learning is committed only after the transaction has passed
         * account/installment validation and TransactionStore.update()
         * has completed successfully.
         *
         * UNKNOWN_BANK is intentionally never learned as a real bank.
         */
        val usableSelectedBank =
            request.selectedBank
                .trim()
                .takeIf {
                    it.isNotBlank() &&
                        it != BankSmsParser.UNKNOWN_BANK
                }

        if (
            request.learnBank &&
            usableSelectedBank != null &&
            effectiveSender.isNotBlank()
        ) {
            SmsLearningManager.saveSenderBank(
                sender = effectiveSender,
                bankName = usableSelectedBank,
            )
        }

        if (
            request.selectedCategory != null &&
            effectiveSender.isNotBlank()
        ) {
            SmsLearningManager.saveSenderCategory(
                sender = effectiveSender,
                category = request.selectedCategory,
            )
        }

        if (
            request.learnBank &&
            usableSelectedBank != null &&
            effectiveSender.isNotBlank() &&
            effectiveRawBody != null
        ) {
            SmsLearningManager.savePatternRule(
                sender = effectiveSender,
                pattern = effectiveRawBody,
                bankName = usableSelectedBank,
                category = request.selectedCategory,
                transactionType = request.selectedType.name,
            )
        }
        return CompletionResult.Success(

            transaction = updatedTransaction,
        )
    }

    /**
     * Ignores future SMS from the selected sender and
     * finalizes the current pending transaction.
     *
     * UI lifecycle and Queue advancement remain outside
     * this business operation.
     */
    /**
     * Removes the current SMS transaction when the user confirms that
     * this message is not a bank transaction.
     *
     * The pending transaction must be removed, not merely marked complete,
     * so it cannot affect the ledger or be uploaded by Sync.
     */
    fun dismissAsNotBankTransaction(
        context: Context,
        transaction: Transaction,
    ) {
        TransactionStore.attachContext(context)
        SmsTransactionMetadataManager.attachContext(context)

        TransactionStore.remove(transaction.id)
        SmsTransactionMetadataManager.remove(transaction.id)
    }

    fun ignoreSender(
        context: Context,
        transaction: Transaction,
        sender: String?,
    ) {
        TransactionStore.attachContext(context)
        SmsTransactionMetadataManager.attachContext(context)
        SmsIgnoreManager.attachContext(context)

        val metadata =
            SmsTransactionMetadataManager.get(transaction.id)

        if (
            !sender.isNullOrBlank() &&
            metadata != null &&
            !metadata.rawBody.isNullOrBlank()
        ) {
            SmsIgnoreManager.ignoreSender(
                sender = sender,
            )
        }

        TransactionStore.update(
            transaction.copy(
                requiresCompletion = false,
            )
        )
    }
    /**
     * Resolves the account using the existing priority:
     *
     * 1. Explicit account
     * 2. Exact SMS account number
     * 3. Exact SMS card last4
     * 4. Exactly one active account for selected bank
     * 5. No guess
     */
    fun resolveAccountId(
        context: Context,
        selectedBank: String,
        selectedAccountId: String?,
        smsAccountNumber: String?,
        smsCardLast4: String?,
    ): String? {

        TransactionStore.attachContext(context)
        AccountStore.attachContext(context)

        val matchingAccounts =
            AccountStore.accounts.filter {
                it.isActive &&
                    it.bankName.trim() == selectedBank.trim()
            }

        if (matchingAccounts.isEmpty()) {
            return null
        }

        val explicit =
            selectedAccountId
                ?.takeIf { it.isNotBlank() }

        if (explicit != null) {
            return matchingAccounts
                .firstOrNull {
                    it.id == explicit
                }
                ?.id
        }

        val cleanAccountNumber =
            smsAccountNumber
                ?.trim()
                ?.takeIf { it.isNotBlank() }

        if (cleanAccountNumber != null) {

            val matches =
                matchingAccounts.filter {
                    it.accountNumber
                        .trim()
                        .equals(
                            cleanAccountNumber,
                            ignoreCase = true,
                        )
                }

            if (matches.size == 1) {
                return matches.first().id
            }
        }

        val cleanCardLast4 =
            smsCardLast4
                ?.trim()
                ?.takeIf { it.isNotBlank() }

        if (cleanCardLast4 != null) {

            val matches =
                matchingAccounts.filter {
                    it.cardLast4
                        .trim()
                        .equals(
                            cleanCardLast4,
                            ignoreCase = true,
                        )
                }

            if (matches.size == 1) {
                return matches.first().id
            }
        }

        return if (matchingAccounts.size == 1) {
            matchingAccounts.first().id
        } else {
            null
        }
    }

    /**
     * Builds the final transaction without persistence.
     */
    fun buildUpdatedTransaction(
        request: CompletionRequest,
        resolvedAccountId: String?,
    ): Transaction {

        val transaction = request.transaction

        val updatedTitle: String
        val updatedSubtitle: String

        if (transaction.subtitle == "تراکنش بانکی") {
            updatedTitle = request.selectedBank
            updatedSubtitle = "تراکنش بانکی"
        } else {
            updatedTitle = transaction.title
            updatedSubtitle = request.selectedBank
        }

        return transaction.copy(
            type = request.selectedType,
            title = if (request.isTransfer) "انتقال بین حساب‌ها" else updatedTitle,
            subtitle = if (request.isTransfer) {
                val source = AccountStore.accounts.firstOrNull { it.id == request.selectedAccountId }
                val destination = AccountStore.accounts.firstOrNull { it.id == request.destinationAccountId }
                if (source != null && destination != null) {
                    "${source.name} → ${destination.name}"
                } else {
                    updatedSubtitle
                }
            } else updatedSubtitle,
            accountId = if (request.isTransfer) null else
                request.selectedAccountId
                    ?.takeIf { it.isNotBlank() }
                    ?: resolvedAccountId,
            iconKey = if (request.isTransfer) "انتقال" else
                request.selectedCategory ?: transaction.iconKey,
            category = if (request.isTransfer) null else
                request.selectedCategory ?: transaction.category,
            kind = if (request.isTransfer) com.example.walletapp.data.TransactionKind.TRANSFER
                else com.example.walletapp.data.TransactionKind.NORMAL,
            sourceAccountId = if (request.isTransfer) {
                request.selectedAccountId?.takeIf { it.isNotBlank() } ?: resolvedAccountId
            } else null,
            destinationAccountId = if (request.isTransfer) {
                request.destinationAccountId?.takeIf { it.isNotBlank() }
            } else null,
            tags = request.tags,
            description = request.description.trim(),
            receiptUri = request.receiptUri,
            requiresCompletion = false,
        )
    }
}










