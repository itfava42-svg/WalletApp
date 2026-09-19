package com.example.walletapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PremiumUi {

    val ScreenPadding = 18.dp
    val CardRadius = 22.dp
    val SmallRadius = 14.dp
    val SectionGap = 18.dp
    val ItemGap = 10.dp

    val Primary = Color(0xFF008F83)
    val PrimaryDark = Color(0xFF006E67)

    val Income = Color(0xFF00A878)
    val Expense = Color(0xFFE85D68)
    val Transfer = Color(0xFF3D8BDE)

    val Surface = Color(0xFFF8FAFB)
    val Card = Color.White
    val Border = Color(0xFFE6ECEE)

    @Composable
    fun SectionTitle(
        title: String,
        modifier: Modifier = Modifier
    ) {
        Text(
            text = title,
            modifier = modifier.fillMaxWidth(),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    @Composable
    fun PremiumCard(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Border,
                    shape = RoundedCornerShape(CardRadius)
                ),
            shape = RoundedCornerShape(CardRadius),
            colors = CardDefaults.cardColors(
                containerColor = Card
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
        ) {
            content()
        }
    }

    @Composable
    fun BalanceCard(
        title: String,
        amount: String,
        subtitle: String? = null,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardRadius))
                .background(Primary)
                .padding(22.dp)
        ) {
            Column {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = .82f),
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = amount,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = .78f),
                        fontSize = 12.sp
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
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ItemGap)
        ) {
            StatItem(
                title = "درآمد",
                value = income,
                color = Income,
                modifier = Modifier.weight(1f)
            )

            StatItem(
                title = "هزینه",
                value = expense,
                color = Expense,
                modifier = Modifier.weight(1f)
            )

            StatItem(
                title = "انتقال",
                value = transfer,
                color = Transfer,
                modifier = Modifier.weight(1f)
            )
        }
    }

    @Composable
    private fun StatItem(
        title: String,
        value: String,
        color: Color,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(SmallRadius),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = .08f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(13.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = color
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    @Composable
    fun QuickAction(
        title: String,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Primary.copy(alpha = .10f)),
                contentAlignment = Alignment.Center
            ) {
                content()
            }

            Spacer(Modifier.height(7.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
