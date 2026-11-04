package uz.dostonbek.namozly.feature.quran

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import uz.dostonbek.namozly.core.designsystem.component.DBackButton
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DIconButton
import uz.dostonbek.namozly.core.designsystem.component.DProgress
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.DividerSoft
import uz.dostonbek.namozly.core.designsystem.component.EmptyState
import uz.dostonbek.namozly.core.designsystem.component.LoadingBox
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.AmiriFamily
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.Ayah
import uz.dostonbek.namozly.core.model.JuzData
import uz.dostonbek.namozly.core.model.PlaybackState
import uz.dostonbek.namozly.core.model.Revelation
import uz.dostonbek.namozly.core.model.Surah
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.localized

private val Cream = Color(0xFFF6F1E6)
private val Mint = Color(0xFFCFE3DA)

/** Dizayn: Quron.dc.html */
@Composable
internal fun SurahListRoute(onOpenSurah: (Int, Int) -> Unit, viewModel: SurahListViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val listening = playback.surah?.takeIf { playback.ayah == null && playback.streamTitle == null }
    Box(Modifier.fillMaxSize().background(t.bg)) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = if (listening != null) 110.dp else 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 20.dp).height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                DText(stringResource(R.string.quran_title), 24.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
                Box(Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(t.surface).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                    DText(state.language.nativeName, 13.sp, FontWeight.Bold, t.strong)
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(t.surface).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.Search, null, tint = t.muted, modifier = Modifier.size(18.dp))
                Box(Modifier.weight(1f)) {
                    if (state.query.isEmpty()) DText(stringResource(R.string.quran_search_hint), 15.sp, FontWeight.Normal, t.faint)
                    BasicTextField(
                        state.query, viewModel::onQuery, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 15.sp, color = t.text),
                        cursorBrush = SolidColor(t.primary),
                    )
                }
            }
        }
        state.lastRead?.let { (surah, ayah) ->
            item {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF0E5A4A)).padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DText(stringResource(R.string.quran_last_read), 12.sp, FontWeight.SemiBold, Mint)
                        DText(surah.name.localized(), 18.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
                        DText(stringResource(R.string.quran_last_read_meta, ayah, surah.number), 13.sp, FontWeight.Normal, Color(0xFFE7DDC7))
                    }
                    Box(
                        Modifier.height(44.dp).clip(RoundedCornerShape(22.dp)).background(AccentGold).clickable { onOpenSurah(surah.number, ayah) }.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) { DText(stringResource(R.string.common_continue), 13.sp, FontWeight.ExtraBold, Color(0xFF2B1F06)) }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TabPill(stringResource(R.string.quran_tab_surahs), state.tab == QuranTab.SURAHS) { viewModel.onTab(QuranTab.SURAHS) }
                TabPill(stringResource(R.string.quran_tab_juz), state.tab == QuranTab.JUZ) { viewModel.onTab(QuranTab.JUZ) }
                TabPill(stringResource(R.string.quran_tab_saved), state.tab == QuranTab.SAVED) { viewModel.onTab(QuranTab.SAVED) }
            }
        }
        item {
            DCard(padding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), spacing = 0.dp) {
                when (state.tab) {
                    QuranTab.SURAHS -> state.surahs.forEachIndexed { i, s ->
                        if (i > 0) HorizontalDivider(thickness = 1.dp, color = DividerSoft)
                        SurahRow(
                            s.number, s.name.localized(), meta(s), s.arabicName,
                            playing = listening == s.number && playback.isPlaying,
                            loading = listening == s.number && playback.isBuffering,
                            onPlay = { viewModel.toggleSurah(s.number) },
                        ) { onOpenSurah(s.number, 0) }
                    }
                    QuranTab.JUZ -> JuzData.all.forEachIndexed { i, j ->
                        if (i > 0) HorizontalDivider(thickness = 1.dp, color = DividerSoft)
                        val s = viewModel.surah(j.surah)
                        SurahRow(j.number, stringResource(R.string.quran_juz_n, j.number), "${s?.name?.localized().orEmpty()} ${j.surah}:${j.ayah}", s?.arabicName.orEmpty()) {
                            onOpenSurah(j.surah, j.ayah)
                        }
                    }
                    QuranTab.SAVED -> if (state.bookmarks.isEmpty()) {
                        DText(stringResource(R.string.quran_saved_empty), 14.sp, FontWeight.Normal, t.muted, Modifier.padding(vertical = 20.dp))
                    } else state.bookmarks.forEachIndexed { i, (s, a) ->
                        if (i > 0) HorizontalDivider(thickness = 1.dp, color = DividerSoft)
                        SurahRow(s.number, s.name.localized(), stringResource(R.string.quran_ayah_n, a), s.arabicName) { onOpenSurah(s.number, a) }
                    }
                }
            }
        }
    }
    listening?.let { n ->
        viewModel.surah(n)?.let { surah ->
            MiniSurahPlayer(
                surah.name.localized(), surah.number, playback,
                onToggle = { viewModel.toggleSurah(n) }, onPrev = { viewModel.skip(-1) }, onNext = { viewModel.skip(1) },
                onClose = viewModel::stop, onOpen = { onOpenSurah(n, 0) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
    }
}

@Composable
private fun meta(s: Surah): String = stringResource(
    R.string.quran_surah_meta,
    stringResource(if (s.revelation == Revelation.MECCAN) R.string.quran_meccan else R.string.quran_medinan),
    s.ayahCount,
)

@Composable
private fun TabPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    Box(
        Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (selected) t.strong.takeIf { !t.isDark } ?: t.primary else t.surface)
            .clickable(onClick = onClick).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) { DText(text, 13.sp, if (selected) FontWeight.Bold else FontWeight.SemiBold, if (selected) Color.White else t.text) }
}

