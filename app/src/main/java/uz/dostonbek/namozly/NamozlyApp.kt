package uz.dostonbek.namozly

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uz.dostonbek.namozly.core.common.ApplicationScope
import uz.dostonbek.namozly.core.data.sync.SyncWorker
import uz.dostonbek.namozly.core.domain.repository.PrayerAlarmScheduler
import uz.dostonbek.namozly.core.notifications.NotificationChannels
import uz.dostonbek.namozly.feature.mosques.YandexMapInitializer
import javax.inject.Inject

@HiltAndroidApp
class NamozlyApp : Application() {

    @Inject lateinit var scheduler: PrayerAlarmScheduler
    @Inject @field:ApplicationScope lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
        runCatching { YandexMapInitializer.init(this, BuildConfig.YANDEX_MAPKIT_API_KEY) }
        appScope.launch { scheduler.rescheduleAll() }
        // Internet ulanganda ma'lumotlarni fonda yangilash (keyin hammasi oflayn ishlaydi).
        SyncWorker.schedule(this)
    }
}
