package com.example.walletapp.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletapp.data.JalaliCalendar
import com.example.walletapp.loan.MatureColors
import com.example.walletapp.data.LoanEntry
import com.example.walletapp.data.LoanPayment
import com.example.walletapp.data.LoanStore
import com.example.walletapp.data.LoanType
import com.example.walletapp.data.toPersianCurrency
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlin.math.pow

private val LoanOrange = MatureColors.Secondary
private val LoanOrangeLight = MatureColors.SecondaryLight
private val LoanGreen = MatureColors.Success
private val LoanGreenLight = MatureColors.Surface
private val LoanBlue = MatureColors.PrimaryLight
private val LoanRed = MatureColors.PrimaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanManagementScreen(
    onBack: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    var editingLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var deleteLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var installmentsLoan by remember { mutableStateOf<LoanEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "\u0645\u062f\u06cc\u0631\u06cc\u062a \u0648\u0627\u0645",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "\u0628\u0627\u0632\u06af\u0634\u062a"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingLoan = null
                    showEditor = true
                },
                containerColor = LoanOrange,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "\u0627\u0641\u0632\u0648\u062f\u0646 \u0648\u0627\u0645"
                )
            }
        }
    ) { padding ->

        if (LoanStore.loans.isEmpty()) {
            EmptyLoanState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAdd = {
                    editingLoan = null
                    showEditor = true
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
                    text = "\u062d\u0630\u0641 \u0648\u0627\u0645",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "\u0622\u06cc\u0627 \u0627\u0632 \u062d\u0630\u0641 \u0648\u0627\u0645 \u00ab${loan.title}\u00bb \u0645\u0637\u0645\u0626\u0646 \u0647\u0633\u062a\u06cc\u062f\u061f"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        LoanStore.remove(loan.id)
                        deleteLoan = null
                    }
                ) {
                    Text("\u062d\u0630\u0641")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        deleteLoan = null
                    }
                ) {
                    Text("\u0627\u0646\u0635\u0631\u0627\u0641")
                }
            }
        )
    }
}

