package uz.dostonbek.namozly.feature.mosques

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Cluster
import com.yandex.mapkit.map.ClusterListener
import com.yandex.mapkit.map.MapObject
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Mosque

/**
 * Yandex MapKit (lite) ishga tushirish. API kalit `local.properties` dagi YANDEX_MAPKIT_API_KEY dan
 * :app BuildConfig orqali keladi. Kalit bo'lmasa xarita o'rniga ro'yxat ko'rsatiladi.
 */
object YandexMapInitializer {
    @Volatile var isReady: Boolean = false
        private set

    fun init(context: Context, apiKey: String) {
        if (isReady || apiKey.isBlank()) return
        MapKitFactory.setApiKey(apiKey)
        MapKitFactory.initialize(context)
        isReady = true
    }
}

@Composable
internal fun MosqueMap(
    center: GeoPoint,
    zoom: Float,
    mosques: List<Mosque>,
    clustered: Boolean,
    onMosqueClick: (Mosque) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context) }
    // Tinglovchilarga kuchli havola saqlanishi shart (MapKit weak reference ishlatadi).
    val tapListener = remember {
        MapObjectTapListener { obj: MapObject, _: Point ->
            (obj.userData as? Mosque)?.let(onMosqueClick)
            true
        }
    }
    val pin = remember { ImageProvider.fromBitmap(pinBitmap(context, 0xFF0E5A4A.toInt())) }
    val clusterListener = remember {
        ClusterListener { cluster: Cluster ->
            cluster.appearance.setIcon(ImageProvider.fromBitmap(clusterBitmap(context, cluster.size)))
        }
    }

    DisposableEffect(lifecycle) {
        var started = false
        fun start() {
            if (started) return
            MapKitFactory.getInstance().onStart(); mapView.onStart(); started = true
        }
        fun stop() {
            if (!started) return
            mapView.onStop(); MapKitFactory.getInstance().onStop(); started = false
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> start()
                Lifecycle.Event.ON_STOP -> stop()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()
        onDispose {
            lifecycle.removeObserver(observer)
            stop()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            val map = view.mapWindow.map
            map.move(CameraPosition(Point(center.latitude, center.longitude), zoom, 0f, 0f))
            map.mapObjects.clear()
            if (clustered) {
                val collection = map.mapObjects.addClusterizedPlacemarkCollection(clusterListener)
                mosques.forEach { m ->
                    collection.addPlacemark().apply {
                        geometry = Point(m.point.latitude, m.point.longitude)
                        setIcon(pin)
                        userData = m
                        addTapListener(tapListener)
                    }
                }
                collection.clusterPlacemarks(60.0, 15)
            } else {
                mosques.forEach { m ->
                    map.mapObjects.addPlacemark().apply {
                        geometry = Point(m.point.latitude, m.point.longitude)
                        setIcon(pin)
                        userData = m
                        addTapListener(tapListener)
                    }
                }
            }
        },
    )
}

private fun pinBitmap(context: Context, color: Int): Bitmap {
    val d = context.resources.displayMetrics.density
    val size = (28 * d).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = 0xFFFFFFFF.toInt(); c.drawCircle(size / 2f, size / 2f, size / 2f, p)
    p.color = color; c.drawCircle(size / 2f, size / 2f, size / 2f - 3 * d, p)
    p.color = 0xFFF3C775.toInt(); c.drawCircle(size / 2f, size / 2f, size / 7f, p)
    return bmp
}

private fun clusterBitmap(context: Context, count: Int): Bitmap {
    val d = context.resources.displayMetrics.density
    val size = (40 * d).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = 0xFFD9A94F.toInt(); c.drawCircle(size / 2f, size / 2f, size / 2f, p)
    p.color = 0xFF0A3D33.toInt(); c.drawCircle(size / 2f, size / 2f, size / 2f - 3 * d, p)
    p.color = 0xFFFFFFFF.toInt(); p.textAlign = Paint.Align.CENTER; p.textSize = 14 * d; p.isFakeBoldText = true
    c.drawText(count.toString(), size / 2f, size / 2f + 5 * d, p)
    return bmp
}
