package uz.dostonbek.namozly.core.model

enum class Revelation { MECCAN, MEDINAN }

data class Surah(
    val number: Int,
    val arabicName: String,
    val name: LocalizedText,
    val ayahCount: Int,
    val revelation: Revelation,
)

data class Ayah(
    val surah: Int,
    val number: Int,
    /** Butun Qur'on bo'yicha tartib raqami (1..6236) — audio uchun. */
    val globalNumber: Int,
    val arabic: String,
    val translation: String,
)

data class AyahOfDay(val surah: Surah, val ayah: Ayah)

/** Audio pleer holati. */
data class PlaybackState(
    val surah: Int? = null,
    val ayah: Int? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    /** Oxirgi element oxirigacha ijro etildi (keyingi oyatga avtomatik o'tish uchun). */
    val ended: Boolean = false,
    val speed: Float = 1f,
    /** Radio/oqim nomi (sura emas). */
    val streamTitle: String? = null,
)

/** Juz boshlanishi (sura:oyat). */
data class Juz(val number: Int, val surah: Int, val ayah: Int)

object JuzData {
    val all: List<Juz> = listOf(
        1 to 1, 2 to 142, 2 to 253, 3 to 93, 4 to 24, 4 to 148, 5 to 82, 6 to 111, 7 to 88, 8 to 41,
        9 to 93, 11 to 6, 12 to 53, 15 to 1, 17 to 1, 18 to 75, 21 to 1, 23 to 1, 25 to 21, 27 to 56,
        29 to 46, 33 to 31, 36 to 28, 39 to 32, 41 to 47, 46 to 1, 51 to 31, 58 to 1, 67 to 1, 78 to 1,
    ).mapIndexed { i, (s, a) -> Juz(i + 1, s, a) }
}

data class RadioStation(val id: Int, val name: String, val url: String)
