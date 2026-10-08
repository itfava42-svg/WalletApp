# CI-188 — ممیزی و پاک‌سازی باقی‌مانده‌های Legacy UI

## وضعیت
- Branch: ci/ci-46-navigation-entry-audit
- Scope: READ-ONLY audit of remaining legacy UI ownership.
- هدف: حذف/ادغام فقط مواردی که مالکیت مشترک تکراری و فعال دارند؛ بدون تغییر رفتار مالی، navigation یا داده.

## بررسی انجام‌شده
- درخت واقعی commit فعلی بررسی شد.
- فایل‌های legacy زیر در مسیر فعال `app/src/main` وجود ندارند:
  - `ui/components/AppComponents.kt`
  - `ui/components/AppScreenContainer.kt`
  - `ui/theme/Theme.kt`
  - `ui/theme/Type.kt`
  - `ui/theme/Color.kt`
  - `ui/theme/Dimens.kt`
- تنها موارد باقی‌مانده در `app/src/main/java/com/example/walletapp/ui/theme` فایل‌های backup با پسوند `.bak` هستند و در source فعال وارد build نمی‌شوند.
- مالکیت فعال shared UI/theme در `:core:designsystem` متمرکز است:
  - `ui/components/AppComponents.kt`
  - `ui/components/AppScreenContainer.kt`
  - `ui/components/PremiumUi.kt`
  - `ui/theme/Color.kt`
  - `ui/theme/Dimens.kt`
  - `ui/theme/Theme.kt`
  - `ui/theme/Type.kt`
- dependency یک‌طرفه `:app -> :core:designsystem` حفظ شده و dependency معکوس مشاهده نشد.

## تصمیم
هیچ حذف یا تغییر production لازم نیست. مواردی که در audit قبلی به‌عنوان legacy ownership ثبت شده بودند، در commit فعلی دیگر فایل فعال نیستند. بنابراین CI-188 به‌صورت audit/no-op production change تکمیل می‌شود.

## حفاظت
- رفتار مالی: بدون تغییر
- navigation: بدون تغییر
- data layer: بدون تغییر
- shared DesignSystem ownership: بدون duplicate فعال
- فونت‌های Vazirmatn: همچنان متعلق به DesignSystem

## معیار تکمیل
- duplicate ownership فعال برای shared component/theme وجود ندارد.
- dependency معکوس DesignSystem -> app وجود ندارد.
- build و GitHub Actions سبز.
