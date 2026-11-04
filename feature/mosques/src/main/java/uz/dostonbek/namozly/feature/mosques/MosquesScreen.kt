package uz.dostonbek.namozly.feature.mosques

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.dostonbek.namozly.core.designsystem.component.DBackButton
import uz.dostonbek.namozly.core.designsystem.component.DIconButton
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.EmptyState
import uz.dostonbek.namozly.core.designsystem.component.LoadingBox
import uz.dostonbek.namozly.core.designsystem.component.MosqueImage
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.domain.repository.LocationRepository
import uz.dostonbek.namozly.core.domain.repository.MosqueRepository
import uz.dostonbek.namozly.core.domain.util.QiblaCalculator
import uz.dostonbek.namozly.core.model.GeoPoint
import uz.dostonbek.namozly.core.model.Mosque
import uz.dostonbek.namozly.core.ui.R
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

data class MosquesUiState(
    val center: GeoPoint = GeoPoint(41.2995, 69.2401),
    val nearby: List<Mosque> = emptyList(),
    val nearbyLoading: Boolean = true,
    val nearbyError: Boolean = false,
    val world: List<Mosque> = emptyList(),
    val worldLoading: Boolean = true,
)

@HiltViewModel
class MosquesViewModel @Inject constructor(
    private val repository: MosqueRepository,
    locationRepository: LocationRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MosquesUiState())
    val uiState: StateFlow<MosquesUiState> = _state.asStateFlow()

    init {
        // Avtomatik joylashuvda — GPS'dan yangilaymiz (eski/standart Toshkent nuqtasi bilan qolmaslik uchun).
        viewModelScope.launch {
            val current = locationRepository.location.first()
            if (current.isAutomatic || current == uz.dostonbek.namozly.core.model.UserLocation.Default) {
                runCatching { locationRepository.refreshFromDevice() }
            }
        }
        viewModelScope.launch {
            locationRepository.location.map { it.point }.distinctUntilChanged().collect { point ->
                _state.update { it.copy(center = point) }
                loadNearby(point)
            }
        }
        viewModelScope.launch {
            val world = repository.worldFamous()
            _state.update { it.copy(world = world, worldLoading = false) }
        }
    }

    private suspend fun loadNearby(point: GeoPoint) {
        _state.update { it.copy(nearbyLoading = true, nearbyError = false) }
        val result = repository.nearby(point, 5000)
        _state.update { it.copy(nearby = result.getOrDefault(emptyList()), nearbyLoading = false, nearbyError = result.isFailure) }
    }

    fun retry() = viewModelScope.launch { loadNearby(_state.value.center) }
}

internal fun formatDistance(meters: Double): String =
    if (meters < 1000) "${meters.toInt()} m" else String.format(Locale.getDefault(), "%.1f km", meters / 1000)

private fun openDirections(context: android.content.Context, m: Mosque) {
    val uri = Uri.parse("geo:${m.point.latitude},${m.point.longitude}?q=${m.point.latitude},${m.point.longitude}(${Uri.encode(m.name)})")
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
}

