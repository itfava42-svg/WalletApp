package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppIconButton

import androidx.compose.foundation.layout.heightIn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.BorderStroke
import com.example.walletapp.ui.theme.AppDimens
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppOutlinedButton
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.ui.components.AppTextField

private data class AddBankOption(
    val name: String,
    val color: Color,
    val iconType: Int,
)

@Composable
fun AddBankAccountScreen(
    onBack: () -> Unit,
    onSave: (
        bankName: String,
        accountNumber: String,
        cardNumber: String,
        openingBalance: Long,
    ) -> Unit,
) {
    val banks =
        remember {
            listOf(
                AddBankOption("بانک ملی", Color(0xFFB8860B), 1),
                AddBankOption("بانک مهر ایران", Color(0xFF4CAF50), 2),
                AddBankOption("کیف پول", Color(0xFFF5C542), 3),
                AddBankOption("بانک ملت", Color(0xFFD32F2F), 2),
                AddBankOption("بلو بانک", Color(0xFF1E88E5), 2),
                AddBankOption("بانک پاسارگاد", Color(0xFFFFB300), 1),
                AddBankOption("بانک سامان", Color(0xFF1976D2), 2),
                AddBankOption("بانک پارسیان", Color(0xFF5E35B1), 2),
                AddBankOption("بانک کشاورزی", Color(0xFF00897B), 2),
                AddBankOption("بانک سپه", Color(0xFFE65100), 2),
                AddBankOption("بانک صادرات", Color(0xFFC62828), 2),
                AddBankOption("بانک تجارت", Color(0xFF2E7D32), 2),
                AddBankOption("بانک رفاه", Color(0xFF6A1B9A), 2),
                AddBankOption("بانک شهر", Color(0xFF0277BD), 2),
                AddBankOption("بانک مسکن", Color(0xFF37474F), 2),
            )
        }

    var selectedBank by remember { mutableStateOf<String?>(null) }
    var accountNumber by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFE9ECEF)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .verticalScroll(scrollState)
                    .padding(
                        start = 20.dp,
                        top = 20.dp,
                        end = 20.dp,
                        bottom = 100.dp,
                    ),
        ) {

            // ---------------------------------------------------------
            // HEADER
            // ---------------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIconButton(
                    onClick = onBack,
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFF157A37),
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "افزودن حساب بانکی",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                )

                Spacer(modifier = Modifier.weight(1f))

                AppIconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp),
                ) {
                    Text(
                        text = "×",
                        fontSize = 24.sp,
                        color = Color(0xFF555555),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------------------------------------------------
            // BANK SELECTION TITLE
            // ---------------------------------------------------------

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = Color(0xFF157A37),
                    modifier = Modifier.size(18.dp),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "انتخاب بانک",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------------------------------------------------------
            // BANK GRID
            // ---------------------------------------------------------

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFF0F0F0),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .background(
                            color = Color(0xFFFAFAFA),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .padding(6.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                banks.chunked(3).forEach { rowBanks ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        rowBanks.forEach { bank ->

                            BankOptionCard(
                                bank = bank,
                                selected = selectedBank == bank.name,
                                onClick = {
                                    selectedBank = bank.name
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }

                        repeat(3 - rowBanks.size) {
                            Spacer(
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------------------------------------------------
            // ACCOUNT NUMBER
            // ---------------------------------------------------------

            AppTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = "شماره حساب",
                placeholder = { Text("شماره حساب خود را وارد کنید") },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------------------------------------------------------
            // CARD NUMBER
            // ---------------------------------------------------------

            AppTextField(
                value = cardNumber,
                onValueChange = { cardNumber = it },
                modifier = Modifier.fillMaxWidth(),
                label = "شماره کارت (اختیاری)",
                placeholder = { Text("شماره کارت خود را وارد کنید") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------------------------------------------------------
            // BALANCE
            // ---------------------------------------------------------

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFD1E7DD),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .background(
                            Color(0xFFF0FDF4),
                            RoundedCornerShape(16.dp),
                        )
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = Color(0xFF157A37),
                        modifier = Modifier.size(20.dp),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "موجودی",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF157A37),
                    )
                }

                AppTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = "موجودی",
                    placeholder = { Text("۰") },
                    suffix = { Text("ریال") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )

                Text(
                    text =
                        if (balanceText.isBlank()) {
                            "۰ تومان"
                        } else {
                            "۰ تومان"
                        },
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                )
            }
        }

        // -------------------------------------------------------------
        // FOOTER
        // -------------------------------------------------------------

        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFF0F0F0),
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp,
                    ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            AppOutlinedButton(
                text = "انصراف",
                onClick = onBack,
                modifier = Modifier.weight(1f),
            )

            AppPrimaryButton(
                text = "ذخیره",
                onClick = {
                    val balance =
                        balanceText
                            .replace(",", "")
                            .toLongOrNull()
                            ?: 0L

                    onSave(
                        selectedBank ?: "",
                        accountNumber.trim(),
                        cardNumber.trim(),
                        balance,
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BankOptionCard(
    bank: AddBankOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        containerColor = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
        ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.ItemGap),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(bank.color),
                contentAlignment = Alignment.Center,
            ) {
                when (bank.iconType) {
                    1 -> Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    2 -> Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    else -> Icon(Icons.Default.Wallet, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Text(
                text = bank.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}
