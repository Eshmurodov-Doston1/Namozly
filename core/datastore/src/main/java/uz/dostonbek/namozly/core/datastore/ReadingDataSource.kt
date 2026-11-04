package uz.dostonbek.namozly.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Kitob o'qish holati: oxirgi kitob/sahifa, shrift o'lchami va fon. */
@Singleton
class ReadingDataSource @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private val book = stringPreferencesKey("reading_book")
    private val number = intPreferencesKey("reading_number")
    private val fontScale = floatPreferencesKey("reading_font_scale")
    private val paper = stringPreferencesKey("reading_paper")
    private val flip = stringPreferencesKey("reading_flip")
    /** "bookId:sahifa" — har bir kitobda qolgan joy. */
    private val positionsKey = stringSetPreferencesKey("reading_positions")
    /** "bookId:sahifa" — xatcho'plar. */
    private val marksKey = stringSetPreferencesKey("reading_bookmarks")

    private val data = dataStore.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }

    val last: Flow<Pair<String, Int>?> = data.map { p -> p[book]?.let { it to (p[number] ?: 1) } }
    val scale: Flow<Float> = data.map { it[fontScale] ?: 1f }
    val paperMode: Flow<String> = data.map { it[paper] ?: "SEPIA" }
    val flipMode: Flow<String> = data.map { it[flip] ?: "CURL" }

    val positions: Flow<Map<String, Int>> = data.map { p ->
        p[positionsKey].orEmpty().mapNotNull { e -> e.substringAfterLast(':').toIntOrNull()?.let { e.substringBeforeLast(':') to it } }.toMap()
    }
    val bookmarks: Flow<Map<String, Set<Int>>> = data.map { p ->
        p[marksKey].orEmpty().mapNotNull { e -> e.substringAfterLast(':').toIntOrNull()?.let { e.substringBeforeLast(':') to it } }
            .groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
    }

    suspend fun setLast(bookId: String, n: Int) = dataStore.edit { p ->
        p[book] = bookId
        p[number] = n
        p[positionsKey] = p[positionsKey].orEmpty().filterNot { it.substringBeforeLast(':') == bookId }.toSet() + "$bookId:$n"
    }.let { }

    suspend fun toggleBookmark(bookId: String, n: Int) = dataStore.edit { p ->
        val e = "$bookId:$n"
        val cur = p[marksKey].orEmpty()
        p[marksKey] = if (e in cur) cur - e else cur + e
    }.let { }

    suspend fun setFlip(v: String) = dataStore.edit { it[flip] = v }.let { }
    suspend fun setScale(v: Float) = dataStore.edit { it[fontScale] = v }.let { }
    suspend fun setPaper(v: String) = dataStore.edit { it[paper] = v }.let { }
}
