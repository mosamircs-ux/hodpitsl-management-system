# 🏥 Smart Hospital SaaS & AI Clinical Assistant System
### نظام إدارة المستشفيات والعيادات الطبية السحابي المدعوم بالذكاء الاصطناعي (Gemini AI)

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android_Studio-2023+-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-UI-4285F4?style=for-the-badge&logo=android&logoColor=white)
![Google Gemini](https://img.shields.io/badge/Google_Gemini-3.5_Flash-8E44AD?style=for-the-badge&logo=google&logoColor=white)
![Room Database](https://img.shields.io/badge/Room-Local_DB-4A154B?style=for-the-badge&logo=sqlite&logoColor=white)

---

## 📝 About The Project / عن المشروع

**Smart Hospital SaaS** هو تطبيق أندرويد متكامل وحديث مخصص لإدارة المستشفيات والعيادات الطبية والمراكز الصحية وفق نموذج الاشتراك السحابي متعدد المستأجرين (**Multi-Tenant SaaS**). تم بناء التطبيق بأحدث تقنيات تطوير أندرويد (**Kotlin & Jetpack Compose**) ويعتمد على قاعدة بيانات محليّة فائقة السرعة (**Room Database**) مدعومة بنظام المساعدة السريرية والتشخيص الذكي بواسطة **Google Gemini AI (3.5 Flash)**.

يوفر النظام حلولاً شمولية لإدارة جميع العمليات التشغيلية داخل المنشأة الطبية بدءاً من استقبال المرضى وتتبع التنويم الداخلي (IPD) والعيادات الخارجية (OPD)، مروراً بتنظيم طواقم الأطباء والمواعيد، وصولاً إلى إدارة المالية وصيدلية المستشفى وإصدار الفواتير.

---

## ✨ Key Features / المميزات الرئيسية

### 📊 1. لوحة التحكم السحابية ومؤشرات الأداء التشغيلي (SaaS Dashboard & KPIs)
- متابعة فورية لأعداد المرضى المراجعين (مرضى التنويم IPD والعيادات الخارجية OPD).
- عرض إجمالي التحصيلات المالية والذمم المستحقة غير المسددة.
- تتبع حالة الاشتراك السحابي للمنشأة (SaaS Gold / Silver / Platinum) ومستويات الاستهلاك.

### 👥 2. سجلات المرضى ورعاية التنويم والعيادات (Patient Management)
- تسجيل مرن للمرضى الجدد مع تصنيف مباشر (تنويم داخلي IPD / عيادات خارجية OPD).
- تسكير أرقام الغرف ومتابعة الأعراض والشكاوى السريرية وفصائل الدم.
- محرك بحث سريعي باسم المريض أو رقم الجوال مع إمكانية تحديث الشكاوى الطبية فورياً.

### 👨‍⚕️ 3. طواقم الأطباء والعيادات المتخصصة (Doctors & Specialties)
- إدخال وتنظيم الأطباء حسب التخصصات الطبية (جراحة العظام، الأطفال والرضع، الباطنية، العيون، وغيرها).
- تتبع حالة الطبيب الفورية (متاح، في غرفة العمليات، خارج المناوبة).
- جدول مناوبات وأيام العمل والتواصل السريع.

### 🤖 4. المساعد السريري الذكي المدعوم بـ Google Gemini AI
- **تشخيص استرشادي فوري:** توليد توصيات طبية وفحوصات مخبرية وإرشادات عاجلة بناءً على الأعراض المسجلة للمريض.
- **تحليل إرشاد الطوارئ:** مساعدة الكادر الطبي على اتخاذ القرارات الأولية الفورية بنقرة واحدة.

### 📅 5. جدول المواعيد والاستشارات الطبية (Smart Appointment Booking)
- جدولة المواعيد مع الربط التلقائي بالطبيب المعالج وتوقيت الزيارة.
- استعراض حالات المواعيد (مؤكد، قيد الانتظار، مكتمل، ملغي).

### 💳 6. المالية وصيدلية المستشفى السحابية (Billing & Cloud Pharmacy)
- **الصيدلية:** إدارة مخزون الأدوية وتصنيفها (مسكنات، مضادات حيوية، فيتامينات) وتحديد الكميات وأسعار الوحدات.
- **المالية:** إصدار الفواتير الطبية ورسوم الكشفيات، متابعة المبالغ المسددة والغير مسددة، وتحصيل المبالغ فورياً.

---

## 📸 App Screenshots / لقطات من التطبيق

<div align="center">

### 📊 1. Dashboard & Operations / لوحة التحكم الرئيسية
![Dashboard](screenshots/Screenshot%202026-06-28%20112438.png)
*لوحة تحكم SaaS تفاعلية تعطي نظرة شاملة على مؤشرات الأداء والمالية وتفاصيل الاشتراك.*

---

### 👥 2. Patient Management & Registration / إدارة المرضى وتسجيل السجلات
<img src="screenshots/Screenshot%202026-06-28%20112506.png" width="45%" /> <img src="screenshots/Screenshot%202026-06-28%20112522.png" width="45%" />
*سجلات المرضى (يمين) ونموذج تسجيل ملف مريض طبي جديد بتصنيف IPD/OPD (يسار).*

---

### 👨‍⚕️ 3. Doctors & Medical Staff / طواقم الأطباء والعيادات
<img src="screenshots/Screenshot%202026-06-28%20112544.png" width="45%" /> <img src="screenshots/Screenshot%202026-06-28%20112601.png" width="45%" />
*قائمة الكادر الطبي وحالتهم التشغيلية (يمين) ونموذج إضافة طبيب جديد للطاقم (يسار).*

---

### 🤖 4. Gemini AI Diagnosis & Appointments / الاستشارات والتشخيص الذكي
<img src="screenshots/Screenshot%202026-06-28%20112619.png" width="45%" /> <img src="screenshots/Screenshot%202026-06-28%20112632.png" width="45%" />
*توصيات التشخيص الاسترشادي بواسطة الذكاء الاصطناعي Gemini (يمين) وجدولة المواعيد السريرية (يسار).*

---

### 💳 5. Billing & Cloud Pharmacy / المالية وصيدلية المستشفى
<img src="screenshots/Screenshot%202026-06-28%20112646.png" width="30%" /> <img src="screenshots/Screenshot%202026-06-28%20112700.png" width="30%" /> <img src="screenshots/Screenshot%202026-06-28%20112716.png" width="30%" />
*إدارة الفواتير والذمم (يمين)، إضافة أصناف الصيدلية (وسط)، وإنشاء فواتير علاجية جديدة (يسار).*

</div>

---

## 🛠️ Run Locally / التشغيل المحلي

**Prerequisites / المطلب الأساسي:** [Android Studio](https://developer.android.com/studio) (Hedgehog or newer recommended)

### Step-by-Step Instructions / خطوات التشغيل:

1. **Open Project:** Open Android Studio, select **Open**, and select this project's root directory.
2. **Sync Gradle:** Allow Android Studio to import and build Gradle dependencies.
3. **API Key Setup:** Create a file named `.env` in the project root directory and add your Gemini API key:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   *(Refer to `.env.example` for reference).*
4. **Gradle Configuration Adjustment:** In `app/build.gradle.kts`, remove or comment out the following debug signing line if running on a standard emulator:
   ```kotlin
   signingConfig = signingConfigs.getByName("debugConfig")
   ```
5. **Run App:** Select your physical device or Android Virtual Device (AVD) and click **Run (Shift+F10)**.

---

<div align="center">
Developed with ❤️ for Smart Healthcare Systems
</div>

