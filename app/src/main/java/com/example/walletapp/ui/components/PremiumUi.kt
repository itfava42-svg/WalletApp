package com.example.walletapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles

/**
 * Compatibility facade for older premium/dashboard call sites.
 *
 * CI-170 keeps the public surface of this helper while routing its rendered
 * UI through the shared WalletApp design system. New screens should prefer
 * AppCard, AppStatCard, AppTextStyles and AppDimens directly.
 */
object PremiumUi {

    // Kept for source compatibility with older callers; rendered UI uses the
    // active Material 3 theme instead of these legacy palette values.
    val Primary = androidx.compose.ui.graphics.Color(0xFF008F83)
    val PrimaryDark = androidx.compose.ui.graphics.Color(0xFF006E67)
    val Income = androidx.compose.ui.graphics.Color(0xFF00A878)
    val Expense = androidx.compose.ui.graphics.Color(0xFFE85D68)
    val Transfer = androidx.compose.ui.graphics.Color(0xFF3D8BDE)
    val Surface = androidx.compose.ui.graphics.Color(0xFFF8FAFB)
    val Card = androidx.compose.ui.graphics.Color.White
    val Border = androidx.compose.ui.graphics.Color(0xFFE6ECEE)

    val ScreenPadding = AppDimens.PagePadding
    val CardRadius = AppDimens.CardRadius
    val SmallRadius = AppDimens.SmallCardRadius
    val SectionGap = AppDimens.SectionGap
    val ItemGap = AppDimens.ItemGap

    @Composable
    fun SectionTitle(
        title: String,
        modifier: Modifier = Modifier,
    ) {
        Text(
            text = title,
            modifier = modifier.fillMaxWidth(),
            style = AppTextStyles.titleMd,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    @Composable
    fun PremiumCard(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        AppCard(modifier = modifier) {
            content()
        }
    }

    @Composable
    fun BalanceCard(
        title: String,
        amount: String,
        subtitle: String? = null,
        modifier: Modifier = Modifier,
    ) {
        AppCard(
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.ItemGap)) {
                Text(
                    text = title,
                    style = AppTextStyles.bodyMd,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = amount,
                    style = AppTextStyles.numeralXl,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = AppTextStyles.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }

    @Composable
    fun StatRow(
        income: String,
        expense: String,
        transfer: String,
        modifier: Modifier = Modifier,
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
        ) {
            StatItem("درآمد", income, modifier = Modifier.weight(1f))
            StatItem("هزینه", expense, modifier = Modifier.weight(1f))
            StatItem("انتقال", transfer, modifier = Modifier.weight(1f))
        }
    }

    @Composable
    private fun StatItem(
        title: String,
        value: String,
        modifier: Modifier = Modifier,
    ) {
        AppCard(
            modifier = modifier,
            contentPadding = PaddingValues(AppDimens.CompactPadding),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppDimens.TightGap)) {
                Text(
                    text = title,
                    style = AppTextStyles.labelMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = AppTextStyles.titleMd,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    @Composable
    fun QuickAction(
        title: String,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(AppDimens.ButtonHeight),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }

            Spacer(Modifier.height(AppDimens.ItemGap))

            Text(
                text = title,
                style = AppTextStyles.labelSmall,
            )
        }
    }
}