/** Dizayn: Masjidlar.dc.html — to'liq ekran xarita + pastdagi masjid kartasi. Xarita kaliti bo'lmasa — ro'yxat. */
@Composable
internal fun MosquesScreenRoute(onBack: () -> Unit, onOpenWorld: () -> Unit, viewModel: MosquesViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var zoom by remember { mutableFloatStateOf(14f) }
    // Standart — ro'yxat; xarita kaliti bo'lsa "Xarita" rejimiga o'tish mumkin.
    var mapMode by rememberSaveable { mutableStateOf(false) }
    val showMap = mapMode && YandexMapInitializer.isReady
    val context = LocalContext.current
    val list = state.nearby.filter { query.isBlank() || it.name.contains(query.trim(), true) }
    val selected = list.firstOrNull { it.id == selectedId } ?: list.firstOrNull()

    Box(Modifier.fillMaxSize().background(t.bg)) {
        when {
            state.nearbyLoading -> LoadingBox()
            state.nearbyError -> EmptyState(Icons.Rounded.CloudOff, stringResource(R.string.error_no_internet), action = stringResource(R.string.common_retry), onAction = { viewModel.retry() }, modifier = Modifier.padding(top = 140.dp))
            list.isEmpty() -> EmptyState(Icons.Rounded.Mosque, stringResource(R.string.mosques_empty), modifier = Modifier.padding(top = 140.dp))
            showMap -> MosqueMap(
                center = selected?.point ?: state.center, zoom = zoom, mosques = list, clustered = false,
                onMosqueClick = { selectedId = it.id }, modifier = Modifier.fillMaxSize(),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 150.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) { items(list, key = { it.id }) { m -> MosqueSheet(m, Modifier, onDirections = { openDirections(context, m) }) } }
        }
        // Yuqori panel: orqaga, qidiruv, filtrlar
        Column(Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DBackButton(onBack, Modifier.shadow(6.dp, CircleShape))
                Row(
                    Modifier.weight(1f).height(48.dp).shadow(6.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(t.surface).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Rounded.Search, null, tint = t.muted, modifier = Modifier.size(18.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) DText(stringResource(R.string.mosques_search_hint), 15.sp, FontWeight.Normal, t.faint)
                        BasicTextField(query, { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 15.sp, color = t.text), cursorBrush = SolidColor(t.primary))
                    }
                }
                DIconButton(Icons.Outlined.Public, onOpenWorld, Modifier.shadow(6.dp, CircleShape), contentDescription = stringResource(R.string.mosques_world))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(stringResource(R.string.mosques_nearest), !showMap) { mapMode = false }
                if (YandexMapInitializer.isReady) Chip(stringResource(R.string.mosques_map), showMap) { mapMode = true }
                Chip(stringResource(R.string.mosques_world), false, onOpenWorld)
            }
        }
        if (showMap && selected != null && !state.nearbyLoading) {
            Column(Modifier.align(Alignment.CenterEnd).padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DIconButton(Icons.Rounded.Add, { zoom = (zoom + 1).coerceAtMost(19f) }, Modifier.shadow(6.dp, CircleShape))
                DIconButton(Icons.Rounded.Remove, { zoom = (zoom - 1).coerceAtLeast(3f) }, Modifier.shadow(6.dp, CircleShape))
            }
            MosqueSheet(
                selected,
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp),
                onDirections = { openDirections(context, selected) },
            )
        }
    }
}

@Composable
private fun Chip(text: String, on: Boolean, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Box(
        Modifier.height(36.dp).shadow(4.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp))
            .background(if (on) t.primary else t.surface).clickable(onClick = onClick).padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { DText(text, 13.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) Color.White else t.text) }
}

/** Pastki karta: rasm, masofa, nom, manzil, "Yo'l ko'rsatish". */
@Composable
private fun MosqueSheet(m: Mosque, modifier: Modifier, onDirections: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Column(modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)).background(t.surface)) {
        Box(Modifier.fillMaxWidth().height(120.dp)) {
            MosqueImage(m.photoUrl, m.id, Modifier.fillMaxSize(), m.name)
            m.distanceMeters?.let { d ->
                Box(Modifier.padding(12.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    DText(stringResource(R.string.mosques_distance_eta, formatDistance(d), (d / 80).toInt().coerceAtLeast(1)), 12.sp, FontWeight.ExtraBold, Color(0xFF0A3D33))
                }
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                DText(m.name, 17.sp, FontWeight.ExtraBold, t.text, maxLines = 1)
                DText(listOfNotNull(m.address, m.city).joinToString(" · ").ifBlank { "OpenStreetMap" }, 13.sp, FontWeight.Normal, t.muted, maxLines = 1)
            }
            Row(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(t.primary).clickable(onClick = onDirections),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                Icon(Icons.Rounded.Directions, null, tint = Color.White, modifier = Modifier.size(18.dp))
                DText(stringResource(R.string.home_directions), 14.sp, FontWeight.ExtraBold, Color.White)
            }
        }
    }
}

