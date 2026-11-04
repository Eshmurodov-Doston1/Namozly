package uz.dostonbek.namozly.core.media

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import java.io.File
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import uz.dostonbek.namozly.core.domain.repository.QuranPlayer
import uz.dostonbek.namozly.core.model.PlaybackState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mishary Rashid Alafasy qiroati — cdn.islamic.network (bepul CDN).
 * Butun sura: audio-surah/128/ar.alafasy/{n}.mp3
 * Bitta oyat:  audio/128/ar.alafasy/{global}.mp3
 */
@OptIn(UnstableApi::class)
@Singleton
class ExoQuranPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) : QuranPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()
    private var progressJob: Job? = null

    /**
     * Tinglangan qiroatlar qurilmada saqlanadi (1.5 GB gacha, eng eski avval o'chadi) —
     * keyingi safar internetsiz ijro etiladi. Radio (cheksiz oqim) keshlanmaydi.
     */
    private val audioCache: SimpleCache by lazy {
        SimpleCache(File(context.filesDir, "quran_audio"), LeastRecentlyUsedCacheEvictor(1_536L * 1024 * 1024), StandaloneDatabaseProvider(context))
    }
    private val http by lazy { DefaultHttpDataSource.Factory().setUserAgent("Namozly-Android/1.0").setAllowCrossProtocolRedirects(true) }
    private val cachedSources by lazy {
        DefaultMediaSourceFactory(
            CacheDataSource.Factory().setCache(audioCache).setUpstreamDataSourceFactory(DefaultDataSource.Factory(context, http))
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR),
        )
    }
    private val streamSources by lazy { DefaultMediaSourceFactory(DefaultDataSource.Factory(context, http)) }

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            setHandleAudioBecomingNoisy(true)
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.update { it.copy(isPlaying = isPlaying) }
                    if (isPlaying) startProgress() else progressJob?.cancel()
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _state.update {
                        it.copy(
                            isBuffering = playbackState == Player.STATE_BUFFERING,
                            durationMs = duration.coerceAtLeast(0),
                        )
                    }
                    if (playbackState == Player.STATE_ENDED) {
                        _state.update { it.copy(isPlaying = false, positionMs = 0, ended = true) }
                    }
                }
            })
        }
    }

    override fun playSurah(surah: Int) {
        play(
            url = "$CDN/audio-surah/128/$RECITER/$surah.mp3",
            title = "Sura $surah",
            cache = true,
        )
        _state.value = PlaybackState(surah = surah, ayah = null, isBuffering = true, speed = _state.value.speed)
    }

    override fun playAyah(surah: Int, ayah: Int, globalNumber: Int) {
        play(
            url = "$CDN/audio/128/$RECITER/$globalNumber.mp3",
            title = "$surah:$ayah",
            cache = true,
        )
        _state.value = PlaybackState(surah = surah, ayah = ayah, isBuffering = true, speed = _state.value.speed)
    }

    private fun play(url: String, title: String, cache: Boolean = false) {
        val item = MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist("Mishary Rashid Alafasy").build())
            .build()
        player.setMediaSource((if (cache) cachedSources else streamSources).createMediaSource(item))
        player.prepare()
        player.play()
    }

    override fun pause() = player.pause()
    override fun resume() = player.play()

    override fun stop() {
        player.stop()
        player.clearMediaItems()
        _state.value = PlaybackState(speed = _state.value.speed)
    }

    override fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    override fun playStream(url: String, title: String) {
        play(url = url, title = title)
        _state.value = PlaybackState(isBuffering = true, streamTitle = title)
    }

    override fun setSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _state.update { it.copy(speed = speed) }
    }

    private fun startProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                _state.update { it.copy(positionMs = player.currentPosition, durationMs = player.duration.coerceAtLeast(0)) }
                delay(500)
            }
        }
    }

    private companion object {
        const val CDN = "https://cdn.islamic.network/quran"
        const val RECITER = "ar.alafasy"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {
    @Binds
    abstract fun bindQuranPlayer(impl: ExoQuranPlayer): QuranPlayer
}
