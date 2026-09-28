package com.example.walletapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.Account
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import com.example.walletapp.ui.components.JalaliDatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.walletapp.data.LoanTrackingMode
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
            loan.paidInstallmentNumbers().size
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
        LoanEditorFullScreen(
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
        LoanEditorFullScreen(
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
        loan.paidInstallmentNumbers().size

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
                    imageVector = Icons.Filled.Payments,                    contentDescription = null
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
        currentLoan.paidInstallmentNumbers()

    val paidCount =
        paidInstallments.size

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

                val hasDueDateBase =
                    (firstYear != null && firstMonth != null && firstDay != null) ||
                        (
                            currentLoan.trackingMode == LoanTrackingMode.EXISTING &&
                                currentLoan.trackingStartYear != null &&
                                currentLoan.trackingStartMonth != null &&
                                currentLoan.trackingStartDay != null
                            )

                if (!hasDueDateBase) {
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
                                currentLoan.installmentDueDate(number)

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

private fun LoanEntry.paidInstallmentNumbers(): Set<Int> {
    val historical = if (trackingMode == LoanTrackingMode.EXISTING) {
        (1..historicalPaidInstallments.coerceAtMost(totalInstallments)).toSet()
    } else emptySet()
    val recorded = payments.map { it.installmentNumber }
        .filter { it in 1..totalInstallments }.toSet()
    return historical + recorded
}

private fun LoanEntry.installmentDueDate(installmentNumber: Int): String {
    if (trackingMode == LoanTrackingMode.EXISTING &&
        trackingStartYear != null && trackingStartMonth != null && trackingStartDay != null &&
        installmentNumber > historicalPaidInstallments
    ) {
        return calculateLoanInstallmentDate(
            year = trackingStartYear,
            month = trackingStartMonth,
            day = trackingStartDay,
            monthsToAdd = (installmentNumber - historicalPaidInstallments - 1) * intervalMonths
        )
    }
    val year = firstInstallmentYear
    val month = firstInstallmentMonth
    val day = firstInstallmentDay
    if (year == null || month == null || day == null) return "—"
    return calculateLoanInstallmentDate(
        year = year,
        month = month,
        day = day,
        monthsToAdd = (installmentNumber - 1) * intervalMonths
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanEditorFullScreen(
    existingLoan: LoanEntry?,
    onDismiss: () -> Unit,
    onSave: (LoanEntry) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(existingLoan?.title ?: "") }
    var loanType by remember { mutableStateOf(existingLoan?.type ?: LoanType.PERSONAL) }
    var amountText by remember { mutableStateOf(existingLoan?.totalAmount?.toString() ?: "") }
    var interestText by remember { mutableStateOf(existingLoan?.interestRatePercent?.toString() ?: "") }
    var installmentText by remember { mutableStateOf(existingLoan?.installmentAmount?.toString() ?: "") }
    var totalInstallmentsText by remember { mutableStateOf(existingLoan?.totalInstallments?.toString() ?: "") }
    var intervalMonths by remember { mutableStateOf(existingLoan?.intervalMonths ?: 1) }
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
                if (it.firstInstallmentYear != null && it.firstInstallmentMonth != null && it.firstInstallmentDay != null) {
                    "${it.firstInstallmentYear}/${it.firstInstallmentMonth.toString().padStart(2, '0')}/${it.firstInstallmentDay.toString().padStart(2, '0')}"
                } else ""
            } ?: ""
        )
    }
    var guaranteeNote by remember { mutableStateOf(existingLoan?.guaranteeNote ?: "") }
    var reminderEnabled by remember { mutableStateOf(existingLoan?.reminderEnabled ?: false) }
    var selectedLoanAccountId by remember { mutableStateOf(existingLoan?.accountId) }
    var trackingMode by remember { mutableStateOf(existingLoan?.trackingMode ?: LoanTrackingMode.NEW) }
    var historicalPaidText by remember { mutableStateOf(existingLoan?.historicalPaidInstallments?.toString() ?: "0") }
    var trackingStartDate by remember {
        mutableStateOf(
            existingLoan?.let {
                if (it.trackingStartYear != null && it.trackingStartMonth != null && it.trackingStartDay != null) {
                    "${it.trackingStartYear}/${it.trackingStartMonth.toString().padStart(2, '0')}/${it.trackingStartDay.toString().padStart(2, '0')}"
                } else JalaliCalendar.todaySlashed()
            } ?: JalaliCalendar.todaySlashed()
        )
    }
    var expandedType by remember { mutableStateOf(false) }
    var expandedInterval by remember { mutableStateOf(false) }

    val calculatedInstallment = remember(amountText, interestText, totalInstallmentsText) {
        calculateMonthlyInstallment(
            principal = amountText.toLongOrNull() ?: 0L,
            annualInterestPercent = interestText.toDoubleOrNull() ?: 0.0,
            numberOfPayments = totalInstallmentsText.toIntOrNull() ?: 0
        )
    }

    fun saveLoan() {
        val amount = amountText.toLongOrNull() ?: 0L
        val interest = interestText.toDoubleOrNull() ?: 0.0
        val installments = totalInstallmentsText.toIntOrNull() ?: 0
        val startParts = parseJalaliDate(startDate)
        val firstParts = parseJalaliDate(firstInstallmentDate)
        val trackingParts = if (trackingMode == LoanTrackingMode.EXISTING) parseJalaliDate(trackingStartDate) else null
        val historicalPaid = historicalPaidText.toIntOrNull() ?: 0
        val actualPaidInstallmentCount = existingLoan?.payments
            ?.map { it.installmentNumber }
            ?.filter { it > 0 }
            ?.toSet()
            ?.size ?: 0
        val totalPaidCount = if (trackingMode == LoanTrackingMode.EXISTING) historicalPaid + actualPaidInstallmentCount else actualPaidInstallmentCount

        if (title.isBlank() || amount <= 0 || installments <= 0 || startParts == null) return
        if (historicalPaid !in 0..installments) return
        if (trackingMode == LoanTrackingMode.EXISTING && trackingParts == null) return
        if (installments < totalPaidCount) return

        val finalInstallment = if (calculatedInstallment > 0) calculatedInstallment
        else installmentText.toLongOrNull() ?: 0L

        onSave(
            LoanEntry(
                id = existingLoan?.id ?: UUID.randomUUID().toString(),
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
                accountId = selectedLoanAccountId,
                payments = existingLoan?.payments ?: emptyList(),
                trackingMode = trackingMode,
                historicalPaidInstallments = historicalPaid,
                trackingStartYear = trackingParts?.get(0),
                trackingStartMonth = trackingParts?.get(1),
                trackingStartDay = trackingParts?.get(2)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (existingLoan == null) "ثبت وام جدید" else "ویرایش وام",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مدیریت مالی شما",
                            fontSize = 11.sp,
                            color = MatureColors.TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        },
        bottomBar = {
            Surface(color = MatureColors.Surface, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp)
                    ) { Text("انصراف") }

                    Button(
                        onClick = ::saveLoan,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MatureColors.Primary)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(7.dp))
                        Text(
                            text = if (existingLoan == null) "ثبت وام" else "ذخیره تغییرات",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MatureColors.Primary)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.AccountBalance,
                                        contentDescription = null,
                                        tint = MatureColors.Secondary,
                                        modifier = Modifier.size(27.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (existingLoan == null) "وام جدید" else "ویرایش وام",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = "اطلاعات و شرایط وام را ثبت کنید",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.76f)
                                )
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(17.dp),
                            color = Color.White.copy(alpha = 0.10f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("قسط ماهیانه", fontSize = 11.sp, color = Color.White.copy(alpha = 0.68f))
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = if (calculatedInstallment > 0) calculatedInstallment.toPersianCurrency() else "—",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("تعداد اقساط", fontSize = 11.sp, color = Color.White.copy(alpha = 0.68f))
                                    Text(
                                        text = totalInstallmentsText.ifBlank { "—" },
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MatureColors.Secondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                LoanEditorDashboardCard(title = "اطلاعات اصلی") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        label = { Text("نام وام") },
                        placeholder = { Text("مثلاً وام خرید") },                        leadingIcon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) }
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter(Char::isDigit) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        label = { Text("مبلغ وام") },
                        placeholder = { Text("مبلغ را وارد کنید") },
                        suffix = { Text("ریال") },
                        leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            item {
                LoanEditorDashboardCard(title = "وضعیت وام") {
                    Text(
                        text = "مشخص کنید این وام را از ابتدا ثبت می‌کنید یا ادامه وضعیت قبلی را پیگیری می‌کنید.",
                        fontSize = 11.sp,
                        color = MatureColors.TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LoanTrackingModeCard(
                            modifier = Modifier.weight(1f),
                            mode = LoanTrackingMode.NEW,
                            selected = trackingMode == LoanTrackingMode.NEW,
                            onClick = { trackingMode = LoanTrackingMode.NEW }
                        )
                        LoanTrackingModeCard(
                            modifier = Modifier.weight(1f),
                            mode = LoanTrackingMode.EXISTING,
                            selected = trackingMode == LoanTrackingMode.EXISTING,
                            onClick = { trackingMode = LoanTrackingMode.EXISTING }
                        )
                    }
                    if (trackingMode == LoanTrackingMode.EXISTING) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = historicalPaidText,
                            onValueChange = { historicalPaidText = it.filter(Char::isDigit) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            label = { Text("تعداد اقساطی که قبلاً پرداخت شده") },
                            leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(Modifier.height(10.dp))
                        DateField(
                            label = "شروع پیگیری از این تاریخ",
                            value = trackingStartDate,
                            onDateSelected = { trackingStartDate = it },
                            context = context
                        )
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            color = MatureColors.SecondaryLight
                        ) {
                            Text(
                                text = "اقساط قبلی فقط به‌عنوان سابقه پرداخت در نظر گرفته می‌شوند و برای آن‌ها تراکنش مالی جدید ساخته نمی‌شود. از تاریخ شروع پیگیری، فقط اقساط باقی‌مانده مدیریت و یادآوری خواهند شد.",
                                modifier = Modifier.padding(14.dp),
                                fontSize = 11.sp,
                                color = MatureColors.TextPrimary
                            )
                        }
                    }
                }
            }

            item {
                LoanEditorDashboardCard(title = "شرایط بازپرداخت") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = interestText,
                            onValueChange = { interestText = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            label = { Text("درصد سود") },
                            suffix = { Text("%") },
                            leadingIcon = { Icon(Icons.Filled.Percent, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = totalInstallmentsText,
                            onValueChange = { totalInstallmentsText = it.filter(Char::isDigit) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            label = { Text("تعداد اقساط") },
                            leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "هر $intervalMonths ماه",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { expandedInterval = true },
                            shape = RoundedCornerShape(18.dp),
                            label = { Text("فاصله اقساط") },
                            leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null) }
                        )
                        DropdownMenu(expanded = expandedInterval, onDismissRequest = { expandedInterval = false }) {
                            listOf(1, 2, 3, 6, 12).forEach { months ->
                                DropdownMenuItem(
                                    text = { Text("هر $months ماه") },
                                    onClick = { intervalMonths = months; expandedInterval = false }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MatureColors.Success.copy(alpha = 0.10f)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Calculate, contentDescription = null, tint = MatureColors.Success, modifier = Modifier.size(23.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("قسط ماهیانه محاسبه‌شده", fontSize = 11.sp, color = MatureColors.TextSecondary)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (calculatedInstallment > 0) calculatedInstallment.toPersianCurrency()
                                    else "با تکمیل مبلغ، سود و تعداد اقساط محاسبه می‌شود",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MatureColors.TextPrimary
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = loanType.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { expandedType = true },
                            shape = RoundedCornerShape(18.dp),
                            label = { Text("نوع وام") },
                            leadingIcon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) }
                        )
                        DropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                            LoanType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.label) },
                                    onClick = { loanType = type; expandedType = false }
                                )
                            }
                        }
                    }
                }
            }

            item {
                LoanEditorDashboardCard(title = "تاریخ‌ها") {
                    DateField(
                        label = "تاریخ وام",
                        value = startDate,
                        onDateSelected = { startDate = it },
                        context = context
                    )
                    Spacer(Modifier.height(10.dp))
                    DateField(
                        label = "تاریخ اولین قسط",
                        value = firstInstallmentDate,
                        onDateSelected = { firstInstallmentDate = it },
                        context = context
                    )
                }
            }

            item {
                LoanEditorDashboardCard(title = "حساب بانکی وام") {
                Text(
                    text = "حسابی را که این وام به آن مربوط است انتخاب کنید. برای وام‌های قدیمی، انتخاب حساب اختیاری است.",
                    fontSize = 11.sp,
                    color = MatureColors.TextSecondary
                )
                Spacer(Modifier.height(10.dp))
                val accountLabel = selectedLoanAccountId?.let { id ->
                    AccountStore.accounts.firstOrNull { it.id == id }?.name
                } ?: "بدون حساب مشخص"
                var expandedAccount by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = accountLabel,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { expandedAccount = true },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        label = { Text("حساب / بانک وام") },
                        trailingIcon = {
                            Icon(Icons.Filled.AccountBalance, contentDescription = null)
                        }
                    )
                    DropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("بدون حساب مشخص") },
                            onClick = {
                                selectedLoanAccountId = null
                                expandedAccount = false
                            }
                        )
                        AccountStore.accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    selectedLoanAccountId = account.id
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            LoanEditorDashboardCard(title = "ضمانت و یادآوری") {
                    OutlinedTextField(
                        value = guaranteeNote,
                        onValueChange = { guaranteeNote = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(18.dp),
                        label = { Text("ضمانت و توضیحات") },
                        placeholder = { Text("توضیحات اختیاری") }
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MatureColors.CardBackground
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("یادآوری اقساط", fontWeight = FontWeight.Bold, color = MatureColors.TextPrimary)
                                Text("برای سررسیدهای این وام یادآوری فعال شود", fontSize = 11.sp, color = MatureColors.TextSecondary)
                            }
                            Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun LoanTrackingModeCard(
    modifier: Modifier,
    mode: LoanTrackingMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 92.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) {
            MatureColors.Primary.copy(alpha = 0.10f)
        } else {
            MatureColors.CardBackground
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected,
                    onClick = onClick
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = mode.label,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MatureColors.TextPrimary,
                    maxLines = 1
                )
            }
            Text(
                text = if (mode == LoanTrackingMode.NEW) {
                    "شروع از قسط اول"
                } else {
                    "ادامه از وضعیت فعلی"
                },
                modifier = Modifier.fillMaxWidth(),
                fontSize = 10.sp,
                color = MatureColors.TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LoanEditorDashboardCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MatureColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MatureColors.TextPrimary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LoanEditorDialog(
    existingLoan: LoanEntry?,
    onDismiss: () -> Unit,
    onSave: (LoanEntry) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(existingLoan?.title ?: "") }
    var loanType by remember { mutableStateOf(existingLoan?.type ?: LoanType.PERSONAL) }
    var amountText by remember { mutableStateOf(existingLoan?.totalAmount?.toString() ?: "") }
    var interestText by remember { mutableStateOf(existingLoan?.interestRatePercent?.toString() ?: "") }
    var installmentText by remember { mutableStateOf(existingLoan?.installmentAmount?.toString() ?: "") }
    var totalInstallmentsText by remember {
        mutableStateOf(existingLoan?.totalInstallments?.toString() ?: "")
    }
    var intervalMonths by remember { mutableStateOf(existingLoan?.intervalMonths ?: 1) }

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

    var guaranteeNote by remember { mutableStateOf(existingLoan?.guaranteeNote ?: "") }
    var reminderEnabled by remember { mutableStateOf(existingLoan?.reminderEnabled ?: false) }
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
        modifier = Modifier.fillMaxWidth(0.94f),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (existingLoan == null) "ثبت وام جدید" else "ویرایش وام",
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        top = 2.dp,
                        bottom = 4.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { LoanEditorSectionTitle(title = "اطلاعات وام") }

                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            label = { Text("نام وام") },
                            placeholder = { Text("مثلاً وام خرید") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalance,
                                    contentDescription = null,
                                    tint = MatureColors.Primary
                                )
                            }
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it.filter(Char::isDigit) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            label = { Text("مبلغ وام") },
                            placeholder = { Text("مبلغ را وارد کنید") },
                            suffix = { Text("ریال") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Payments,
                                    contentDescription = null,
                                    tint = LoanOrange
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = interestText,
                                onValueChange = { interestText = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                label = { Text("درصد سود") },
                                placeholder = { Text("۰") },
                                suffix = { Text("%") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Percent,
                                        contentDescription = null,
                                        tint = LoanGreen
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                )
                            )

                            OutlinedTextField(
                                value = totalInstallmentsText,
                                onValueChange = {
                                    totalInstallmentsText = it.filter(Char::isDigit)
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                label = { Text("تعداد اقساط") },
                                placeholder = { Text("۱۲") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = MatureColors.Primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                )
                            )
                        }
                    }

                    item { LoanEditorSectionTitle(title = "تنظیمات اقساط") }

                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = "هر $intervalMonths ماه",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedInterval = true },
                                shape = RoundedCornerShape(14.dp),
                                label = { Text("فاصله اقساط") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null,
                                        tint = MatureColors.Primary
                                    )
                                }
                            )

                            DropdownMenu(
                                expanded = expandedInterval,
                                onDismissRequest = { expandedInterval = false }
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

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = LoanGreenLight
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 14.dp,
                                    vertical = 11.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Payments,
                                    contentDescription = null,
                                    tint = LoanGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.size(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "قسط ماهیانه محاسبه‌شده",
                                        fontSize = 12.sp,
                                        color = MatureColors.TextSecondary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = if (calculatedInstallment > 0) {
                                            calculatedInstallment.toPersianCurrency()
                                        } else {
                                            "—"
                                        },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MatureColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = loanType.label,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedType = true },
                                shape = RoundedCornerShape(14.dp),
                                label = { Text("نوع وام") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.AccountBalance,
                                        contentDescription = null,
                                        tint = MatureColors.Primary
                                    )
                                }
                            )

                            DropdownMenu(
                                expanded = expandedType,
                                onDismissRequest = { expandedType = false }
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

                    item { LoanEditorSectionTitle(title = "تاریخ‌ها") }

                    item {
                        DateField(
                            label = "تاریخ وام",
                            value = startDate,
                            onDateSelected = { startDate = it },
                            context = context
                        )
                    }

                    item {
                        DateField(
                            label = "تاریخ اولین قسط",
                            value = firstInstallmentDate,
                            onDateSelected = { firstInstallmentDate = it },
                            context = context
                        )
                    }

                    item { LoanEditorSectionTitle(title = "ضمانت و توضیحات") }

                    item {
                        OutlinedTextField(
                            value = guaranteeNote,
                            onValueChange = { guaranteeNote = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp),
                            label = { Text("ضمانت و توضیحات") },
                            placeholder = { Text("توضیحات اختیاری") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    val interest = interestText.toDoubleOrNull() ?: 0.0
                    val installments = totalInstallmentsText.toIntOrNull() ?: 0
                    val startParts = parseJalaliDate(startDate)
                    val firstParts = parseJalaliDate(firstInstallmentDate)

                    if (
                        title.isBlank() ||
                        amount <= 0 ||
                        installments <= 0 ||
                        startParts == null
                    ) {
                        return@Button
                    }

                    val paidInstallmentCount = existingLoan?.payments?.size ?: 0

                    if (existingLoan != null && installments < paidInstallmentCount) {
                        return@Button
                    }

                    val finalInstallment = if (calculatedInstallment > 0) {
                        calculatedInstallment
                    } else {
                        installmentText.toLongOrNull() ?: 0L
                    }

                    val loan = LoanEntry(
                        id = existingLoan?.id ?: UUID.randomUUID().toString(),
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatureColors.Primary
                )
            ) {
                Text(
                    if (existingLoan == null) "ثبت وام" else "ذخیره تغییرات",
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
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(
                width = 4.dp,
                height = 20.dp
            ),
            shape = RoundedCornerShape(8.dp),
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
    var showPicker by remember { mutableStateOf(false) }

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
                    showPicker = true
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

    if (showPicker) {
        JalaliDatePickerDialog(
            initialDate = value,
            onDismissRequest = {
                showPicker = false
            },
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
