/** Dizayn: DunyoMasjidlari.dc.html + DunyoXarita.dc.html */
@Composable
internal fun WorldMosquesScreen(onBack: () -> Unit, viewModel: MosquesViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var mapMode by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val countries = state.world.groupBy { it.country.orEmpty() }.toList().sortedByDescending { it.second.size }.map { it.first }
    val filtered = state.world.filter { m ->
        (country == null || m.country == country) &&
            (query.isBlank() || listOfNotNull(m.name, m.city, m.country).any { it.contains(query.trim(), true) })
    }
    val featured = state.world.getOrNull((LocalDate.now().toEpochDay() / 7 % state.world.size.coerceAtLeast(1)).toInt())
    val nf = NumberFormat.getIntegerInstance(Locale.getDefault())

    Column(Modifier.fillMaxSize().background(t.bg)) {
        Row(Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DBackButton(onBack)
            Column(Modifier.weight(1f)) {
                DText(stringResource(R.string.mosques_world), 22.sp, FontWeight.ExtraBold, t.strong, maxLines = 1)
                DText(stringResource(R.string.mosques_world_sub), 12.sp, FontWeight.Normal, t.muted, maxLines = 1)
            }
            Row(Modifier.clip(RoundedCornerShape(12.dp)).background(if (t.isDark) t.chip else Color(0xFFEAE3D3)).padding(3.dp)) {
                listOf(false to R.string.mosques_list, true to R.string.mosques_map).forEach { (m, label) ->
                    val on = mapMode == m
                    Box(
                        Modifier.height(32.dp).clip(RoundedCornerShape(9.dp)).background(if (on) t.surface else Color.Transparent).clickable { mapMode = m }.padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) { DText(stringResource(label), 12.sp, if (on) FontWeight.ExtraBold else FontWeight.SemiBold, if (on) t.strong else t.muted) }
                }
            }
        }
        if (state.worldLoading) { LoadingBox(); return@Column }
        if (mapMode && YandexMapInitializer.isReady) {
            Box(Modifier.weight(1f).padding(top = 12.dp)) {
                MosqueMap(
                    center = filtered.firstOrNull()?.point?.takeIf { country != null } ?: GeoPoint(30.0, 45.0),
                    zoom = if (country != null) 4.5f else 2.4f, mosques = filtered, clustered = true,
                    onMosqueClick = { openDirections(context, it) }, modifier = Modifier.fillMaxSize(),
                )
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(t.surface)
                        .navigationBarsPadding().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CountryChips(countries, country) { country = it }
                    DText(country ?: stringResource(R.string.common_all), 16.sp, FontWeight.ExtraBold, t.strong)
                    filtered.groupBy { it.city.orEmpty() }.entries.take(3).forEach { (city, ms) ->
                        DText("$city · ${ms.size}", 12.sp, FontWeight.Bold, t.accent)
                        ms.take(3).forEach { m -> DText(m.name, 14.sp, FontWeight.SemiBold, t.text, maxLines = 1) }
                    }
                }
            }
            return@Column
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                Row(
                    Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(t.surface).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Rounded.Search, null, tint = t.muted, modifier = Modifier.size(18.dp))
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) DText(stringResource(R.string.mosques_search_hint), 15.sp, FontWeight.Normal, t.faint)
                        BasicTextField(query, { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 15.sp, color = t.text), cursorBrush = SolidColor(t.primary))
                    }
                }
            }
            item(span = { GridItemSpan(2) }) { CountryChips(countries, country) { country = it } }
            if (featured != null && country == null && query.isBlank()) {
                item(span = { GridItemSpan(2) }) {
                    Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp)).clickable { openDirections(context, featured) }) {
                        MosqueImage(featured.photoUrl, featured.id, Modifier.fillMaxSize(), featured.name)
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6071310)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(AccentGold).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                DText(stringResource(R.string.mosques_of_week), 11.sp, FontWeight.ExtraBold, Color(0xFF2B1F06), spacing = 0.06.em)
                            }
                            DText(featured.name, 20.sp, FontWeight.ExtraBold, Color(0xFFF6F1E6))
                            DText("${featured.city}, ${featured.country} · ${nf.format(QiblaCalculator.distanceKm(state.center, featured.point).toLong())} km", 12.sp, FontWeight.Normal, Color(0xFFCFE3DA))
                        }
                    }
                }
            }
            item(span = { GridItemSpan(2) }) {
                Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    DText(stringResource(R.string.mosques_famous), 17.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
                    DText(stringResource(R.string.mosques_countries_n, countries.size), 13.sp, FontWeight.SemiBold, t.muted)
                }
            }
            items(filtered.size) { i ->
                val m = filtered[i]
                Column(Modifier.clip(RoundedCornerShape(20.dp)).background(t.surface).clickable { openDirections(context, m) }) {
                    MosqueImage(m.photoUrl, m.id, Modifier.fillMaxWidth().aspectRatio(1.3f), m.name)
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        DText(m.name, 14.sp, FontWeight.ExtraBold, t.text, maxLines = 1)
                        DText("${m.city}, ${m.country}", 12.sp, FontWeight.Normal, t.muted, maxLines = 1)
                    }
                }
            }
            item(span = { GridItemSpan(2) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Info, null, tint = t.muted, modifier = Modifier.size(16.dp))
                    DText(stringResource(R.string.mosques_photo_note), 12.sp, FontWeight.Normal, t.muted, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun CountryChips(countries: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    val t = NamozlyThemeTokens.t
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            val on = selected == null
            Box(Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (on) (if (t.isDark) t.primary else t.strong) else t.surface).clickable { onSelect(null) }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                DText(stringResource(R.string.common_all), 13.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) Color.White else t.text)
            }
        }
        items(countries) { c ->
            val on = selected == c
            Box(Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (on) (if (t.isDark) t.primary else t.strong) else t.surface).clickable { onSelect(c) }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                DText(c, 13.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) Color.White else t.text)
            }
        }
    }
}

