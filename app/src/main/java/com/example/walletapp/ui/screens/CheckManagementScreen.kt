package com.example.walletapp.ui.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Numbers
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletapp.data.CheckEntry
import com.example.walletapp.data.CheckStatus
import com.example.walletapp.data.CheckStore
import com.example.walletapp.data.CheckType
import com.example.walletapp.data.JalaliCalendar
import com.example.walletapp.data.toPersianCurrency
import java.util.Calendar
import java.util.UUID

private val CheckOrange = Color(0xFFFF8A00)
private val CheckOrangeLight = Color(0xFFFFF1DD)
private val CheckGreen = Color(0xFF16A34A)
private val CheckGreenLight = Color(0xFFE8F7ED)
private val CheckRed = Color(0xFFDC2626)
private val CheckBlue = Color(0xFF2563EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckManagementScreen(
    onBack: () -> Unit
) {

    var showEditor by remember {
        mutableStateOf(false)
    }

    var editingCheck by remember {
        mutableStateOf<CheckEntry?>(null)
    }

    var deleteCheck by remember {
        mutableStateOf<CheckEntry?>(null)
    }

    Scaffold(
        topBar = {

            TopAppBar(
                title = {
                    Text(
                        text = "\u0645\u062f\u06cc\u0631\u06cc\u062a \u0686\u06a9",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription =
                                "\u0628\u0627\u0632\u06af\u0634\u062a"
                        )
                    }
                }
            )
        },
        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    editingCheck = null
                    showEditor = true
                },
                containerColor = CheckOrange,
                contentColor = Color.White
            ) {

                Icon(
                    Icons.Filled.Add,
                    contentDescription =
                        "\u0627\u0641\u0632\u0648\u062f\u0646 \u0686\u06a9"
                )
            }
        }
    ) { padding ->

        if (CheckStore.checks.isEmpty()) {

            EmptyCheckState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAdd = {
                    editingCheck = null
                    showEditor = true
                }
            )

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding =
                    androidx.compose.foundation.layout
                        .PaddingValues(
                            horizontal = 16.dp,
                            vertical = 16.dp
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                items(
                    items = CheckStore.checks,
                    key = { it.id }
                ) { check ->

                    CheckCard(
                        check = check,
                        onEdit = {
                            editingCheck = check
                            showEditor = true
                        },
                        onDelete = {
                            deleteCheck = check
                        }
                    )
                }
            }
        }
    }

    if (showEditor) {

        CheckEditorDialog(
            existingCheck = editingCheck,
            onDismiss = {
                showEditor = false
                editingCheck = null
            },
            onSave = { check ->

                if (editingCheck == null) {
                    CheckStore.add(check)
                } else {
                    CheckStore.update(check)
                }

                showEditor = false
                editingCheck = null
            }
        )
    }

    deleteCheck?.let { check ->

        AlertDialog(
            onDismissRequest = {
                deleteCheck = null
            },
            title = {
                Text(
                    text = "\u062d\u0630\u0641 \u0686\u06a9",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text =
                        "\u0622\u06cc\u0627 \u0627\u0632 \u062d\u0630\u0641 \u0686\u06a9 \u00ab${check.title}\u00bb \u0645\u0637\u0645\u0626\u0646 \u0647\u0633\u062a\u06cc\u062f\u061f"
                )
            },
            confirmButton = {

                Button(
                    onClick = {
                        CheckStore.remove(check.id)
                        deleteCheck = null
                    }
                ) {
                    Text("\u062d\u0630\u0641")
                }
            },
            dismissButton = {

                OutlinedButton(
                    onClick = {
                        deleteCheck = null
                    }
                ) {
                    Text("\u0627\u0646\u0635\u0631\u0627\u0641")
                }
            }
        )
    }
}

