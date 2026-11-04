# Namozly — arxitektura

Multi-module Clean Architecture (Now in Android uslubida). Kotlin · Jetpack Compose · Hilt · Retrofit · Room · DataStore · Coroutines/Flow.

## Modullar ierarxiyasi

```
                        :app  (Activity, NavHost, Hilt grafi, BottomBar + Qibla kompasi)
                          │
        ┌─────────────────┼──────────────────────────────┐
   :feature:*         :core:data  :core:media  :core:notifications
 (UI + ViewModel)         │            │              │
        │            ┌────┼─────┬──────┘──────────────┘
        │       :core:network :core:database :core:datastore
        │                 │
        └──► :core:ui ──► :core:designsystem
        └──────────► :core:domain ──► :core:model
                         └────────► :core:common
```

### Qat'iy qoidalar
1. **Feature modul boshqa feature'ga bog'lanmaydi.** Ekranlar orasidagi o'tishlar faqat `:app/navigation/NamozlyNavHost.kt` da ulanadi.
2. **Feature faqat `core:domain` interfeyslarini ko'radi.** `core:data`, `core:network`, `core:database` ga to'g'ridan-to'g'ri bog'lanish taqiqlangan (convention plugin buni ta'minlaydi).
3. **`core:model`, `core:common`, `core:domain` — sof Kotlin (JVM)**, Android SDK'siz. Biznes mantiq (qibla, hijriy taqvim, namoz holati) shu yerda va unit-test qilinadi.
4. Implementatsiyalar (`*RepositoryImpl`, `ExoQuranPlayer`, `PrayerAlarmSchedulerImpl`) faqat Hilt `@Binds` orqali interfeysga ulanadi.
5. Offline-first: namoz vaqtlari Room keshidan, bo'lmasa qurilmada (adhan) hisoblanadi; Qur'on matni bir marta yuklanib saqlanadi.

| Modul | Turi | Vazifasi |
|---|---|---|
| `core:model` | JVM | Domen modellari, `LocalizedText`, lotin→kirill transliterator |
| `core:common` | JVM | Dispatcher qualifier'lari, `Result`, `TimeProvider` |
| `core:domain` | JVM | Repository interfeyslari, use-case'lar, `QiblaCalculator`, `HijriCalendar` |
| `core:network` | Android | Retrofit servislari va DTO'lar |
| `core:database` | Android | Room: namoz keshi, oyatlar, tasbeh, qazo, bildirishnomalar |
| `core:datastore` | Android | Sozlamalar, joylashuv, profil (DataStore Preferences) |
| `core:data` | Android | Repository implementatsiyalari, oflayn hisoblash, statik kontent |
| `core:media` | Android | ExoPlayer — Qur'on qiroati |
| `core:notifications` | Android | AlarmManager, kanallar, BroadcastReceiver'lar |
| `core:designsystem` | Compose | Tema, ranglar, NavBar, 3D Qibla kompasi, quyosh/oy animatsiyasi |
| `core:ui` | Compose | Umumiy matnlar (4 til), sensor (kompas), formatlash |
| `feature:*` | Compose | home, quran, qibla, tasbih (+musobaqa), calendar, mosques, names, duas, qaza, ramadan, learn, live, settings |

## Ochiq manbalar (API)
| Ma'lumot | Manba | Kalit |
|---|---|---|
| Namoz vaqtlari | `api.aladhan.com/v1/calendar` + oflayn `adhan-java` | yo'q |
| Qur'on (arabcha, uz-lotin, ru, en) | `api.alquran.cloud` (`quran-uthmani`, `uz.sodik`, `ru.kuliev`, `en.sahih`) | yo'q |
| Qur'on (uz-kirill) | `quranenc.com` (`uzbek_sadiq`) | yo'q |
| Qiroat audio | `cdn.islamic.network` (Mishary Alafasy) | yo'q |
| Masjidlar | OpenStreetMap Overpass | yo'q |
| Shahar qidiruvi | OpenStreetMap Nominatim | yo'q (User-Agent) |
| Masjid rasmlari | Wikipedia REST summary | yo'q |
| Xarita | Yandex MapKit lite | **bor** (`local.properties`) |

## Sozlash
`local.properties` ga qo'shing:
```
YANDEX_MAPKIT_API_KEY=sizning-kalitingiz
```
Kalit bo'lmasa ilova ishlaydi, faqat masjidlar xaritasi o'rniga ro'yxat ko'rsatiladi.

## Yangi feature qo'shish
1. `feature/xyz/build.gradle.kts`: `plugins { alias(libs.plugins.namozly.android.feature) }`
2. `settings.gradle.kts`: `include(":feature:xyz")`
3. `@Serializable data object XyzRoute` + `NavGraphBuilder.xyzScreen()` + `NavController.navigateToXyz()`
4. `:app` da bog'lang va `NamozlyNavHost` ga qo'shing.
