package uz.dostonbek.namozly.feature.books

import android.content.Intent
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.core.designsystem.component.DBackButton
import uz.dostonbek.namozly.core.designsystem.component.DIconButton
import uz.dostonbek.namozly.core.designsystem.component.DPill
import uz.dostonbek.namozly.core.designsystem.component.DProgress
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.component.EmptyState
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.AmiriFamily
import uz.dostonbek.namozly.core.designsystem.theme.JakartaFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.domain.repository.BookRepository
import uz.dostonbek.namozly.core.domain.repository.SettingsRepository
import uz.dostonbek.namozly.core.model.AppLanguage
import uz.dostonbek.namozly.core.model.Book
import uz.dostonbek.namozly.core.model.BookCategory
import uz.dostonbek.namozly.core.model.BookPage
import uz.dostonbek.namozly.core.model.ReadingPosition
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.localized
import javax.inject.Inject

@Serializable data object BooksRoute
@Serializable data class BookReaderRoute(val bookId: String, val number: Int = 1)

fun NavController.navigateToBooks() = navigate(BooksRoute)
fun NavController.navigateToBookReader(bookId: String, number: Int) = navigate(BookReaderRoute(bookId, number))

fun NavGraphBuilder.booksScreens(onBack: () -> Unit, onOpen: (String, Int) -> Unit) {
    composable<BooksRoute> { BooksScreen(onBack, onOpen) }
    composable<BookReaderRoute> { BookReaderScreen(onBack) }
}

private val Cream = Color(0xFFF6F1E6)
private val Mint = Color(0xFFCFE3DA)

data class BooksUiState(
    val books: List<Book> = emptyList(),
    val query: String = "",
    val category: BookCategory? = null,
    val last: Pair<Book, Int>? = null,
    val positions: Map<String, Int> = emptyMap(),
)

@HiltViewModel
class BooksViewModel @Inject constructor(private val repository: BookRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<BookCategory?>(null)
    val uiState: StateFlow<BooksUiState> = combine(query, category, repository.lastRead, repository.positions) { q, c, last, positions ->
        val all = repository.books()
        BooksUiState(
            books = all.filter { b ->
                (c == null || b.category == c) &&
                    (q.isBlank() || listOf(b.title.uz, b.title.ru, b.title.en, b.author.uz, b.author.en).any { it.contains(q.trim(), true) })
            },
            query = q,
            category = c,
            last = last?.let { p: ReadingPosition -> all.firstOrNull { it.id == p.bookId }?.let { it to p.number } },
            positions = positions,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BooksUiState(repository.books()))

    fun onQuery(v: String) { query.value = v }
    fun onCategory(c: BookCategory?) { category.value = c }
}

/** Dizayn: Kitoblar.dc.html */
@Composable
internal fun BooksScreen(onBack: () -> Unit, onOpen: (String, Int) -> Unit, viewModel: BooksViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = listOf(
        null to R.string.common_all, BookCategory.HADITH to R.string.books_cat_hadith, BookCategory.FIQH to R.string.books_cat_fiqh,
        BookCategory.AQIDA to R.string.books_cat_aqida,
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize().background(t.bg),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item(span = { GridItemSpan(3) }) {
            Row(Modifier.statusBarsPadding().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DBackButton(onBack)
                DText(stringResource(R.string.books_title), 22.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
            }
        }
        item(span = { GridItemSpan(3) }) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(24.dp)).background(t.surface).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.Search, null, tint = t.muted, modifier = Modifier.size(18.dp))
                Box(Modifier.weight(1f)) {
                    if (state.query.isEmpty()) DText(stringResource(R.string.books_search_hint), 15.sp, FontWeight.Normal, t.faint)
                    BasicTextField(state.query, viewModel::onQuery, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontFamily = JakartaFamily, fontSize = 15.sp, color = t.text), cursorBrush = SolidColor(t.primary))
                }
            }
        }
        state.last?.let { (book, n) ->
            item(span = { GridItemSpan(3) }) {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF0E5A4A)).clickable { onOpen(book.id, n) }.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    BookCover(book, Modifier.width(64.dp).height(96.dp), showTitle = false)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        DText(stringResource(R.string.books_continue_reading), 12.sp, FontWeight.SemiBold, Mint)
                        DText(book.title.localized(), 17.sp, FontWeight.ExtraBold, Cream, maxLines = 1)
                        DText(stringResource(R.string.books_hadith_n, n), 12.sp, FontWeight.Normal, Color(0xFFE7DDC7))
                        DProgress(n.toFloat() / book.total, Cream.copy(alpha = 0.2f), AccentGold, Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
        item(span = { GridItemSpan(3) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { (c, label) ->
                    val on = state.category == c
                    Box(
                        Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (on) (if (t.isDark) t.primary else t.strong) else t.surface)
                            .clickable { viewModel.onCategory(c) }.padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) { DText(stringResource(label), 13.sp, if (on) FontWeight.Bold else FontWeight.SemiBold, if (on) Color.White else t.text) }
                }
            }
        }
        item(span = { GridItemSpan(3) }) {
            Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                DText(stringResource(R.string.books_popular), 17.sp, FontWeight.ExtraBold, t.strong, Modifier.weight(1f))
                DText(stringResource(R.string.books_count, state.books.size), 13.sp, FontWeight.SemiBold, t.muted)
            }
        }
        items(state.books, key = { it.id }) { book ->
            Column(Modifier.clickable { onOpen(book.id, state.positions[book.id] ?: 1) }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BookCover(book, Modifier.fillMaxWidth().aspectRatio(2f / 3f), showTitle = true)
                DText(book.title.localized(), 13.sp, FontWeight.Bold, t.text, maxLines = 2, lineHeight = 16.sp)
                DText(book.author.localized(), 11.sp, FontWeight.Normal, t.muted, maxLines = 1)
                state.positions[book.id]?.let { n ->
                    DProgress(n.toFloat() / book.total, if (t.isDark) t.line else Color(0xFFEFE8D9), t.primary, height = 3.dp)
                }
            }
        }
        item(span = { GridItemSpan(3) }) {
            DText(stringResource(R.string.books_source_note), 11.sp, FontWeight.Normal, t.muted, lineHeight = 16.sp)
        }
    }
}

