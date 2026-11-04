package uz.dostonbek.namozly.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.CalculationMethod
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Madhab
import uz.dostonbek.namozly.core.model.NotificationSettings
import uz.dostonbek.namozly.core.model.Prayer
import uz.dostonbek.namozly.core.model.PrayerSettings
import uz.dostonbek.namozly.core.model.TasbihStyle
import uz.dostonbek.namozly.core.model.ThemeMode
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.model.UserProfile
import uz.dostonbek.namozly.core.model.UserSettings
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NamozlyPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val language = stringPreferencesKey("language")
        /** Til foydalanuvchi tomonidan qo'lda tanlanganmi (aks holda mamlakatga qarab avtomatik). */
        val languageManual = booleanPreferencesKey("language_manual")
        val theme = stringPreferencesKey("theme")
        val method = stringPreferencesKey("method")
        val madhab = stringPreferencesKey("madhab")
        val useOnline = booleanPreferencesKey("use_online")
        fun adjustment(p: Prayer) = intPreferencesKey("adj_${p.name}")

        val notifEnabled = booleanPreferencesKey("notif_enabled")
        val notifPrayers = stringSetPreferencesKey("notif_prayers")
        val preAlert = intPreferencesKey("notif_pre_alert")
        val ramadan = booleanPreferencesKey("notif_ramadan")
        val friday = booleanPreferencesKey("notif_friday")
        val ayah = booleanPreferencesKey("notif_ayah")
        val news = booleanPreferencesKey("notif_news")

        val tasbihStyle = stringPreferencesKey("tasbih_style")
        val tasbihTarget = intPreferencesKey("tasbih_target")
        val haptics = booleanPreferencesKey("haptics")
        val onboarding = booleanPreferencesKey("onboarding_done")

        val locLat = doublePreferencesKey("loc_lat")
        val locLon = doublePreferencesKey("loc_lon")
        val locCity = stringPreferencesKey("loc_city")
        val locRegion = stringPreferencesKey("loc_region")
        val locDistrict = stringPreferencesKey("loc_district")
        val locCountry = stringPreferencesKey("loc_country")
        val locCountryCode = stringPreferencesKey("loc_cc")
        val locAuto = booleanPreferencesKey("loc_auto")
        val locMahalla = stringPreferencesKey("loc_mahalla")
        val locStreet = stringPreferencesKey("loc_street")
        val locAccuracy = intPreferencesKey("loc_accuracy")

        val lastReadSurah = intPreferencesKey("last_read_surah")
        val lastReadAyah = intPreferencesKey("last_read_ayah")

        val profileUid = stringPreferencesKey("profile_uid")
        val profileName = stringPreferencesKey("profile_name")
        val profileCountry = stringPreferencesKey("profile_cc")
        val profileRegion = stringPreferencesKey("profile_region")
        val profileDistrict = stringPreferencesKey("profile_district")
        val profileMahalla = stringPreferencesKey("profile_mahalla")
        val profilePhone = stringPreferencesKey("profile_phone")
        val profileAnon = booleanPreferencesKey("profile_anon")
    }

    private val data: Flow<Preferences> = dataStore.data.catch {
        if (it is IOException) emit(emptyPreferences()) else throw it
    }

    val settings: Flow<UserSettings> = data.map { p ->
        val defaults = UserSettings()
        UserSettings(
            language = AppLanguage.fromTag(p[Keys.language]),
            themeMode = p[Keys.theme].toEnum(defaults.themeMode),
            prayer = PrayerSettings(
                method = p[Keys.method].toEnum(defaults.prayer.method),
                madhab = p[Keys.madhab].toEnum(defaults.prayer.madhab),
                adjustments = Prayer.entries.associateWith { p[Keys.adjustment(it)] ?: 0 }.filterValues { it != 0 },
                useOnlineSource = p[Keys.useOnline] ?: defaults.prayer.useOnlineSource,
            ),
            notifications = NotificationSettings(
                enabled = p[Keys.notifEnabled] ?: defaults.notifications.enabled,
                prayers = p[Keys.notifPrayers]?.mapNotNull { name -> Prayer.entries.firstOrNull { it.name == name } }?.toSet()
                    ?: defaults.notifications.prayers,
                preAlertMinutes = p[Keys.preAlert] ?: defaults.notifications.preAlertMinutes,
                ramadanAlerts = p[Keys.ramadan] ?: defaults.notifications.ramadanAlerts,
                fridayReminder = p[Keys.friday] ?: defaults.notifications.fridayReminder,
                dailyAyah = p[Keys.ayah] ?: defaults.notifications.dailyAyah,
                news = p[Keys.news] ?: defaults.notifications.news,
            ),
            tasbihStyle = p[Keys.tasbihStyle].toEnum(defaults.tasbihStyle),
            tasbihTarget = p[Keys.tasbihTarget] ?: defaults.tasbihTarget,
            haptics = p[Keys.haptics] ?: defaults.haptics,
            onboardingDone = p[Keys.onboarding] ?: false,
        )
    }.distinctUntilChanged()

    val location: Flow<UserLocation?> = data.map { p ->
        val lat = p[Keys.locLat] ?: return@map null
        val lon = p[Keys.locLon] ?: return@map null
        UserLocation(
            point = GeoPoint(lat, lon),
            city = p[Keys.locCity].orEmpty(),
            region = p[Keys.locRegion],
            district = p[Keys.locDistrict],
            country = p[Keys.locCountry].orEmpty(),
            countryCode = p[Keys.locCountryCode].orEmpty(),
            isAutomatic = p[Keys.locAuto] ?: true,
            mahalla = p[Keys.locMahalla],
            street = p[Keys.locStreet],
            accuracyMeters = p[Keys.locAccuracy],
        )
    }.distinctUntilChanged()

    val lastRead: Flow<Pair<Int, Int>?> = data.map { p ->
        val s = p[Keys.lastReadSurah] ?: return@map null
        s to (p[Keys.lastReadAyah] ?: 1)
    }.distinctUntilChanged()

    val profile: Flow<UserProfile?> = data.map { p ->
        val uid = p[Keys.profileUid] ?: return@map null
        UserProfile(
            uid = uid,
            displayName = p[Keys.profileName].orEmpty(),
            countryCode = p[Keys.profileCountry].orEmpty(),
            region = p[Keys.profileRegion],
            district = p[Keys.profileDistrict],
            mahalla = p[Keys.profileMahalla],
            phone = p[Keys.profilePhone],
            anonymous = p[Keys.profileAnon] ?: false,
        )
    }.distinctUntilChanged()

    suspend fun setLanguage(language: AppLanguage) = edit { it[Keys.language] = language.tag; it[Keys.languageManual] = true }

    /** Mamlakat/qurilma bo'yicha avtomatik tanlangan til (qo'lda tanlov belgisini qo'ymaydi). */
    suspend fun setLanguageAuto(language: AppLanguage) = edit { it[Keys.language] = language.tag }

    val languageManual: Flow<Boolean> = data.map { it[Keys.languageManual] ?: false }
    suspend fun setTheme(mode: ThemeMode) = edit { it[Keys.theme] = mode.name }
    suspend fun setTasbihStyle(style: TasbihStyle) = edit { it[Keys.tasbihStyle] = style.name }
    suspend fun setTasbihTarget(target: Int) = edit { it[Keys.tasbihTarget] = target }
    suspend fun setHaptics(enabled: Boolean) = edit { it[Keys.haptics] = enabled }
    suspend fun setOnboardingDone() = edit { it[Keys.onboarding] = true }

    suspend fun setPrayerSettings(s: PrayerSettings) = edit { p ->
        p[Keys.method] = s.method.name
        p[Keys.madhab] = s.madhab.name
        p[Keys.useOnline] = s.useOnlineSource
        Prayer.entries.forEach { prayer -> p[Keys.adjustment(prayer)] = s.adjustments[prayer] ?: 0 }
    }

    suspend fun setNotificationSettings(s: NotificationSettings) = edit { p ->
        p[Keys.notifEnabled] = s.enabled
        p[Keys.notifPrayers] = s.prayers.map { it.name }.toSet()
        p[Keys.preAlert] = s.preAlertMinutes
        p[Keys.ramadan] = s.ramadanAlerts
        p[Keys.friday] = s.fridayReminder
        p[Keys.ayah] = s.dailyAyah
        p[Keys.news] = s.news
    }

    suspend fun setLocation(l: UserLocation) = edit { p ->
        p[Keys.locLat] = l.point.latitude
        p[Keys.locLon] = l.point.longitude
        p[Keys.locCity] = l.city
        l.region?.let { p[Keys.locRegion] = it } ?: p.remove(Keys.locRegion)
        l.district?.let { p[Keys.locDistrict] = it } ?: p.remove(Keys.locDistrict)
        p[Keys.locCountry] = l.country
        p[Keys.locCountryCode] = l.countryCode
        p[Keys.locAuto] = l.isAutomatic
        l.mahalla?.let { p[Keys.locMahalla] = it } ?: p.remove(Keys.locMahalla)
        l.street?.let { p[Keys.locStreet] = it } ?: p.remove(Keys.locStreet)
        l.accuracyMeters?.let { p[Keys.locAccuracy] = it } ?: p.remove(Keys.locAccuracy)
    }

    suspend fun setLastRead(surah: Int, ayah: Int) = edit { p ->
        p[Keys.lastReadSurah] = surah
        p[Keys.lastReadAyah] = ayah
    }

    suspend fun setProfile(profile: UserProfile?) = edit { p ->
        if (profile == null) {
            listOf(Keys.profileUid, Keys.profileName, Keys.profileCountry, Keys.profileRegion, Keys.profileDistrict, Keys.profileMahalla, Keys.profilePhone)
                .forEach { p.remove(it) }
        } else {
            p[Keys.profileUid] = profile.uid
            p[Keys.profileName] = profile.displayName
            p[Keys.profileCountry] = profile.countryCode
            profile.region?.let { p[Keys.profileRegion] = it }
            profile.district?.let { p[Keys.profileDistrict] = it }
            profile.mahalla?.let { p[Keys.profileMahalla] = it }
            profile.phone?.let { p[Keys.profilePhone] = it }
            p[Keys.profileAnon] = profile.anonymous
        }
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit { block(it) }
    }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