/** Sakkiz qirrali yulduz ichidagi raqam. */
@Composable
internal fun StarNumber(number: Int, size: androidx.compose.ui.unit.Dp = 38.dp) {
    val t = NamozlyThemeTokens.t
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val side = this.size.minDimension * 0.66f
            val tl = center - androidx.compose.ui.geometry.Offset(side / 2, side / 2)
            val sz = androidx.compose.ui.geometry.Size(side, side)
            drawRect(t.chip, tl, sz)
            rotate(45f) { drawRect(t.chip, tl, sz) }
            drawRect(AccentGold.copy(alpha = 0.6f), tl, sz, style = Stroke(1.2.dp.toPx()))
            rotate(45f) { drawRect(AccentGold.copy(alpha = 0.6f), tl, sz, style = Stroke(1.2.dp.toPx())) }
        }
        DText(number.toString(), 12.sp, FontWeight.ExtraBold, t.strong)
    }
}

@Composable
private fun SurahRow(
    number: Int, name: String, meta: String, arabic: String,
    playing: Boolean = false, loading: Boolean = false, onPlay: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val t = NamozlyThemeTokens.t
    Row(
        Modifier.fillMaxWidth().height(60.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StarNumber(number)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            DText(name, 15.sp, FontWeight.Bold, if (playing || loading) t.primary else t.text, maxLines = 1)
            DText(meta, 12.sp, FontWeight.Medium, t.muted, maxLines = 1)
        }
        Text(arabic, fontFamily = AmiriFamily, fontSize = 20.sp, color = t.icon)
        if (onPlay != null) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(if (playing || loading) t.primary else t.chip).clickable(onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    loading -> CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    else -> Icon(
                        if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        stringResource(if (playing) R.string.common_pause else R.string.common_listen),
                        tint = if (playing) Color.White else t.icon, modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

/** Ro'yxat ostidagi kichik pleyer: butun sura tinglanadi, tugagach keyingisi boshlanadi. */
@Composable
private fun MiniSurahPlayer(
    name: String, number: Int, playback: PlaybackState,
    onToggle: () -> Unit, onPrev: () -> Unit, onNext: () -> Unit, onClose: () -> Unit, onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(Color(0xFF0A3D33))
            .clickable(onClick = onOpen).navigationBarsPadding().padding(start = 18.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val progress = if (playback.durationMs > 0) playback.positionMs.toFloat() / playback.durationMs else 0f
        Box(Modifier.padding(end = 8.dp).fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)).background(Cream.copy(alpha = 0.18f))) {
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(3.dp).background(AccentGold))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(Modifier.weight(1f)) {
                DText(name, 15.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
                DText(
                    "$number · Mishary Alafasy" + if (playback.durationMs > 0) " · ${fmt(playback.positionMs)} / ${fmt(playback.durationMs)}" else "",
                    12.sp, FontWeight.Normal, Mint, maxLines = 1,
                )
            }
            MiniBtn(Icons.Rounded.SkipPrevious, onPrev)
            Box(Modifier.size(44.dp).clip(CircleShape).background(AccentGold).clickable(onClick = onToggle), contentAlignment = Alignment.Center) {
                if (playback.isBuffering) CircularProgressIndicator(Modifier.size(18.dp), color = Color(0xFF2B1F06), strokeWidth = 2.dp)
                else Icon(if (playback.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Color(0xFF2B1F06), modifier = Modifier.size(24.dp))
            }
            MiniBtn(Icons.Rounded.SkipNext, onNext)
            MiniBtn(Icons.Rounded.Close, onClose)
        }
    }
}

@Composable
private fun MiniBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = Cream, modifier = Modifier.size(22.dp))
    }
}