/** Muqova: rangli fon + oltin islomiy naqsh + nom. */
@Composable
private fun BookCover(book: Book, modifier: Modifier, showTitle: Boolean) {
    val color = Color(book.coverColor)
    Box(modifier.shadow(6.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(color)) {
        Canvas(Modifier.fillMaxSize()) {
            val gold = Color(0xFFD9A94F)
            val c = Offset(size.width / 2, size.height * 0.36f)
            val half = size.width * 0.24f
            drawRect(gold.copy(alpha = 0.55f), c - Offset(half, half), Size(half * 2, half * 2), style = Stroke(1.5.dp.toPx()))
            rotate(45f, c) { drawRect(gold.copy(alpha = 0.55f), c - Offset(half, half), Size(half * 2, half * 2), style = Stroke(1.5.dp.toPx())) }
            drawCircle(gold.copy(alpha = 0.55f), half * 0.55f, c, style = Stroke(1.5.dp.toPx()))
            drawRect(Color.Black.copy(alpha = 0.18f), Offset(0f, 0f), Size(4.dp.toPx(), size.height))
            drawRect(gold.copy(alpha = 0.8f), Offset(10.dp.toPx(), size.height - 6.dp.toPx()), Size(size.width - 20.dp.toPx(), 1.5.dp.toPx()))
        }
        if (showTitle) {
            DText(book.title.localized(), 12.sp, FontWeight.ExtraBold, Cream, Modifier.align(Alignment.BottomStart).padding(start = 12.dp, end = 8.dp, bottom = 14.dp), lineHeight = 14.sp)
        }
    }
}

// ───────────────────────── O'qish ─────────────────────────

sealed interface PageState {
    data object Loading : PageState
    data class Ready(val page: BookPage) : PageState
    data object Failed : PageState
}

data class ReaderUiState(
    val book: Book? = null,
    /** Ochilish sahifasi (kitobda qolgan joy). null — hali aniqlanmoqda. */
    val startPage: Int? = null,
    val language: AppLanguage = AppLanguage.UZ_LATN,
    val pages: Map<Int, PageState> = emptyMap(),
    val scale: Float = 1f,
    val paper: String = "SEPIA",
    val flip: String = "CURL",
    val bookmarks: Set<Int> = emptySet(),
    val saved: Int = 0,
    val download: Pair<Int, Int>? = null,
    val downloadFailed: Boolean = false,
)

@HiltViewModel
class BookReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BookRepository,
    settings: SettingsRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<BookReaderRoute>()
    private val book = repository.books().firstOrNull { it.id == route.bookId }
    private val start = MutableStateFlow<Int?>(null)
    private val language = MutableStateFlow<AppLanguage?>(null)
    private val pages = MutableStateFlow<Map<Int, PageState>>(emptyMap())
    private val saved = MutableStateFlow(0)
    private val download = MutableStateFlow<Pair<Int, Int>?>(null)
    private val downloadFailed = MutableStateFlow(false)
    private var downloadJob: Job? = null

    val uiState: StateFlow<ReaderUiState> = combine(
        combine(start, language, pages, saved) { a, b, c, d -> arrayOf<Any?>(a, b, c, d) },
        combine(repository.readerScale, repository.readerPaper, repository.readerFlip) { a, b, c -> Triple(a, b, c) },
        repository.bookmarks,
        combine(download, downloadFailed) { a, b -> a to b },
    ) { a, (scale, paper, flip), marks, (dl, failed) ->
        @Suppress("UNCHECKED_CAST")
        ReaderUiState(
            book, a[0] as Int?, (a[1] as AppLanguage?) ?: AppLanguage.UZ_LATN, a[2] as Map<Int, PageState>,
            scale, paper, flip, book?.let { marks[it.id] }.orEmpty(), a[3] as Int, dl, failed,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReaderUiState(book))

    init {
        viewModelScope.launch {
            val b = book ?: return@launch
            language.value = settings.settings.first().language
            // Ro'yxatdan aniq sahifa kelmasa — kitobda qolgan joydan davom etamiz.
            start.value = (if (route.number > 1) route.number else repository.positions.first()[b.id] ?: 1).coerceIn(1, b.total)
            refreshSaved()
        }
    }

    /** Sahifa ko'rinishga kelganda chaqiriladi: yuklaydi va qo'shni sahifalarni oldindan tayyorlaydi. */
    fun onPageVisible(n: Int) {
        val b = book ?: return
        viewModelScope.launch { repository.setLastRead(b.id, n) }
        request(n)
        request(n + 1)
        request(n - 1)
    }

    fun request(n: Int, force: Boolean = false) {
        val b = book ?: return
        val lang = language.value ?: return
        if (n !in 1..b.total) return
        val cur = pages.value[n]
        if (!force && (cur is PageState.Ready || cur is PageState.Loading)) return
        pages.update { it + (n to PageState.Loading) }
        viewModelScope.launch {
            val r = repository.page(b, n, lang)
            if (language.value != lang) return@launch
            pages.update { it + (n to (r.getOrNull()?.let { PageState.Ready(it) } ?: PageState.Failed)) }
        }
    }

    fun setLanguage(l: AppLanguage) {
        if (language.value == l) return
        language.value = l
        pages.value = emptyMap()
        refreshSaved()
    }

    fun toggleBookmark(n: Int) { book?.let { b -> viewModelScope.launch { repository.toggleBookmark(b.id, n) } } }
    fun setScale(v: Float) = viewModelScope.launch { repository.setReaderScale(v) }
    fun setPaper(v: String) = viewModelScope.launch { repository.setReaderPaper(v) }
    fun setFlip(v: String) = viewModelScope.launch { repository.setReaderFlip(v) }

    /** Butun kitobni qurilmaga yuklash (internetsiz o'qish uchun). */
    fun toggleDownload() {
        val b = book ?: return
        val lang = language.value ?: return
        if (downloadJob?.isActive == true) { downloadJob?.cancel(); download.value = null; refreshSaved(); return }
        downloadFailed.value = false
        download.value = 0 to b.total
        downloadJob = viewModelScope.launch {
            val r = repository.download(b, lang) { done, total -> download.value = done to total }
            downloadFailed.value = r.isFailure
            download.value = null
            refreshSaved()
        }
    }

    private fun refreshSaved() {
        val b = book ?: return
        val lang = language.value ?: return
        viewModelScope.launch { saved.value = repository.savedCount(b, lang) }
    }
}

private data class Paper(val bg: Color, val text: Color, val title: Color, val muted: Color, val line: Color, val card: Color)

private fun paper(mode: String) = when (mode) {
    "WHITE" -> Paper(Color(0xFFFFFFFF), Color(0xFF1C2420), Color(0xFF0A3D33), Color(0xFF5E625C), Color(0xFFE2DACA), Color(0xFFF6F1E6))
    "NIGHT" -> Paper(Color(0xFF13221E), Color(0xFFECE7DC), Color(0xFFF6F1E6), Color(0xFFA3ADA7), Color(0xFF2A3D37), Color(0xFF1C2E2A))
    else -> Paper(Color(0xFFF4ECDC), Color(0xFF2A2418), Color(0xFF0A3D33), Color(0xFF6B5E45), Color(0xFFD9C9A3), Color(0xFFFFFFFF))
}

/** Dizayn: KitobOqish.dc.html — kitobdek varaqlash yoki tugma bilan o'tish. */
@Composable
internal fun BookReaderScreen(onBack: () -> Unit, viewModel: BookReaderViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val book = state.book ?: return
    val startPage = state.startPage
    val p = paper(state.paper)
    if (startPage == null) {
        Box(Modifier.fillMaxSize().background(p.bg), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = t.primary) }
        return
    }
    val pager = rememberPagerState(initialPage = startPage - 1) { book.total }
    val scope = rememberCoroutineScope()
    var panel by remember { mutableStateOf(false) }
    var jump by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val current = pager.currentPage + 1
    val curl = state.flip == "CURL"

    LaunchedEffect(pager, state.language) {
        snapshotFlow { pager.currentPage }.collect { viewModel.onPageVisible(it + 1) }
    }
    val go: (Int) -> Unit = { delta ->
        val target = (pager.currentPage + delta).coerceIn(0, book.total - 1)
        scope.launch { pager.animateScrollToPage(target, animationSpec = tween(if (curl) 520 else 300)) }
    }

    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().background(p.bg)) {
        Row(
            Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DBackButton(onBack)
            Column(Modifier.weight(1f)) {
                DText(book.title.localized(), 15.sp, FontWeight.ExtraBold, p.title, maxLines = 1)
                val section = (state.pages[current] as? PageState.Ready)?.page?.section
                DText(section ?: book.author.localized(), 12.sp, FontWeight.Normal, p.muted, maxLines = 1)
            }
            val marked = current in state.bookmarks
            DIconButton(
                if (marked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, { viewModel.toggleBookmark(current) },
                tint = if (marked) Color(0xFF8A6420) else null, contentDescription = stringResource(R.string.books_bookmark),
            )
            DIconButton(Icons.Rounded.TextFields, { panel = !panel })
        }
        HorizontalPager(
            state = pager,
            modifier = Modifier.weight(1f),
            userScrollEnabled = curl,
            beyondViewportPageCount = 1,
            key = { it },
        ) { index ->
            val n = index + 1
            val offset = (pager.currentPage - index) + pager.currentPageOffsetFraction
            Box(
                Modifier.fillMaxSize()
                    .then(if (curl) Modifier.pageTurn(offset) else Modifier)
                    .background(p.bg),
            ) {
                when (val ps = state.pages[n]) {
                    is PageState.Ready -> PageContent(ps.page, state.scale, p)
                    PageState.Failed -> EmptyState(
                        Icons.Rounded.CloudOff, stringResource(R.string.books_offline_page),
                        action = stringResource(R.string.common_retry), onAction = { viewModel.request(n, force = true) },
                    )
                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = t.primary)
                        LaunchedEffect(n, state.language) { viewModel.request(n) }
                    }
                }
            }
        }
        // Pastki boshqaruv: oldingi / sahifa / keyingi
        Row(
            Modifier.fillMaxWidth().background(if (state.paper == "NIGHT") Color(0xFF1C2E2A) else Color.White.copy(alpha = 0.92f))
                .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ReaderButton(Icons.AutoMirrored.Rounded.ArrowBack, false, enabled = current > 1) { go(-1) }
            Column(
                Modifier.clip(RoundedCornerShape(12.dp)).clickable { jump = true }.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DText("$current / ${book.total}", 14.sp, FontWeight.ExtraBold, p.title)
                DText(stringResource(if (curl) R.string.books_swipe_hint else R.string.books_go_to_page), 11.sp, FontWeight.Normal, p.muted)
            }
            ReaderButton(Icons.AutoMirrored.Rounded.ArrowForward, true, enabled = current < book.total) { go(1) }
        }
    }
        // Sozlamalar paneli: tashqariga bosish, pastga surish, ✕ yoki "Orqaga" bilan yopiladi.
        BackHandler(enabled = panel) { panel = false }
        AnimatedVisibility(panel, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)).clickable(remember { MutableInteractionSource() }, indication = null) { panel = false })
        }
        AnimatedVisibility(
            panel, modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it }, exit = slideOutVertically { it },
        ) {
            var drag by remember { mutableStateOf(0f) }
            Column(
                Modifier.fillMaxWidth().heightIn(max = 620.dp)
                    .graphicsLayer { translationY = drag.coerceAtLeast(0f) }
                    .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(if (state.paper == "NIGHT") Color(0xFF1C2E2A) else Color.White)
                    .navigationBarsPadding().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Yuqori tutqich: pastga suring — panel yopiladi.
                Box(
                    Modifier.fillMaxWidth().height(36.dp).draggable(
                        rememberDraggableState { d -> drag = (drag + d).coerceAtLeast(0f) },
                        Orientation.Vertical,
                        onDragStopped = { if (drag > 120f) panel = false; drag = 0f },
                    ),
                ) {
                    Box(Modifier.align(Alignment.TopCenter).padding(top = 2.dp).width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFD6CDB8)))
                    Box(
                        Modifier.align(Alignment.TopEnd).size(36.dp).clip(CircleShape).background(p.card).clickable { panel = false },
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Rounded.Close, stringResource(R.string.common_close), tint = p.title, modifier = Modifier.size(20.dp)) }
                }
                // Tarjima tili
                DText(stringResource(R.string.books_translation_lang), 12.sp, FontWeight.Bold, p.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { l ->
                        Choice(l.nativeName, state.language == l, p, Modifier.weight(1f)) { viewModel.setLanguage(l) }
                    }
                }
                // Varaqlash usuli
                DText(stringResource(R.string.books_page_mode), 12.sp, FontWeight.Bold, p.muted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Choice(stringResource(R.string.books_mode_curl), curl, p, Modifier.weight(1f)) { viewModel.setFlip("CURL") }
                    Choice(stringResource(R.string.books_mode_buttons), !curl, p, Modifier.weight(1f)) { viewModel.setFlip("BUTTONS") }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DText("Aa", 13.sp, FontWeight.Bold, p.muted)
                    Slider(
                        value = state.scale, onValueChange = { viewModel.setScale(it) }, valueRange = 0.8f..1.6f, modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color(0xFF0E5A4A), inactiveTrackColor = Color(0xFFEFE8D9)),
                    )
                    DText("Aa", 20.sp, FontWeight.Bold, p.muted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("WHITE" to R.string.books_paper_white, "SEPIA" to R.string.books_paper_sepia, "NIGHT" to R.string.books_paper_night).forEach { (mode, label) ->
                        val pp = paper(mode)
                        val on = state.paper == mode
                        Box(
                            Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(14.dp)).background(pp.bg)
                                .border(2.dp, if (on) Color(0xFF0E5A4A) else Color(0xFFE2DACA), RoundedCornerShape(14.dp)).clickable { viewModel.setPaper(mode) },
                            contentAlignment = Alignment.Center,
                        ) { DText(stringResource(label), 13.sp, if (on) FontWeight.ExtraBold else FontWeight.Bold, pp.text) }
                    }
                }
                // Xatcho'plar
                if (state.bookmarks.isNotEmpty()) {
                    DText(stringResource(R.string.books_bookmarks), 12.sp, FontWeight.Bold, p.muted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.bookmarks.sorted()) { n ->
                            Choice(stringResource(R.string.books_hadith_n, n), n == current, p) {
                                scope.launch { pager.scrollToPage(n - 1) }
                            }
                        }
                    }
                }
                // Oflayn
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f)) {
                        DText(stringResource(R.string.books_offline_title), 14.sp, FontWeight.ExtraBold, p.title)
                        val dl = state.download
                        DText(
                            when {
                                dl != null -> stringResource(R.string.books_downloading, dl.first, dl.second)
                                state.downloadFailed -> stringResource(R.string.books_download_failed)
                                else -> stringResource(R.string.books_saved_n, state.saved, book.total)
                            },
                            12.sp, FontWeight.Normal, p.muted,
                        )
                        dl?.let { DProgress(it.first.toFloat() / it.second.coerceAtLeast(1), Color(0xFFEFE8D9), Color(0xFF0E5A4A), Modifier.padding(top = 6.dp)) }
                    }
                    Box(
                        Modifier.height(40.dp).clip(RoundedCornerShape(20.dp)).background(if (state.download != null) Color(0xFFE3EEE8) else Color(0xFF0E5A4A))
                            .clickable { viewModel.toggleDownload() }.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        DText(
                            stringResource(if (state.download != null) R.string.common_cancel else R.string.books_download),
                            13.sp, FontWeight.ExtraBold, if (state.download != null) Color(0xFF0A3D33) else Color.White,
                        )
                    }
                }
                if (state.language == AppLanguage.UZ_LATN || state.language == AppLanguage.UZ_CYRL) {
                    DText(stringResource(R.string.books_uz_download_note), 11.sp, FontWeight.Normal, p.muted, lineHeight = 15.sp)
                }
                val bookTitle = book.title.localized()
                Row(
                    Modifier.clip(RoundedCornerShape(12.dp)).clickable {
                        (state.pages[current] as? PageState.Ready)?.page?.let { pg ->
                            val text = "${pg.arabic}\n\n${pg.translation}\n\n— $bookTitle, ${pg.number}"
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
                        }
                    }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.Share, null, tint = p.title, modifier = Modifier.size(18.dp))
                    DText(stringResource(R.string.common_share), 14.sp, FontWeight.Bold, p.title)
                }
            }
        }
    }
    if (jump) JumpDialog(book.total, current, onDismiss = { jump = false }) { n ->
        jump = false
        scope.launch { pager.scrollToPage(n - 1) }
    }
}

