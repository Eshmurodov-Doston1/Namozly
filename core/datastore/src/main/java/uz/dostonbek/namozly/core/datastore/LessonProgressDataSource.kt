package uz.dostonbek.namozly.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonProgressDataSource @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private val key = stringSetPreferencesKey("lesson_progress")

    val entries: Flow<Set<String>> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[key].orEmpty() }

    private val genderKey = stringPreferencesKey("lesson_gender")

    /** "MALE" / "FEMALE" yoki null (hali tanlanmagan). */
    val gender: Flow<String?> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[genderKey] }

    suspend fun setGender(value: String) {
        dataStore.edit { it[genderKey] = value }
    }

    suspend fun add(entry: String) {
        dataStore.edit { p -> p[key] = p[key].orEmpty() + entry }
    }
}
