package uz.dostonbek.namozly.core.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import uz.dostonbek.namozly.core.common.Dispatcher
import uz.dostonbek.namozly.core.common.NamozlyDispatchers
import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.datastore.NamozlyPreferencesDataSource
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.network.api.NominatimService
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: NamozlyPreferencesDataSource,
    private val nominatim: NominatimService,
    @Dispatcher(NamozlyDispatchers.IO) private val io: CoroutineDispatcher,
) : LocationRepository {

    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }

    override val location: Flow<UserLocation> = preferences.location.map { it ?: UserLocation.Default }

    private fun hasPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override suspend fun refreshFromDevice(): Boolean = withContext(io) {
        if (!hasPermission()) return@withContext false
        val result = runSuspendCatching {
            val cts = CancellationTokenSource()
            val current = withTimeoutOrNull(10_000) {
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token).await()
            }
            current ?: fused.lastLocation.await()
        }.getOrNull() ?: return@withContext false

        val point = GeoPoint(result.latitude, result.longitude)
        val address = reverseGeocode(point)
        preferences.setLocation(
            UserLocation(
                point = point,
                city = address?.locality ?: address?.subAdminArea ?: address?.adminArea.orEmpty(),
                region = address?.adminArea,
                district = address?.subAdminArea,
                country = address?.countryName.orEmpty(),
                countryCode = address?.countryCode.orEmpty().uppercase(),
                isAutomatic = true,
                mahalla = address?.subLocality,
                street = address?.thoroughfare,
                accuracyMeters = if (result.hasAccuracy()) result.accuracy.toInt() else null,
            ),
        )
        true
    }

    override suspend fun setManual(location: UserLocation) {
        preferences.setLocation(location.copy(isAutomatic = false))
    }

    override suspend fun search(query: String): List<UserLocation> = withContext(io) {
        if (query.isBlank()) return@withContext emptyList()
        runSuspendCatching {
            nominatim.search(query = query, language = Locale.getDefault().language).mapNotNull { p ->
                val lat = p.lat.toDoubleOrNull() ?: return@mapNotNull null
                val lon = p.lon.toDoubleOrNull() ?: return@mapNotNull null
                val a = p.address
                UserLocation(
                    point = GeoPoint(lat, lon),
                    city = a?.city ?: a?.town ?: a?.village ?: p.displayName.substringBefore(","),
                    region = a?.state,
                    district = a?.county,
                    country = a?.country.orEmpty(),
                    countryCode = a?.countryCode.orEmpty().uppercase(),
                    isAutomatic = false,
                )
            }
        }.getOrDefault(emptyList())
    }

    private suspend fun reverseGeocode(point: GeoPoint): Address? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context, Locale.getDefault())
        return runSuspendCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                withTimeoutOrNull(8_000) {
                    suspendCancellableCoroutine<Address?> { cont ->
                        geocoder.getFromLocation(point.latitude, point.longitude, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                if (cont.isActive) cont.resume(addresses.firstOrNull())
                            }
                            override fun onError(errorMessage: String?) {
                                if (cont.isActive) cont.resume(null)
                            }
                        })
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull()
            }
        }.getOrNull()
    }
}
