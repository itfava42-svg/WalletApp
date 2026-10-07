package com.example.walletapp.ui.components

import com.example.walletapp.data.toPersianDigits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.JalaliCalendar
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles
import com.example.walletapp.ui.components.AppIconButton

@Composable
fun JalaliDatePickerDialog(
    initialDate: String? = null,
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val today =
        JalaliCalendar.fromEpochMillisSlashed(
            System.currentTimeMillis()
        )

    val todayParts =
        parseJalaliDate(today)
            ?: Triple(1400, 1, 1)

    val initialParts =
        parseJalaliDate(initialDate)

    var selectedYear by remember {
        mutableStateOf(
            initialParts?.first
                ?: todayParts.first
        )
    }

    var selectedMonth by remember {
        mutableStateOf(
            initialParts?.second
                ?: todayParts.second
        )
    }

    var selectedDay by remember {
        mutableStateOf(
            initialParts?.third
                ?: todayParts.third
        )
    }

    var displayYear by remember {
        mutableStateOf(selectedYear)
    }

    var displayMonth by remember {
        mutableStateOf(selectedMonth)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(
                imageVector =
                    Icons.Filled.CalendarMonth,
                contentDescription = null
            )
        },
        title = {
            Text(
                text = "انتخاب تاریخ",
                style = AppTextStyles.titleMd
            )
        },
        text = {
            Column(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppDimens.ItemGap),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    AppIconButton(
                        onClick = {
                            if (displayMonth == 1) {
                                displayMonth = 12
                                displayYear--
                            } else {
                                displayMonth--
                            }
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Filled.ChevronRight,
                            contentDescription =
                                "ماه قبل"
                        )
                    }

                    Text(
                        text =
                            JalaliCalendar
                                .monthNames[
                                    displayMonth - 1
                                ] +
                                " " +
                                displayYear
                                    .toString()
                                    .toPersianDigits(),
                        fontWeight =
                            FontWeight.Bold,
                        style = AppTextStyles.titleMd
                    )

                    IconButton(
                        onClick = {
                            if (displayMonth == 12) {
                                displayMonth = 1
                                displayYear++
                            } else {
                                displayMonth++
                            }
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Filled.ChevronLeft,
                            contentDescription =
                                "ماه بعد"
                        )
                    }
                }

                val daysInMonth =
                    when {
                        displayMonth <= 6 ->
                            31

                        displayMonth <= 11 ->
                            30

                        isJalaliLeapYear(
                            displayYear
                        ) ->
                            30

                        else ->
                            29
                    }

                val firstGregorian =
                    JalaliCalendar.jalaliToGregorian(
                        displayYear,
                        displayMonth,
                        1
                    )

                val firstCalendarDay =
                    java.util.Calendar
                        .getInstance()
                        .apply {
                            set(
                                firstGregorian.first,
                                firstGregorian.second - 1,
                                firstGregorian.third,
                                12,
                                0,
                                0
                            )

                            set(
                                java.util.Calendar.MILLISECOND,
                                0
                            )
                        }
                        .get(
                            java.util.Calendar.DAY_OF_WEEK
                        )

                val offset =
                    when (firstCalendarDay) {
                        java.util.Calendar.SATURDAY ->
                            0

                        java.util.Calendar.SUNDAY ->
                            1

                        java.util.Calendar.MONDAY ->
                            2

                        java.util.Calendar.TUESDAY ->
                            3

                        java.util.Calendar.WEDNESDAY ->
                            4

                        java.util.Calendar.THURSDAY ->
                            5

                        java.util.Calendar.FRIDAY ->
                            6

                        else ->
                            0
                    }

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "ش",
                        "ی",
                        "د",
                        "س",
                        "چ",
                        "پ",
                        "ج"
                    ).forEach { dayName ->

                        Box(
                            modifier =
                                Modifier.size(AppDimens.IconBox),
                            contentAlignment =
                                Alignment.Center
                        ) {
                            Text(
                                text = dayName,
                                fontWeight =
                                    FontWeight.Bold,
                                style = AppTextStyles.labelMd
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(AppDimens.IntroGap)
                )

                val totalCells =
                    ((offset + daysInMonth + 6) / 7) * 7

                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    for (
                        rowStart
                        in 0 until totalCells step 7
                    ) {

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceEvenly
                        ) {

                            for (
                                cell
                                in 0 until 7
                            ) {

                                val index =
                                    rowStart + cell

                                val day =
                                    index - offset + 1

                                if (
                                    day in
                                    1..daysInMonth
                                ) {

                                    val isSelected =
                                        displayYear ==
                                            selectedYear &&
                                            displayMonth ==
                                            selectedMonth &&
                                            day ==
                                            selectedDay

                                    Box(
                                        modifier =
                                            Modifier
                                                .size(38.dp)
                                                .background(
                                                    color =
                                                        if (isSelected) {
                                                            MaterialTheme.colorScheme.surfaceVariant
                                                        } else {
                                                            Color.Transparent
                                                        },
                                                    shape =
                                                        RoundedCornerShape(50)
                                                )
                                                .clickable {

                                                    selectedYear =
                                                        displayYear

                                                    selectedMonth =
                                                        displayMonth

                                                    selectedDay =
                                                        day
                                                },
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Text(
                                            text =
                                                day
                                                    .toString()
                                                    .toPersianDigits(),
                                            fontSize =
                                                14.sp,
                                            fontWeight =
                                                if (
                                                    isSelected
                                                ) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                }
                                        )
                                    }

                                } else {

                                    Spacer(
                                        modifier =
                                            Modifier.size(
                                                38.dp
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(AppDimens.ItemGap)
                )

                Text(
                    text =
                        formatJalaliDate(
                            selectedYear,
                            selectedMonth,
                            selectedDay
                        ),
                    fontWeight =
                        FontWeight.Bold,
                    style = AppTextStyles.titleSm
                )
            }
        },
        confirmButton = {
            AppTextButton(
                onClick = {
                    onDateSelected(
                        formatJalaliDate(
                            selectedYear,
                            selectedMonth,
                            selectedDay
                        )
                    )
                }
            ) {
                Text("انتخاب")
            }
        },
        dismissButton = {
            AppTextButton(
                onClick =
                    onDismissRequest
            ) {
                Text("لغو")
            }
        }
    )
}

private fun formatJalaliDate(
    year: Int,
    month: Int,
    day: Int
): String {
    return year
        .toString()
        .toPersianDigits() +
        "/" +
        month
            .toString()
            .padStart(2, '0')
            .toPersianDigits() +
        "/" +
        day
            .toString()
            .padStart(2, '0')
            .toPersianDigits()
}

private fun parseJalaliDate(
    value: String?
): Triple<Int, Int, Int>? {

    if (value.isNullOrBlank()) {
        return null
    }

    val parts =
        value.split("/")

    if (parts.size != 3) {
        return null
    }

    val year =
        parsePersianOrAsciiNumber(
            parts[0]
        ) ?: return null

    val month =
        parsePersianOrAsciiNumber(
            parts[1]
        ) ?: return null

    val day =
        parsePersianOrAsciiNumber(
            parts[2]
        ) ?: return null

    if (
        year < 1 ||
        month !in 1..12 ||
        day < 1 ||
        day > 31
    ) {
        return null
    }

    return Triple(
        year,
        month,
        day
    )
}

private fun parsePersianOrAsciiNumber(
    value: String
): Int? {

    var result = 0

    if (value.isBlank()) {
        return null
    }

    for (character in value) {

        val digit =
            when (character.code) {

                in 48..57 ->
                    character.code - 48

                in 0x06F0..0x06F9 ->
                    character.code - 0x06F0

                in 0x0660..0x0669 ->
                    character.code - 0x0660

                else ->
                    return null
            }

        result =
            result * 10 + digit
    }

    return result
}

private fun isJalaliLeapYear(
    year: Int
): Boolean {

    val currentYearEpoch =
        JalaliCalendar.jalaliToEpochMillis(
            year,
            1,
            1
        )

    val nextYearEpoch =
        JalaliCalendar.jalaliToEpochMillis(
            year + 1,
            1,
            1
        )

    val days =
        (
            nextYearEpoch -
                currentYearEpoch
            ) /
            (
                24L *
                    60L *
                    60L *
                    1000L
            )

    return days >= 366
}




