package uz.dostonbek.namozly.feature.live

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.style.TextAlign
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.update
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.HolyPlace
import uz.dostonbek.namozly.core.designsystem.component.HolyScene
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.domain.repository.QuranPlayer
import uz.dostonbek.namozly.core.domain.repository.RadioRepository
import uz.dostonbek.namozly.core.model.LiveChannel
import uz.dostonbek.namozly.core.model.PlaybackState
import uz.dostonbek.namozly.core.model.RadioStation
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.localized
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Serializable data object LiveRoute
@Serializable data class LivePlayerRoute(val channelId: String)

fun NavController.navigateToLive() = navigate(LiveRoute)
fun NavController.navigateToLivePlayer(id: String) = navigate(LivePlayerRoute(id))

fun NavGraphBuilder.liveScreens(onBack: () -> Unit, onOpen: (String) -> Unit) {
    composable<LiveRoute> { LiveListScreen(onBack, onOpen) }
    composable<LivePlayerRoute> { LivePlayerScreen(onBack) }
}

private val Bg = Color(0xFF07130F)
private val Card = Color(0xFF11231F)
private val Cream = Color(0xFFF6F1E6)
private val Sub = Color(0xFFAFC2BA)
private val Red = Color(0xFFE5484D)

private fun place(id: String) = when (id) { "nabawi" -> HolyPlace.MADINAH; "aqsa" -> HolyPlace.QUDS; else -> HolyPlace.MAKKAH }
private fun zone(id: String): ZoneId = ZoneId.of(if (id == "aqsa") "Asia/Jerusalem" else "Asia/Riyadh")
private fun localTime(id: String): String = ZonedDateTime.now(zone(id)).format(DateTimeFormatter.ofPattern("HH:mm"))

@HiltViewModel
class LiveViewModel @Inject constructor(
    content: ContentRepository,
    savedStateHandle: SavedStateHandle,
    private val radio: RadioRepository,
    private val player: QuranPlayer,
) : ViewModel() {
    val channels: List<LiveChannel> = content.liveChannels()
    private val initial = runCatching { savedStateHandle.toRoute<LivePlayerRoute>().channelId }.getOrNull()
    private val _selected = MutableStateFlow(channels.firstOrNull { it.id == initial } ?: channels.first())
    val selected: StateFlow<LiveChannel> = _selected.asStateFlow()
    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()
    val playback: StateFlow<PlaybackState> = player.state

    init {
        viewModelScope.launch {
            val all = radio.stations()
            // Avval Saudiya/Makka radiolari, so'ng qolganlari
            _stations.value = all.sortedByDescending { s -> listOf("Saudi", "Makkah", "Mecca", "Sharjah").any { s.name.contains(it, true) } }.take(4)
        }
    }

    fun select(c: LiveChannel) { _selected.value = c }

    fun toggleRadio(s: RadioStation) {
        val st = player.state.value
        if (st.streamTitle == s.name && st.isPlaying) player.pause()
        else if (st.streamTitle == s.name) player.resume()
        else player.playStream(s.url, s.name)
    }
}

@Composable
private fun LiveBadge(modifier: Modifier = Modifier, height: Int = 26) {
    val t = rememberInfiniteTransition(label = "live")
    val ring by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Restart), label = "r")
    Row(
        modifier.height(height.dp).clip(RoundedCornerShape((height / 2).dp)).background(Red).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Canvas(Modifier.size(7.dp)) {
            drawCircle(Color.White.copy(alpha = 0.7f * (1 - ring)), radius = size.minDimension / 2 * (1 + ring * 1.2f))
            drawCircle(Color.White)
        }
        DText(stringResource(R.string.live_badge), 11.sp, FontWeight.ExtraBold, Color.White, spacing = 0.06.em)
    }
}

@Composable
private fun DarkBack(onBack: () -> Unit) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(Cream.copy(alpha = 0.10f)).clickable(onClick = onBack), contentAlignment = Alignment.Center) {
        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = Cream, modifier = Modifier.size(20.dp))
    }
}