/**
 * Kitob varag'i effekti: o'ngdagi (keyingi) varaq chap qirra — kitob tikilgan joy — atrofida 3D aylanib
 * joriy sahifa ustiga yopiladi; ortga varaqlaganda xuddi shu varaq o'ngga ochilib, ostidagi sahifa ko'rinadi.
 * Joriy sahifa joyida qoladi va asta soyalanadi. (Pager keyingi sahifani ustida chizadi — shunga mos.)
 */
private fun Modifier.pageTurn(offset: Float): Modifier = graphicsLayer {
    val o = offset.coerceIn(-1f, 1f)
    translationX = o * size.width
    if (o < 0f) {
        cameraDistance = 16f * density
        transformOrigin = TransformOrigin(0f, 0.5f)
        rotationY = 90f * -o
        alpha = if (o < -0.96f) 0f else 1f
        shadowElevation = 24f * (1f + o)
    } else if (o > 0f) {
        alpha = 1f - 0.35f * o
    }
}

@Composable
private fun PageContent(page: BookPage, scale: Float, p: Paper) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f).height(1.dp).background(p.line))
            DText(stringResource(R.string.books_hadith_n, page.number).uppercase(), 12.sp, FontWeight.ExtraBold, Color(0xFF8A6420), spacing = 0.1.em)
            Box(Modifier.weight(1f).height(1.dp).background(p.line))
        }
        if (page.arabic.isNotBlank()) {
            Text(
                page.arabic, Modifier.fillMaxWidth(), fontFamily = AmiriFamily, fontSize = (26 * scale).sp, lineHeight = (46 * scale).sp,
                color = p.title, textAlign = TextAlign.Center, style = TextStyle(textDirection = TextDirection.Rtl),
            )
        }
        Text(page.translation, fontFamily = FontFamily.Serif, fontSize = (18 * scale).sp, lineHeight = (30 * scale).sp, color = p.text)
        if (page.machineTranslated) {
            DText(stringResource(R.string.books_machine_note), 11.sp, FontWeight.Normal, p.muted, lineHeight = 15.sp)
        }
    }
}

