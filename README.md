# Nomozly — Professional Islamic Android Application 🌙

> Zamonaviy, to‘liq oflayn ishlay oladigan, ko‘p modulli Clean Architecture asosida qurilgan islomiy Android ilovasi.

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-green?logo=jetpackcompose)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20Multi--Module-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 📱 Imkoniyatlar (Features)

* ⏰ **Namoz vaqtlari:**
  * Oflayn astronomik hisoblash (`adhan-java`) va onlayn Aladhan API sinxronizatsiyasi.
  * O‘zbekiston Musulmonlari idorasi, Butunjahon Islom Ligasi, Misr, Karachi, Umm al-Qura va boshqa hisoblash uslublari.
  * Keyingi namoz vaqtini ko‘rsatuvchi soniyali jonli teskari taymer.
  * Quyosh va oyning osmondagi harakati aks etgan dinamik manzara (`SunMoonSky`).

* 📖 **Qur’oni Karim:**
  * 114 ta to‘liq sura, 30 pora (Juz) va xatcho‘plar.
  * 4 ta tilda ma’nolar tarjimasi: O‘zbekcha (Lotin), Ўзбекча (Кирилл), Русский, English.
  * Mishari Rashid Alafasy qiroati (1.5 GB gacha oflayn kesh, ko‘p serverli tezkor CDN).
  * Oyatma-oyat yoki to‘liq sura bo‘yicha audio tinglash.

* 🧭 **Qibla Kompasi:**
  * Geomagnit va akselerometr datchiklari orqali real-vaqtda yo‘nalishni aniqlash.
  * Magnit og‘ishi (Magnetic Declination) hisobga olingan haqiqiy geografik aniqlik.
  * Qiblaga to‘g‘ri qaratilganda taktil (haptik) tebranish bilan tasdiqlash.

* 📅 **Taqvim:**
  * Milodiy va Hijriy sanalar bir vaqtda ko‘rinadigan interaktiv oylik taqvim.
  * Muhim islomiy sanalar bo‘yicha vertikal zanjir (timeline) va eslatmalar.
  * Oylik 30 kunlik to‘liq namoz vaqtlari jadvali.
  * Hijriy / Milodiy sanalarni bir zumda o‘zaro aylantiruvchi konvertor.

* 📿 **Tasbeh:**
  * 5 xil nafis mavzu: Zumrad, Oltin, Oq Dur, Qora Yoqut, Tabiiy Yog‘och.
  * Har bir bosishda fizik aylanuvchi marjon donalari (Canvas animatsiyasi).
  * 33, 99, 100 va cheksiz rejimlar, davra hisoblagich, taktil tebranish.

* 🕌 **Masjidlar:**
  * OpenStreetMap (Overpass API) va Yandex MapKit integratsiyasi.
  * Foydalanuvchiga eng yaqin masjidlar, masofasi, fotosuratlari va navigator orqali yo‘nalish olish.

* 🌟 **Allohning 99 Ismi:**
  * To‘liq 99 ta Asmo’ul Husna arabcha xattotligi, talaffuzi va 4 tildagi ma’nosi bilan.
  * Tezkor qidiruv va batafsil sharh oynasi.

* 🤲 **Namoz o‘rganish va Darsliklar:**
  * Erkaklar va ayollar uchun qadam-baqadam namoz o‘qishni o‘rganish.
  * Vektorli tana holatlari illyustratsiyalari (Niyat, Qiyom, Ruku, Sajda, Jalsa, Salom).
  * O‘qiladigan duolarning talaffuzi va arabcha matnlari.

* 🎥 **Makka va Madina Jonli Efiri:**
  * Ka'ba va Masjid an-Nabaviydan 24/7 jonli HD video translyatsiya (Media3 ExoPlayer).

* 🧮 **Qazo Namozlari:**
  * Qazo bo‘lgan namozlar hisobi, statistika va monitoring.

* 🌙 **Ro‘za / Ramazon:**
  * Saharlik va iftorlik vaqtlari, jonli sanagich va iftorlik duolari.

* ⚙️ **Moslashuvchan Sozlamalar:**
  * Tizim, Tungi va Kunduzgi mavzular.
  * 4 ta ranglar palitrasi.
  * Til tanlash (UZ Lotin, KRIL, RU, EN).

---

## 🏗 Arxitektura (Architecture)

Ilova **Google "Now in Android"** andazasiga muvofiq ko‘p modulli (Multi-module) qilib qurilgan:

```
├── app/                  # Yagona kirish nuqtasi, NavHost va Hilt Activity
├── build-logic/          # Gradle Convention Plugins (Hilt, Compose, Room, Android)
├── core/
│   ├── model/            # Sof Kotlin ma'lumotlar modellari
│   ├── common/           # Result wrapper, Dispatchers, TimeProvider
│   ├── domain/           # UseCase'lar, QiblaCalculator, HijriCalendar, Repositories
│   ├── data/             # Repositories implementation, oflayn kalkulyator, sinxronizatsiya
│   ├── database/         # Room 2.6 database, 7 ta entity va DAOlar
│   ├── network/          # Retrofit API xizmatlari (Aladhan, Quran, Overpass, Nominatim)
│   ├── datastore/        # Preferences DataStore (Foydalanuvchi sozlamalari, kesh)
│   ├── media/            # Jetpack Media3 (ExoPlayer), 1.5GB keshlovchi audio oqim
│   ├── notifications/    # AlarmManager orqali aniq azon signallari
│   ├── designsystem/     # NamozlyTheme, ranglar palitrasi, ikonlar, vizual komponentlar
│   └── ui/               # Lokalizatsiya, Heading datchiklari, UI helperlar
└── feature/              # 14 ta mustaqil feature moduli (Home, Quran, Qibla, Tasbih, ...)
```

---

## 🛠 Texnologiyalar (Tech Stack)

* **Til:** [Kotlin](https://kotlinlang.org/) 2.2.10
* **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material 3
* **Arxitektura:** Clean Architecture + MVI / MVVM, Unidirectional Data Flow (UDF)
* **Dependency Injection:** [Hilt](https://dagger.dev/hilt/) 2.60.1
* **Ma'lumotlar bazasi:** [Room](https://developer.android.com/training/data-storage/room) 2.6.1 + SQLite
* **Tarmoq (Network):** [Retrofit](https://square.github.io/retrofit/) 2.11 + OkHttp 4 + Kotlinx Serialization
* **Audio & Video:** [Jetpack Media3](https://developer.android.com/media/media3) ExoPlayer
* **Xaritalar:** OpenStreetMap (Overpass API) / Yandex MapKit
* **Astronomik hisob-kitoblar:** [Adhan-Java](https://github.com/batoulapps/adhan-java) 1.2.1

---

## 🚀 Loyihani ishga tushirish (Setup)

1. Loyihani yuklab oling:
```bash
git clone https://github.com/Eshmurodov-Doston1/Namozly.git
cd Namozly
```

2. `local.properties` faylida Yandex MapKit kalitini (ixtiyoriy) ko‘rsating:
```properties
sdk.dir=/path/to/android/sdk
YANDEX_MAPKIT_API_KEY=your_optional_yandex_mapkit_key
```

3. Loyihani yig‘ish va o‘rnatish:
```bash
./gradlew assembleDebug
```

---

## 📄 Litsenziya (License)

Ushbu loyiha [MIT Litsenziyasi](LICENSE) asosida ochiq manbali tarzda tarqatiladi.
Barcha mo‘min-musulmonlarga manfaatli bo‘lsin! 🤲
