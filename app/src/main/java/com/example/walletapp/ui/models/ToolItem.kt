package com.example.walletapp.ui.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolAction {
    PERSONALIZATION,
    SECURITY,
    PROFILE,
    SAVINGS,
    DEBT_CREDIT,
    BILL_SPLIT,
    PRICES,
    OPERATIONS,
    REPORTS,
    CHECK_MANAGEMENT,
    LOAN_MANAGEMENT,
    FINANCIAL_CALENDAR,
    REMINDER,
    OPEN_SETTINGS_LIST,
    BANK_LIST,
    TRANSACTION_SYNC,
    SMS_LEARNING,
    THEME_BUILDER,
    EXPORT,
    TRANSPARENCY,
    TERMS,
    SUPPORT,
    UPDATE,
    SHARE,
}

data class ToolItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val action: ToolAction,
)

data class ToolSection(
    val title: String,
    val items: List<ToolItem>
)

/** گزینه‌های سریع بالای صفحه ابزارها */
val toolsTopRow = listOf(
    ToolItem(
        "پروفایل",
        "اطلاعات شخصی و حساب کاربری",
        Icons.Filled.Person,
        ToolAction.PROFILE
    ),

    ToolItem(
        "شخصی‌سازی",
        "تنظیمات ظاهری و عملکرد برنامه",
        Icons.Filled.Tune,
        ToolAction.PERSONALIZATION
    ),
    ToolItem(
        "تنظیمات",
        "امنیت، پشتیبان‌گیری و تنظیمات برنامه",
        Icons.Filled.Settings,
        ToolAction.OPEN_SETTINGS_LIST
    ),
)

/** ابزارهای اصلی مالی */
val toolsGrid = listOf(
    ToolItem(
        "پس‌انداز",
        "کیف پول، برداشت مستقیم و پس‌انداز خودکار",
        Icons.Filled.Savings,
        ToolAction.SAVINGS
    ),

    ToolItem(
        "قیمت‌ها",
        "قیمت لحظه‌ای طلا، سکه، ارز و سایر موارد",
        Icons.Filled.TrendingUp,
        ToolAction.PRICES
    ),
    ToolItem(
        "مدیریت چک",
        "ثبت و پیگیری چک‌های دریافتی و پرداختی",
        Icons.Filled.FactCheck,
        ToolAction.CHECK_MANAGEMENT
    ),
    ToolItem(
        "مدیریت وام",
        "ثبت و پیگیری وام‌های دریافتی و اقساط",
        Icons.Filled.AccountBalance,
        ToolAction.LOAN_MANAGEMENT
    ),
    ToolItem(
        "عملیات",
        "مدیریت سایر عملیات مالی برنامه",
        Icons.Filled.SwapHoriz,
        ToolAction.OPERATIONS
    ),
    ToolItem(
        "گزارش‌ها",
        "گزارش درآمد، هزینه، دسته‌بندی و حساب‌ها",
        Icons.Filled.Assessment,
        ToolAction.REPORTS
    ),
    ToolItem(
        "تقویم مالی",
        "مشاهده دخل و خرج در یک نگاه",
        Icons.Filled.CalendarMonth,
        ToolAction.FINANCIAL_CALENDAR
    ),
    ToolItem(
        "یادآور",
        "یادآوری اقساط و پرداخت‌های مهم",
        Icons.Filled.Alarm,
        ToolAction.REMINDER
    ),
)

/** فهرست کامل تنظیمات و امکانات بیشتر */
object SettingsMenu {

    val sections = listOf(

        ToolSection(
            title = "مدیریت داده‌ها",
            items = listOf(

                ToolItem(
                    "لیست بانک‌ها",
                    "مدیریت حساب‌های بانکی",
                    Icons.Filled.AccountBalance,
                    ToolAction.BANK_LIST
                ),

                ToolItem(
                    "همگام‌سازی داده‌ها",
                    "همگام‌سازی تراکنش‌ها و اطلاعات مالی با سرور",
                    Icons.Filled.Sync,
                    ToolAction.TRANSACTION_SYNC
                ),

                ToolItem(
                    "خروجی اکسل/PDF",
                    "دانلود گزارش‌های مالی",
                    Icons.Filled.FileDownload,
                    ToolAction.EXPORT
                ),

                ToolItem(
                    "یادگیری پیامک",
                    "مدیریت الگوهای یادگرفته‌شده پیامک‌های بانکی",
                    Icons.Filled.Settings,
                    ToolAction.SMS_LEARNING
                ),

                ToolItem(
                    "تم‌ساز",
                    "انتخاب رنگ و حالت نمایش برنامه",
                    Icons.Filled.Tune,
                    ToolAction.THEME_BUILDER
                ),
            )
        ),

        ToolSection(
            title = "پشتیبانی و اطلاعات",
            items = listOf(

                ToolItem(
                    "تنظیمات امنیتی",
                    "احراز هویت بیومتریک و رمز عبور",
                    Icons.Filled.Security,
                    ToolAction.SECURITY
                ),

                ToolItem(
                    "شفاف‌سازی",
                    "اطلاعات دسترسی و حریم خصوصی",
                    Icons.Filled.Visibility,
                    ToolAction.TRANSPARENCY
                ),

                ToolItem(
                    "قوانین و مقررات",
                    "مقررات استفاده از برنامه",
                    Icons.Filled.Description,
                    ToolAction.TERMS
                ),

                ToolItem(
                    "پشتیبانی",
                    "ارتباط با تیم پشتیبانی",
                    Icons.Filled.SupportAgent,
                    ToolAction.SUPPORT
                ),

                ToolItem(
                    "بررسی به‌روزرسانی",
                    "آخرین نسخه برنامه",
                    Icons.Filled.SystemUpdate,
                    ToolAction.UPDATE
                ),

                ToolItem(
                    "معرفی به دوستان",
                    "اشتراک‌گذاری برنامه",
                    Icons.Filled.Share,
                    ToolAction.SHARE
                ),
            )
        )
    )
}


