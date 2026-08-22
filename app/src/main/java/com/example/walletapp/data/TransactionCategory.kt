package com.example.walletapp.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

data class TransactionCategory(val label: String, val icon: ImageVector)

object TransactionCategories {
    val expense = listOf(
        TransactionCategory("خوراک", Icons.Filled.Fastfood),
        TransactionCategory("حمل‌ونقل", Icons.Filled.DirectionsCar),
        TransactionCategory("قبض", Icons.Filled.CreditCard),
        TransactionCategory("قسط", Icons.Filled.CreditCard),
        TransactionCategory("خرید", Icons.Filled.ShoppingBag),
        TransactionCategory("سلامت", Icons.Filled.LocalHospital),
        TransactionCategory("تفریح", Icons.Filled.SportsEsports),
        TransactionCategory("سایر", Icons.Filled.Category),
    )

    val income = listOf(
        TransactionCategory("حقوق", Icons.Filled.AttachMoney),
        TransactionCategory("هدیه", Icons.Filled.CardGiftcard),
        TransactionCategory("سرمایه‌گذاری", Icons.Filled.TrendingUp),
        TransactionCategory("بازگشت وجه", Icons.Filled.Replay),
        TransactionCategory("سایر", Icons.Filled.Category),
    )
}

/** Resolves a Transaction.iconKey (a category label, or "بانک" for SMS-sourced entries) to its icon. */
fun resolveTransactionIcon(iconKey: String): ImageVector {
    if (iconKey == "بانک") return Icons.Filled.AccountBalance
    (TransactionCategories.expense + TransactionCategories.income)
        .firstOrNull { it.label == iconKey }
        ?.let { return it.icon }
    return Icons.Filled.Category
}

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