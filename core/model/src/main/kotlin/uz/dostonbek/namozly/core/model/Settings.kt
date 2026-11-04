package uz.dostonbek.namozly.core.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class TasbihStyle { CLASSIC, MINIMAL, BEADS, CRESCENT, DIGITAL }

data class PrayerSettings(
    val method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val madhab: Madhab = Madhab.HANAFI,
    /** Rasmiy taqvimga moslash uchun daqiqa tuzatishlari (masalan, muslim.uz bilan). */
    val adjustments: Map<Prayer, Int> = emptyMap(),
    val useOnlineSource: Boolean = true,
)

data class NotificationSettings(
    val enabled: Boolean = true,
    val prayers: Set<Prayer> = setOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA),
    /** Namozdan necha daqiqa oldin ogohlantirish (0 — o'chiq). */
    val preAlertMinutes: Int = 10,
    val ramadanAlerts: Boolean = true,
    val fridayReminder: Boolean = true,
    val dailyAyah: Boolean = true,
    val news: Boolean = false,
)

data class UserSettings(
    val language: AppLanguage = AppLanguage.UZ_LATN,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val prayer: PrayerSettings = PrayerSettings(),
    val notifications: NotificationSettings = NotificationSettings(),
    val tasbihStyle: TasbihStyle = TasbihStyle.CLASSIC,
    val tasbihTarget: Int = 33,
    val haptics: Boolean = true,
    val onboardingDone: Boolean = false,
)
