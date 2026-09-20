package com.example.walletapp.data


data class TransactionCategory(val label: String, val iconKey: String = label)

object TransactionCategories {
    val expense = listOf(
        TransactionCategory("خوراک", "خوراک"),
        TransactionCategory("حمل‌ونقل", "حمل‌ونقل"),
        TransactionCategory("قبض", "قبض"),
        TransactionCategory("قسط", "قسط"),
        TransactionCategory("خرید", "خرید"),
        TransactionCategory("سلامت", "سلامت"),
        TransactionCategory("تفریح", "تفریح"),
        TransactionCategory("سایر", "سایر"),
    )

    val income = listOf(
        TransactionCategory("حقوق", "حقوق"),
        TransactionCategory("هدیه", "هدیه"),
        TransactionCategory("سرمایه‌گذاری", "سرمایه‌گذاری"),
        TransactionCategory("بازگشت وجه", "بازگشت وجه"),
        TransactionCategory("سایر", "سایر"),
    )
}

/** Provides the standard transaction categories used by the domain model. */


// Keyword -> category label, used to auto-suggest a category from a
// transaction's title as the person types/edits it (e.g. typing
// "سوپرمارکت" suggests "خوراک"). The person can always override the
// suggestion by tapping a different category chip.
private val expenseTitleKeywords: List<Pair<String, String>> = listOf(
    "سوپرمارکت" to "خوراک", "رستوران" to "خوراک", "کافه" to "خوراک", "فست فود" to "خوراک", "نانوایی" to "خوراک",
    "تاکسی" to "حمل‌ونقل", "بنزین" to "حمل‌ونقل", "مترو" to "حمل‌ونقل", "اتوبوس" to "حمل‌ونقل", "اسنپ" to "حمل‌ونقل", "تپسی" to "حمل‌ونقل",
    "برق" to "قبض", "آب" to "قبض", "گاز" to "قبض", "قبض" to "قبض", "اینترنت" to "قبض", "شارژ" to "قبض",
    "قسط" to "قسط", "وام" to "قسط",
    "فروشگاه" to "خرید", "پوشاک" to "خرید", "دیجی‌کالا" to "خرید",
    "دکتر" to "سلامت", "دارو" to "سلامت", "بیمارستان" to "سلامت", "درمانگاه" to "سلامت",
    "سینما" to "تفریح", "کنسرت" to "تفریح", "بازی" to "تفریح",
)

private val incomeTitleKeywords: List<Pair<String, String>> = listOf(
    "حقوق" to "حقوق", "دستمزد" to "حقوق",
    "هدیه" to "هدیه",
    "سود" to "سرمایه‌گذاری", "سرمایه" to "سرمایه‌گذاری", "سهام" to "سرمایه‌گذاری",
    "بازگشت" to "بازگشت وجه", "استرداد" to "بازگشت وجه",
)

/** Best-effort category guess from a transaction title; null if nothing matched. */
fun suggestCategoryFromTitle(title: String, type: TransactionType): String? {
    val keywords = if (type == TransactionType.EXPENSE) expenseTitleKeywords else incomeTitleKeywords
    return keywords.firstOrNull { (kw, _) -> title.contains(kw) }?.second
}