@Composable
private fun EmptyLoanState(
    modifier: Modifier,
    onAdd: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(82.dp),
                shape = RoundedCornerShape(24.dp),
                color = LoanOrangeLight
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = LoanOrange
                    )
                }
            }

            Text(
                text = "\u0647\u0646\u0648\u0632 \u0648\u0627\u0645\u06cc \u062b\u0628\u062a \u0646\u0634\u062f\u0647 \u0627\u0633\u062a",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "\u0628\u0631\u0627\u06cc \u0634\u0631\u0648\u0639\u060c \u0648\u0627\u0645 \u062c\u062f\u06cc\u062f \u0627\u0636\u0627\u0641\u0647 \u06a9\u0646\u06cc\u062f",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAdd
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("\u062b\u0628\u062a \u0648\u0627\u0645 \u062c\u062f\u06cc\u062f")
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
    val paidCount = loan.payments.size.coerceAtMost(loan.totalInstallments)
    val remainingCount =
        (loan.totalInstallments - paidCount).coerceAtLeast(0)

    val paidAmount =
        loan.payments.sumOf { it.amount }

    val totalScheduledAmount =
        loan.installmentAmount * loan.totalInstallments.toLong()

    val remainingAmount =
        (totalScheduledAmount - paidAmount).coerceAtLeast(0L)

    val progress =
        if (loan.totalInstallments > 0) {
            paidCount.toFloat() / loan.totalInstallments.toFloat()
        } else {
            0f
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MatureColors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
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
                            modifier = Modifier.size(27.dp)
                        )
                    }
                }

                Spacer(Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = loan.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MatureColors.TextPrimary
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = loan.type.label,
                        fontSize = 13.sp,
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                    icon = Icons.Filled.Payments,
                    iconTint = MatureColors.Success
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallLoanInfo(
                    modifier = Modifier.weight(1f),
                    title = "نرخ سود",
                    value = "${loan.interestRatePercent}%"
                )

                SmallLoanInfo(
                    modifier = Modifier.weight(1f),
                    title = "کل اقساط",
                    value = loan.totalInstallments.toString()
                )

                SmallLoanInfo(
                    modifier = Modifier.weight(1f),
                    title = "باقیمانده",
                    value = remainingCount.toString()
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
                        color = MatureColors.TextSecondary,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "$paidCount از ${loan.totalInstallments} قسط",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MatureColors.Primary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MatureColors.SecondaryLight)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MatureColors.Success)
                    )
                }
            }

            OutlinedButton(
                onClick = onInstallments,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "مشاهده اقساط",
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MatureColors.Surface
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
                            text = "مانده بدهی",
                            fontSize = 12.sp,
                            color = MatureColors.TextSecondary
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = remainingAmount.toPersianCurrency(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatureColors.Primary
                        )
                    }

                    Text(
                        text = if (remainingCount == 0) {
                            "تسویه شده"
                        } else {
                            "در حال بازپرداخت"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingCount == 0) {
                            MatureColors.Success
                        } else {
                            MatureColors.PrimaryLight
                        }
                    )
                }
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
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
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
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanInstallmentsDialog(
    loan: LoanEntry,
    onDismiss: () -> Unit
) {
    val currentLoan = LoanStore.getById(loan.id) ?: loan

    val firstYear = currentLoan.firstInstallmentYear
    val firstMonth = currentLoan.firstInstallmentMonth
    val firstDay = currentLoan.firstInstallmentDay

    val paidInstallments =
        currentLoan.payments
            .map { it.installmentNumber }
            .filter { it > 0 }
            .toSet()

    val paidCount =
        currentLoan.payments.size.coerceAtMost(currentLoan.totalInstallments)

    val remainingCount =
        (currentLoan.totalInstallments - paidCount).coerceAtLeast(0)

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
                    shape = RoundedCornerShape(16.dp),
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
                            value = currentLoan.totalInstallments.toString()
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
                            items = (1..currentLoan.totalInstallments).toList(),
                            key = { it }
                        ) { number ->

                            val isPaid =
                                paidInstallments.contains(number)

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
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
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
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = number.toString(),
                                                    fontWeight = FontWeight.Bold,
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
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "قسط شماره $number",
                                                fontWeight = FontWeight.Bold,
                                                color = MatureColors.TextPrimary
                                            )

                                            Text(
                                                text = "سررسید: $dueDate",
                                                fontSize = 12.sp,
                                                color = MatureColors.TextSecondary
                                            )

                                            Text(
                                                text = currentLoan.installmentAmount
                                                    .toPersianCurrency(),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MatureColors.Primary
                                            )
                                        }

                                        if (isPaid) {
                                            Text(
                                                text = "پرداخت شده",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MatureColors.Success
                                            )
                                        } else {
                                            Button(
                                                onClick = {
                                                    val alreadyPaid =
                                                        LoanStore
                                                            .getById(currentLoan.id)
                                                            ?.payments
                                                            ?.any {
                                                                it.installmentNumber == number
                                                            } == true

                                                    if (!alreadyPaid) {
                                                        val today =
                                                            JalaliCalendar
                                                                .fromEpochMillisSlashed(
                                                                    System.currentTimeMillis()
                                                                )

                                                        LoanStore.addPayment(
                                                            loanId = currentLoan.id,
                                                            payment = LoanPayment(
                                                                id = UUID.randomUUID().toString(),
                                                                installmentNumber = number,
                                                                amount = currentLoan.installmentAmount,
                                                                dateLabel = today
                                                            )
                                                        )
                                                    }
                                                },
                                                colors =
                                                    androidx.compose.material3.ButtonDefaults
                                                        .buttonColors(
                                                            containerColor =
                                                                MatureColors.Primary
                                                        )
                                            ) {
                                                Text(
                                                    text = "ثبت پرداخت",
                                                    fontSize = 11.sp
                                                )
                                            }
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
                onClick = onDismiss
            ) {
                Text("بستن")
            }
        }
    )
}

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

    val newYear = totalMonths / 12
    val newMonth = (totalMonths % 12) + 1

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
@OptIn(ExperimentalMaterial3Api::class)
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
        mutableStateOf(existingLoan?.type ?: LoanType.PERSONAL)
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
        mutableStateOf(existingLoan?.guaranteeNote ?: "")
    }

    var reminderEnabled by remember {
        mutableStateOf(existingLoan?.reminderEnabled ?: false)
    }

    var expandedType by remember { mutableStateOf(false) }
    var expandedInterval by remember { mutableStateOf(false) }

    val calculatedInstallment = remember(
        amountText,
        interestText,
        totalInstallmentsText
    ) {
        calculateMonthlyInstallment(
            principal = amountText.toLongOrNull() ?: 0L,
            annualInterestPercent = interestText.toDoubleOrNull() ?: 0.0,
            numberOfPayments = totalInstallmentsText.toIntOrNull() ?: 0
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(
                text = if (existingLoan == null)
                    "\u062b\u062b\u062a \u0648\u0627\u0645 \u062c\u062f\u06cc\u062f"
                else
                    "\u0648\u06cc\u0631\u0627\u06cc\u0634 \u0648\u0627\u0645",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text("\u0646\u0627\u0645 \u0648\u0627\u0645")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription = null
                            )
                        }
                    )
                }

                item {
                    LoanInputTile(
                        title = "\u0645\u0628\u0644\u063a \u0648\u0627\u0645",
                        value = amountText,
                        onValueChange = {
                            amountText = it.filter { c -> c.isDigit() }
                        },
                        background = LoanOrangeLight,
                        accent = LoanOrange,
                        icon = Icons.Filled.Payments
                    )
                }

                item {
                    LoanInputTile(
                        title = "\u062f\u0631\u0635\u062f \u0633\u0648\u062f",
                        value = interestText,
                        onValueChange = {
                            interestText = it
                        },
                        background = LoanGreenLight,
                        accent = LoanGreen,
                        icon = Icons.Filled.Percent
                    )
                }

                item {
                    OutlinedTextField(
                        value = totalInstallmentsText,
                        onValueChange = {
                            totalInstallmentsText =
                                it.filter { c -> c.isDigit() }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        label = {
                            Text("\u062a\u0639\u062f\u0627\u062f \u0627\u0642\u0633\u0627\u0637")
                        }
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedInterval,
                        onExpandedChange = {
                            expandedInterval = !expandedInterval
                        }
                    ) {
                        OutlinedTextField(
                            value = "\u0647\u0631 $intervalMonths \u0645\u0627\u0647",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = {
                                Text("\u0641\u0627\u0635\u0644\u0647 \u0627\u0642\u0633\u0627\u0637")
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedInterval
                                )
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = expandedInterval,
                            onDismissRequest = {
                                expandedInterval = false
                            }
                        ) {
                            listOf(1, 2, 3, 6, 12).forEach { months ->
                                DropdownMenuItem(
                                    text = {
                                        Text("\u0647\u0631 $months \u0645\u0627\u0647")
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
                        title = "\u0642\u0633\u0637 \u0645\u0627\u0647\u06cc\u0627\u0646\u0647",
                        value = if (calculatedInstallment > 0)
                            calculatedInstallment.toPersianCurrency()
                        else
                            "\u2014",
                        onValueChange = {},
                        background = LoanGreenLight,
                        accent = LoanGreen,
                        icon = Icons.Filled.Payments,
                        readOnly = true
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = {
                            expandedType = !expandedType
                        }
                    ) {
                        OutlinedTextField(
                            value = loanType.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = {
                                Text("\u0646\u0648\u0639 \u0648\u0627\u0645")
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expandedType
                                )
                            }
                        )

                        ExposedDropdownMenu(
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
                    DateField(
                        label = "\u062a\u0627\u0631\u06cc\u062e \u0648\u0627\u0645",
                        value = startDate,
                        onDateSelected = {
                            startDate = it
                        },
                        context = context
                    )
                }

                item {
                    DateField(
                        label = "\u062a\u0627\u0631\u06cc\u062e \u0627\u0648\u0644\u06cc\u0646 \u0642\u0633\u0637",
                        value = firstInstallmentDate,
                        onDateSelected = {
                            firstInstallmentDate = it
                        },
                        context = context
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
                        label = {
                            Text("\u062a\u0636\u0645\u06cc\u0646 \u0648 \u062a\u0648\u0636\u06cc\u062d\u0627\u062a")
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
                        totalInstallmentsText.toIntOrNull() ?: 0

                    val startParts =
                        parseJalaliDate(startDate)

                    val firstParts =
                        parseJalaliDate(firstInstallmentDate)

                    if (
                        title.isBlank() ||
                        amount <= 0 ||
                        installments <= 0 ||
                        startParts == null
                    ) {
                        return@Button
                    }

                    val finalInstallment =
                        if (calculatedInstallment > 0)
                            calculatedInstallment
                        else
                            installmentText.toLongOrNull() ?: 0L

                    val loan = LoanEntry(
                        id = existingLoan?.id
                            ?: UUID.randomUUID().toString(),

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
                            existingLoan?.payments ?: emptyList()
                    )

                    onSave(loan)
                }
            ) {
                Text(
                    if (existingLoan == null)
                        "\u062b\u0628\u062a \u0648\u0627\u0645"
                    else
                        "\u0630\u062e\u06cc\u0631\u0647 \u062a\u063a\u06cc\u06cc\u0631\u0627\u062a"
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("\u0627\u0646\u0635\u0631\u0627\u0641")
            }
        }
    )
}

@Composable
private fun LoanInputTile(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    background: Color,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    readOnly: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = background
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.size(8.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                readOnly = readOnly,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                placeholder = {
                    Text("\u0648\u0627\u0631\u062f \u06a9\u0646\u06cc\u062f")
                }
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
        label = {
            Text(label)
        },
        leadingIcon = {
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    val calendar = Calendar.getInstance()

                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val selected =
                                Calendar.getInstance().apply {
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
                                JalaliCalendar.fromEpochMillisSlashed(
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
                    Icons.Filled.CalendarMonth,
                    contentDescription = "\u0627\u0646\u062a\u062e\u0627\u0628 \u062a\u0627\u0631\u06cc\u062e"
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

    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null

    if (month !in 1..12 || day !in 1..31) {
        return null
    }

    return intArrayOf(year, month, day)
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
        annualInterestPercent / 100.0 / 12.0

    val power =
        (1.0 + monthlyRate).pow(numberOfPayments)

    val payment =
        principal *
            (monthlyRate * power) /
            (power - 1.0)

    return payment.toLong()
}












