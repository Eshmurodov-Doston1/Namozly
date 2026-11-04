package uz.dostonbek.namozly.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uz.dostonbek.namozly.core.datastore.PrayerLogDataSource
import uz.dostonbek.namozly.core.domain.repository.PrayerLogRepository
import uz.dostonbek.namozly.core.model.Prayer
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerLogRepositoryImpl @Inject constructor(
    private val source: PrayerLogDataSource,
) : PrayerLogRepository {
    override fun observe(): Flow<Map<LocalDate, Set<Prayer>>> = source.entries.map { set ->
        set.mapNotNull { e ->
            val date = runCatching { LocalDate.parse(e.substringBefore('|')) }.getOrNull() ?: return@mapNotNull null
            val prayer = Prayer.entries.firstOrNull { it.name == e.substringAfter('|') } ?: return@mapNotNull null
            date to prayer
        }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
    }

    override suspend fun toggle(date: LocalDate, prayer: Prayer) {
        source.toggle("$date|${prayer.name}", keepAfter = date.minusDays(90).toString())
    }
}
