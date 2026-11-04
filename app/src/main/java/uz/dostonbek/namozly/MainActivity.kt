package uz.dostonbek.namozly

import android.os.Bundle
import uz.dostonbek.namozly.ui.AppLanguageResolver
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.combine
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyTheme
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.model.ThemeMode
import uz.dostonbek.namozly.core.model.UserSettings
import uz.dostonbek.namozly.core.ui.LocalAppLanguage
import uz.dostonbek.namozly.ui.NamozlyAppRoot
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {
    val settings: StateFlow<UserSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    init {
        // Til qo'lda tanlanmagan bo'lsa — mamlakatga qarab (joy o'zgarsa ham kuzatib boriladi).
        viewModelScope.launch {
            combine(repository.languageChosenByUser, repository.settings, locationRepository.location) { manual, s, loc -> Triple(manual, s.language, loc) }
                .distinctUntilChanged()
                .collect { (manual, current, loc) ->
                    val target = if (manual) current else AppLanguageResolver.resolve(context, loc)
                    if (!manual && target != current) repository.setLanguageAuto(target)
                    applyLocale(target)
                }
        }
    }

    /** Ilova resurslari tanlangan tilda bo'lishi uchun (aks holda Android telefon tilini oladi). */
    private fun applyLocale(language: AppLanguage) {
        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        if (!currentTags.equals(language.tag, ignoreCase = true)) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
        }
    }
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CompositionLocalProvider(LocalAppLanguage provides settings.language) {
                NamozlyTheme(darkTheme = dark) {
                    NamozlyAppRoot()
                }
            }
        }
    }
}
