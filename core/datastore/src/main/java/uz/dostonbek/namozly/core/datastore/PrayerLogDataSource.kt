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

/** "yyyy-MM-dd|FAJR" ko'rinishidagi yozuvlar; faqat oxirgi 90 kun saqlanadi. */
@Singleton
class PrayerLogDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val key = stringSetPreferencesKey("prayer_log")

    val entries: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[key].orEmpty() }

    suspend fun toggle(entry: String, keepAfter: String) {
        dataStore.edit { p ->
            val current = p[key].orEmpty()
            val updated = if (entry in current) current - entry else current + entry
            p[key] = updated.filter { it.substringBefore('|') >= keepAfter }.toSet()
        }
    }
}
