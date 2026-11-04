package uz.dostonbek.namozly.core.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uz.dostonbek.namozly.core.common.ApplicationScope
import uz.dostonbek.namozly.core.domain.repository.NotificationRepository
import uz.dostonbek.namozly.core.domain.repository.PrayerAlarmScheduler
import uz.dostonbek.namozly.core.model.NotificationType
import uz.dostonbek.namozly.core.model.Prayer
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationsEntryPoint {
    fun scheduler(): PrayerAlarmScheduler
    fun notificationRepository(): NotificationRepository
    @ApplicationScope fun appScope(): CoroutineScope
}

private fun Context.entryPoint() = EntryPointAccessors.fromApplication(applicationContext, NotificationsEntryPoint::class.java)

/** Namoz vaqti kirganda bildirishnoma ko'rsatadi va keyingi jadvalni yangilaydi. */
class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra(EXTRA_PRAYER)?.let { runCatching { Prayer.valueOf(it) }.getOrNull() } ?: return
        val kind = intent.getStringExtra(EXTRA_KIND)?.let { runCatching { AlarmKind.valueOf(it) }.getOrNull() } ?: AlarmKind.PRAYER
        val minutes = intent.getIntExtra(EXTRA_MINUTES, 0)

        val name = context.getString(prayer.labelRes())
        val (channel, title, body, type) = when (kind) {
            AlarmKind.PRAYER -> Quad(
                NotificationChannels.PRAYER,
                context.getString(R.string.notif_prayer_title, name),
                context.getString(R.string.notif_prayer_body, name, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))),
                NotificationType.PRAYER,
            )
            AlarmKind.PRE_ALERT -> Quad(
                NotificationChannels.PRE_ALERT,
                context.getString(R.string.notif_pre_title, name, minutes),
                context.getString(R.string.notif_pre_body),
                NotificationType.PRE_ALERT,
            )
            AlarmKind.SUHOOR -> Quad(
                NotificationChannels.RAMADAN,
                context.getString(R.string.notif_suhoor_title, minutes),
                name,
                NotificationType.RAMADAN,
            )
            AlarmKind.IFTAR -> Quad(
                NotificationChannels.RAMADAN,
                context.getString(R.string.notif_iftar_title),
                context.getString(R.string.notif_iftar_body),
                NotificationType.RAMADAN,
            )
        }
        show(context, channel, title, body, id = (kind.ordinal * 10) + prayer.ordinal)

        val pending = goAsync()
        val ep = context.entryPoint()
        ep.appScope().launch {
            try {
                ep.notificationRepository().add(type, title, body)
                if (kind == AlarmKind.PRAYER && prayer == Prayer.ISHA) ep.scheduler().rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }

    private fun show(context: Context, channel: String, title: String, body: String, id: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        NotificationChannels.create(context)
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent = launch?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_namozly)
            .setContentTitle(title)
            .setContentText(body)
            .setColor(0xFF0E5A4A.toInt())
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    private data class Quad(val channel: String, val title: String, val body: String, val type: NotificationType)

    companion object {
        const val ACTION_ALARM = "uz.dostonbek.namozly.action.PRAYER_ALARM"
        private const val EXTRA_PRAYER = "prayer"
        private const val EXTRA_KIND = "kind"
        private const val EXTRA_MINUTES = "minutes"

        fun intent(context: Context, prayer: Prayer, kind: AlarmKind, minutes: Int): Intent =
            Intent(context, PrayerAlarmReceiver::class.java)
                .setAction(ACTION_ALARM)
                .putExtra(EXTRA_PRAYER, prayer.name)
                .putExtra(EXTRA_KIND, kind.name)
                .putExtra(EXTRA_MINUTES, minutes)
    }
}

/** Qayta yoqilish / vaqt o'zgarishi / ilova yangilanishida jadvalni tiklaydi. */
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val ep = context.entryPoint()
        ep.appScope().launch {
            try { ep.scheduler().rescheduleAll() } finally { pending.finish() }
        }
    }
}

fun Prayer.labelRes(): Int = when (this) {
    Prayer.FAJR -> R.string.prayer_fajr
    Prayer.SUNRISE -> R.string.prayer_sunrise
    Prayer.DHUHR -> R.string.prayer_dhuhr
    Prayer.ASR -> R.string.prayer_asr
    Prayer.MAGHRIB -> R.string.prayer_maghrib
    Prayer.ISHA -> R.string.prayer_isha
}
