package uz.dostonbek.namozly.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager

object NotificationChannels {
    const val PRAYER = "prayer_times"
    const val PRE_ALERT = "prayer_pre_alert"
    const val RAMADAN = "ramadan"

    fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(PRAYER, context.getString(R.string.notif_channel_prayer), NotificationManager.IMPORTANCE_HIGH).apply {
                    description = context.getString(R.string.notif_channel_prayer_desc)
                    setSound(sound, attrs)
                    enableVibration(true)
                },
                NotificationChannel(PRE_ALERT, context.getString(R.string.notif_channel_pre), NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(RAMADAN, context.getString(R.string.notif_channel_ramadan), NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(sound, attrs)
                },
            ),
        )
    }
}
