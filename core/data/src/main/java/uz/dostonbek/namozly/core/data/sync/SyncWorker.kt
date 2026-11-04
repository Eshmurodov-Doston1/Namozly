package uz.dostonbek.namozly.core.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import uz.dostonbek.namozly.core.common.runSuspendCatching
import uz.dostonbek.namozly.core.data.repository.RadioRepositoryImpl
import uz.dostonbek.namozly.core.domain.repository.BookRepository
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.repository.MosqueRepository
import uz.dostonbek.namozly.core.domain.repository.PrayerAlarmScheduler
import uz.dostonbek.namozly.core.domain.repository.PrayerTimesRepository
import uz.dostonbek.namozly.core.domain.repository.QuranRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import java.util.concurrent.TimeUnit

/**
 * Fonda ma'lumotlarni yangilash (internet bo'lganda). Bir marta yuklangan hamma narsa Room/DataStore'da
 * qoladi, shuning uchun ilova keyin internetsiz ham to'liq ishlaydi:
 *  - namoz vaqtlari: joriy + keyingi 2 oy (internet bo'lmasa ham adhan bilan hisoblanadi);
 *  - Qur'on: 114 sura matni va tanlangan tildagi tarjimasi;
 *  - kitoblar: har bir o'qilayotgan kitobda qolgan joydan keyingi sahifalar;
 *  - masjidlar (joriy joy atrofida) va radio ro'yxati.
 * Har bir bosqich mustaqil: biri xato bersa, qolganlari davom etadi.
 */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Deps {
        fun prayer(): PrayerTimesRepository
        fun alarms(): PrayerAlarmScheduler
        fun quran(): QuranRepository
        fun books(): BookRepository
        fun settings(): SettingsRepository
        fun location(): LocationRepository
        fun mosques(): MosqueRepository
        fun radio(): RadioRepositoryImpl
    }

    override suspend fun doWork(): Result {
        val d = EntryPointAccessors.fromApplication(applicationContext, Deps::class.java)
        var failures = 0
        suspend fun step(name: String, block: suspend () -> Unit) {
            runSuspendCatching { block() }.onFailure { failures++; Log.w(TAG, "sync $name failed", it) }
        }
        val language = d.settings().settings.first().language

        step("prayer") {
            d.prayer().refresh(months = 3)
            d.alarms().rescheduleAll()
        }
        step("quran") {
            d.quran().surahs().forEach { s -> d.quran().sync(s.number, language) }
        }
        step("books") {
            val books = d.books().books().associateBy { it.id }
            d.books().positions.first().forEach { (id, at) ->
                val book = books[id] ?: return@forEach
                for (n in at..(at + PREFETCH_PAGES).coerceAtMost(book.total)) d.books().page(book, n, language)
            }
        }
        step("mosques") {
            d.mosques().nearby(d.location().location.first().point).getOrThrow()
            d.mosques().worldFamous()
        }
        step("radio") { d.radio().refresh() }

        // Qisman muvaffaqiyat ham yetarli; to'liq muvaffaqiyatsizlikda keyinroq qayta urinamiz.
        return if (failures >= 5 && runAttemptCount < 3) Result.retry() else Result.success()
    }

    companion object {
        private const val TAG = "NamozlySync"
        private const val PREFETCH_PAGES = 20
        private const val ONCE = "namozly_sync_now"
        private const val PERIODIC = "namozly_sync_periodic"

        private val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        /**
         * Ilova ishga tushganda chaqiriladi: internet ulanishi bilan bir martalik yangilash
         * va har 12 soatda takrorlanadigan fon yangilash (faqat internet bo'lganda).
         */
        fun schedule(context: Context) {
            val wm = WorkManager.getInstance(context)
            wm.enqueueUniqueWork(
                ONCE, ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(network)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES).build(),
            )
            wm.enqueueUniquePeriodicWork(
                PERIODIC, ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<SyncWorker>(12, TimeUnit.HOURS).setConstraints(network).build(),
            )
        }

        /** Sozlama (til, joy) o'zgarganda — darhol qayta yangilash. */
        fun syncNow(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONCE, ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(network).build(),
            )
        }
    }
}
