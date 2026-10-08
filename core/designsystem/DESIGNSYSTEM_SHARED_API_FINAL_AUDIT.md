# CI-190 — ممیزی نهایی API/کامپوننت‌های مشترک

## هدف
تأیید اینکه APIهای مشترک DesignSystem قبل از هر حذف یا تغییر، مالکیت روشن و مصرف‌کننده معتبر دارند.

## یافته‌ها
- `:core:designsystem` مالک فعلی کامپوننت‌های مشترک و توکن‌های UI است.
- `AppComponents.kt` شامل APIهای مشترک فعال از جمله `AppCard`, `AppTile`, `AppTextField`, `AppBankCardCarousel` و کنترل‌های مشترک است.
- `AppScreenContainer.kt` و `PremiumUi.kt` در همین ماژول قرار دارند؛ وجود آن‌ها duplicate ownership محسوب نمی‌شود.
- پکیج‌های `com.example.walletapp.ui.components` و `com.example.walletapp.ui.theme` در حال حاضر داخل ماژول DesignSystem قرار دارند؛ بنابراین حذف/rename صرفاً بر اساس نام package می‌تواند مصرف‌کننده‌ها را بشکند.
- `Theme.kt`، `Type.kt` و `Dimens.kt` منبع واحد theme/typography/dimensions هستند و فونت‌های Vazirmatn نیز داخل منابع DesignSystem نگهداری می‌شوند.
- جست‌وجوی API کد در GitHub با endpoint عمومی Code Search پاسخ قابل اتکایی نداد (`incomplete_results=true` و صفر نتیجه)، بنابراین نتیجه منفی آن به‌عنوان اثبات unused بودن APIها استفاده نشد.
- در نتیجه، هیچ API یا کامپوننتی در این مرحله با شواهد کافی برای حذف یا rename شناسایی نشد.

## تصمیم
CI-190 یک مرحله ممیزی/ایمنی و بدون تغییر runtime است. حذف یا بازنام‌گذاری APIهای مشترک به مرحله بعد موکول می‌شود و فقط در صورت وجود شواهد مصرف‌نشدن واقعی انجام خواهد شد.

## نتیجه
- تغییر production: ندارد
- ریسک regression: حداقل
- نیاز به تغییر کد مشترک: ندارد
- وضعیت پیشنهادی: GREEN / NEXT
- مرحله بعد: CI-191
