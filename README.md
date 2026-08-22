# WalletApp — راهنمای راه‌اندازی

این پوشه یک پروژه‌ی Jetpack Compose برای صفحه‌ی داشبورد کیف پول است که بر اساس
`DESIGN.md` و طرح جدید شما ساخته شده.

## روش پیشنهادی (ساده‌ترین راه)

چون فایل‌های Gradle Wrapper (gradlew.jar و ...) اینجا قرار داده نشده‌اند، بهتر
است این‌طور عمل کنید:

1. در Android Studio یک پروژه‌ی جدید بسازید:
   `File > New > New Project > Empty Activity (Compose)`
   - Name: WalletApp
   - Package name: `com.example.walletapp`
   - Language: Kotlin
   - Minimum SDK: 24

2. بعد از اینکه پروژه ساخته و Sync شد، فایل‌های زیر را از این پوشه در پروژه‌ی
   جدید **جایگزین/اضافه** کنید (مسیرها دقیقاً مطابق ساختار پروژه‌ی Android Studio است):

   - `app/src/main/AndroidManifest.xml` → جایگزین کنید (برای `supportsRtl` و عنوان اپ)
   - `app/src/main/res/values/strings.xml` → جایگزین کنید
   - `app/src/main/java/com/example/walletapp/MainActivity.kt` → جایگزین کنید
   - `app/src/main/java/com/example/walletapp/ui/theme/Color.kt` → جایگزین کنید
   - `app/src/main/java/com/example/walletapp/ui/theme/Theme.kt` → جایگزین کنید
   - `app/src/main/java/com/example/walletapp/ui/theme/Type.kt` → جایگزین کنید
   - `app/src/main/java/com/example/walletapp/data/Transaction.kt` → اضافه کنید (پوشه data را بسازید)
   - `app/src/main/java/com/example/walletapp/data/PersianNumber.kt` → اضافه کنید
   - `app/src/main/java/com/example/walletapp/ui/components/DashboardComponents.kt` → اضافه کنید
   - `app/src/main/java/com/example/walletapp/ui/screens/DashboardScreen.kt` → اضافه کنید

3. در `app/build.gradle.kts` مطمئن شوید این وابستگی هم اضافه شده باشد (برای آیکون‌های
   بیشتر مثل `TrendingUp`, `ShoppingCart` و غیره):

   ```kotlin
   implementation("androidx.compose.material:material-icons-extended")
   ```

4. پروژه را Sync و Run کنید.

## فونت Vazirmatn (اختیاری، برای دقیق‌تر شدن ظاهر)

طرح اصلی از فونت Vazirmatn استفاده می‌کند. برای اضافه کردنش:

1. فایل‌های `.ttf` فونت Vazirmatn را از Google Fonts دانلود کنید.
2. یک پوشه‌ی `app/src/main/res/font/` بسازید و فایل‌ها را در آن قرار دهید
   (اسم فایل باید فقط حروف کوچک انگلیسی و `_` باشد، مثل `vazirmatn_regular.ttf`).
3. در `Type.kt`، خط `AppFontFamily` را طبق راهنمای داخل کامنت آن فایل عوض کنید.

## ساختار رنگ‌ها

همه‌ی رنگ‌ها مستقیماً از `DESIGN.md` (بخش `colors:`) گرفته شده‌اند تا هماهنگی کامل
بین طراحی وب و اپلیکیشن اندروید حفظ شود.

## مرحله بعد

صفحات دیگر (مثل حساب‌ها، عملیات، گزارشات) را که آماده کردید، همان‌طور که این صفحه
ساخته شد، هرکدام را به‌صورت یک فایل جدید در `ui/screens/` اضافه می‌کنیم.