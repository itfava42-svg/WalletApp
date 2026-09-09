package com.example.walletapp.data

import java.util.Calendar

/**
 * Gregorian -> Jalali (Persian/Shamsi) calendar conversion.
 *
 * Implements the well-known public-domain conversion algorithm (the same
 * one used by the widely-used "jalaali-js" library), which is accurate
 * across the whole practically-relevant date range.
 */
object JalaliCalendar {
    private val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

    /** Returns (jalaliYear, jalaliMonth[1-12], jalaliDay[1-31]) for a Gregorian date. */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        var gyLocal = gy
        var jy: Int
        if (gyLocal > 1600) {
            jy = 979
            gyLocal -= 1600
        } else {
            jy = 0
            gyLocal -= 621
        }
        val gy2 = if (gm > 2) gyLocal + 1 else gyLocal
        var days = (365 * gyLocal) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) - 80 + gd + gDaysInMonth[gm - 1]
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

    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
    )

    /** e.g. "۱۴۰۲/۰۷/۱۵" */
    fun formatSlashed(gy: Int, gm: Int, gd: Int): String {
        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)
        val mm = jm.toString().padStart(2, '0')
        val dd = jd.toString().padStart(2, '0')
        return "$jy/$mm/$dd".toPersianDigits()
    }

    /** e.g. "۱۵ مهر ۱۴۰۲" */
    fun formatLong(gy: Int, gm: Int, gd: Int): String {
        val (jy, jm, jd) = gregorianToJalali(gy, gm, gd)
        return "$jd ${monthNames[jm - 1]} $jy".toPersianDigits()
    }

    fun todaySlashed(): String {
        val cal = Calendar.getInstance()
        return formatSlashed(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    fun fromEpochMillisSlashed(epochMillis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        return formatSlashed(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    fun daysAgoSlashed(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo)
        return formatSlashed(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }
}