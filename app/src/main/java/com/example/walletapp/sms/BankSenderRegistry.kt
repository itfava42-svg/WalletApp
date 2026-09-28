package com.example.walletapp.sms

/**
 * Built-in trust registry for official bank SMS sender identifiers.
 *
 * IMPORTANT:
 * - Matching is exact after safe normalization only.
 * - Incomplete/wildcard-like sender values are intentionally NOT registered.
 * - This registry identifies the sender/bank; it does not by itself prove
 *   that an SMS contains a financial transaction.
 */
object BankSenderRegistry {

    val senderToBank: Map<String, String> = mapOf(
        "9830009419" to "بانک صادرات",
        "200060" to "بانک صادرات",

        "98300058" to "بانک سرمایه",
        "987007058" to "بانک سرمایه",
        "100088" to "بانک سرمایه",

        "98999984080" to "بانک سامان",
        "989999920000" to "بانک سامان",
        "20000" to "بانک سامان",

        "982000766" to "بانک دی",
        "9820004002" to "بانک دی",

        "985000973189" to "بانک تجارت",
        "TejaratBank" to "بانک تجارت",
        "200070" to "بانک تجارت",

        "986715000" to "بانک سپه",
        "98671" to "بانک سپه",
        "SEPAH BANK" to "بانک سپه",
        "200020" to "بانک سپه",
        "200021" to "بانک سپه",
        "200022" to "بانک سپه",

        "98500094" to "بانک مسکن",
        "9810002503" to "بانک مسکن",
        "BankMaskan" to "بانک مسکن",
        "300014" to "بانک مسکن",

        "9820006438" to "بانک توسعه تعاون",
        "985000157" to "بانک توسعه تعاون",
        "TTBANK" to "بانک توسعه تعاون",

        "98200035" to "بانک شهر",
        "Bank Shahr" to "بانک شهر",

        "98200050" to "بانک اقتصاد نوین",
        "98200051" to "بانک اقتصاد نوین",
        "EN Bank" to "بانک اقتصاد نوین",
        "200050" to "بانک اقتصاد نوین",

        "9899902318" to "بانک پارسیان",
        "PARSIANBANK" to "بانک پارسیان",

        "9820004001" to "بانک آینده",
        "9820004002" to "بانک آینده",
        "Ayandeh Bank" to "بانک آینده",

        "98500045" to "بانک ایران زمین",
        "IZBANK" to "بانک ایران زمین",
        "IZ BANK" to "بانک ایران زمین",

        "98200048700730" to "بانک توسعه صادرات",

        "98300066" to "بانک رفاه کارگران",
        "RefahBank" to "بانک رفاه کارگران",
        "REFAH" to "بانک رفاه کارگران",

        "98500048" to "بانک سینا",
        "98500019" to "بانک سینا",
        "SinaBank" to "بانک سینا",
        "200038" to "بانک سینا",
        "500048" to "بانک سینا",

        "98100099" to "بانک صنعت و معدن",

        "9850004747" to "بانک قرض الحسنه رسالت",
        "ResalatBank" to "بانک قرض الحسنه رسالت",

        "982000300" to "بانک گردشگری",
        "TourismBank" to "بانک گردشگری",
        "2000300" to "بانک گردشگری",

        "9820004321" to "بانک کارآفرین",
        "B.Karafarin" to "بانک کارآفرین",
        "200057780" to "بانک کارآفرین",
        "200004321" to "بانک کارآفرین",

        "98500050004940" to "پست بانک",
        "POSTBANK" to "پست بانک",

        "9820004861" to "بانک خاورمیانه",
        "KH M BANK" to "بانک خاورمیانه",
        "20004860" to "بانک خاورمیانه",

        "989830008528" to "بانک قرض الحسنه مهر ایران",
        "989810008528" to "بانک قرض الحسنه مهر ایران",
        "B.QMEHRIRAN" to "بانک قرض الحسنه مهر ایران",

        "989810008700" to "بانک ملی",
        "98700717" to "بانک ملی",
        "98300" to "بانک ملی",
        "Bank Melli" to "بانک ملی",
        "300017" to "بانک ملی",

        "981000214" to "بانک پاسارگاد",
        "985000114" to "بانک پاسارگاد",
        "B.Pasargad" to "بانک پاسارگاد",

        "9850009200" to "بانک ملت",
        "Bank Mellat" to "بانک ملت",
        "2000333" to "بانک ملت",

        "98300081301" to "بانک کشاورزی",
        "985000181301" to "بانک کشاورزی",
        "KESHAVARZI" to "بانک کشاورزی",
        "2000911" to "بانک کشاورزی",
        "200093" to "بانک کشاورزی",

        // Explicit legacy identifiers already present in the project.
        "300066" to "بانک رفاه کارگران",
        "300044" to "بانک رفاه کارگران",
        "300054" to "بانک پارسیان",
        "50001099" to "بانک پارسیان",
        "300055" to "بانک پارسیان",
        "30007" to "بانک پارسیان",
        "1000900" to "بانک پاسارگاد",
        "50009000" to "بانک پاسارگاد",
        "300028" to "بانک سینا",
        "989999987641" to "بلو (بانک آینده)",
        "9999987641" to "بلو (بانک آینده)",
        "V.Maskan" to "بانک مسکن",
        "V.Refah" to "بانک رفاه کارگران",
    )

    /**
     * Exact lookup after safe sender normalization.
     * No prefix, suffix, or wildcard matching is performed.
     */
    fun lookup(sender: String?): String? {
        val normalized = normalizeSender(sender) ?: return null

        return senderToBank.entries.firstOrNull {
            normalizeSender(it.key) == normalized
        }?.value
    }

    private fun normalizeSender(sender: String?): String? {
        if (sender.isNullOrBlank()) return null

        val normalizedDigits = sender
            .trim()
            .map { char ->
                when {
                    char in '۰'..'۹' -> ('0'.code + (char.code - '۰'.code)).toChar()
                    char in '٠'..'٩' -> ('0'.code + (char.code - '٠'.code)).toChar()
                    else -> char
                }
            }
            .joinToString("")

        val compact = normalizedDigits
            .replace(Regex("[\\s\\-\\u2010\\u2011\\u2012\\u2013\\u2014]+"), "")
            .replace("+", "")

        val numeric = compact.let {
            when {
                it.startsWith("0098") -> it.removePrefix("0098")
                it.startsWith("98") -> it.removePrefix("98")
                else -> it
            }
        }

        return if (numeric.isNotEmpty() && numeric.all { it in '0'..'9' }) {
            numeric
        } else {
            normalizedDigits.trim().lowercase()
        }
    }
}