/** Dizayn: JonliEfir.dc.html */
@Composable
internal fun LiveListScreen(onBack: () -> Unit, onOpen: (String) -> Unit, viewModel: LiveViewModel = hiltViewModel()) {
    val stations by viewModel.stations.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Har doim ilova ichidagi pleyer ochiladi; oqim bo'lmasa (Aqso) — pleyerning o'zida YouTube tugmasi bor.
    val open: (LiveChannel) -> Unit = { ch -> onOpen(ch.id) }
    val featured = viewModel.channels.first()
    LazyColumn(Modifier.fillMaxSize().background(Bg), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.statusBarsPadding(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DarkBack(onBack)
                Column(Modifier.weight(1f)) {
                    DText(stringResource(R.string.live_title), 22.sp, FontWeight.ExtraBold, Cream)
                    DText(stringResource(R.string.live_subtitle), 12.sp, FontWeight.Normal, Sub)
                }
            }
        }
        item {
            Box(Modifier.fillMaxWidth().height(232.dp).shadow(18.dp, RoundedCornerShape(26.dp)).clip(RoundedCornerShape(26.dp)).clickable { open(featured) }) {
                HolyScene(HolyPlace.MAKKAH, Modifier.fillMaxSize())
                LiveBadge(Modifier.align(Alignment.TopStart).padding(14.dp))
                Box(Modifier.align(Alignment.TopEnd).padding(14.dp).height(26.dp).clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.45f)).padding(horizontal = 9.dp), contentAlignment = Alignment.Center) {
                    DText("HD", 11.sp, FontWeight.ExtraBold, Cream)
                }
                Row(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Bg.copy(alpha = 0f), Bg.copy(alpha = 0.92f))))
                        .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 14.dp),
                    verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        DText(featured.name.localized(), 20.sp, FontWeight.ExtraBold, Cream)
                        DText(stringResource(R.string.live_local_time, featured.city.localized(), localTime(featured.id)), 12.sp, FontWeight.Normal, Color(0xFFCFE3DA))
                    }
                    Box(Modifier.size(52.dp).clip(CircleShape).background(AccentGold), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = Color(0xFF2B1F06), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        items(viewModel.channels.drop(1), key = { it.id }) { ch ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).clickable { open(ch) }.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(Modifier.width(140.dp).height(79.dp).clip(RoundedCornerShape(14.dp))) {
                    HolyScene(place(ch.id), Modifier.fillMaxSize())
                    Box(Modifier.align(Alignment.Center).size(34.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DText(ch.name.localized(), 16.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
                    DText(stringResource(R.string.live_local_time, ch.city.localized(), localTime(ch.id)), 12.sp, FontWeight.Normal, Sub, maxLines = 1)
                    if (ch.id == "aqsa") DText(stringResource(R.string.live_aqsa_note), 11.sp, FontWeight.ExtraBold, Color(0xFFE7CF97), maxLines = 1)
                    else DText(stringResource(R.string.live_24_7), 11.sp, FontWeight.ExtraBold, Color(0xFFFF8E91))
                }
            }
        }
        if (stations.isNotEmpty()) {
            item {
                Row(Modifier.padding(horizontal = 4.dp).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    DText(stringResource(R.string.live_radio), 16.sp, FontWeight.ExtraBold, Cream, Modifier.weight(1f))
                    DText(stringResource(R.string.live_radio_bg), 12.sp, FontWeight.Normal, Sub)
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    stations.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { s ->
                                val on = playback.streamTitle == s.name
                                Row(
                                    Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(18.dp)).background(Card)
                                        .border(1.dp, if (on) AccentGold else Cream.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
                                        .clickable { viewModel.toggleRadio(s) }.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    if (on && playback.isPlaying) Equalizer() else Box(Modifier.size(28.dp).clip(CircleShape).background(Cream.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.PlayArrow, null, tint = Cream, modifier = Modifier.size(14.dp))
                                    }
                                    Column(Modifier.weight(1f)) {
                                        DText(s.name, 13.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
                                        DText(stringResource(R.string.live_radio_label), 11.sp, FontWeight.Normal, Sub)
                                    }
                                }
                            }
                            if (row.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        item {
            Row(Modifier.padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, null, tint = Color(0xFF8FA79E), modifier = Modifier.size(16.dp))
                DText(stringResource(R.string.live_note), 12.sp, FontWeight.Normal, Color(0xFF8FA79E))
            }
        }
    }
}

@Composable
private fun Equalizer() {
    val t = rememberInfiniteTransition(label = "eq")
    val bars = listOf(0, 180, 360).map { d -> t.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500, delayMillis = d), RepeatMode.Reverse), label = "b$d") }
    Row(Modifier.height(20.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        bars.forEach { b -> Box(Modifier.width(4.dp).height((20 * b.value).dp).clip(RoundedCornerShape(2.dp)).background(AccentGold)) }
    }
}

/**
 * Ilova ichidagi jonli video pleyer (media3 ExoPlayer, HLS). Bir nechta rasmiy oqim navbat bilan sinaladi:
 * birinchisi ishlamasa avtomatik keyingisiga o'tadi.
 */
@HiltViewModel
class LiveVideoViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {
    data class UiState(
        val channelId: String? = null,
        val streamIndex: Int = 0,
        val streamCount: Int = 0,
        val playing: Boolean = false,
        val buffering: Boolean = true,
        val failed: Boolean = false,
        val muted: Boolean = false,
        val audioOnly: Boolean = false,
        val lowQuality: Boolean = false,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    private var streams: List<String> = emptyList()

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        setAudioAttributes(AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true)
        setHandleAudioBecomingNoisy(true)
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { _state.update { it.copy(playing = isPlaying) } }
            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update { it.copy(buffering = playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_IDLE && !it.failed) }
            }
            override fun onPlayerError(error: PlaybackException) { next() }
        })
    }

    fun open(channel: LiveChannel, index: Int = 0) {
        streams = channel.streams
        _state.update { it.copy(channelId = channel.id, streamIndex = index, streamCount = streams.size, failed = streams.isEmpty(), buffering = streams.isNotEmpty()) }
        if (streams.isEmpty()) { player.stop(); return }
        player.setMediaItem(MediaItem.Builder().setUri(streams[index]).setMimeType(MimeTypes.APPLICATION_M3U8).build())
        player.prepare()
        player.playWhenReady = true
    }

    /** Joriy oqim xato bersa — keyingi zaxira oqim. */
    private fun next() {
        val i = _state.value.streamIndex + 1
        if (i < streams.size) {
            _state.update { it.copy(streamIndex = i) }
            player.setMediaItem(MediaItem.Builder().setUri(streams[i]).setMimeType(MimeTypes.APPLICATION_M3U8).build())
            player.prepare()
            player.playWhenReady = true
        } else {
            _state.update { it.copy(failed = true, buffering = false, playing = false) }
        }
    }

    fun selectSource(i: Int) {
        val id = _state.value.channelId ?: return
        if (i !in streams.indices) return
        _state.update { it.copy(failed = false, channelId = id) }
        player.setMediaItem(MediaItem.Builder().setUri(streams[i]).setMimeType(MimeTypes.APPLICATION_M3U8).build())
        _state.update { it.copy(streamIndex = i, buffering = true) }
        player.prepare()
        player.playWhenReady = true
    }

    fun retry() = selectSource(0)

    fun togglePlay() {
        if (player.isPlaying) player.pause() else {
            // Jonli efirda pauzadan keyin — eng so'nggi joyga qaytamiz.
            player.seekToDefaultPosition()
            player.play()
        }
    }

    fun toggleMute() {
        val m = !_state.value.muted
        player.volume = if (m) 0f else 1f
        _state.update { it.copy(muted = m) }
    }

    /** Faqat ovoz: video yo'lagi o'chiriladi — internet trafigi kamayadi. */
    fun toggleAudioOnly() {
        val a = !_state.value.audioOnly
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, a).build()
        _state.update { it.copy(audioOnly = a) }
    }

    /** Past sifat (≤ 480p) — sekin internet uchun. */
    fun toggleQuality() {
        val low = !_state.value.lowQuality
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .apply { if (low) setMaxVideoSize(854, 480) else clearVideoSizeConstraints() }.build()
        _state.update { it.copy(lowQuality = low) }
    }

    fun pause() = player.pause()

    override fun onCleared() { player.release() }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/** Dizayn: JonliPlayer.dc.html — o'zimizning pleyer: ilova ichida, o'z boshqaruv paneli bilan. */
