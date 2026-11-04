package uz.dostonbek.namozly.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.dostonbek.namozly.core.datastore.LessonProgressDataSource
import uz.dostonbek.namozly.core.domain.repository.LessonProgressRepository
import uz.dostonbek.namozly.core.model.Gender
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonProgressRepositoryImpl @Inject constructor(private val source: LessonProgressDataSource) : LessonProgressRepository {
    override fun observe(): Flow<Map<String, Set<Int>>> = source.entries.map { set ->
        set.mapNotNull { e -> e.substringAfterLast(':').toIntOrNull()?.let { e.substringBeforeLast(':') to it } }
            .groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
    }

    override suspend fun markViewed(lessonId: String, step: Int) = source.add("$lessonId:$step")

    override val gender: Flow<Gender> = source.gender.map { v -> Gender.entries.firstOrNull { it.name == v } ?: Gender.MALE }

    override suspend fun setGender(gender: Gender) = source.setGender(gender.name)
}
