package com.example.walletapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles
import com.example.walletapp.data.TransactionSyncState

/**
 * Reusable Sync status card.
 *
 * This component only renders state and reports the user's
 * request to synchronize. It contains no financial logic.
 */
@Composable
fun TransactionSyncCard(
    state: TransactionSyncState,
    pendingCount: Int,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    AppCard(modifier = modifier) {

        Column(
            modifier = Modifier.padding(AppDimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.ItemGap)
        ) {

            Text(
                text = "همگام‌سازی",
                style = AppTextStyles.titleMd
            )

            when (state) {

                is TransactionSyncState.Idle -> {
                    Text(
                        text =
                            if (pendingCount > 0) {
                                "$pendingCount تراکنش در انتظار همگام‌سازی"
                            } else {
                                "همه تراکنش‌ها همگام هستند"
                            }
                    )
                }

                is TransactionSyncState.Syncing -> {
                    Text(
                        text =
                            "در حال همگام‌سازی..."
                    )
                }

                is TransactionSyncState.Success -> {
                    Text(
                        text =
                            "همگام‌سازی با موفقیت انجام شد"
                    )

                    Text(
                        text =
                            "ارسال: ${state.uploaded}  |  دریافت: ${state.downloaded}"
                    )
                }

                is TransactionSyncState.Failed -> {
                    Text(
                        text =
                            "همگام‌سازی ناموفق بود"
                    )

                    Text(
                        text = state.message
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                AppPrimaryButton(
                    text = "همگام‌سازی",
                    onClick = onSyncClick,
                    enabled =
                        state !is TransactionSyncState.Syncing
                )
            }
        }
    }
}
