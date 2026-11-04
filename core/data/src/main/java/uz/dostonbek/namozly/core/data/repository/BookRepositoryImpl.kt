package uz.dostonbek.namozly.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import uz.dostonbek.namozly.core.common.Dispatcher
import uz.dostonbek.namozly.core.common.NamozlyDispatchers
import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.data.translate.TextTranslator
import uz.dostonbek.namozly.core.database.dao.BookPageDao
import uz.dostonbek.namozly.core.database.entity.BookPageEntity
import uz.dostonbek.namozly.core.datastore.ReadingDataSource
import uz.dostonbek.namozly.core.domain.repository.BookRepository
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.Book
import uz.dostonbek.namozly.core.model.BookCategory
import uz.dostonbek.namozly.core.model.BookPage
import uz.dostonbek.namozly.core.model.ReadingPosition
import uz.dostonbek.namozly.core.model.UzTransliterator
import uz.dostonbek.namozly.core.model.uzText
import uz.dostonbek.namozly.core.network.api.HadithService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hadis to'plamlari — ochiq manba fawazahmed0/hadith-api.
 * - Arabcha asl matn + ruscha/inglizcha tarjima to'g'ridan-to'g'ri manbadan.
 * - O'zbekcha: inglizcha matndan mashina tarjimasi (lotin), kirill — lotindan transliteratsiya.
 * Har bir o'qilgan sahifa Room'ga yoziladi — keyin internetsiz ochiladi.
 */
