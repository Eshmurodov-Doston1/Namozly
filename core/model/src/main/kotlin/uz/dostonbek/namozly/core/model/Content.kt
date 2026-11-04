package uz.dostonbek.namozly.core.model

data class AllahName(
    val number: Int,
    val arabic: String,
    val name: LocalizedText,
    val meaning: LocalizedText,
)

enum class DuaCategory { MORNING_EVENING, PRAYER, RAMADAN, FOOD, HOME, MOSQUE, SLEEP, TRAVEL, GENERAL }

data class Dua(
    val id: String,
    val category: DuaCategory,
    val title: LocalizedText,
    val arabic: String,
    val transliteration: String,
    val translation: LocalizedText,
    val source: String? = null,
)

enum class Gender { MALE, FEMALE }

data class PrayerStep(
    val stepNumber: Int,
    val titleUz: String,
    val titleRu: String = titleUz,
    val titleEn: String = titleUz,
    val titleKril: String = titleUz,
    val descriptionUz: String,
    val descriptionRu: String = descriptionUz,
    val descriptionEn: String = descriptionUz,
    val descriptionKril: String = descriptionUz,
    val arabicText: String? = null,
    val pronunciation: String? = null,
    val translationUz: String? = null,
    val iconName: String = "qiyam",
) {
    val title: LocalizedText get() = LocalizedText(titleUz, titleKril, titleRu, titleEn)
    val description: LocalizedText get() = LocalizedText(descriptionUz, descriptionKril, descriptionRu, descriptionEn)
}

data class PrayerGuide(
    val id: String,
    val prayerName: String,
    val rakatDescription: String,
    val gender: Gender,
    val steps: List<PrayerStep>,
)

data class LiveChannel(
    val id: String,
    val name: LocalizedText,
    val city: LocalizedText,
    /** YouTube kanal ID — embed/live_stream?channel=... orqali joriy efir ochiladi. */
    val youtubeChannelId: String,
    /** Muqobil: aniq video ID (agar kanal efiri ishlamasa). */
    val fallbackVideoId: String? = null,
    /** Kanal ID bo'lmasa YouTube qidiruvi uchun so'rov. */
    val searchQuery: String = "",
    /** Ilova ichidagi pleyer uchun HLS oqimlar (birinchisi ishlamasa — keyingisi). */
    val streams: List<String> = emptyList(),
)

data class IslamicEvent(
    val name: LocalizedText,
    val hijriMonth: Int,
    val hijriDay: Int,
)

data class Mosque(
    val id: Long,
    val name: String,
    val point: GeoPoint,
    val distanceMeters: Double? = null,
    val address: String? = null,
    val photoUrl: String? = null,
    val city: String? = null,
    val country: String? = null,
    val description: String? = null,
)
