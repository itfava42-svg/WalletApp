package com.example.walletapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletapp.data.JalaliCalendar
import com.example.walletapp.data.LoanEntry
import com.example.walletapp.data.LoanPayment
import com.example.walletapp.data.LoanStore
import com.example.walletapp.data.LoanType
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.loan.MatureColors
import java.util.Calendar
import java.util.UUID
import kotlin.math.pow

private val LoanOrange = MatureColors.Secondary
private val LoanOrangeLight = MatureColors.SecondaryLight
private val LoanGreen = MatureColors.Success
private val LoanGreenLight = MatureColors.Surface
private val LoanBlue = MatureColors.PrimaryLight
private val LoanRed = MatureColors.PrimaryDark

@Composable
fun LoanManagementScreen(
    onBack: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    var editingLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var deleteLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var installmentsLoan by remember { mutableStateOf<LoanEntry?>(null) }

    val totalLoans = LoanStore.loans.size

    val totalLoanAmount =
        LoanStore.loans.sumOf { it.totalAmount }

    val totalPaidAmount =
        LoanStore.loans.sumOf { loan ->
            loan.payments.sumOf { it.amount }
        }

    val totalScheduledAmount =
        LoanStore.loans.sumOf { loan ->
            loan.installmentAmount *
                loan.totalInstallments.toLong()
        }

    val totalRemainingAmount =
        (totalScheduledAmount - totalPaidAmount)
            .coerceAtLeast(0L)

    val totalPaidInstallments =
        LoanStore.loans.sumOf { loan ->
            loan.payments.size
                .coerceAtMost(loan.totalInstallments)
        }

    val totalInstallments =
        LoanStore.loans.sumOf {
            it.totalInstallments
        }

    Scaffold(
        containerColor = MatureColors.Surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingLoan = null
                    showEditor = true
                },
                containerColor = LoanOrange,
                contentColor = MatureColors.PrimaryDark,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "افزودن وام",
                    modifier = Modifier.size(27.dp)
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                LoanHeader(
                    totalLoans = totalLoans,
                    totalRemainingAmount = totalRemainingAmount,
                    onBack = onBack
                )
            }

            if (LoanStore.loans.isNotEmpty()) {

                item {
                    LoanSectionTitle(
                        title = "خلاصه وضعیت",
                        subtitle = "نمای کلی تعهدات مالی"
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LoanSummaryTile(
                            modifier = Modifier.weight(1f),
                            title = "مجموع وام",
                            value = totalLoanAmount.toPersianCurrency(),
                            icon = Icons.Filled.Payments,
                            background = MatureColors.SecondaryLight,
                            iconTint = MatureColors.Primary
                        )

                        LoanSummaryTile(
                            modifier = Modifier.weight(1f),
                            title = "مانده بدهی",
                            value = totalRemainingAmount.toPersianCurrency(),
                            icon = Icons.Filled.AccountBalance,
                            background = MatureColors.Surface,
                            iconTint = MatureColors.PrimaryDark
                        )
                    }
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MatureColors.CardBackground
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(13.dp),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            LoanMiniSummary(
                                modifier = Modifier.weight(1f),
                                title = "کل اقساط",
                                value = totalInstallments.toString()
                            )

                            LoanMiniSummary(
                                modifier = Modifier.weight(1f),
                                title = "پرداخت‌شده",
                                value = totalPaidInstallments.toString()
                            )

                            LoanMiniSummary(
                                modifier = Modifier.weight(1f),
                                title = "باقی‌مانده",
                                value = (
                                    totalInstallments -
                                        totalPaidInstallments
                                    ).coerceAtLeast(0).toString()
                            )
                        }
                    }
                }

                item {
                    LoanSectionTitle(
                        title = "فهرست وام‌ها",
                        subtitle = "برای مشاهده جزئیات هر وام انتخاب کنید"
                    )
                }

                items(
                    items = LoanStore.loans,
                    key = { it.id }
                ) { loan ->
                    LoanCard(
                        loan = loan,
                        onEdit = {
                            editingLoan = loan
                            showEditor = true
                        },
                        onDelete = {
                            deleteLoan = loan
                        },
                        onInstallments = {
                            installmentsLoan = loan
                        }
                    )
                }

            } else {

                item {
                    EmptyLoanState(
                        onAdd = {
                            editingLoan = null
                            showEditor = true
                        }
                    )
                }
            }
        }
    }

    if (showEditor) {
        LoanEditorDialog(
            existingLoan = editingLoan,
            onDismiss = {
                showEditor = false
                editingLoan = null
            },
            onSave = { loan ->
                if (editingLoan == null) {
                    LoanStore.add(loan)
                } else {
                    LoanStore.update(loan)
                }

                showEditor = false
                editingLoan = null
            }
        )
    }

    installmentsLoan?.let { loan ->
        LoanInstallmentsDialog(
            loan = loan,
            onDismiss = {
                installmentsLoan = null
            }
        )
    }

    deleteLoan?.let { loan ->
        AlertDialog(
            onDismissRequest = {
                deleteLoan = null
            },
            title = {
                Text(
                    text = "حذف وام",
                    fontWeight = FontWeight.Bold,
                    color = MatureColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "آیا از حذف وام «${loan.title}» مطمئن هستید؟",
                    color = MatureColors.TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        LoanStore.remove(loan.id)
                        deleteLoan = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatureColors.PrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        deleteLoan = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun LoanHeader(
    totalLoans: Int,
    totalRemainingAmount: Long,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(27.dp),
        color = MatureColors.Primary
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MatureColors.PrimaryGradient)
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        color = Color.White.copy(alpha = 0.12f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = MatureColors.Secondary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(Modifier.size(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "مدیریت وام‌ها",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(Modifier.height(3.dp))

                        Text(
                            text = "مدیریت و پیگیری تعهدات مالی",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.76f)
                        )
                    }

                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp),
                    color = Color.White.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "تعداد وام",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.68f)
                            )

                            Text(
                                text = totalLoans.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(
                            modifier = Modifier.weight(2f)
                        ) {
                            Text(
                                text = "مانده تعهدات",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.68f)
                            )

                            Text(
                                text = totalRemainingAmount.toPersianCurrency(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatureColors.Secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanSectionTitle(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(
            start = 3.dp,
            end = 3.dp,
            top = 3.dp
        ),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MatureColors.TextPrimary
        )

        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = MatureColors.TextSecondary
        )
    }
}

@Composable
private fun EmptyLoanState(
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        color = MatureColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(23.dp),
                color = MatureColors.SecondaryLight
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = MatureColors.Primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Text(
                text = "هنوز وامی ثبت نشده است",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MatureColors.TextPrimary
            )

            Text(
                text = "برای شروع، اولین وام خود را ثبت کنید.",
                fontSize = 13.sp,
                color = MatureColors.TextSecondary
            )

            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatureColors.Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.size(7.dp))

                Text(
                    text = "ثبت وام جدید",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LoanCard(
    loan: LoanEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onInstallments: () -> Unit
) {
    val paidCount =
        loan.payments.size.coerceAtMost(loan.totalInstallments)

    val remainingCount =
        (loan.totalInstallments - paidCount)
            .coerceAtLeast(0)

    val paidAmount =
        loan.payments.sumOf { it.amount }

    val totalScheduledAmount =
        loan.installmentAmount *
            loan.totalInstallments.toLong()

    val remainingAmount =
        (totalScheduledAmount - paidAmount)
            .coerceAtLeast(0L)

    val progress =
        if (loan.totalInstallments > 0) {
            paidCount.toFloat() /
                loan.totalInstallments.toFloat()
        } else {
            0f
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MatureColors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = MatureColors.SecondaryLight
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalance,
                            contentDescription = null,
                            tint = MatureColors.Primary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                Spacer(Modifier.size(11.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = loan.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MatureColors.TextPrimary
                    )

                    Text(
                        text = loan.type.label,
                        fontSize = 12.sp,
                        color = MatureColors.TextSecondary
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "ویرایش",
                        tint = MatureColors.Primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "حذف",
                        tint = MatureColors.PrimaryDark
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                LoanInfoTile(
                    modifier = Modifier.weight(1f),
                    title = "مبلغ وام",
                    value = loan.totalAmount.toPersianCurrency(),
                    background = MatureColors.SecondaryLight,
                    icon = Icons.Filled.Payments,
                    iconTint = MatureColors.Primary
                )

                LoanInfoTile(
                    modifier = Modifier.weight(1f),
                    title = "مبلغ هر قسط",
                    value = loan.installmentAmount.toPersianCurrency(),
                    background = MatureColors.Surface,
                    icon = Icons.Filled.Schedule,
                    iconTint = MatureColors.Success
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LoanDetailChip(
                    modifier = Modifier.weight(1f),
                    title = "نرخ سود",
                    value = "${loan.interestRatePercent}٪",
                    icon = Icons.Filled.Percent
                )

                LoanDetailChip(
                    modifier = Modifier.weight(1f),
                    title = "کل اقساط",
                    value = loan.totalInstallments.toString(),
                    icon = Icons.Filled.Schedule
                )

                LoanDetailChip(
                    modifier = Modifier.weight(1f),
                    title = "باقی",
                    value = remainingCount.toString(),
                    icon = Icons.Filled.CalendarMonth
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "پیشرفت بازپرداخت",
                        fontSize = 12.sp,
                        color = MatureColors.TextSecondary
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "$paidCount از ${loan.totalInstallments} قسط",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MatureColors.Primary
                    )
                }

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = MatureColors.Success,
                    trackColor = MatureColors.SecondaryLight
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(17.dp),
                color = if (remainingCount == 0) {
                    MatureColors.Surface
                } else {
                    MatureColors.SecondaryLight.copy(alpha = 0.70f)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 13.dp,
                            vertical = 11.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (remainingCount == 0) {
                                "وضعیت وام"
                            } else {
                                "مانده بدهی"
                            },
                            fontSize = 11.sp,
                            color = MatureColors.TextSecondary
                        )

                        Spacer(Modifier.height(3.dp))

                        Text(
                            text = if (remainingCount == 0) {
                                "تسویه شده"
                            } else {
                                remainingAmount.toPersianCurrency()
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingCount == 0) {
                                MatureColors.Success
                            } else {
                                MatureColors.Primary
                            }
                        )
                    }

                    if (remainingCount == 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MatureColors.Success
                        ) {
                            Text(
                                text = "✓ تسویه",
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onInstallments,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(Modifier.size(8.dp))

                Text(
                    text = "مشاهده و مدیریت اقساط",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LoanInfoTile(
    modifier: Modifier,
    title: String,
    value: String,
    background: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(23.dp)
            )

            Text(
                text = title,
                fontSize = 11.sp,
                color = MatureColors.TextSecondary
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MatureColors.TextPrimary
            )
        }
    }
}

@Composable
private fun LoanSummaryTile(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    background: Color,
    iconTint: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(23.dp)
            )

            Text(
                text = title,
                fontSize = 11.sp,
                color = MatureColors.TextSecondary
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MatureColors.TextPrimary
            )
        }
    }
}

@Composable
private fun LoanMiniSummary(
    modifier: Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MatureColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = MatureColors.TextSecondary
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MatureColors.Primary
            )
        }
    }
}

