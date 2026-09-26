package com.example.walletapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import com.example.walletapp.ui.components.JalaliDatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
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
import com.example.walletapp.data.LoanManager
import com.example.walletapp.data.LoanPaymentDateValidator
import com.example.walletapp.data.LoanPaymentManager
import com.example.walletapp.data.LoanType
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.loan.MatureColors
import com.example.walletapp.reminders.LoanReminderSynchronizer
import java.util.Calendar
import java.util.UUID
import kotlin.math.pow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.MoreVert

private val LoanOrange = MatureColors.Secondary
private val LoanOrangeLight = MatureColors.SecondaryLight
private val LoanGreen = MatureColors.Success
private val LoanGreenLight = MatureColors.Surface
private val LoanBlue = MatureColors.PrimaryLight
private val LoanRed = MatureColors.PrimaryDark

@Composable
fun LoanManagementScreen(
    onNavigate: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var editingLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var deleteLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var installmentsLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val totalLoans = LoanStore.loans.size

    val totalPrincipal =
        LoanStore.loans.sumOf { it.totalAmount }

    val totalPaid =
        LoanStore.loans.sumOf { loan ->
            loan.payments.sumOf { it.amount }
        }

    val totalPlanned =
        LoanStore.loans.sumOf { loan ->
            loan.installmentAmount *
                loan.totalInstallments.toLong()
        }

    val totalPaidInstallments =
        LoanStore.loans.sumOf { loan ->
            loan.payments.size
                .coerceAtMost(loan.totalInstallments)
        }

    val totalInstallments =
        LoanStore.loans.sumOf {
            it.totalInstallments
        }

    val totalRemainingAmount =
        (totalPlanned - totalPaid).coerceAtLeast(0L)

    Scaffold(
        topBar = {
            LoanHeader(
                totalLoans = totalLoans,
                totalRemainingAmount = totalRemainingAmount,
                onBack = {
                    onNavigate("dashboard")
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MatureColors.Primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "افزودن وام"
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MatureColors.Surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {

                        Text(
                            text = "وضعیت وام‌ها",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatureColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "نمای کلی تعهدات و پرداخت‌های شما",
                            fontSize = 12.sp,
                            color = MatureColors.TextPrimary.copy(alpha = 0.62f)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            LoanSummaryTile(
                                modifier = Modifier.weight(1f),
                                title = "تعداد وام",
                                value = totalLoans.toString(),
                                icon = Icons.Filled.AccountBalance,
                                background = MatureColors.SecondaryLight,
                                iconTint = MatureColors.Secondary
                            )

                            LoanSummaryTile(
                                modifier = Modifier.weight(1f),
                                title = "پرداخت شده",
                                value = totalPaid.toPersianCurrency(),
                                icon = Icons.Filled.Payments,
                                background = MatureColors.Success.copy(alpha = 0.10f),
                                iconTint = MatureColors.Success
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            LoanSummaryTile(
                                modifier = Modifier.weight(1f),
                                title = "کل تعهد",
                                value = totalPlanned.toPersianCurrency(),
                                icon = Icons.Filled.Schedule,
                                background = MatureColors.CardBackground,
                                iconTint = MatureColors.Primary
                            )

                            LoanSummaryTile(
                                modifier = Modifier.weight(1f),
                                title = "اقساط",
                                value = "$totalPaidInstallments / $totalInstallments",
                                icon = Icons.Filled.CalendarMonth,
                                background = MatureColors.SecondaryLight,
                                iconTint = MatureColors.Secondary
                            )
                        }

                        if (totalInstallments > 0) {

                            Spacer(modifier = Modifier.height(18.dp))

                            val progress =
                                (
                                    totalPaidInstallments.toFloat() /
                                        totalInstallments.toFloat()
                                ).coerceIn(0f, 1f)

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = MatureColors.Primary,
                                trackColor = MatureColors.CardBackground
                            )

                            Spacer(modifier = Modifier.height(7.dp))

                            Text(
                                text =
                                    "${(progress * 100).toInt()}٪ از اقساط کل پرداخت شده",
                                fontSize = 11.sp,
                                color = MatureColors.TextPrimary.copy(alpha = 0.60f)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "وام‌های من",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MatureColors.TextPrimary
                        )

                        Text(
                            text = "برای مشاهده جزئیات و اقساط انتخاب کنید",
                            fontSize = 12.sp,
                            color = MatureColors.TextPrimary.copy(alpha = 0.58f)
                        )
                    }

                    Text(
                        text = "$totalLoans مورد",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MatureColors.Primary
                    )
                }
            }

            if (LoanStore.loans.isEmpty()) {

                item {
                    EmptyLoanState(
                        onAdd = {
                            showAddDialog = true
                        }
                    )
                }

            } else {

                items(
                    items = LoanStore.loans,
                    key = { it.id }
                ) { loan ->

                    LoanCard(
                        loan = loan,
                        onEdit = {
                            editingLoan = loan
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

    if (showAddDialog) {
        LoanEditorDialog(
            existingLoan = null,
            onDismiss = {
                showAddDialog = false
            },
            onSave = { loan ->
                LoanManager.add(loan)
                LoanReminderSynchronizer.syncLoan(context, loan)
                showAddDialog = false
            }
        )
    }

    editingLoan?.let { loan ->
        LoanEditorDialog(
            existingLoan = loan,
            onDismiss = {
                editingLoan = null
            },
            onSave = { updatedLoan ->
                LoanManager.update(updatedLoan)
                LoanReminderSynchronizer.syncLoan(context, updatedLoan)
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
                Text("حذف وام")
            },
            text = {
                Text(
                    "آیا از حذف وام «${loan.title}» مطمئن هستید؟"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        LoanManager.remove(loan.id)
                        deleteLoan = null
                    }
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteLoan = null
                    }
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
        loan.payments.size
            .coerceAtMost(loan.totalInstallments)

    val remainingCount =
        (loan.totalInstallments - paidCount)
            .coerceAtLeast(0)

    val paidAmount =
        loan.payments.sumOf { it.amount }

    val totalPlanned =
        loan.installmentAmount *
            loan.totalInstallments.toLong()

    val progress =
        if (loan.totalInstallments > 0) {
            (
                paidCount.toFloat() /
                    loan.totalInstallments.toFloat()
            ).coerceIn(0f, 1f)
        } else {
            0f
        }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MatureColors.Surface
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            MatureColors.Primary.copy(alpha = 0.10f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        tint = MatureColors.Primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = loan.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MatureColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = loan.type.label,
                        fontSize = 12.sp,
                        color = MatureColors.TextPrimary.copy(alpha = 0.58f)
                    )
                }

                Box {
                    IconButton(
                        onClick = {
                            menuExpanded = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "گزینه‌ها"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            menuExpanded = false
                        }
                    ) {

                        DropdownMenuItem(
                            text = {
                                Text("ویرایش")
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("اقساط")
                            },
                            onClick = {
                                menuExpanded = false
                                onInstallments()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Payments,
                                    contentDescription = null
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text("حذف")
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "مبلغ وام",
                fontSize = 11.sp,
                color = MatureColors.TextPrimary.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = loan.totalAmount.toPersianCurrency(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MatureColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                LoanMiniSummary(
                    modifier = Modifier.weight(1f),
                    title = "قسط ماهانه",
                    value = loan.installmentAmount.toPersianCurrency()
                )

                LoanMiniSummary(
                    modifier = Modifier.weight(1f),
                    title = "پرداخت شده",
                    value = paidAmount.toPersianCurrency()
                )

                LoanMiniSummary(
                    modifier = Modifier.weight(1f),
                    title = "باقی‌مانده",
                    value = remainingCount.toString()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "$paidCount از ${loan.totalInstallments} قسط",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MatureColors.TextPrimary.copy(alpha = 0.68f),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${(progress * 100).toInt()}٪",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatureColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MatureColors.Primary,
                trackColor = MatureColors.CardBackground
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onInstallments,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Payments,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("مشاهده و مدیریت اقساط")
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
    var loanPaymentError by remember { mutableStateOf<String?>(null) }

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
                        shape = RoundedCornerShape(20.dp),
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
                                shape = RoundedCornerShape(20.dp),
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
                                        shape = RoundedCornerShape(14.dp),
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
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("بستن")
            }
        }
    )

    loanPaymentError?.let { message ->
        AlertDialog(
            onDismissRequest = { loanPaymentError = null },
            title = {
                Text(
                    text = "خطای ثبت پرداخت",
                    fontWeight = FontWeight.Bold,
                    color = MatureColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = message,
                    color = MatureColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { loanPaymentError = null }
                ) {
                    Text("متوجه شدم")
                }
            }
        )
    }
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
                                            RoundedCornerShape(14.dp)
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

                            if (!LoanPaymentDateValidator.isPaymentDateValid(currentLoan, now)) {
                                loanPaymentError = LoanPaymentDateValidator.ERROR_MESSAGE
                                return@Button
                            }

                            val today =
                                JalaliCalendar
                                    .fromEpochMillisSlashed(
                                        now
                                    )

                            val recorded =
                                LoanPaymentManager.recordManualInstallmentPayment(
                                    context = context,
                                    loan = currentLoan,
                                    installmentNumber = installmentNumber,
                                    accountId = accountId,
                                    paymentDateEpochMillis = now
                                )

                            if (recorded) {
                                selectedInstallmentNumber = null
                                selectedAccountId = null
                            } else {
                                loanPaymentError =
                                    "ثبت پرداخت قسط انجام نشد."
                            }
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
        mutableStateOf(existingLoan?.type ?: LoanType.PERSONAL)
    }

    var amountText by remember {
        mutableStateOf(existingLoan?.totalAmount?.toString() ?: "")
    }

    var interestText by remember {
        mutableStateOf(existingLoan?.interestRatePercent?.toString() ?: "")
    }

    var installmentText by remember {
        mutableStateOf(existingLoan?.installmentAmount?.toString() ?: "")
    }

    var totalInstallmentsText by remember {
        mutableStateOf(existingLoan?.totalInstallments?.toString() ?: "")
    }

    var intervalMonths by remember {
        mutableStateOf(existingLoan?.intervalMonths ?: 1)
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

    var validationMessage by remember { mutableStateOf<String?>(null) }

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

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides
                androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(
                                start = 20.dp,
                                end = 12.dp,
                                top = 12.dp,
                                bottom = 8.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (existingLoan == null) {
                                    "ثبت وام جدید"
                                } else {
                                    "ویرایش وام"
                                },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(Modifier.height(3.dp))

                            Text(
                                text = "اطلاعات وام را وارد کنید",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 10.dp,
                            bottom = 20.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            LoanFormField(
                                label = "نام وام",
                                value = title,
                                onValueChange = { title = it },
                                placeholder = "مثلاً وام خرید",
                                icon = Icons.Filled.AccountBalance,
                                keyboardType = KeyboardType.Text
                            )
                        }

                        item {
                            LoanAmountCard(
                                value = amountText,
                                onValueChange = {
                                    amountText = normalizeLoanDigits(it).filter { c -> c.isDigit() }
                                }
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LoanFormField(
                                    modifier = Modifier.weight(1f),
                                    label = "درصد سود",
                                    value = interestText,
                                    onValueChange = {
                                        interestText = normalizeLoanDigits(it)
                                    },
                                    placeholder = "۰",
                                    icon = Icons.Filled.Percent,
                                    keyboardType = KeyboardType.Decimal
                                )

                                LoanFormField(
                                    modifier = Modifier.weight(1f),
                                    label = "تعداد اقساط",
                                    value = totalInstallmentsText,
                                    onValueChange = {
                                        totalInstallmentsText =
                                            normalizeLoanDigits(it).filter { c -> c.isDigit() }
                                    },
                                    placeholder = "۱۲",
                                    icon = Icons.Filled.Schedule,
                                    keyboardType = KeyboardType.Number
                                )

                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = "هر $intervalMonths ماه",
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        shape = MaterialTheme.shapes.medium,
                                        label = { Text("فاصله اقساط") },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Schedule,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    )

                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable {
                                                expandedInterval = true
                                            }
                                    )

                                    DropdownMenu(
                                        expanded = expandedInterval,
                                        onDismissRequest = {
                                            expandedInterval = false
                                        }
                                    ) {
                                        listOf(1, 2, 3, 6, 12).forEach { months ->
                                            DropdownMenuItem(
                                                text = { Text("هر $months ماه") },
                                                onClick = {
                                                    intervalMonths = months
                                                    expandedInterval = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            LoanMonthlyInstallmentCard(
                                value = if (calculatedInstallment > 0) {
                                    calculatedInstallment.toPersianCurrency()
                                } else {
                                    "—"
                                }
                            )
                        }

                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = loanType.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp)
                                        .clickable { expandedType = true },
                                    shape = MaterialTheme.shapes.medium,
                                    label = { Text("نوع وام") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.AccountBalance,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.MoreVert,
                                            contentDescription = "انتخاب نوع وام",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                )

                                DropdownMenu(
                                    expanded = expandedType,
                                    onDismissRequest = {
                                        expandedType = false
                                    }
                                ) {
                                    LoanType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type.label) },
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
                            Text(
                                text = "تاریخ‌ها",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(
                                    top = 4.dp,
                                    bottom = 0.dp
                                )
                            )
                        }

                        item {
                            LoanDateField(
                                label = "تاریخ دریافت وام",
                                value = startDate,
                                onClick = {
                                    // Date picker is opened by the field itself.
                                },
                                context = context,
                                onDateSelected = { startDate = it }
                            )
                        }

                        item {
                            LoanDateField(
                                label = "تاریخ اولین قسط (اختیاری)",
                                value = firstInstallmentDate,
                                onClick = {},
                                context = context,
                                onDateSelected = { firstInstallmentDate = it }
                            )
                        }

                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "یادآوری وام",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = "برای اقساط این وام یادآوری خودکار ایجاد شود",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = reminderEnabled,
                                        onCheckedChange = { reminderEnabled = it }
                                    )
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = guaranteeNote,
                                onValueChange = { guaranteeNote = it },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = MaterialTheme.shapes.medium,
                                label = { Text("ضمانت و توضیحات") },
                                placeholder = {
                                    Text("توضیحات اختیاری")
                                },
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }

                    if (validationMessage != null) {
                        Text(
                            text = validationMessage!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 4.dp),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 10.dp,
                                bottom = 20.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Text(
                                "انصراف",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

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

                                if (title.isBlank()) {
                                    validationMessage = "نام وام را وارد کنید"
                                    return@Button
                                }

                                if (amount <= 0L) {
                                    validationMessage = "مبلغ وام را وارد کنید"
                                    return@Button
                                }

                                if (installments <= 0) {
                                    validationMessage = "تعداد اقساط را وارد کنید"
                                    return@Button
                                }

                                if (startParts == null) {
                                    validationMessage = "تاریخ دریافت وام را انتخاب کنید"
                                    return@Button
                                }

                                if (reminderEnabled && firstParts == null) {
                                    validationMessage = "برای فعال‌سازی یادآوری، تاریخ اولین قسط را انتخاب کنید"
                                    return@Button
                                }

                                val paidInstallmentCount =
                                    existingLoan?.payments?.size ?: 0

                                if (
                                    existingLoan != null &&
                                    installments < paidInstallmentCount
                                ) {
                                    validationMessage = "تعداد اقساط نمی‌تواند از اقساط پرداخت‌شده کمتر باشد"
                                    return@Button
                                }

                                validationMessage = null

                                val finalInstallment =
                                    if (calculatedInstallment > 0) {
                                        calculatedInstallment
                                    } else {
                                        installmentText.toLongOrNull() ?: 0L
                                    }

                                val loan =
                                    LoanEntry(
                                        id =
                                            existingLoan?.id
                                                ?: UUID.randomUUID().toString(),
                                        title = title.trim(),
                                        type = loanType,
                                        totalAmount = amount,
                                        interestRatePercent = interest,
                                        installmentAmount = finalInstallment,
                                        totalInstallments = installments,
                                        intervalMonths = intervalMonths,
                                        startYear = startParts[0],
                                        startMonth = startParts[1],
                                        startDay = startParts[2],
                                        firstInstallmentYear = firstParts?.get(0),
                                        firstInstallmentMonth = firstParts?.get(1),
                                        firstInstallmentDay = firstParts?.get(2),
                                        guaranteeNote = guaranteeNote.trim(),
                                        reminderEnabled = reminderEnabled,
                                        payments = existingLoan?.payments ?: emptyList()
                                    )

                                onSave(loan)
                            },
                            modifier = Modifier
                                .weight(1.35f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                if (existingLoan == null) {
                                    "ثبت وام"
                                } else {
                                    "ذخیره تغییرات"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanFormField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
private fun LoanAmountCard(
    value: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MatureColors.Success.copy(alpha = 0.10f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MatureColors.Success.copy(alpha = 0.30f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Payments,
                    contentDescription = null,
                    tint = MatureColors.Success,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.size(8.dp))

                Text(
                    text = "مبلغ وام",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                placeholder = {
                    Text(
                        "مبلغ وام را وارد کنید",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                suffix = {
                    Text(
                        "ریال",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MatureColors.Success,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun LoanMonthlyInstallmentCard(
    value: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MatureColors.SecondaryLight.copy(alpha = 0.70f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MatureColors.Secondary.copy(alpha = 0.28f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.medium,
                color = MatureColors.Secondary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Payments,
                        contentDescription = null,
                        tint = MatureColors.Secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "قسط ماهیانه",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "محاسبه‌شده",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoanDateField(
    label: String,
    value: String,
    onClick: () -> Unit,
    context: android.content.Context,
    onDateSelected: (String) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { showPicker = true },
        shape = MaterialTheme.shapes.medium,
        label = { Text(label) },
        placeholder = { Text("انتخاب تاریخ") },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "انتخاب تاریخ",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )

    if (showPicker) {
        JalaliDatePickerDialog(
            initialDate = value,
            onDismissRequest = { showPicker = false },
            onDateSelected = {
                onDateSelected(it)
                showPicker = false
            }
        )
    }
}

@Composable
private fun LoanEditorSectionTitle(
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(width = 4.dp, height = 22.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.colorScheme.primary
        ) {}

        Spacer(Modifier.size(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
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
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = MaterialTheme.shapes.small,
                    color = accent.copy(alpha = 0.10f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.size(9.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                readOnly = readOnly,
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                placeholder = { Text("وارد کنید") },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}

@Composable
private fun LoanMoneyCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(Modifier.size(8.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                placeholder = { Text("مبلغ را وارد کنید") },
                suffix = { Text("ریال") }
            )
        }
    }
}

@Composable
private fun LoanDropdownField(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    menuContent: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "انتخاب " + label,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onExpandedChange(true) }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            menuContent()
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
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "انتخاب تاریخ",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )

    if (showPicker) {
        JalaliDatePickerDialog(
            initialDate = value,
            onDismissRequest = { showPicker = false },
            onDateSelected = { selectedDate ->
                onDateSelected(selectedDate)
                showPicker = false
            }
        )
    }
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

private fun normalizeLoanDigits(value: String): String =
    value.map { ch ->
        when (ch) {
            in '۰'..'۹' -> ('0'.code + (ch.code - '۰'.code)).toChar()
            in '٠'..'٩' -> ('0'.code + (ch.code - '٠'.code)).toChar()
            else -> ch
        }
    }.joinToString("")

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
























