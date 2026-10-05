package com.example.walletapp.sms

enum class BankSmsSenderType {
    TRANSACTION,
    OTP,
    INFORMATION,
    ADVERTISEMENT,
    CHEQUE,
    MIXED,
    UNKNOWN,
}

enum class BankSmsValidationStatus {
    USER_PROVIDED_PENDING_VALIDATION,
    VALIDATED,
    DEPRECATED,
}

data class BankSmsSender(
    val bankId: String,
    val bankName: String,
    val sender: String,
    val type: BankSmsSenderType,
    val transactionAllowed: Boolean,
    val otpAllowed: Boolean,
    val infoAllowed: Boolean,
    val confidence: Int,
    val validationStatus: BankSmsValidationStatus =
        BankSmsValidationStatus.USER_PROVIDED_PENDING_VALIDATION,
)

/**
 * Central bank-SMS sender registry.
 *
 * Sender identity is deliberately separated from message classification.
 * A known sender alone is never sufficient to create a transaction.
 */
object BankSmsLibrary {

    val senders: List<BankSmsSender> = listOf(
        sender("tejarat", "بانک تجارت", "100080", BankSmsSenderType.TRANSACTION),
        sender("tejarat", "بانک تجارت", "Tejaratbank", BankSmsSenderType.TRANSACTION),
        sender("tejarat", "بانک تجارت", "TejaratBank", BankSmsSenderType.TRANSACTION),

        sender("saman", "بانک سامان", "20000", BankSmsSenderType.TRANSACTION),
        sender("saman", "بانک سامان", "0999920000", BankSmsSenderType.TRANSACTION),
        sender("saman", "بانک سامان", "9999920000", BankSmsSenderType.TRANSACTION),

        sender("sarmayeh", "بانک سرمایه", "7007058", BankSmsSenderType.TRANSACTION),
        sender("sarmayeh", "بانک سرمایه", "7007583", BankSmsSenderType.INFORMATION),

        sender("saderat", "بانک صادرات", "3000940", BankSmsSenderType.OTP),
        sender("saderat", "بانک صادرات", "4040701701", BankSmsSenderType.CHEQUE),

        sender("sepah", "بانک سپه", "671557", BankSmsSenderType.TRANSACTION),
        sender("maskan", "بانک مسکن", "10002516", BankSmsSenderType.OTP),
        sender("day", "بانک دی", "3000272612", BankSmsSenderType.TRANSACTION),

        sender("sina", "بانک سینا", "200038", BankSmsSenderType.TRANSACTION),
        sender("sina", "بانک سینا", "500048", BankSmsSenderType.INFORMATION),

        sender("parsian", "بانک پارسیان", "10005401", BankSmsSenderType.TRANSACTION),
        sender("tourism", "بانک گردشگری", "2000300", BankSmsSenderType.TRANSACTION),
        sender("postbank", "پست بانک ایران", "50004940", BankSmsSenderType.TRANSACTION),
        sender("middle_east", "بانک خاورمیانه", "20004860", BankSmsSenderType.TRANSACTION),

        sender("refah", "بانک رفاه کارگران", "5000226", BankSmsSenderType.TRANSACTION),

        sender("iranzamin", "بانک ایران زمین", "500045", BankSmsSenderType.TRANSACTION),
        sender("iranzamin", "بانک ایران زمین", "9999996905", BankSmsSenderType.INFORMATION),

        sender("shahr", "بانک شهر", "200035", BankSmsSenderType.TRANSACTION),
        sender("tosee_taavon", "بانک توسعه تعاون", "500015741", BankSmsSenderType.ADVERTISEMENT),
        sender("ayandeh", "بانک آینده", "7007161", BankSmsSenderType.TRANSACTION),

        sender("melli", "بانک ملی", "3000941001", BankSmsSenderType.TRANSACTION),
        sender("melli", "بانک ملی", "70071001", BankSmsSenderType.OTP),
        sender("melli", "بانک ملی", "30009417", BankSmsSenderType.TRANSACTION),
    )

    fun lookup(sender: String?): List<BankSmsSender> {
        val normalized = normalizeSender(sender) ?: return emptyList()
        return senders.filter { normalizeSender(it.sender) == normalized }
    }

    fun lookupTransactionSender(sender: String?): BankSmsSender? =
        lookup(sender).firstOrNull { it.transactionAllowed }

    fun isKnownSender(sender: String?): Boolean =
        lookup(sender).isNotEmpty()

    private fun sender(
        bankId: String,
        bankName: String,
        value: String,
        type: BankSmsSenderType,
    ): BankSmsSender {
        return BankSmsSender(
            bankId = bankId,
            bankName = bankName,
            sender = value,
            type = type,
            transactionAllowed = type == BankSmsSenderType.TRANSACTION ||
                type == BankSmsSenderType.MIXED,
            otpAllowed = type == BankSmsSenderType.OTP ||
                type == BankSmsSenderType.MIXED,
            infoAllowed = type == BankSmsSenderType.INFORMATION ||
                type == BankSmsSenderType.MIXED,
            confidence = when (type) {
                BankSmsSenderType.TRANSACTION -> 100
                BankSmsSenderType.OTP -> 100
                BankSmsSenderType.INFORMATION -> 90
                BankSmsSenderType.ADVERTISEMENT -> 90
                BankSmsSenderType.CHEQUE -> 90
                BankSmsSenderType.MIXED -> 100
                BankSmsSenderType.UNKNOWN -> 0
            },
        )
    }

    private fun normalizeSender(sender: String?): String? {
        val value = sender?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return value
            .removePrefix("+98")
            .removePrefix("0098")
            .removePrefix("98")
            .removePrefix("0")
            .lowercase()
    }
}
