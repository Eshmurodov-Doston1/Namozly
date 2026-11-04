package uz.dostonbek.namozly.feature.duas

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.core.designsystem.component.NamozlyCard
import uz.dostonbek.namozly.core.designsystem.component.NamozlyTopBar
import uz.dostonbek.namozly.core.designsystem.component.Pill
import uz.dostonbek.namozly.core.designsystem.theme.ArabicTextStyle
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.model.Dua
import uz.dostonbek.namozly.core.model.DuaCategory
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.localized
import javax.inject.Inject

@Serializable data object DuasRoute

fun NavController.navigateToDuas() = navigate(DuasRoute)

fun NavGraphBuilder.duasScreen(onBack: () -> Unit) {
    composable<DuasRoute> { DuasScreen(onBack) }
}

@HiltViewModel
class DuasViewModel @Inject constructor(content: ContentRepository) : ViewModel() {
    val duas: List<Dua> = content.duas()
}

@Composable
internal fun DuasScreen(onBack: () -> Unit, viewModel: DuasViewModel = hiltViewModel()) {
    var category by rememberSaveable { mutableStateOf<DuaCategory?>(null) }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    val categories = remember(viewModel.duas) { viewModel.duas.map { it.category }.distinct() }
    val list = viewModel.duas.filter { category == null || it.category == category }
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NamozlyTopBar(stringResource(R.string.duas_title), onBack = onBack)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Pill(stringResource(R.string.common_all), selected = category == null, onClick = { category = null }) }
            items(categories) { c -> Pill(stringResource(c.labelRes()), selected = category == c, onClick = { category = c }) }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list, key = { it.id }) { dua ->
                val open = expanded == dua.id
                NamozlyCard(Modifier.fillMaxWidth(), onClick = { expanded = if (open) null else dua.id }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(dua.title.localized(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Icon(if (open) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        dua.arabic,
                        style = ArabicTextStyle.copy(textDirection = TextDirection.Rtl),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    AnimatedVisibility(open) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            Text(dua.transliteration, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic)
                            Spacer(Modifier.height(6.dp))
                            Text(dua.translation.localized(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            dua.source?.let {
                                Spacer(Modifier.height(6.dp))
                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                val translation = dua.translation.localized()
                                IconButton(onClick = {
                                    val text = "${dua.arabic}\n\n${dua.transliteration}\n\n$translation"
                                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null))
                                }) { Icon(Icons.Rounded.Share, stringResource(R.string.common_share)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DuaCategory.labelRes(): Int = when (this) {
    DuaCategory.MORNING_EVENING -> R.string.dua_cat_morning
    DuaCategory.PRAYER -> R.string.dua_cat_prayer
    DuaCategory.RAMADAN -> R.string.ramadan_title
    DuaCategory.FOOD -> R.string.dua_cat_food
    DuaCategory.HOME -> R.string.dua_cat_home
    DuaCategory.MOSQUE -> R.string.dua_cat_mosque
    DuaCategory.SLEEP -> R.string.dua_cat_sleep
    DuaCategory.TRAVEL -> R.string.dua_cat_travel
    DuaCategory.GENERAL -> R.string.dua_cat_general
}