@OptIn(UnstableApi::class)
@Composable
internal fun LivePlayerScreen(
    onBack: () -> Unit,
    viewModel: LiveViewModel = hiltViewModel(),
    video: LiveVideoViewModel = hiltViewModel(),
) {
    val channel by viewModel.selected.collectAsStateWithLifecycle()
    val st by video.state.collectAsStateWithLifecycle()
    var ui by remember { mutableStateOf(true) }
    var fullscreen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    LaunchedEffect(channel.id) { if (st.channelId != channel.id) video.open(channel) }
    LaunchedEffect(ui, st.playing) { if (ui && st.playing) { delay(3500); ui = false } }
    // Ilova fonga o'tsa — to'xtatamiz (trafik va batareya).
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e -> if (e == androidx.lifecycle.Lifecycle.Event.ON_STOP) video.pause() }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }
    DisposableEffect(fullscreen) {
        activity?.requestedOrientation = if (fullscreen) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose { if (fullscreen) activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }
    BackHandler(enabled = fullscreen) { fullscreen = false }

    val openYoutube = {
        val url = if (channel.youtubeChannelId.isNotBlank()) "https://www.youtube.com/channel/${channel.youtubeChannelId}/live"
        else "https://www.youtube.com/results?search_query=" + Uri.encode(channel.searchQuery)
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
        Unit
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF050B0A))) {
        if (!fullscreen) {
            Row(Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DarkBack(onBack)
                Column(Modifier.weight(1f)) {
                    DText(channel.name.localized(), 17.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
                    DText(stringResource(R.string.live_local_time, channel.city.localized(), localTime(channel.id)), 12.sp, FontWeight.Normal, Sub)
                }
            }
        }
        Box(
            (if (fullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(16f / 9f)).background(Color.Black),
        ) {
            if (channel.streams.isNotEmpty()) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            setShutterBackgroundColor(android.graphics.Color.BLACK)
                            keepScreenOn = true
                            player = video.player
                        }
                    },
                    update = { it.player = video.player },
                    onRelease = { it.player = null },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (st.audioOnly || channel.streams.isEmpty() || st.failed) {
                HolyScene(place(channel.id), Modifier.fillMaxSize())
            }
            if (st.audioOnly && !st.failed) Box(Modifier.align(Alignment.Center).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.5f)).padding(horizontal = 12.dp, vertical = 8.dp)) {
                DText(stringResource(R.string.live_audio_only), 13.sp, FontWeight.Bold, Cream)
            }
            if (st.buffering && !st.failed && channel.streams.isNotEmpty()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center).size(42.dp), color = AccentGold, strokeWidth = 3.dp)
            }
            if (st.failed || channel.streams.isEmpty()) {
                Column(
                    Modifier.align(Alignment.Center).clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.6f)).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DText(stringResource(if (channel.streams.isEmpty()) R.string.live_no_stream else R.string.live_stream_failed), 13.sp, FontWeight.Bold, Cream, align = TextAlign.Center)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (channel.streams.isNotEmpty()) {
                            Box(Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(AccentGold).clickable { video.retry() }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                DText(stringResource(R.string.common_retry), 12.sp, FontWeight.ExtraBold, Color(0xFF2B1F06))
                            }
                        }
                        Box(Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(Cream.copy(alpha = 0.15f)).clickable { openYoutube() }.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                            DText(stringResource(R.string.live_open_youtube), 12.sp, FontWeight.ExtraBold, Cream)
                        }
                    }
                }
            }
            if (!st.failed && channel.streams.isNotEmpty()) {
                Box(Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) { ui = !ui })
            }
            if (ui && !st.failed && channel.streams.isNotEmpty()) {
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.55f), 0.35f to Color.Transparent, 0.6f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.7f)))) {
                    LiveBadge(Modifier.align(Alignment.TopStart).padding(12.dp))
                    if (fullscreen) DText(channel.name.localized(), 14.sp, FontWeight.ExtraBold, Cream, Modifier.align(Alignment.TopCenter).padding(top = 14.dp))
                    Box(
                        Modifier.align(Alignment.Center).size(64.dp).clip(CircleShape).background(Cream.copy(alpha = 0.18f)).clickable { video.togglePlay() },
                        contentAlignment = Alignment.Center,
                    ) { Icon(if (st.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
                    Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(start = 6.dp, end = 6.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        OverlayBtn(if (st.muted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp) { video.toggleMute() }
                        DText(stringResource(R.string.live_at_live), 12.sp, FontWeight.Bold, Color.White, Modifier.weight(1f))
                        Box(
                            Modifier.height(28.dp).clip(RoundedCornerShape(14.dp)).background(Cream.copy(alpha = 0.18f)).clickable { video.toggleQuality() }.padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) { DText(stringResource(if (st.lowQuality) R.string.live_quality_low else R.string.live_quality_auto), 11.sp, FontWeight.ExtraBold, Color.White) }
                        OverlayBtn(Icons.Rounded.Refresh) { video.selectSource(st.streamIndex) }
                        OverlayBtn(if (fullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen) { fullscreen = !fullscreen }
                    }
                }
            }
        }
        if (fullscreen) return@Column
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                viewModel.channels.forEach { ch ->
                    val on = ch.id == channel.id
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(if (on) Card else Color.Transparent)
                            .border(2.dp, if (on) AccentGold else Cream.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .clickable { viewModel.select(ch) }.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(Modifier.fillMaxWidth().height(58.dp).clip(RoundedCornerShape(12.dp))) { HolyScene(place(ch.id), Modifier.fillMaxSize()) }
                        DText(ch.city.localized(), 12.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
                    }
                }
            }
            if (channel.streams.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DText(stringResource(R.string.live_source), 13.sp, FontWeight.Bold, Sub, Modifier.weight(1f))
                        Icon(Icons.Rounded.Verified, null, tint = Color(0xFF8FD6BC), modifier = Modifier.size(14.dp))
                        DText(" " + stringResource(R.string.live_official), 12.sp, FontWeight.ExtraBold, Color(0xFF8FD6BC))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        channel.streams.indices.forEach { i ->
                            val on = st.streamIndex == i
                            Box(
                                Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (on) AccentGold else Cream.copy(alpha = 0.08f)).clickable { video.selectSource(i) }.padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                DText("${i + 1} · " + stringResource(if (i == 0) R.string.live_source_main else R.string.live_source_backup), 12.sp, FontWeight.ExtraBold, if (on) Color(0xFF2B1F06) else Cream)
                            }
                        }
                    }
                }
            }
            val shareText = "${channel.name.localized()} — ${stringResource(R.string.live_title)}\n#Namozly"
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionTile(Icons.Outlined.Headphones, stringResource(R.string.live_audio_only), st.audioOnly) { video.toggleAudioOnly() }
                ActionTile(Icons.Outlined.OpenInNew, stringResource(R.string.live_open_youtube), false) { openYoutube() }
                ActionTile(Icons.Outlined.Share, stringResource(R.string.common_share), false) {
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText), null))
                }
            }
            DText(stringResource(R.string.live_tap_hint), 12.sp, FontWeight.Normal, Color(0xFF8FA79E), lineHeight = 18.sp)
        }
    }
}

@Composable
private fun OverlayBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ActionTile(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, on: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(18.dp)).background(if (on) AccentGold.copy(alpha = 0.25f) else Card).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
    ) {
        Icon(icon, null, tint = Cream, modifier = Modifier.size(22.dp))
        DText(label, 12.sp, FontWeight.Bold, Cream, maxLines = 1)
    }
}

