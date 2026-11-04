package uz.dostonbek.namozly.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.Book
import uz.dostonbek.namozly.core.model.BookPage
import uz.dostonbek.namozly.core.model.ReadingPosition

interface BookRepository {
    fun books(): List<Book>
    /** Avval qurilmadagi nusxadan, bo'lmasa tarmoqdan (keyin saqlab qo'yiladi). */
    suspend fun page(book: Book, number: Int, language: AppLanguage): Result<BookPage>
    /** Oxirgi o'qilgan kitob (umumiy). */
    val lastRead: Flow<ReadingPosition?>
    suspend fun setLastRead(bookId: String, number: Int)
    /** Har bir kitobda qolgan joy: bookId -> sahifa. */
    val positions: Flow<Map<String, Int>>
    /** Saqlangan sahifalar (xatcho'plar): bookId -> sahifalar. */
    val bookmarks: Flow<Map<String, Set<Int>>>
    suspend fun toggleBookmark(bookId: String, number: Int)
    /** Butun kitobni (arabcha + tanlangan til) qurilmaga yuklab olish. [onProgress] — (bajarildi, jami). */
    suspend fun download(book: Book, language: AppLanguage, onProgress: (Int, Int) -> Unit = { _, _ -> }): Result<Unit>
    /** Qurilmada saqlangan sahifalar soni. */
    suspend fun savedCount(book: Book, language: AppLanguage): Int
    /** O'quvchi sozlamalari: shrift koeffitsienti (0.8..1.6), fon (WHITE / SEPIA / NIGHT), varaqlash (CURL / SLIDE). */
    val readerScale: Flow<Float>
    val readerPaper: Flow<String>
    val readerFlip: Flow<String>
    suspend fun setReaderScale(scale: Float)
    suspend fun setReaderPaper(paper: String)
    suspend fun setReaderFlip(mode: String)
}
