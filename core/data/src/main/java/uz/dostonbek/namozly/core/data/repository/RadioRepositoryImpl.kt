package uz.dostonbek.namozly.core.data.repository

import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.data.cache.OfflineCache
import uz.dostonbek.namozly.core.domain.repository.RadioRepository
import uz.dostonbek.namozly.core.model.RadioStation
import uz.dostonbek.namozly.core.network.api.RadioService
import javax.inject.Inject
import javax.inject.Singleton

/** Radiolar ro'yxati — tarmoqdan, bo'lmasa oxirgi saqlangan ro'yxat (oflayn). */
@Singleton
class RadioRepositoryImpl @Inject constructor(
    private val api: RadioService,
    private val offline: OfflineCache,
) : RadioRepository {
    private var cache: List<RadioStation>? = null

    override suspend fun stations(): List<RadioStation> {
        cache?.let { return it }
        val list = runSuspendCatching { api.radios().radios }.getOrDefault(emptyList())
            .filter { it.url.startsWith("http") }
            .map { RadioStation(it.id, it.name.trim(), it.url) }
        if (list.isNotEmpty()) {
            cache = list
            runSuspendCatching { offline.putRadios(list) }
            return list
        }
        return offline.radios().orEmpty().also { if (it.isNotEmpty()) cache = it }
    }

    /** Fon yangilash uchun: xotiradagi nusxani chetlab o'tadi. */
    suspend fun refresh() { cache = null; stations() }
}
