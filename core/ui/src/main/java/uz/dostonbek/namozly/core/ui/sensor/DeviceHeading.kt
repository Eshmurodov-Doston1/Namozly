package uz.dostonbek.namozly.core.ui.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import kotlin.math.abs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/** Kompas holati: [azimuth] — magnit shimolga nisbatan telefon yo'nalishi (0..360), [accuracy] — sensor aniqligi. */
data class HeadingState(val azimuth: Float, val accuracy: Int)

/**
 * Rotation-vector sensoridan qurilma yo'nalishi. Ekran faqat RESUMED holatda bo'lganda tinglaydi
 * (batareyani tejash). Sensor yo'q bo'lsa null qaytadi.
 */
@Composable
fun rememberDeviceHeading(): State<HeadingState?> {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val state = remember { mutableStateOf<HeadingState?>(null) }

    DisposableEffect(context, lifecycle) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sm?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        val rotation = FloatArray(9)
        val remapped = FloatArray(9)
        val orientation = FloatArray(3)
        var smoothed = Float.NaN
        var accuracy = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                val (axisX, axisY) = when (displayRotation(context)) {
                    Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
                    Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
                    Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
                    else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
                }
                SensorManager.remapCoordinateSystem(rotation, axisX, axisY, remapped)
                SensorManager.getOrientation(remapped, orientation)
                val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
                // Moslashuvchan filtr: kichik titrashni silliqlaydi, tez burilishda esa darhol ergashadi.
                smoothed = if (smoothed.isNaN()) deg else {
                    val d = abs(angleDiff(smoothed, deg))
                    lowPass(smoothed, deg, if (d > 25f) 0.85f else if (d > 6f) 0.5f else 0.25f)
                }
                // Juda mayda o'zgarishlarda (≈0.3°) qayta chizmaymiz — UI yengil ishlaydi.
                val prev = state.value?.azimuth
                if (prev == null || abs(angleDiff(prev, smoothed)) >= 0.3f || state.value?.accuracy != accuracy) {
                    state.value = HeadingState(smoothed, accuracy)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, acc: Int) {
                accuracy = acc
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> if (sensor != null) sm?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
                Lifecycle.Event.ON_PAUSE -> sm?.unregisterListener(listener)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) && sensor != null) {
            sm?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose {
            lifecycle.removeObserver(observer)
            sm?.unregisterListener(listener)
        }
    }
    return state
}

/** -180..180 oralig'idagi eng qisqa burchak farqi. */
private fun angleDiff(from: Float, to: Float): Float {
    var d = (to - from) % 360f
    if (d > 180f) d -= 360f
    if (d < -180f) d += 360f
    return d
}

/** 359° -> 1° o'tishini to'g'ri silliqlaydi. */
private fun lowPass(old: Float, new: Float, alpha: Float): Float {
    var delta = new - old
    if (delta > 180) delta -= 360
    if (delta < -180) delta += 360
    return (old + alpha * delta + 360) % 360
}

@Suppress("DEPRECATION")
private fun displayRotation(context: Context): Int =
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        context.display?.rotation ?: Surface.ROTATION_0
    } else {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
    }

/** Ekranga nisbatan qibla burchagi — ignani shu burchakka buriladi. */
fun qiblaNeedleRotation(qiblaBearing: Double, heading: Float?): Float =
    if (heading == null) qiblaBearing.toFloat() else ((qiblaBearing - heading + 360) % 360).toFloat()

fun isAlignedToQibla(qiblaBearing: Double, heading: Float?, toleranceDeg: Float = 5f): Boolean {
    if (heading == null) return false
    val diff = ((qiblaBearing - heading + 540) % 360) - 180
    return kotlin.math.abs(diff) <= toleranceDeg
}

/** Magnit og'ishi (deklinatsiya): haqiqiy shimol = magnit shimol + declination. */
fun magneticDeclination(latitude: Double, longitude: Double): Float =
    android.hardware.GeomagneticField(latitude.toFloat(), longitude.toFloat(), 0f, System.currentTimeMillis()).declination

fun trueHeading(magnetic: Float, declination: Float): Float = (magnetic + declination + 360f) % 360f
