package uz.dostonbek.namozly.core.model

enum class BookCategory { HADITH, TAFSIR, FIQH, SIRAH, AQIDA, AUDIO }

data class Book(
    val id: String,
    val title: LocalizedText,
    val author: LocalizedText,
    val category: BookCategory,
    /** Muqova rangi (ARGB). */
    val coverColor: Long,
    /** Jami hadislar soni (sahifalash uchun). */
    val total: Int,
    /** hadith-api nashr kaliti: "bukhari", "muslim" ... */
    val edition: String,
)

data class BookPage(
    val number: Int,
    val arabic: String,
    val translation: String,
    val section: String?,
    /** Tarjima mashina orqali qilingan (o'zbekcha). */
    val machineTranslated: Boolean = false,
)

/** Kitobni oflayn yuklash holati. */
data class BookDownload(val bookId: String, val done: Int, val total: Int, val finished: Boolean = false, val failed: Boolean = false)

data class ReadingPosition(val bookId: String, val number: Int)