@Composable
private fun EmptyCheckState(
    modifier: Modifier,
    onAdd: () -> Unit
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            Surface(
                modifier = Modifier.size(82.dp),
                shape = RoundedCornerShape(24.dp),
                color = CheckOrangeLight
            ) {

                Box(
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        Icons.Filled.FactCheck,
                        contentDescription = null,
                        modifier = Modifier.size(42.dp),
                        tint = CheckOrange
                    )
                }
            }

            Text(
                text =
                    "\u0647\u0646\u0648\u0632 \u0686\u06a9\u06cc \u062b\u0628\u062a \u0646\u0634\u062f\u0647 \u0627\u0633\u062a",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "\u0686\u06a9\u0647\u0627\u06cc \u062f\u0631\u06cc\u0627\u0641\u062a\u06cc \u0648 \u067e\u0631\u062f\u0627\u062e\u062a\u06cc \u062e\u0648\u062f \u0631\u0627 \u0645\u062f\u06cc\u0631\u06cc\u062a \u06a9\u0646\u06cc\u062f",
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Button(
                onClick = onAdd
            ) {

                Icon(
                    Icons.Filled.Add,
                    contentDescription = null
                )

                Spacer(Modifier.size(6.dp))

                Text(
                    "\u062b\u0628\u062a \u0686\u06a9 \u062c\u062f\u06cc\u062f"
                )
            }
        }
    }
}

@Composable
private fun CheckCard(
    check: CheckEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val statusColor =
        when (check.status) {

            CheckStatus.PENDING ->
                CheckOrange

            CheckStatus.RECEIVED,
            CheckStatus.PAID ->
                CheckGreen

            CheckStatus.RETURNED ->
                CheckRed

            CheckStatus.CANCELLED ->
                Color.Gray
        }

    val statusBackground =
        when (check.status) {

            CheckStatus.PENDING ->
                CheckOrangeLight

            CheckStatus.RECEIVED,
            CheckStatus.PAID ->
                CheckGreenLight

            CheckStatus.RETURNED ->
                Color(0xFFFFE8E8)

            CheckStatus.CANCELLED ->
                Color.LightGray
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color =
                        if (check.type == CheckType.RECEIVED)
                            CheckGreenLight
                        else
                            CheckOrangeLight
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            Icons.Filled.FactCheck,
                            contentDescription = null,
                            tint =
                                if (check.type == CheckType.RECEIVED)
                                    CheckGreen
                                else
                                    CheckOrange
                        )
                    }
                }

                Spacer(Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = check.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = check.type.label,
                        fontSize = 13.sp,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription =
                            "\u0648\u06cc\u0631\u0627\u06cc\u0634"
                    )
                }

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription =
                            "\u062d\u0630\u0641",
                        tint = CheckRed
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = statusBackground
            ) {

                Column(
                    modifier = Modifier.padding(14.dp)
                ) {

                    Text(
                        text = "\u0645\u0628\u0644\u063a \u0686\u06a9",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )

                    Text(
                        text =
                            check.amount
                                .toPersianCurrency(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )

                    Spacer(
                        Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "\u0648\u0636\u0639\u06cc\u062a: ${check.status.label}",
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                CheckSmallInfo(
                    modifier = Modifier.weight(1f),
                    title = "\u0628\u0627\u0646\u06a9",
                    value =
                        check.bank.ifBlank {
                            "\u2014"
                        }
                )

                CheckSmallInfo(
                    modifier = Modifier.weight(1f),
                    title = "\u0634\u0645\u0627\u0631\u0647 \u0686\u06a9",
                    value =
                        check.checkNumber.ifBlank {
                            "\u2014"
                        }
                )
            }

            CheckSmallInfo(
                modifier = Modifier.fillMaxWidth(),
                title = "\u062a\u0627\u0631\u06cc\u062e \u0633\u0631\u0631\u0633\u06cc",
                value =
                    "${check.dueYear}/${check.dueMonth.toString().padStart(2, '0')}/${check.dueDay.toString().padStart(2, '0')}"
            )

            if (check.personName.isNotBlank()) {

                CheckSmallInfo(
                    modifier = Modifier.fillMaxWidth(),
                    title =
                        if (check.type == CheckType.RECEIVED)
                            "\u0635\u0627\u062f\u0631\u06a9\u0646\u0646\u062f\u0647"
                        else
                            "\u062f\u0631\u06cc\u0627\u0641\u062a\u200c\u06a9\u0646\u0646\u062f\u0647",
                    value = check.personName
                )
            }
        }
    }
}

