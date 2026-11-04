package uz.dostonbek.namozly.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkDataSource @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private val key = stringSetPreferencesKey("quran_bookmarks")

    val entries: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[key].orEmpty() }

    suspend fun toggle(entry: String) {
        dataStore.edit { p ->
            val cur = p[key].orEmpty()
            p[key] = if (entry in cur) cur - entry else cur + entry
        }
    }
}
