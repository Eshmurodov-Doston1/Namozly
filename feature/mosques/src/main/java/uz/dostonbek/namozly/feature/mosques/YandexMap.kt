package uz.dostonbek.namozly.feature.mosques

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yandex.mapkit.Animation
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
 * Yandex MapKit ishga tushirish.
 */
object YandexMapInitializer {
    @Volatile var isReady: Boolean = false
        private set

    fun init(context: Context, apiKey: String) {
        if (isReady) return
        val key = apiKey.ifBlank { "1679252a-bd27-4314-9146-6634f6461607" }
        runCatching {
            MapKitFactory.setApiKey(key)
        }
        runCatching {
            MapKitFactory.initialize(context)
            isReady = true
        }
    }
}

/**
 * Yandex MapKit xaritasi (Klasterlash va individual pinlar bilan).
 */
@Composable
internal fun MosqueMap(
    center: GeoPoint,
    zoom: Float,
    mosques: List<Mosque>,
    clustered: Boolean,
    selectedId: Long? = null,
    userLocation: GeoPoint? = null,
    onMosqueClick: (Mosque) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // Yandex MapKit initializatsiyasini kafolatlaymiz
    if (!YandexMapInitializer.isReady) {
        YandexMapInitializer.init(context, "")
    }

    val mapView = remember { MapView(context) }
    val tapListener = remember {
        MapObjectTapListener { obj: MapObject, _: Point ->
            (obj.userData as? Mosque)?.let(onMosqueClick)
            true
        }
    }
    val defaultPin = remember { ImageProvider.fromBitmap(pinBitmap(context, 0xFF0A3D33.toInt(), 0xFFF3C775.toInt())) }
    val selectedPin = remember { ImageProvider.fromBitmap(pinBitmap(context, 0xFFD9A94F.toInt(), 0xFF0A3D33.toInt())) }

    val clusterListener = remember {
        ClusterListener { cluster: Cluster ->
            cluster.appearance.setIcon(ImageProvider.fromBitmap(clusterBitmap(context, cluster.size)))
            cluster.addClusterTapListener { c ->
                val curZoom = mapView.mapWindow.map.cameraPosition.zoom
                mapView.mapWindow.map.move(
                    CameraPosition(c.appearance.geometry, (curZoom + 2.5f).coerceAtMost(18f), 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 0.4f),
                    null,
                )
                true
            }
        }
    }

    DisposableEffect(lifecycle) {
        var started = false
        fun start() {
            if (started) return
            runCatching {
                MapKitFactory.getInstance().onStart()
                mapView.onStart()
                started = true
            }
        }
        fun stop() {
            if (!started) return
            runCatching {
                mapView.onStop()
                MapKitFactory.getInstance().onStop()
                started = false
            }
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

    // Kamera harakati
    LaunchedEffect(center.latitude, center.longitude, zoom) {
        runCatching {
            mapView.mapWindow.map.move(
                CameraPosition(Point(center.latitude, center.longitude), zoom, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 0.4f),
                null,
            )
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            runCatching {
                val map = view.mapWindow.map
                map.mapObjects.clear()
                if (clustered) {
                    val collection = map.mapObjects.addClusterizedPlacemarkCollection(clusterListener)
                    mosques.forEach { m ->
                        val isSelected = m.id == selectedId
                        collection.addPlacemark().apply {
                            geometry = Point(m.point.latitude, m.point.longitude)
                            setIcon(if (isSelected) selectedPin else defaultPin)
                            userData = m
                            addTapListener(tapListener)
                        }
                    }
                    collection.clusterPlacemarks(60.0, 15)
                } else {
                    mosques.forEach { m ->
                        val isSelected = m.id == selectedId
                        map.mapObjects.addPlacemark().apply {
                            geometry = Point(m.point.latitude, m.point.longitude)
                            setIcon(if (isSelected) selectedPin else defaultPin)
                            userData = m
                            addTapListener(tapListener)
                        }
                    }
                }
            }
        },
    )
}

private fun pinBitmap(context: Context, mainColor: Int, innerColor: Int): Bitmap {
    val d = context.resources.displayMetrics.density
    val size = (30 * d).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = 0xFFFFFFFF.toInt(); c.drawCircle(size / 2f, size / 2f, size / 2f, p)
    p.color = mainColor; c.drawCircle(size / 2f, size / 2f, size / 2f - 2.5f * d, p)
    p.color = innerColor; c.drawCircle(size / 2f, size / 2f, size / 6f, p)
    return bmp
}

private fun clusterBitmap(context: Context, count: Int): Bitmap {
    val d = context.resources.displayMetrics.density
    val size = (42 * d).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = 0xFFD9A94F.toInt(); c.drawCircle(size / 2f, size / 2f, size / 2f, p)
    p.color = 0xFF0A3D33.toInt(); c.drawCircle(size / 2f, size / 2f, size / 2f - 3 * d, p)
    p.color = 0xFFFFFFFF.toInt(); p.textAlign = Paint.Align.CENTER; p.textSize = 14 * d; p.isFakeBoldText = true
    c.drawText(count.toString(), size / 2f, size / 2f + 5 * d, p)
    return bmp
}