@Composable
private fun CheckSmallInfo(
    modifier: Modifier,
    title: String,
    value: String
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color =
            MaterialTheme.colorScheme
                .surfaceVariant
    ) {

        Column(
            modifier = Modifier.padding(10.dp)
        ) {

            Text(
                text = title,
                fontSize = 11.sp,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(3.dp)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckEditorDialog(
    existingCheck: CheckEntry?,
    onDismiss: () -> Unit,
    onSave: (CheckEntry) -> Unit
) {

    val context = androidx.compose.ui.platform
        .LocalContext.current

    var title by remember {
        mutableStateOf(
            existingCheck?.title ?: ""
        )
    }

    var type by remember {
        mutableStateOf(
            existingCheck?.type
                ?: CheckType.RECEIVED
        )
    }

    var checkNumber by remember {
        mutableStateOf(
            existingCheck?.checkNumber ?: ""
        )
    }

    var bank by remember {
        mutableStateOf(
            existingCheck?.bank ?: ""
        )
    }

    var amount by remember {
        mutableStateOf(
            existingCheck?.amount
                ?.toString()
                ?: ""
        )
    }

    var personName by remember {
        mutableStateOf(
            existingCheck?.personName ?: ""
        )
    }

    var issueDate by remember {

        mutableStateOf(
            existingCheck?.let {
                "${it.issueYear}/${it.issueMonth.toString().padStart(2, '0')}/${it.issueDay.toString().padStart(2, '0')}"
            } ?: JalaliCalendar.todaySlashed()
        )
    }

    var dueDate by remember {

        mutableStateOf(
            existingCheck?.let {
                "${it.dueYear}/${it.dueMonth.toString().padStart(2, '0')}/${it.dueDay.toString().padStart(2, '0')}"
            } ?: ""
        )
    }

    var status by remember {
        mutableStateOf(
            existingCheck?.status
                ?: CheckStatus.PENDING
        )
    }

    var description by remember {
        mutableStateOf(
            existingCheck?.description ?: ""
        )
    }

    var expandedType by remember {
        mutableStateOf(false)
    }

    var expandedStatus by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        title = {

            Text(
                text =
                    if (existingCheck == null)
                        "\u062b\u0628\u062a \u0686\u06a9 \u062c\u062f\u06cc\u062f"
                    else
                        "\u0648\u06cc\u0631\u0627\u06cc\u0634 \u0686\u06a9",
                fontWeight = FontWeight.Bold
            )
        },
        text = {

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(11.dp)
            ) {

                item {

                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text(
                                "\u0639\u0646\u0648\u0627\u0646 \u0686\u06a9"
                            )
                        }
                    )
                }

                item {

                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = {
                            expandedType =
                                !expandedType
                        }
                    ) {

                        OutlinedTextField(
                            value = type.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = {
                                Text(
                                    "\u0646\u0648\u0639 \u0686\u06a9"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults
                                    .TrailingIcon(
                                        expanded =
                                            expandedType
                                    )
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = {
                                expandedType = false
                            }
                        ) {

                            CheckType.entries.forEach {
                                itemType ->

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            itemType.label
                                        )
                                    },
                                    onClick = {
                                        type = itemType
                                        expandedType =
                                            false
                                    }
                                )
                            }
                        }
                    }
                }

                item {

                    CheckInputTile(
                        title =
                            "\u0645\u0628\u0644\u063a \u0686\u06a9",
                        value = amount,
                        onValueChange = {
                            amount =
                                it.filter {
                                    c -> c.isDigit()
                                }
                        },
                        background =
                            CheckOrangeLight,
                        accent = CheckOrange,
                        icon =
                            Icons.Filled.Payments
                    )
                }

                item {

                    OutlinedTextField(
                        value = checkNumber,
                        onValueChange = {
                            checkNumber = it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text(
                                "\u0634\u0645\u0627\u0631\u0647 \u0686\u06a9"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Numbers,
                                contentDescription =
                                    null
                            )
                        }
                    )
                }

                item {

                    OutlinedTextField(
                        value = bank,
                        onValueChange = {
                            bank = it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text(
                                "\u0646\u0627\u0645 \u0628\u0627\u0646\u06a9"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription =
                                    null
                            )
                        }
                    )
                }

                item {

                    OutlinedTextField(
                        value = personName,
                        onValueChange = {
                            personName = it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text(
                                if (type == CheckType.RECEIVED)
                                    "\u0635\u0627\u062f\u0631\u06a9\u0646\u0646\u062f\u0647"
                                else
                                    "\u062f\u0631\u06cc\u0627\u0641\u062a\u200c\u06a9\u0646\u0646\u062f\u0647"
                            )
                        }
                    )
                }

                item {

                    DateField(
                        label =
                            "\u062a\u0627\u0631\u06cc\u062e \u0635\u062f\u0648\u0631",
                        value = issueDate,
                        onDateSelected = {
                            issueDate = it
                        },
                        context = context
                    )
                }

                item {

                    DateField(
                        label =
                            "\u062a\u0627\u0631\u06cc\u062e \u0633\u0631\u0631\u0633\u06cc",
                        value = dueDate,
                        onDateSelected = {
                            dueDate = it
                        },
                        context = context
                    )
                }

                item {

                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = {
                            expandedStatus =
                                !expandedStatus
                        }
                    ) {

                        OutlinedTextField(
                            value = status.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = {
                                Text(
                                    "\u0648\u0636\u0639\u06cc\u062a \u0686\u06a9"
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults
                                    .TrailingIcon(
                                        expanded =
                                            expandedStatus
                                    )
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = {
                                expandedStatus = false
                            }
                        ) {

                            CheckStatus.entries.forEach {
                                itemStatus ->

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            itemStatus.label
                                        )
                                    },
                                    onClick = {
                                        status =
                                            itemStatus
                                        expandedStatus =
                                            false
                                    }
                                )
                            }
                        }
                    }
                }

                item {

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        minLines = 2,
                        label = {
                            Text(
                                "\u062a\u0648\u0636\u06cc\u062d\u0627\u062a"
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {

            Button(
                onClick = {

                    val amountValue =
                        amount.toLongOrNull()
                            ?: 0L

                    val issueParts =
                        parseJalaliDate(issueDate)

                    val dueParts =
                        parseJalaliDate(dueDate)

                    if (
                        title.isBlank() ||
                        amountValue <= 0 ||
                        issueParts == null ||
                        dueParts == null
                    ) {
                        return@Button
                    }

                    val check =
                        CheckEntry(
                            id =
                                existingCheck?.id
                                    ?: UUID.randomUUID()
                                        .toString(),

                            title =
                                title.trim(),

                            type = type,

                            checkNumber =
                                checkNumber.trim(),

                            bank =
                                bank.trim(),

                            amount =
                                amountValue,

                            personName =
                                personName.trim(),

                            issueYear =
                                issueParts[0],

                            issueMonth =
                                issueParts[1],

                            issueDay =
                                issueParts[2],

                            dueYear =
                                dueParts[0],

                            dueMonth =
                                dueParts[1],

                            dueDay =
                                dueParts[2],

                            status = status,

                            description =
                                description.trim()
                        )

                    onSave(check)
                }
            ) {

                Text(
                    if (existingCheck == null)
                        "\u062b\u0628\u062a \u0686\u06a9"
                    else
                        "\u0630\u062e\u06cc\u0631\u0647 \u062a\u063a\u06cc\u06cc\u0631\u0627\u062a"
                )
            }
        },
        dismissButton = {

            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("\u0627\u0646\u0635\u0631\u0627\u0641")
            }
        }
    )
}

@Composable
private fun CheckInputTile(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    background: Color,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(
                    Modifier.size(8.dp)
                )

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Spacer(
                Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier =
                    Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Number
                    ),
                placeholder = {
                    Text(
                        "\u0648\u0627\u0631\u062f \u06a9\u0646\u06cc\u062f"
                    )
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
        modifier =
            Modifier.fillMaxWidth(),
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
                                        selected
                                            .timeInMillis
                                    )
                            )
                        },
                        calendar.get(
                            Calendar.YEAR
                        ),
                        calendar.get(
                            Calendar.MONTH
                        ),
                        calendar.get(
                            Calendar.DAY_OF_MONTH
                        )
                    ).show()
                }
            ) {

                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription =
                        "\u0627\u0646\u062a\u062e\u0627\u0628 \u062a\u0627\u0631\u06cc\u062e"
                )
            }
        }
    )
}

private fun parseJalaliDate(
    value: String
): IntArray? {

    val parts =
        value.split("/")

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