/** Dizayn: Sura.dc.html */
@Composable
internal fun SurahDetailRoute(onBack: () -> Unit, viewModel: SurahDetailViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    val surah = state.surah ?: return
    val listState = rememberLazyListState()
    LaunchedEffect(state.ayahs.isNotEmpty(), state.initialAyah) {
        if (state.ayahs.isNotEmpty() && state.initialAyah > 1) listState.scrollToItem(state.initialAyah.coerceAtMost(state.ayahs.size))
    }
    val active = playback.surah == surah.number

    Column(Modifier.fillMaxSize().background(t.bg)) {
        Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DBackButton(onBack)
                Column(Modifier.weight(1f)) {
                    DText(surah.name.localized(), 20.sp, FontWeight.ExtraBold, t.strong, maxLines = 1)
                    DText(stringResource(R.string.quran_surah_header_meta, surah.number, meta(surah)), 12.sp, FontWeight.Normal, t.muted, maxLines = 1)
                }
                val current = playback.ayah?.takeIf { active } ?: state.ayahs.firstOrNull()?.number ?: 1
                DIconButton(
                    if (current in state.bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    { viewModel.toggleBookmark(current) },
                    contentDescription = stringResource(R.string.quran_bookmark),
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(listOf(AppLanguage.UZ_LATN to "Lotin", AppLanguage.UZ_CYRL to "Кирилл", AppLanguage.RU to "Русский", AppLanguage.EN to "English")) { (lang, label) ->
                    val on = state.language == lang
                    Box(
                        Modifier.height(34.dp).clip(RoundedCornerShape(17.dp)).background(if (on) (if (t.isDark) t.primary else t.strong) else t.surface)
                            .clickable { viewModel.setLanguage(lang) }.padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) { DText(label, 12.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) Color.White else t.text) }
                }
            }
        }
        Box(Modifier.weight(1f)) {
            when {
                state.loading -> LoadingBox()
                state.error -> EmptyState(Icons.Rounded.CloudOff, stringResource(R.string.error_no_internet), stringResource(R.string.quran_download_hint),
                    action = stringResource(R.string.common_retry), onAction = viewModel::retry)
                else -> LazyColumn(state = listState, contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)) {
                    item {
                        if (surah.number != 1 && surah.number != 9) {
                            Text(
                                "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                fontFamily = AmiriFamily, fontSize = 24.sp, color = t.strong, textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            )
                        }
                    }
                    items(state.ayahs, key = { it.number }) { ayah ->
                        AyahItem(
                            surah = surah.number, ayah = ayah, first = ayah.number == state.ayahs.first().number,
                            last = ayah.number == state.ayahs.last().number,
                            playing = active && playback.ayah == ayah.number,
                            onClick = { viewModel.playAyah(ayah) },
                        )
                    }
                }
            }
        }
        PlayerPanel(state, playback, active, viewModel)
    }
}

