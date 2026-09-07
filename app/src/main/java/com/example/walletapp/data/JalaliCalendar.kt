package com.example.walletapp.data

import java.util.Calendar

/**
 * Jalali/Persian calendar utilities.
 *
 * Gregorian -> Jalali conversion follows the well-known jalaali-js
 * compatible algorithm.
 *
 * Jalali -> Gregorian is implemented as the exact inverse conversion
 * needed by loan installment scheduling.
 */
object JalaliCalendar {

    private val gDaysInMonth =
        intArrayOf(
            0, 31, 59, 90, 120, 151,
            181, 212, 243, 273, 304, 334
        )

    private val jDaysInMonth =
        intArrayOf(
            31, 31, 31, 31, 31, 31,
            30, 30, 30, 30, 30, 29
        )

    fun gregorianToJalali(
        gy: Int,
        gm: Int,
        gd: Int
    ): Triple<Int, Int, Int> {

        var gyLocal = gy
        var jy: Int

        if (gyLocal > 1600) {
            jy = 979
            gyLocal -= 1600
        } else {
            jy = 0
            gyLocal -= 621
        }

        val gy2 =
            if (gm > 2) gyLocal + 1
            else gyLocal

        var days =
            (365 * gyLocal) +
                ((gy2 + 3) / 4) -
                ((gy2 + 99) / 100) +
                ((gy2 + 399) / 400) -
                80 +
                gd +
                gDaysInMonth[gm - 1]

        jy += 33 * (days / 12053)
        days %= 12053

        jy += 4 * (days / 1461)
        days %= 1461

        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }

        val jm: Int
        val jd: Int

        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }

        return Triple(jy, jm, jd)
    }

    /**
     * Converts a Jalali date to Gregorian.
     *
     * Returns (year, month[1-12], day).
     */
    fun jalaliToGregorian(
        jy: Int,
        jm: Int,
        jd: Int
    ): Triple<Int, Int, Int> {

        var jyLocal = jy

        var gy: Int

        if (jyLocal > 979) {
            gy = 1600
            jyLocal -= 979
        } else {
            gy = 621
        }

        var days =
            (365 * jyLocal) +
                (jyLocal / 33) * 8 +
                ((jyLocal % 33 + 3) / 4) +
                78 +
                jd

        if (jm < 7) {
            days += (jm - 1) * 31
        } else {
            days += ((jm - 7) * 30) + 186
        }

        gy += 400 * (days / 146097)
        days %= 146097

        if (days > 36524) {

            gy += 100 * ((days - 1) / 36524)

            days =
                (days - 1) % 36524

            if (days >= 365) {
                days += 1
            }
        }

        gy += 4 * (days / 1461)
        days %= 1461

        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }

        val gd = days + 1

        val leap =
            (gy % 4 == 0 && gy % 100 != 0) ||
                (gy % 400 == 0)

        val monthLengths =
            if (leap) {
                intArrayOf(
                    31, 29, 31, 30, 31, 30,
                    31, 31, 30, 31, 30, 31
                )
            } else {
                intArrayOf(
                    31, 28, 31, 30, 31, 30,
                    31, 31, 30, 31, 30, 31
                )
            }

        var remaining = gd
        var gm = 1

        while (
            gm <= 12 &&
            remaining > monthLengths[gm - 1]
        ) {
            remaining -= monthLengths[gm - 1]
            gm++
        }

        return Triple(
            gy,
            gm,
            remaining
        )
    }

    /**
     * Creates the local-device epoch millis for a Jalali date.
     *
     * Time is deliberately set to the beginning of the local day.
     * This makes countdown calculations stable and avoids UTC/timezone
     * date shifts.
     */
    fun jalaliToEpochMillis(
        jy: Int,
        jm: Int,
        jd: Int
    ): Long {

        val (gy, gm, gd) =
            jalaliToGregorian(
                jy,
                jm,
                jd
            )

        return Calendar.getInstance().apply {
            clear()

            set(
                Calendar.YEAR,
                gy
            )

            set(
                Calendar.MONTH,
                gm - 1
            )

            set(
                Calendar.DAY_OF_MONTH,
                gd
            )

            set(
                Calendar.HOUR_OF_DAY,
                9
            )

            set(
                Calendar.MINUTE,
                0
            )

            set(
                Calendar.SECOND,
                0
            )

            set(
                Calendar.MILLISECOND,
                0
            )
        }.timeInMillis
    }

    val monthNames =
        listOf(
            "فروردین",
            "اردیبهشت",
            "خرداد",
            "تیر",
            "مرداد",
            "شهریور",
            "مهر",
            "آبان",
            "آذر",
            "دی",
            "بهمن",
            "اسفند"
        )

    fun formatSlashed(
        gy: Int,
        gm: Int,
        gd: Int
    ): String {

        val (jy, jm, jd) =
            gregorianToJalali(
                gy,
                gm,
                gd
            )

        val mm =
            jm.toString().padStart(
                2,
                '0'
            )

        val dd =
            jd.toString().padStart(
                2,
                '0'
            )

        return "$jy/$mm/$dd"
            .toPersianDigits()
    }

    fun formatLong(
        gy: Int,
        gm: Int,
        gd: Int
    ): String {

        val (jy, jm, jd) =
            gregorianToJalali(
                gy,
                gm,
                gd
            )

        return "$jd ${monthNames[jm - 1]} $jy"
            .toPersianDigits()
    }

    fun todaySlashed(): String {

        val cal =
            Calendar.getInstance()

        return formatSlashed(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun fromEpochMillisSlashed(
        epochMillis: Long
    ): String {

        val cal =
            Calendar.getInstance()

        cal.timeInMillis =
            epochMillis

        return formatSlashed(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun daysAgoSlashed(
        daysAgo: Int
    ): String {

        val cal =
            Calendar.getInstance()

        cal.add(
            Calendar.DAY_OF_MONTH,
            -daysAgo
        )

        return formatSlashed(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