@Composable
private fun LoanDetailChip(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MatureColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MatureColors.PrimaryLight,
                modifier = Modifier.size(17.dp)
            )

            Text(
                text = title,
                fontSize = 10.sp,
                color = MatureColors.TextSecondary
            )

            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MatureColors.TextPrimary
            )
        }
    }
}

@Composable
private fun LoanInstallmentsDialog(
    loan: LoanEntry,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AccountStore.attachContext(context)
    TransactionStore.attachContext(context)

    var selectedInstallmentNumber by remember {
        mutableStateOf<Int?>(null)
    }

    var selectedAccountId by remember {
        mutableStateOf<String?>(null)
    }

    val activeAccounts =
        AccountStore.accounts.filter { it.isActive }

    val currentLoan =
        LoanStore.getById(loan.id) ?: loan

    val firstYear =
        currentLoan.firstInstallmentYear

    val firstMonth =
        currentLoan.firstInstallmentMonth

    val firstDay =
        currentLoan.firstInstallmentDay

    val paidInstallments =
        currentLoan.payments
            .map { it.installmentNumber }
            .filter { it > 0 }
            .toSet()

    val paidCount =
        currentLoan.payments.size
            .coerceAtMost(currentLoan.totalInstallments)

    val remainingCount =
        (currentLoan.totalInstallments - paidCount)
            .coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "مدیریت اقساط",
                    fontWeight = FontWeight.Bold,
                    color = MatureColors.TextPrimary
                )

                Text(
                    text = currentLoan.title,
                    fontSize = 13.sp,
                    color = MatureColors.TextSecondary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(17.dp),
                    color = MatureColors.Surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallLoanInfo(
                            modifier = Modifier.weight(1f),
                            title = "کل اقساط",
                            value =
                                currentLoan.totalInstallments
                                    .toString()
                        )

                        SmallLoanInfo(
                            modifier = Modifier.weight(1f),
                            title = "پرداخت‌شده",
                            value = paidCount.toString()
                        )

                        SmallLoanInfo(
                            modifier = Modifier.weight(1f),
                            title = "باقی‌مانده",
                            value = remainingCount.toString()
                        )
                    }
                }

                if (
                    firstYear == null ||
                    firstMonth == null ||
                    firstDay == null
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MatureColors.SecondaryLight
                    ) {
                        Text(
                            text = "تاریخ اولین قسط برای این وام ثبت نشده است. برای نمایش سررسید اقساط، ابتدا تاریخ اولین قسط را در ویرایش وام وارد کنید.",
                            modifier = Modifier.padding(14.dp),
                            fontSize = 13.sp,
                            color = MatureColors.TextPrimary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(430.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = (
                                1..currentLoan.totalInstallments
                            ).toList(),
                            key = { it }
                        ) { number ->

                            val isPaid =
                                paidInstallments
                                    .contains(number)

                            val dueDate =
                                calculateLoanInstallmentDate(
                                    year = firstYear,
                                    month = firstMonth,
                                    day = firstDay,
                                    monthsToAdd =
                                        (number - 1) *
                                            currentLoan.intervalMonths
                                )

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isPaid) {
                                    MatureColors.Surface
                                } else {
                                    MatureColors.CardBackground
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isPaid) {
                                            MatureColors.Success
                                        } else {
                                            MatureColors.SecondaryLight
                                        }
                                    ) {
                                        Box(
                                            contentAlignment =
                                                Alignment.Center
                                        ) {
                                            Text(
                                                text = number.toString(),
                                                fontWeight =
                                                    FontWeight.Bold,
                                                color = if (isPaid) {
                                                    Color.White
                                                } else {
                                                    MatureColors.Primary
                                                }
                                            )
                                        }
                                    }

                                    Spacer(Modifier.size(10.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement =
                                            Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "قسط شماره $number",
                                            fontWeight =
                                                FontWeight.Bold,
                                            color =
                                                MatureColors.TextPrimary
                                        )

                                        Text(
                                            text = "سررسید: $dueDate",
                                            fontSize = 12.sp,
                                            color =
                                                MatureColors.TextSecondary
                                        )

                                        Text(
                                            text =
                                                currentLoan
                                                    .installmentAmount
                                                    .toPersianCurrency(),
                                            fontSize = 13.sp,
                                            fontWeight =
                                                FontWeight.Bold,
                                            color =
                                                MatureColors.Primary
                                        )
                                    }

                                    if (isPaid) {
                                        Text(
                                            text = "پرداخت شده",
                                            fontSize = 11.sp,
                                            fontWeight =
                                                FontWeight.Bold,
                                            color =
                                                MatureColors.Success
                                        )
                                    } else {
                                        Button(
                                            onClick = {
                                                val alreadyPaid =
                                                    LoanStore
                                                        .getById(
                                                            currentLoan.id
                                                        )
                                                        ?.payments
                                                        ?.any {
                                                            it.installmentNumber ==
                                                                number
                                                        } == true

                                                if (!alreadyPaid) {
                                                    selectedInstallmentNumber = number
                                                    selectedAccountId = null
                                                }
                                            },
                                            colors =
                                                ButtonDefaults.buttonColors(
                                                    containerColor =
                                                        MatureColors.Primary
                                                ),
                                            shape =
                                                RoundedCornerShape(11.dp)
                                        ) {
                                            Text(
                                                text = "ثبت پرداخت",
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("بستن")
            }
        }
    )

    selectedInstallmentNumber?.let { installmentNumber ->

        AlertDialog(
            onDismissRequest = {
                selectedInstallmentNumber = null
                selectedAccountId = null
            },
            title = {
                Text(
                    text = "انتخاب حساب پرداخت",
                    fontWeight = FontWeight.Bold,
                    color = MatureColors.TextPrimary
                )
            },
            text = {
                if (activeAccounts.isEmpty()) {

                    Text(
                        text = "هیچ حساب فعالی برای ثبت پرداخت وجود ندارد.",
                        color = MatureColors.TextSecondary
                    )

                } else {

                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(6.dp)
                    ) {

                        activeAccounts.forEach { account ->

                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            selectedAccountId =
                                                account.id
                                        }
                                        .padding(8.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                RadioButton(
                                    selected =
                                        selectedAccountId ==
                                            account.id,
                                    onClick = {
                                        selectedAccountId =
                                            account.id
                                    }
                                )

                                Spacer(
                                    Modifier.size(8.dp)
                                )

                                Column {

                                    Text(
                                        text = account.name,
                                        fontWeight =
                                            FontWeight.Bold,
                                        color =
                                            MatureColors.TextPrimary
                                    )

                                    if (
                                        account.bankName.isNotBlank()
                                    ) {
                                        Text(
                                            text = account.bankName,
                                            fontSize = 11.sp,
                                            color =
                                                MatureColors.TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {

                Button(
                    onClick = {

                        val accountId =
                            selectedAccountId
                                ?: return@Button

                        val alreadyPaid =
                            LoanStore
                                .getById(currentLoan.id)
                                ?.payments
                                ?.any {
                                    it.installmentNumber ==
                                        installmentNumber
                                } == true

                        if (!alreadyPaid) {

                            val now =
                                System.currentTimeMillis()

                            val today =
                                JalaliCalendar
                                    .fromEpochMillisSlashed(
                                        now
                                    )

                            val transaction =
                                Transaction(
                                    id =
                                        UUID.randomUUID()
                                            .toString(),
                                    title =
                                        "پرداخت قسط ${currentLoan.title}",
                                    subtitle =
                                        "قسط شماره $installmentNumber",
                                    amount =
                                        currentLoan.installmentAmount,
                                    type =
                                        TransactionType.EXPENSE,
                                    iconKey = "وام",
                                    category = "وام",
                                    dateEpochMillis = now,
                                    note = "",
                                    description =
                                        "پرداخت قسط ${currentLoan.title}",
                                    accountId =
                                        accountId
                                )

                            TransactionStore.addManual(
                                transaction
                            )

                            LoanStore.addPayment(
                                loanId =
                                    currentLoan.id,
                                payment =
                                    LoanPayment(
                                        id =
                                            UUID.randomUUID()
                                                .toString(),
                                        installmentNumber =
                                            installmentNumber,
                                        amount =
                                            currentLoan
                                                .installmentAmount,
                                        dateLabel =
                                            today,
                                        transactionId =
                                            transaction.id
                                    )
                            )

                            selectedInstallmentNumber = null
                            selectedAccountId = null
                        }
                    },
                    enabled =
                        selectedAccountId != null &&
                            activeAccounts.isNotEmpty(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                MatureColors.Primary
                        ),
                    shape =
                        RoundedCornerShape(11.dp)
                ) {
                    Text(
                        text = "ثبت پرداخت",
                        fontSize = 13.sp
                    )
                }
            },
            dismissButton = {

                OutlinedButton(
                    onClick = {
                        selectedInstallmentNumber = null
                        selectedAccountId = null
                    },
                    shape =
                        RoundedCornerShape(11.dp)
                ) {
                    Text("انصراف")
                }
            }
        )
    }}

private fun calculateLoanInstallmentDate(
    year: Int,
    month: Int,
    day: Int,
    monthsToAdd: Int
): String {
    val totalMonths =
        (year * 12) +
            (month - 1) +
            monthsToAdd

    val newYear =
        totalMonths / 12

    val newMonth =
        (totalMonths % 12) + 1

    val maxDay =
        if (newMonth <= 6) {
            31
        } else {
            30
        }

    val newDay =
        day.coerceAtMost(maxDay)

    return "$newYear/${newMonth.toString().padStart(2, '0')}/${newDay.toString().padStart(2, '0')}"
}

@Composable
private fun LoanEditorDialog(
    existingLoan: LoanEntry?,
    onDismiss: () -> Unit,
    onSave: (LoanEntry) -> Unit
) {
    val context = LocalContext.current

    var title by remember {
        mutableStateOf(existingLoan?.title ?: "")
    }

    var loanType by remember {
        mutableStateOf(
            existingLoan?.type ?: LoanType.PERSONAL
        )
    }

    var amountText by remember {
        mutableStateOf(
            existingLoan?.totalAmount?.toString() ?: ""
        )
    }

    var interestText by remember {
        mutableStateOf(
            existingLoan?.interestRatePercent?.toString() ?: ""
        )
    }

    var installmentText by remember {
        mutableStateOf(
            existingLoan?.installmentAmount?.toString() ?: ""
        )
    }

    var totalInstallmentsText by remember {
        mutableStateOf(
            existingLoan?.totalInstallments?.toString() ?: ""
        )
    }

    var intervalMonths by remember {
        mutableStateOf(
            existingLoan?.intervalMonths ?: 1
        )
    }

    var startDate by remember {
        mutableStateOf(
            existingLoan?.let {
                "${it.startYear}/${it.startMonth.toString().padStart(2, '0')}/${it.startDay.toString().padStart(2, '0')}"
            } ?: JalaliCalendar.todaySlashed()
        )
    }

    var firstInstallmentDate by remember {
        mutableStateOf(
            existingLoan?.let {
                if (
                    it.firstInstallmentYear != null &&
                    it.firstInstallmentMonth != null &&
                    it.firstInstallmentDay != null
                ) {
                    "${it.firstInstallmentYear}/${it.firstInstallmentMonth.toString().padStart(2, '0')}/${it.firstInstallmentDay.toString().padStart(2, '0')}"
                } else {
                    ""
                }
            } ?: ""
        )
    }

    var guaranteeNote by remember {
        mutableStateOf(
            existingLoan?.guaranteeNote ?: ""
        )
    }

    var reminderEnabled by remember {
        mutableStateOf(
            existingLoan?.reminderEnabled ?: false
        )
    }

    var expandedType by remember {
        mutableStateOf(false)
    }

    var expandedInterval by remember {
        mutableStateOf(false)
    }

    val calculatedInstallment =
        remember(
            amountText,
            interestText,
            totalInstallmentsText
        ) {
            calculateMonthlyInstallment(
                principal =
                    amountText.toLongOrNull() ?: 0L,
                annualInterestPercent =
                    interestText.toDoubleOrNull() ?: 0.0,
                numberOfPayments =
                    totalInstallmentsText.toIntOrNull() ?: 0
            )
        }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (existingLoan == null) {
                        "ثبت وام جدید"
                    } else {
                        "ویرایش وام"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatureColors.TextPrimary
                )

                Text(
                    text = "اطلاعات وام را وارد کنید",
                    fontSize = 12.sp,
                    color = MatureColors.TextSecondary
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    LoanEditorSectionTitle(
                        title = "اطلاعات وام"
                    )
                }

                item {
                    LoanInputTile(
                        title = "نام وام",
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        background = MatureColors.Surface,
                        accent = MatureColors.Primary,
                        icon = Icons.Filled.AccountBalance,
                        keyboardType = KeyboardType.Text
                    )
                }

                item {
                    LoanInputTile(
                        title = "مبلغ وام",
                        value = amountText,
                        onValueChange = {
                            amountText =
                                it.filter { c -> c.isDigit() }
                        },
                        background = LoanOrangeLight,
                        accent = LoanOrange,
                        icon = Icons.Filled.Payments
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(9.dp)
                    ) {
                        LoanInputTile(
                            modifier = Modifier.weight(1f),
                            title = "درصد سود",
                            value = interestText,
                            onValueChange = {
                                interestText = it
                            },
                            background = LoanGreenLight,
                            accent = LoanGreen,
                            icon = Icons.Filled.Percent,
                            keyboardType = KeyboardType.Decimal
                        )

                        LoanInputTile(
                            modifier = Modifier.weight(1f),
                            title = "تعداد اقساط",
                            value = totalInstallmentsText,
                            onValueChange = {
                                totalInstallmentsText =
                                    it.filter { c ->
                                        c.isDigit()
                                    }
                            },
                            background = MatureColors.Surface,
                            accent = MatureColors.Primary,
                            icon = Icons.Filled.Schedule
                        )
                    }
                }

                item {
                    LoanEditorSectionTitle(
                        title = "تنظیمات اقساط"
                    )
                }

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = "هر $intervalMonths ماه",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            label = {
                                Text("فاصله اقساط")
                            }
                        )

                        DropdownMenu(
                            expanded = expandedInterval,
                            onDismissRequest = {
                                expandedInterval = false
                            }
                        ) {
                            listOf(
                                1,
                                2,
                                3,
                                6,
                                12
                            ).forEach { months ->
                                DropdownMenuItem(
                                    text = {
                                        Text("هر $months ماه")
                                    },
                                    onClick = {
                                        intervalMonths = months
                                        expandedInterval = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    LoanInputTile(
                        title = "قسط ماهیانه محاسبه‌شده",
                        value =
                            if (calculatedInstallment > 0) {
                                calculatedInstallment
                                    .toPersianCurrency()
                            } else {
                                "—"
                            },
                        onValueChange = {},
                        background = LoanGreenLight,
                        accent = LoanGreen,
                        icon = Icons.Filled.Payments,
                        readOnly = true,
                        keyboardType = KeyboardType.Number
                    )
                }

                item {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = loanType.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            label = {
                                Text("نوع وام")
                            }
                        )

                        DropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = {
                                expandedType = false
                            }
                        ) {
                            LoanType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(type.label)
                                    },
                                    onClick = {
                                        loanType = type
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    LoanEditorSectionTitle(
                        title = "تاریخ‌ها"
                    )
                }

                item {
                    DateField(
                        label = "تاریخ وام",
                        value = startDate,
                        onDateSelected = {
                            startDate = it
                        },
                        context = context
                    )
                }

                item {
                    DateField(
                        label = "تاریخ اولین قسط",
                        value = firstInstallmentDate,
                        onDateSelected = {
                            firstInstallmentDate = it
                        },
                        context = context
                    )
                }

                item {
                    LoanEditorSectionTitle(
                        title = "ضمانت و توضیحات"
                    )
                }

                item {
                    OutlinedTextField(
                        value = guaranteeNote,
                        onValueChange = {
                            guaranteeNote = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(14.dp),
                        label = {
                            Text("ضمانت و توضیحات")
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount =
                        amountText.toLongOrNull() ?: 0L

                    val interest =
                        interestText.toDoubleOrNull() ?: 0.0

                    val installments =
                        totalInstallmentsText
                            .toIntOrNull() ?: 0

                    val startParts =
                        parseJalaliDate(startDate)

                    val firstParts =
                        parseJalaliDate(
                            firstInstallmentDate
                        )

                    if (
                        title.isBlank() ||
                        amount <= 0 ||
                        installments <= 0 ||
                        startParts == null
                    ) {
                        return@Button
                    }

                    val finalInstallment =
                        if (calculatedInstallment > 0) {
                            calculatedInstallment
                        } else {
                            installmentText
                                .toLongOrNull() ?: 0L
                        }

                    val loan =
                        LoanEntry(
                            id =
                                existingLoan?.id
                                    ?: UUID.randomUUID()
                                        .toString(),
                            title = title.trim(),
                            type = loanType,
                            totalAmount = amount,
                            interestRatePercent = interest,
                            installmentAmount =
                                finalInstallment,
                            totalInstallments =
                                installments,
                            intervalMonths =
                                intervalMonths,
                            startYear =
                                startParts[0],
                            startMonth =
                                startParts[1],
                            startDay =
                                startParts[2],
                            firstInstallmentYear =
                                firstParts?.get(0),
                            firstInstallmentMonth =
                                firstParts?.get(1),
                            firstInstallmentDay =
                                firstParts?.get(2),
                            guaranteeNote =
                                guaranteeNote.trim(),
                            reminderEnabled =
                                reminderEnabled,
                            payments =
                                existingLoan?.payments
                                    ?: emptyList()
                        )

                    onSave(loan)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatureColors.Primary
                )
            ) {
                Text(
                    if (existingLoan == null) {
                        "ثبت وام"
                    } else {
                        "ذخیره تغییرات"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("انصراف")
            }
        }
    )
}

@Composable
private fun LoanEditorSectionTitle(
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 3.dp,
                bottom = 1.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(7.dp),
            shape = RoundedCornerShape(10.dp),
            color = MatureColors.Secondary
        ) {}

        Spacer(Modifier.size(8.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MatureColors.Primary
        )
    }
}

@Composable
private fun LoanInputTile(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    background: Color,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(21.dp)
                )

                Spacer(Modifier.size(7.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatureColors.TextPrimary
                )
            }

            Spacer(Modifier.height(7.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                readOnly = readOnly,
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType
                ),
                placeholder = {
                    Text("وارد کنید")
                }
            )
        }
    }
}

@Composable
private fun SmallLoanInfo(
    modifier: Modifier,
    title: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MatureColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = MatureColors.TextSecondary
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MatureColors.TextPrimary
            )
        }
    }
}

@Composable
private fun DateField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    context: android.content.Context
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        label = {
            Text(label)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MatureColors.Primary
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    val calendar =
                        Calendar.getInstance()

                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val selected =
                                Calendar.getInstance()
                                    .apply {
                                        set(
                                            year,
                                            month,
                                            day,
                                            12,
                                            0,
                                            0
                                        )

                                        set(
                                            Calendar.MILLISECOND,
                                            0
                                        )
                                    }

                            onDateSelected(
                                JalaliCalendar
                                    .fromEpochMillisSlashed(
                                        selected.timeInMillis
                                    )
                            )
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Icon(
                    imageVector =
                        Icons.Filled.CalendarMonth,
                    contentDescription =
                        "انتخاب تاریخ",
                    tint = MatureColors.Primary
                )
            }
        }
    )
}

private fun parseJalaliDate(
    value: String
): IntArray? {
    val parts = value.split("/")

    if (parts.size != 3) {
        return null
    }

    val year =
        parts[0].toIntOrNull()
            ?: return null

    val month =
        parts[1].toIntOrNull()
            ?: return null

    val day =
        parts[2].toIntOrNull()
            ?: return null

    if (
        month !in 1..12 ||
        day !in 1..31
    ) {
        return null
    }

    return intArrayOf(
        year,
        month,
        day
    )
}

private fun calculateMonthlyInstallment(
    principal: Long,
    annualInterestPercent: Double,
    numberOfPayments: Int
): Long {
    if (
        principal <= 0L ||
        numberOfPayments <= 0
    ) {
        return 0L
    }

    if (annualInterestPercent <= 0.0) {
        return principal / numberOfPayments
    }

    val monthlyRate =
        annualInterestPercent /
            100.0 /
            12.0

    val power =
        (1.0 + monthlyRate)
            .pow(numberOfPayments)

    val payment =
        principal *
            (monthlyRate * power) /
            (power - 1.0)

    return payment.toLong()
}