@Singleton
class BookRepositoryImpl @Inject constructor(
    private val api: HadithService,
    private val dao: BookPageDao,
    private val translator: TextTranslator,
    private val reading: ReadingDataSource,
    @Dispatcher(NamozlyDispatchers.IO) private val io: CoroutineDispatcher,
) : BookRepository {


    private val books = listOf(
        Book("bukhari", uzText("Sahihi Buxoriy", "Сахих аль-Бухари", "Sahih al-Bukhari"), uzText("Imom Buxoriy", "Имам аль-Бухари", "Imam al-Bukhari"), BookCategory.HADITH, 0xFF0E5A4A, 7563, "bukhari"),
        Book("muslim", uzText("Sahihi Muslim", "Сахих Муслим", "Sahih Muslim"), uzText("Imom Muslim", "Имам Муслим", "Imam Muslim"), BookCategory.HADITH, 0xFF6B2737, 7563, "muslim"),
        Book("nawawi", uzText("Arba'in Navaviy", "40 хадисов ан-Навави", "Forty Hadith of Nawawi"), uzText("Imom Navaviy", "Имам ан-Навави", "Imam an-Nawawi"), BookCategory.HADITH, 0xFF1F3A5F, 42, "nawawi"),
        Book("qudsi", uzText("Hadisi qudsiy", "Хадисы кудси", "Hadith Qudsi"), uzText("40 hadis", "40 хадисов", "40 hadith"), BookCategory.AQIDA, 0xFF7A5A1E, 40, "qudsi"),
        Book("abudawud", uzText("Sunani Abu Dovud", "Сунан Абу Дауда", "Sunan Abu Dawud"), uzText("Abu Dovud", "Абу Дауд", "Abu Dawud"), BookCategory.FIQH, 0xFF135E62, 5274, "abudawud"),
        Book("tirmidhi", uzText("Sunani Termiziy", "Сунан ат-Тирмизи", "Jami at-Tirmidhi"), uzText("Imom Termiziy", "Имам ат-Тирмизи", "Imam at-Tirmidhi"), BookCategory.HADITH, 0xFF4B2E52, 3956, "tirmidhi"),
        Book("nasai", uzText("Sunani Nasoiy", "Сунан ан-Насаи", "Sunan an-Nasa'i"), uzText("Imom Nasoiy", "Имам ан-Насаи", "Imam an-Nasa'i"), BookCategory.FIQH, 0xFF2E4A2B, 5758, "nasai"),
        Book("ibnmajah", uzText("Sunani Ibn Moja", "Сунан Ибн Маджа", "Sunan Ibn Majah"), uzText("Ibn Moja", "Ибн Маджа", "Ibn Majah"), BookCategory.FIQH, 0xFF5A3A1A, 4341, "ibnmajah"),
        Book("malik", uzText("Muvatto", "Муватта", "Muwatta Malik"), uzText("Imom Molik", "Имам Малик", "Imam Malik"), BookCategory.FIQH, 0xFF3A3A5C, 1858, "malik"),
    )

    override fun books(): List<Book> = books

    override suspend fun page(book: Book, number: Int, language: AppLanguage): Result<BookPage> = withContext(io) {
        runSuspendCatching {
            when (language) {
                // Kirill — lotin sahifasidan (bir xil tarjima, faqat yozuv boshqa).
                AppLanguage.UZ_CYRL -> {
                    val latin = page(book, number, AppLanguage.UZ_LATN).getOrThrow()
                    latin.copy(
                        translation = UzTransliterator.toCyrillic(latin.translation),
                        section = latin.section?.let(UzTransliterator::toCyrillic),
                    )
                }
                else -> dao.get(book.id, number, language.tag)?.toModel(language) ?: fetch(book, number, language)
            }
        }
    }

    private suspend fun fetch(book: Book, number: Int, language: AppLanguage): BookPage = coroutineScope {
        val arabic = async { api.hadith("ara-${book.edition}", number) }
        val source = async {
            if (language == AppLanguage.RU) runSuspendCatching { api.hadith("rus-${book.edition}", number) }.getOrNull()
            else null
        }
        val english = async { runSuspendCatching { api.hadith("eng-${book.edition}", number) }.getOrNull() }
        val a = arabic.await()
        val ru = source.await()
        val en = english.await()
        val sectionId = a.hadiths.firstOrNull()?.reference?.book?.toString()
        val enText = en?.hadiths?.firstOrNull()?.text.orEmpty()
        val enSection = sectionId?.let { en?.metadata?.section?.get(it) }
        val (text, section) = when (language) {
            AppLanguage.EN -> enText to enSection
            AppLanguage.RU -> {
                val ruText = ru?.hadiths?.firstOrNull()?.text
                if (!ruText.isNullOrBlank()) ruText to sectionId?.let { ru?.metadata?.section?.get(it) }
                else translator.translate(enText, "en", "ru") to enSection?.let { translator.translate(it, "en", "ru") }
            }
            else -> translator.translate(enText, "en", "uz") to enSection?.let { translator.translate(it, "en", "uz") }
        }
        val entity = BookPageEntity(book.id, number, language.tag, a.hadiths.firstOrNull()?.text.orEmpty(), text, section ?: sectionId?.let { a.metadata?.section?.get(it) })
        dao.upsert(entity)
        entity.toModel(language)
    }

    override suspend fun download(book: Book, language: AppLanguage, onProgress: (Int, Int) -> Unit): Result<Unit> = withContext(io) {
        runSuspendCatching {
            val lang = if (language == AppLanguage.UZ_CYRL) AppLanguage.UZ_LATN else language
            val arabic = api.edition("ara-${book.edition}")
            val russian = if (lang == AppLanguage.RU) runSuspendCatching { api.edition("rus-${book.edition}") }.getOrNull() else null
            val translationEdition = russian ?: api.edition("eng-${book.edition}")
            // Ruscha nashr bo'lmasa — inglizchadan ruschaga, o'zbekcha uchun — inglizchadan o'zbekchaga mashina tarjimasi.
            val mtTarget = when {
                lang == AppLanguage.UZ_LATN -> "uz"
                lang == AppLanguage.RU && russian == null -> "ru"
                else -> null
            }
            val tr = translationEdition.hadiths.associateBy { it.hadithnumber.toInt() }
            val sections = translationEdition.metadata?.sections.orEmpty()
            val arSections = arabic.metadata?.sections.orEmpty()
            val have = dao.numbers(book.id, lang.tag).toHashSet()
            val items = arabic.hadiths.filter { it.hadithnumber.toInt() > 0 }
            items.forEachIndexed { i, a ->
                coroutineContext.ensureActive()
                val n = a.hadithnumber.toInt()
                if (n !in have) {
                    val src = tr[n]?.text.orEmpty()
                    val sectionKey = a.reference?.book?.toString()
                    val text = mtTarget?.let { translator.translate(src, "en", it) } ?: src
                    dao.upsert(BookPageEntity(book.id, n, lang.tag, a.text, text, sectionKey?.let { sections[it] ?: arSections[it] }))
                }
                if (i % 10 == 0 || i == items.lastIndex) onProgress(i + 1, items.size)
            }
        }
    }

    override suspend fun savedCount(book: Book, language: AppLanguage): Int = withContext(io) {
        dao.count(book.id, (if (language == AppLanguage.UZ_CYRL) AppLanguage.UZ_LATN else language).tag)
    }

    private fun BookPageEntity.toModel(language: AppLanguage) = BookPage(
        number = number, arabic = arabic, translation = translation, section = section,
        machineTranslated = language == AppLanguage.UZ_LATN || language == AppLanguage.UZ_CYRL,
    )

    override val lastRead: Flow<ReadingPosition?> = reading.last.map { it?.let { (b, n) -> ReadingPosition(b, n) } }

    override suspend fun setLastRead(bookId: String, number: Int) = reading.setLast(bookId, number)

    override val positions: Flow<Map<String, Int>> = reading.positions
    override val bookmarks: Flow<Map<String, Set<Int>>> = reading.bookmarks
    override suspend fun toggleBookmark(bookId: String, number: Int) = reading.toggleBookmark(bookId, number)
    override val readerFlip: Flow<String> = reading.flipMode
    override suspend fun setReaderFlip(mode: String) = reading.setFlip(mode)

    override val readerScale: Flow<Float> = reading.scale
    override val readerPaper: Flow<String> = reading.paperMode
    override suspend fun setReaderScale(scale: Float) = reading.setScale(scale)
    override suspend fun setReaderPaper(paper: String) = reading.setPaper(paper)
}
