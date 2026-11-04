package uz.dostonbek.namozly.core.model

/** Dars rasmidagi holat (Compose'da chiziladi). */
enum class Posture { NIYAT, TAKBIR, QIYAM, RUKU, QAVMA, SAJDA, QADA, SALAM, QUNUT, WUDU_HANDS, WUDU_MOUTH, WUDU_FACE, WUDU_ARMS, WUDU_HEAD, WUDU_FEET, GHUSL, TAYAMMUM, BOOK }

enum class LessonIcon { WUDU, GHUSL, TWO, FOUR, DUAS, SPECIAL }

data class LessonRecitation(
    val arabic: String,
    val transliteration: String,
    val meaning: LocalizedText,
    val times: Int = 1,
)

data class LessonStep(
    val title: LocalizedText,
    val text: LocalizedText,
    val posture: Posture,
    val rakat: Int? = null,
    val recite: LessonRecitation? = null,
    /** Qisqa nom (pastki qadamlar lentasi uchun). */
    val short: LocalizedText = title,
)

data class Lesson(
    val id: String,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val icon: LessonIcon,
    val steps: List<LessonStep>,
)
