package uz.dostonbek.namozly.core.domain.repository

import kotlinx.coroutines.flow.Flow
import uz.dostonbek.namozly.core.model.Gender

/** Ko'rilgan dars qadamlari: lessonId -> qadam indekslari; tanlangan jins ham saqlanadi. */
interface LessonProgressRepository {
    fun observe(): Flow<Map<String, Set<Int>>>
    suspend fun markViewed(lessonId: String, step: Int)
    val gender: Flow<Gender>
    suspend fun setGender(gender: Gender)
}
