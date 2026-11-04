package uz.dostonbek.namozly.feature.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.outlined.Shower
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.core.designsystem.component.DBackButton
import uz.dostonbek.namozly.core.designsystem.component.DCard
import uz.dostonbek.namozly.core.designsystem.component.DHeader
import uz.dostonbek.namozly.core.designsystem.component.DProgress
import uz.dostonbek.namozly.core.designsystem.component.DText
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.AmiriFamily
import uz.dostonbek.namozly.core.designsystem.theme.NamozlyThemeTokens
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.domain.repository.LessonProgressRepository
import uz.dostonbek.namozly.core.model.Gender
import uz.dostonbek.namozly.core.model.Lesson
import uz.dostonbek.namozly.core.model.LessonIcon
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.localized
import javax.inject.Inject

@Serializable data object LearnRoute
@Serializable data class LessonRoute(val lessonId: String, val step: Int = 0, val female: Boolean = false)

fun NavController.navigateToLearn() = navigate(LearnRoute)

fun NavGraphBuilder.learnScreen(onBack: () -> Unit, onOpenLesson: (LessonRoute) -> Unit = {}) {
    composable<LearnRoute> { LearnScreen(onBack, onOpenLesson) }
    composable<LessonRoute> { LessonScreen(onBack) }
}

fun NavController.navigateToLesson(route: LessonRoute) = navigate(route)

private val Cream = Color(0xFFF6F1E6)
private val Mint = Color(0xFFCFE3DA)

data class LearnUiState(
    val gender: Gender = Gender.MALE,
    val lessons: List<Lesson> = emptyList(),
    val progress: Map<String, Set<Int>> = emptyMap(),
) {
    fun done(l: Lesson) = progress[l.id]?.size ?: 0
    fun completed(l: Lesson) = done(l) >= l.steps.size
    /** Birinchi tugallanmagan dars va undagi birinchi ko'rilmagan qadam. */
    val current: Pair<Lesson, Int>? get() = lessons.firstOrNull { !completed(it) }?.let { l ->
        l to (l.steps.indices.firstOrNull { it !in progress[l.id].orEmpty() } ?: 0)
    }
}

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val content: ContentRepository,
    private val progress: LessonProgressRepository,
) : ViewModel() {
    val uiState: StateFlow<LearnUiState> = combine(progress.gender, progress.observe()) { g, p ->
        LearnUiState(g, content.lessons(g), p)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnUiState(lessons = content.lessons(Gender.MALE)))

    /** Tanlov saqlanadi — keyingi safar ham shu bo'lim ochiladi. */
    fun setGender(g: Gender) { viewModelScope.launch { progress.setGender(g) } }
}