@Composable
private fun AyahItem(surah: Int, ayah: Ayah, first: Boolean, last: Boolean, playing: Boolean, onClick: () -> Unit) {
    val t = NamozlyThemeTokens.t
    val shape = RoundedCornerShape(
        topStart = if (first || playing) 20.dp else 0.dp, topEnd = if (first || playing) 20.dp else 0.dp,
        bottomStart = if (last || playing) 20.dp else 0.dp, bottomEnd = if (last || playing) 20.dp else 0.dp,
    )
    Column(Modifier.fillMaxWidth().clip(shape).background(t.surface)) {
        if (!first) HorizontalDivider(Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = DividerSoft)
        Column(
            Modifier.padding(horizontal = 4.dp, vertical = if (playing) 4.dp else 0.dp).fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)).background(if (playing) (if (t.isDark) t.chip else Color(0xFFE6EFEA)) else Color.Transparent)
                .clickable(onClick = onClick).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DText("$surah:${ayah.number}", 11.sp, FontWeight.ExtraBold, if (playing) t.icon else t.accent)
                Text(
                    ayah.arabic, Modifier.weight(1f).padding(start = 12.dp),
                    fontFamily = AmiriFamily, fontSize = 24.sp, lineHeight = 38.sp, color = t.strong,
                    style = TextStyle(textDirection = TextDirection.Rtl),
                )
            }
            DText(ayah.translation, 14.sp, if (playing) FontWeight.SemiBold else FontWeight.Normal, if (playing) t.text else t.muted, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun PlayerPanel(state: SurahDetailUiState, playback: PlaybackState, active: Boolean, viewModel: SurahDetailViewModel) {
    val playing = active && playback.isPlaying
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(Color(0xFF0A3D33))
            .navigationBarsPadding().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                DText("Mishary Rashid Alafasy", 14.sp, FontWeight.Bold, Cream)
                DText(
                    if (active && playback.ayah != null) stringResource(R.string.quran_playing_ayah, playback.ayah ?: 0) else stringResource(R.string.quran_tap_to_listen),
                    12.sp, FontWeight.Normal, Mint,
                )
            }
            Box(
                Modifier.height(32.dp).clip(RoundedCornerShape(16.dp)).background(Cream.copy(alpha = 0.08f)).clickable { viewModel.cycleSpeed() }.padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) { DText("${playback.speed}×", 12.sp, FontWeight.Bold, Cream) }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val p = if (active && playback.durationMs > 0) playback.positionMs.toFloat() / playback.durationMs else 0f
            DProgress(p, Cream.copy(alpha = 0.2f), AccentGold, height = 4.dp)
            Row(Modifier.fillMaxWidth()) {
                DText(if (active) fmt(playback.positionMs) else "0:00", 11.sp, FontWeight.Normal, Mint, Modifier.weight(1f))
                DText(if (active) fmt(playback.durationMs) else "0:00", 11.sp, FontWeight.Normal, Mint)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            PanelIcon(if (state.repeat) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat, if (state.repeat) AccentGold else Cream) { viewModel.toggleRepeat() }
            PanelIcon(Icons.Rounded.SkipPrevious, Cream) { viewModel.step(-1) }
            Box(
                Modifier.size(60.dp).clip(CircleShape).background(AccentGold).clickable { viewModel.togglePlay() },
                contentAlignment = Alignment.Center,
            ) {
                if (active && playback.isBuffering) CircularProgressIndicator(Modifier.size(24.dp), color = Color(0xFF2B1F06), strokeWidth = 2.dp)
                else Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Color(0xFF2B1F06), modifier = Modifier.size(28.dp))
            }
            PanelIcon(Icons.Rounded.SkipNext, Cream) { viewModel.step(1) }
            val current = playback.ayah?.takeIf { active } ?: 1
            PanelIcon(if (current in state.bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder, Cream) { viewModel.toggleBookmark(current) }
        }
    }
}

@Composable
private fun PanelIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

private fun fmt(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}
