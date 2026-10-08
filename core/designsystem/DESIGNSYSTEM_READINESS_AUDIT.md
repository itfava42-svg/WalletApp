# CI-187 — ممیزی نهایی آمادگی DesignSystem برای توسعه بعدی

## وضعیت
- Branch: ci/ci-46-navigation-entry-audit
- Scope: READ-ONLY audit; no production behavior change.

## یافته‌های تأییدشده
1. core:designsystem مستقل از :app است و dependency معکوس ندارد.
2. DesignSystem مالک توکن‌های مشترک فعلی است: AppDimens، AppTextStyles، AppTypography و WalletAppTheme.
3. :app به :core:designsystem وابسته است.
4. فونت‌های Vazirmatn داخل resourceهای DesignSystem هستند و Type.kt از resourceهای همان ماژول استفاده می‌کند.
5. در :app هنوز legacy UI ownership باقی مانده است؛ از جمله ui/components/AppComponents.kt، ui/components/AppScreenContainer.kt و ui/theme/*. این موارد باید در مرحله بعد با بررسی مصرف واقعی، کنترل‌شده مهاجرت/ادغام/حذف شوند.
6. در این ممیزی هیچ dependency از DesignSystem به app مشاهده نشد و تغییر runtime برای CI-187 لازم نیست.

## تصمیم مرحله بعد
CI-188 — ممیزی و پاک‌سازی باقی‌مانده‌های Legacy UI:
- مصرف واقعی component/themeهای legacy را پیدا کن.
- فقط مواردی را حذف/ادغام کن که مالکیت مشترک تکراری دارند.
- رفتار مالی، navigation و داده‌ها تغییر نکند.
- بعد از build/test، فقط تغییرات سالم commit/push شوند.

## معیار تکمیل
- بدون dependency معکوس DesignSystem -> app
- بدون duplicate ownership برای component/theme مشترک فعال
- build و CI سبز
