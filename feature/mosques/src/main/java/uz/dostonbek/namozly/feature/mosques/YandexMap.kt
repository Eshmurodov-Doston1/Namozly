package uz.dostonbek.namozly.feature.mosques

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.json.JSONArray
import org.json.JSONObject
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Mosque

/**
 * Yandex MapKit (lite) ishga tushirish. API kalit `local.properties` dagi YANDEX_MAPKIT_API_KEY dan keladi.
 * Agar kalit bo'lmasa, ilova bepul va ochiq OpenStreetMap (Leaflet MarkerCluster) xaritasidan foydalanadi.
 */
object YandexMapInitializer {
    @Volatile var isReady: Boolean = false
        private set

    fun init(context: Context, apiKey: String) {
        if (isReady || apiKey.isBlank()) return
        runCatching {
            MapKitFactory.setApiKey(apiKey)
            MapKitFactory.initialize(context)
            isReady = true
        }
    }
}

/**
 * Umumiy xarita komponenti:
 * Yandex kaliti mavjud bo'lsa — Yandex MapKit, aks holda — OpenStreetMap (Leaflet).
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
    if (YandexMapInitializer.isReady) {
        YandexMapView(
            center = center,
            zoom = zoom,
            mosques = mosques,
            clustered = clustered,
            onMosqueClick = onMosqueClick,
            modifier = modifier,
        )
    } else {
        OsmLeafletMap(
            center = center,
            zoom = zoom,
            mosques = mosques,
            clustered = clustered,
            selectedId = selectedId,
            userLocation = userLocation,
            onMosqueClick = onMosqueClick,
            modifier = modifier,
            isDark = NamozlyThemeTokens.t.isDark,
        )
    }
}

/**
 * OpenStreetMap (Leaflet + MarkerCluster) — mutlaqo bepul, kalitsiz, barcha davlatlar bo'yicha
 * masjidlarni guruhlab (klaster) va alohida ko'rsatish imkoniyatiga ega.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun OsmLeafletMap(
    center: GeoPoint,
    zoom: Float,
    mosques: List<Mosque>,
    clustered: Boolean,
    selectedId: Long?,
    userLocation: GeoPoint?,
    onMosqueClick: (Mosque) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isPageLoaded by remember { mutableStateOf(false) }

    // JavaScript'dan Compose'ga hodisalarni uzatuvchi ko'prik
    val bridge = remember(mosques) {
        object {
            @JavascriptInterface
            fun onMosqueClick(id: Long) {
                val found = mosques.firstOrNull { it.id == id } ?: return
                Handler(Looper.getMainLooper()).post { onMosqueClick(found) }
            }

            @JavascriptInterface
            fun onMapReady() {
                Handler(Looper.getMainLooper()).post {
                    isPageLoaded = true
                }
            }
        }
    }

    fun serializeMosques(list: List<Mosque>): String {
        val array = JSONArray()
        list.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("lat", m.point.latitude)
                put("lng", m.point.longitude)
                put("city", m.city.orEmpty())
                put("country", m.country.orEmpty())
            }
            array.put(obj)
        }
        return array.toString()
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    // Sahifa tayyor bo'lganda yoki masjidlar ro'yxati o'zgarganda xaritaga yangi ma'lumotlarni yuboramiz
    LaunchedEffect(isPageLoaded, mosques, selectedId) {
        if (isPageLoaded) {
            val json = serializeMosques(mosques)
            val sel = selectedId?.toString() ?: "null"
            webViewRef?.evaluateJavascript("setMosques($json, $sel);", null)
        }
    }

    // Markaz yoki masshtab o'zgarganda
    LaunchedEffect(isPageLoaded, center.latitude, center.longitude, zoom) {
        if (isPageLoaded) {
            webViewRef?.evaluateJavascript("flyTo(${center.latitude}, ${center.longitude}, $zoom);", null)
        }
    }

    // Foydalanuvchi GPS koordinatasi o'zgarganda
    LaunchedEffect(isPageLoaded, userLocation) {
        if (isPageLoaded && userLocation != null) {
            webViewRef?.evaluateJavascript("setUserLocation(${userLocation.latitude}, ${userLocation.longitude});", null)
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript(
                            "initMap(${center.latitude}, ${center.longitude}, $zoom, $clustered, $isDark);",
                            null,
                        )
                        val json = serializeMosques(mosques)
                        val sel = selectedId?.toString() ?: "null"
                        view?.evaluateJavascript("setMosques($json, $sel);", null)
                        if (userLocation != null) {
                            view?.evaluateJavascript("setUserLocation(${userLocation.latitude}, ${userLocation.longitude});", null)
                        }
                        isPageLoaded = true
                    }
                }
                addJavascriptInterface(bridge, "AndroidBridge")
                loadUrl("file:///android_asset/leaflet/map.html")
                webViewRef = this
            }
        },
        modifier = modifier,
    )
}

/**
 * Yandex MapKit orqali xarita ko'rinishi (kalit mavjud bo'lganda).
 */
@Composable
private fun YandexMapView(
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
