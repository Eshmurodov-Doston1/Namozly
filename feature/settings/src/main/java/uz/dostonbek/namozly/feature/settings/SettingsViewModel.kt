package uz.dostonbek.namozly.feature.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.repository.NotificationRepository
import uz.dostonbek.namozly.core.domain.repository.PrayerAlarmScheduler
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.AppNotification
import uz.dostonbek.namozly.core.model.NotificationSettings
import uz.dostonbek.namozly.core.model.PrayerSettings
import uz.dostonbek.namozly.core.model.ThemeMode
import uz.dostonbek.namozly.core.model.UserLocation
import uz.dostonbek.namozly.core.model.UserSettings
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val scheduler: PrayerAlarmScheduler,
    locationRepository: LocationRepository,
) : ViewModel() {
    val state: StateFlow<UserSettings> = settings.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())
    val location: StateFlow<UserLocation> = locationRepository.location
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserLocation.Default)

    fun setLanguage(language: AppLanguage) = viewModelScope.launch {
        settings.setLanguage(language)
        // Android 13+ da tizim sozlamalari bilan sinxron, eskilarida AppCompat saqlaydi.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { settings.setHaptics(enabled) }

    fun updatePrayer(transform: (PrayerSettings) -> PrayerSettings) = viewModelScope.launch {
        settings.setPrayerSettings(transform(state.value.prayer))
        scheduler.rescheduleAll()
    }

    fun updateNotifications(transform: (NotificationSettings) -> NotificationSettings) = viewModelScope.launch {
        settings.setNotificationSettings(transform(state.value.notifications))
        scheduler.rescheduleAll()
    }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {
    val items: StateFlow<List<AppNotification>> = repository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markAllRead() = viewModelScope.launch { repository.markAllRead() }
    fun clear() = viewModelScope.launch { repository.clear() }
}

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository,
    private val scheduler: PrayerAlarmScheduler,
) : ViewModel() {
    val current: StateFlow<UserLocation> = repository.location
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserLocation.Default)

    private val _results = MutableStateFlow<List<UserLocation>>(emptyList())
    val results: StateFlow<List<UserLocation>> = _results.asStateFlow()
    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    fun search(query: String) = viewModelScope.launch {
        _searching.value = true
        _results.value = repository.search(query)
        _searching.value = false
    }

    fun select(location: UserLocation, onDone: () -> Unit) = viewModelScope.launch {
        repository.setManual(location)
        scheduler.rescheduleAll()
        onDone()
    }

    fun detect(onResult: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = repository.refreshFromDevice()
        if (ok) scheduler.rescheduleAll()
        onResult(ok)
    }
}