@Composable
private fun Choice(text: String, on: Boolean, p: Paper, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(38.dp).clip(RoundedCornerShape(12.dp)).background(if (on) Color(0xFF0E5A4A) else p.card)
            .border(1.dp, if (on) Color(0xFF0E5A4A) else p.line, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) { DText(text, 12.sp, if (on) FontWeight.ExtraBold else FontWeight.Bold, if (on) Color.White else p.text, maxLines = 1) }
}

@Composable
private fun JumpDialog(total: Int, current: Int, onDismiss: () -> Unit, onGo: (Int) -> Unit) {
    var value by remember { mutableStateOf(current.toString()) }
    val n = value.toIntOrNull()?.takeIf { it in 1..total }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.books_go_to_page)) },
        text = {
            OutlinedTextField(
                value, { v -> value = v.filter(Char::isDigit).take(5) }, singleLine = true,
                label = { Text("1 – $total") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = { TextButton(onClick = { n?.let(onGo) }, enabled = n != null) { Text(stringResource(R.string.common_ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    )
}

@Composable
private fun ReaderButton(icon: androidx.compose.ui.graphics.vector.ImageVector, primary: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(CircleShape).background(if (primary) Color(0xFF0E5A4A) else Color(0xFFE3EEE8))
            .graphicsLayer { alpha = if (enabled) 1f else 0.4f }.clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = if (primary) Color.White else Color(0xFF0A3D33), modifier = Modifier.size(20.dp)) }
}