/** Dizayn: NamozOrganish.dc.html */
@Composable
internal fun LearnScreen(onBack: () -> Unit, onOpenLesson: (LessonRoute) -> Unit, viewModel: LearnViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val female = state.gender == Gender.FEMALE
    val completedCount = state.lessons.count { state.completed(it) }

    LazyColumn(Modifier.fillMaxSize().background(t.bg), contentPadding = PaddingValues(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { DHeader(stringResource(R.string.learn_title), onBack) }
        item {
            Row(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (t.isDark) t.chip else Color(0xFFEAE3D3)).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                listOf(Gender.MALE to R.string.learn_for_men, Gender.FEMALE to R.string.learn_for_women).forEach { (g, label) ->
                    val on = state.gender == g
                    Box(
                        Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(12.dp)).background(if (on) t.surface else Color.Transparent).clickable { viewModel.setGender(g) },
                        contentAlignment = Alignment.Center,
                    ) { DText(stringResource(label), 14.sp, if (on) FontWeight.ExtraBold else FontWeight.SemiBold, if (on) t.strong else t.muted) }
                }
            }
        }
        item {
            val current = state.current
            Row(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color(0xFF0E5A4A)).padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                    val total = state.lessons.size.coerceAtLeast(1)
                    Canvas(Modifier.size(96.dp)) {
                        val s = 7.dp.toPx()
                        drawArc(Cream.copy(alpha = 0.18f), 0f, 360f, false, Offset(s / 2, s / 2), Size(size.width - s, size.height - s), style = Stroke(s))
                        drawArc(AccentGold, -90f, 360f * completedCount / total, false, Offset(s / 2, s / 2), Size(size.width - s, size.height - s), style = Stroke(s, cap = StrokeCap.Round))
                    }
                    PostureFigure(current?.let { (l, i) -> l.steps[i].posture } ?: uz.dostonbek.namozly.core.model.Posture.SALAM, Cream, Modifier.size(58.dp), female = female)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DText(stringResource(R.string.learn_your_path), 12.sp, FontWeight.SemiBold, Mint)
                    DText(stringResource(R.string.learn_lessons_progress, completedCount, state.lessons.size), 20.sp, FontWeight.ExtraBold, Cream)
                    DProgress(completedCount.toFloat() / state.lessons.size.coerceAtLeast(1), Cream.copy(alpha = 0.2f), AccentGold)
                    current?.let { (l, i) ->
                        Box(
                            Modifier.padding(top = 4.dp).height(40.dp).clip(RoundedCornerShape(20.dp)).background(AccentGold)
                                .clickable { onOpenLesson(LessonRoute(l.id, i, female)) }.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) { DText(stringResource(R.string.learn_continue_step, l.steps[i].short.localized()), 13.sp, FontWeight.ExtraBold, Color(0xFF2B1F06), maxLines = 1) }
                    }
                }
            }
        }
        itemsIndexed(state.lessons, key = { _, l -> l.id }) { index, l ->
            val done = state.completed(l)
            val isCurrent = state.current?.first?.id == l.id
            Row(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(t.surface)
                    .then(if (isCurrent) Modifier.border(2.dp, t.primary, RoundedCornerShape(22.dp)) else Modifier)
                    .clickable { onOpenLesson(LessonRoute(l.id, if (done) 0 else (l.steps.indices.firstOrNull { it !in state.progress[l.id].orEmpty() } ?: 0), female)) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(
                        when { isCurrent -> AccentGold; done -> t.chip; else -> if (t.isDark) t.chip else Color(0xFFF0EBDF) },
                    ),
                    contentAlignment = Alignment.Center,
                ) { LessonIconView(l.icon, if (isCurrent) Color(0xFF2B1F06) else t.icon) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(if (isCurrent) 4.dp else 2.dp)) {
                    DText(l.title.localized(), 15.sp, FontWeight.ExtraBold, t.text, maxLines = 1)
                    DText(l.subtitle.localized(), 12.sp, FontWeight.Normal, t.muted, maxLines = 1)
                    if (isCurrent) DProgress(state.done(l).toFloat() / l.steps.size, if (t.isDark) t.line else Color(0xFFEFE8D9), t.primary, height = 4.dp)
                }
                if (done) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(t.primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                } else Icon(Icons.Rounded.ChevronRight, null, tint = t.faint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun LessonIconView(icon: LessonIcon, tint: Color) {
    when (icon) {
        LessonIcon.WUDU -> Icon(Icons.Outlined.WaterDrop, null, tint = tint, modifier = Modifier.size(24.dp))
        LessonIcon.GHUSL -> Icon(Icons.Outlined.Shower, null, tint = tint, modifier = Modifier.size(24.dp))
        LessonIcon.TWO -> DText("2", 17.sp, FontWeight.ExtraBold, tint)
        LessonIcon.FOUR -> DText("4", 17.sp, FontWeight.ExtraBold, tint)
        LessonIcon.DUAS -> Icon(Icons.Outlined.AutoStories, null, tint = tint, modifier = Modifier.size(24.dp))
        LessonIcon.SPECIAL -> Icon(Icons.Outlined.Star, null, tint = tint, modifier = Modifier.size(24.dp))
    }
}

@HiltViewModel
class LessonViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    content: ContentRepository,
    private val progress: LessonProgressRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<LessonRoute>()
    val female: Boolean = route.female
    val lesson: Lesson? = content.lessons(if (route.female) Gender.FEMALE else Gender.MALE).firstOrNull { it.id == route.lessonId }
    private val _step = MutableStateFlow(route.step.coerceIn(0, ((lesson?.steps?.size ?: 1) - 1).coerceAtLeast(0)))
    val step: StateFlow<Int> = _step

    init { markViewed() }

    fun go(delta: Int) {
        val l = lesson ?: return
        _step.value = (_step.value + delta).coerceIn(0, l.steps.lastIndex)
        markViewed()
    }
    fun select(i: Int) { _step.value = i; markViewed() }
    private fun markViewed() = lesson?.let { l -> viewModelScope.launch { progress.markViewed(l.id, _step.value) } }
}

/** Dizayn: NamozDars.dc.html */
@Composable
internal fun LessonScreen(onBack: () -> Unit, viewModel: LessonViewModel = hiltViewModel()) {
    val t = NamozlyThemeTokens.t
    val lesson = viewModel.lesson ?: return
    val index by viewModel.step.collectAsStateWithLifecycle()
    val step = lesson.steps[index]
    val strip = rememberLazyListState()
    LaunchedEffect(index) { strip.animateScrollToItem((index - 2).coerceAtLeast(0)) }

    Column(Modifier.fillMaxSize().background(t.bg)) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DHeader(stringResource(R.string.learn_step_of, index + 1, lesson.steps.size), onBack, lesson.title.localized())
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                lesson.steps.indices.forEach { i ->
                    Box(
                        Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(
                            when { i < index -> t.primary; i == index -> AccentGold; else -> if (t.isDark) t.line else Color(0xFFE2DACA) },
                        ),
                    )
                }
            }
            Box(
                Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(230.dp).clip(RoundedCornerShape(28.dp)).background(if (t.isDark) t.primarySoft else Color(0xFFDCEBE3)),
                contentAlignment = Alignment.Center,
            ) {
                PostureFigure(step.posture, t.icon, Modifier.size(200.dp), female = viewModel.female)
                step.rakat?.let {
                    Box(
                        Modifier.align(Alignment.TopEnd).padding(14.dp).clip(RoundedCornerShape(12.dp)).background(t.surface).padding(horizontal = 10.dp, vertical = 6.dp),
                    ) { DText(stringResource(R.string.learn_rakat_n, it), 12.sp, FontWeight.ExtraBold, t.strong) }
                }
            }
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DText(step.title.localized(), 24.sp, FontWeight.ExtraBold, t.strong)
                DText(step.text.localized(), 14.sp, FontWeight.Normal, if (t.isDark) t.text else Color(0xFF3F433D), lineHeight = 22.sp)
            }
            step.recite?.let { r ->
                DCard(Modifier.padding(horizontal = 16.dp), radius = 24.dp) {
                    DText(
                        (if (r.times > 1) stringResource(R.string.learn_say_times, r.times) else stringResource(R.string.learn_say)).uppercase(),
                        12.sp, FontWeight.ExtraBold, t.accent, spacing = 0.06.em,
                    )
                    Text(
                        r.arabic, Modifier.fillMaxWidth(), fontFamily = AmiriFamily, fontSize = 28.sp, lineHeight = 42.sp, color = t.strong,
                        style = TextStyle(textDirection = TextDirection.Rtl),
                    )
                    DText(r.transliteration, 15.sp, FontWeight.Bold, t.text, lineHeight = 21.sp)
                    DText(r.meaning.localized(), 13.sp, FontWeight.Normal, t.muted, lineHeight = 19.sp)
                }
            }
            LazyRow(state = strip, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(lesson.steps) { i, s ->
                    val on = i == index
                    Column(
                        Modifier.width(58.dp).height(64.dp).clip(RoundedCornerShape(16.dp)).background(if (on) t.primary else t.surface).clickable { viewModel.select(i) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    ) {
                        PostureFigure(s.posture, if (on) Color.White else t.icon, Modifier.size(width = 30.dp, height = 34.dp), accent = if (on) AccentGold else t.dash, female = viewModel.female)
                        DText(s.short.localized(), 10.sp, if (on) FontWeight.ExtraBold else FontWeight.Bold, if (on) Color.White else t.muted, maxLines = 1)
                    }
                }
            }
        }
        Row(
            Modifier.navigationBarsPadding().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                Modifier.height(54.dp).clip(RoundedCornerShape(27.dp)).background(t.surface).clickable(enabled = index > 0) { viewModel.go(-1) }.padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) { DText(stringResource(R.string.learn_previous), 15.sp, FontWeight.ExtraBold, t.strong.copy(alpha = if (index > 0) 1f else 0.4f)) }
            val last = index == lesson.steps.lastIndex
            Row(
                Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(27.dp)).background(t.primary)
                    .clickable { if (last) onBack() else viewModel.go(1) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                DText(
                    if (last) stringResource(R.string.learn_finish) else stringResource(R.string.learn_next_step, lesson.steps[index + 1].short.localized()),
                    15.sp, FontWeight.ExtraBold, Color.White, maxLines = 1,
                )
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

