package com.example.walletapp.sms

/**
 * Known official SMS sender short-codes for major Iranian banks.
 * Sourced from banks' published SMS-banking service numbers (user-verified
 * list). Some banks use more than one number for different services (info
 * line vs. transaction alerts), and numbers can change over time — if a
 * bank's SMS isn't being recognized, check the actual sender number on the
 * device and add it here.
 */
object BankSenderRegistry {
    val senderToBank: Map<String, String> = mapOf(
        "300017" to "بانک ملی",
        "9830009417" to "بانک ملی",

        "200020" to "بانک سپه",
        "200021" to "بانک سپه",
        "200022" to "بانک سپه",

        "200070" to "بانک تجارت",
        "20007010" to "بانک تجارت",

        "200033" to "بانک ملت",
        "200030" to "بانک ملت",

        "200060" to "بانک صادرات",

        "2000911" to "بانک کشاورزی",
        "200093" to "بانک کشاورزی",

        "300014" to "بانک مسکن",

        "1000700" to "بانک توسعه تعاون",
        "100070007" to "بانک توسعه تعاون",

        "300066" to "بانک رفاه کارگران",
        "300044" to "بانک رفاه کارگران",

        "200050" to "بانک اقتصاد نوین",

        "300054" to "بانک پارسیان",
        "50001099" to "بانک پارسیان",
        "300055" to "بانک پارسیان",
        "30007" to "بانک پارسیان",

        "20000" to "بانک سامان",

        "1000900" to "بانک پاسارگاد",
        "50009000" to "بانک پاسارگاد",

        "200038" to "بانک سینا",
        "500048" to "بانک سینا",
        "300028" to "بانک سینا",

        "100088" to "بانک سرمایه",

        "989999987641" to "بلو (بانک آینده)",
        "9999987641" to "بلو (بانک آینده)",
        "ResalatBank" to "بانک قرض الحسنه رسالت",

        // Alphanumeric English sender IDs — many Iranian banks use these
        // instead of (or in addition to) numeric short-codes.
        "TTBANK" to "بانک توسعه تعاون",
        "Bank Melli" to "بانک ملی",
        "Bank Mellat" to "بانک ملت",
        "Bank Maskan" to "بانک مسکن",
        "Maskan Bank" to "بانک مسکن",
        "Bank Shahr" to "بانک شهر",
        "KESHAVARZI" to "بانک کشاورزی",
        "REFAH" to "بانک رفاه کارگران",
        "Refah Bank" to "بانک رفاه کارگران",
        "Sina Bank" to "بانک سینا",
        "B.Pasargad" to "بانک پاسارگاد",
        "B.QMEHRIRAN" to "بانک قرض‌الحسنه مهر ایران",
        "V.Maskan" to "بانک مسکن",
        "V.Refah" to "بانک رفاه کارگران",
    )

    /** Returns the bank name for a sender number, trying a few normalized forms. */
    fun lookup(sender: String?): String? {
        if (sender == null) return null
        val trimmed = sender.trim()
        val cleaned = trimmed.removePrefix("+98").removePrefix("0098").removePrefix("98")
        return senderToBank[trimmed]
            ?: senderToBank[cleaned]
            ?: senderToBank["0$cleaned"]
    }
}