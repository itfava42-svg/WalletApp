package com.example.walletapp.ui.components

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

fun resolveTransactionIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "بانک" -> Icons.Filled.AccountBalance
        "خوراک" -> Icons.Filled.Fastfood
        "حمل‌ونقل" -> Icons.Filled.DirectionsCar
        "قبض", "قسط" -> Icons.Filled.CreditCard
        "خرید" -> Icons.Filled.ShoppingBag
        "سلامت" -> Icons.Filled.LocalHospital
        "تفریح" -> Icons.Filled.SportsEsports
        "حقوق" -> Icons.Filled.AttachMoney
        "هدیه" -> Icons.Filled.CardGiftcard
        "سرمایه‌گذاری" -> Icons.Filled.TrendingUp
        "بازگشت وجه" -> Icons.Filled.Replay
        else -> Icons.Filled.Category
    }
}
