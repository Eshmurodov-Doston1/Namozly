package uz.dostonbek.namozly.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import uz.dostonbek.namozly.core.common.Dispatcher
import uz.dostonbek.namozly.core.common.NamozlyDispatchers
import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.data.cache.OfflineCache
import uz.dostonbek.namozly.core.data.source.MosquesData
import uz.dostonbek.namozly.core.domain.repository.MosqueRepository
import uz.dostonbek.namozly.core.domain.util.QiblaCalculator
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Mosque
import uz.dostonbek.namozly.core.network.api.OverpassService
import uz.dostonbek.namozly.core.network.api.WikipediaService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Masjidlar: boy mahalliy oflayn baza + zaruratda OpenStreetMap (Overpass) yangilanishi.
 * Foydalanuvchi joylashuviga qarab masofa aniq hisoblanadi.
 */
@Singleton
class MosqueRepositoryImpl @Inject constructor(
    private val overpass: OverpassService,
    private val offline: OfflineCache,
    private val wikipedia: WikipediaService,
    @Dispatcher(NamozlyDispatchers.IO) private val io: CoroutineDispatcher,
) : MosqueRepository {

    private val photoCache = mutableMapOf<String, String?>()
    private val cacheMutex = Mutex()
    private var worldCache: List<Mosque>? = null

    override suspend fun nearby(point: GeoPoint, radiusMeters: Int): Result<List<Mosque>> = withContext(io) {
        // 1. Oflayn bazadan joriy koordinataga nisbatan masofalarni hisoblaymiz.
        val localMosques = MosquesData.all.map { item ->
            val dist = QiblaCalculator.distanceKm(point, item.point) * 1000
            item.toMosque(calculatedDistance = dist)
        }.sortedBy { it.distanceMeters ?: Double.MAX_VALUE }

        val nearestLocal = localMosques.take(45)

        // 2. Tarmoq bo'lsa, qisqa timeout (3 soniya) bilan qo'shimcha OSM masjidlarini qidirib ko'ramiz.
        val onlineResult = withTimeoutOrNull(3_500) {
            runSuspendCatching {
                val response = queryMirrors(OverpassService.mosquesAround(point.latitude, point.longitude, radiusMeters.coerceAtLeast(6000), limit = 25))
                response.elements.mapNotNull { e ->
                    val lat = e.lat ?: e.center?.lat ?: return@mapNotNull null
                    val lon = e.lon ?: e.center?.lon ?: return@mapNotNull null
                    val p = GeoPoint(lat, lon)
                    val tags = e.tags
                    val name = tags["name:uz"] ?: tags["name"] ?: tags["name:en"] ?: tags["name:ru"] ?: return@mapNotNull null
                    Mosque(
                        id = e.id,
                        name = name,
                        point = p,
                        distanceMeters = QiblaCalculator.distanceKm(point, p) * 1000,
                        address = listOfNotNull(tags["addr:street"], tags["addr:housenumber"]).joinToString(" ").ifBlank { null },
                        photoUrl = tags["image"]?.takeIf { it.startsWith("https://") },
                        city = tags["addr:city"],
                    )
                }
            }.getOrNull()
        }

        // Agar onlayn natija bo'lsa, birlashtiramiz va nom bo'yicha takrorlarini filtrlaymiz
        val combined = if (!onlineResult.isNullOrEmpty()) {
            (onlineResult + nearestLocal)
                .distinctBy { it.name.trim().lowercase() }
                .sortedBy { it.distanceMeters ?: Double.MAX_VALUE }
                .take(50)
        } else {
            nearestLocal
        }

        if (combined.isNotEmpty()) {
            runSuspendCatching { offline.putMosques(OfflineCache.MOSQUES_LAST, combined) }
            Result.success(combined)
        } else {
            val cached = offline.mosques(OfflineCache.MOSQUES_LAST)
                ?.map { it.copy(distanceMeters = QiblaCalculator.distanceKm(point, it.point) * 1000) }
                ?.sortedBy { it.distanceMeters }
            Result.success(cached ?: nearestLocal)
        }
    }

    private suspend fun queryMirrors(query: String): uz.dostonbek.namozly.core.network.dto.OverpassResponse {
        var last: Throwable? = null
        for (url in OverpassService.MIRRORS) {
            val r = runSuspendCatching { overpass.query(url, query) }
            r.getOrNull()?.let { return it }
            last = r.exceptionOrNull()
        }
        throw last ?: IllegalStateException("overpass")
    }

    override suspend fun worldFamous(): List<Mosque> = withContext(io) {
        worldCache?.let { return@withContext it }
        val list = MosquesData.world.map { item ->
            item.toMosque()
        }
        worldCache = list
        list
    }
}
