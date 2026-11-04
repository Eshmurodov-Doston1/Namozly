package uz.dostonbek.namozly.feature.names

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable
import uz.dostonbek.namozly.core.designsystem.component.NamozlyCard
import uz.dostonbek.namozly.core.designsystem.component.NamozlyTopBar
import uz.dostonbek.namozly.core.designsystem.theme.AccentGold
import uz.dostonbek.namozly.core.designsystem.theme.ArabicTextStyle
import uz.dostonbek.namozly.core.domain.repository.ContentRepository
import uz.dostonbek.namozly.core.model.AllahName
import uz.dostonbek.namozly.core.ui.R
import uz.dostonbek.namozly.core.ui.localized
import javax.inject.Inject

@Serializable data object NamesRoute

fun NavController.navigateToNames() = navigate(NamesRoute)

fun NavGraphBuilder.namesScreen(onBack: () -> Unit) {
    composable<NamesRoute> { NamesScreen(onBack) }
}

@HiltViewModel
class NamesViewModel @Inject constructor(content: ContentRepository) : ViewModel() {
    val names: List<AllahName> = content.allahNames()
}

@Composable
internal fun NamesScreen(onBack: () -> Unit, viewModel: NamesViewModel = hiltViewModel()) {
    var selected by remember { mutableStateOf<AllahName?>(null) }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NamozlyTopBar(stringResource(R.string.names_title), onBack = onBack)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(viewModel.names, key = { it.number }) { n ->
                NamozlyCard(Modifier.fillMaxWidth(), onClick = { selected = n }) {
                    Text(n.number.toString(), style = MaterialTheme.typography.labelSmall, color = AccentGold)
                    Text(
                        n.arabic,
                        style = ArabicTextStyle.copy(fontSize = 28.sp, lineHeight = 44.sp),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(n.name.localized(), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Text(
                        n.meaning.localized(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
    selected?.let { n ->
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = { TextButton(onClick = { selected = null }) { Text(stringResource(R.string.common_close)) } },
            title = { Text("${n.number}. ${n.name.localized()}") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(n.arabic, style = ArabicTextStyle.copy(fontSize = 40.sp, lineHeight = 60.sp), color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text(n.meaning.localized(), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            },
        )
    }
}
