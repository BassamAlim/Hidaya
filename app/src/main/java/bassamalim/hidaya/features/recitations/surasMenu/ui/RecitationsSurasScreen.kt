package bassamalim.hidaya.features.recitations.surasMenu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyDownloadButton
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout
import kotlinx.coroutines.flow.Flow

@Composable
fun RecitationSurasMenuScreen(viewModel: RecitationsSurasViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose(viewModel::onStop)
    }

    MyScaffold(
        title = state.title,
        onBack = viewModel::onBackPressed
    ) { padding ->
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite),
                stringResource(R.string.downloaded)
            ),
            modifier = Modifier.padding(padding),
            searchComponent = {
                SearchComponent(
                    value = state.searchText,
                    hint = stringResource(R.string.suras_search_hint),
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = viewModel::onSearchChange
                )
            }
        ) { page ->
            Tab(
                surasFlow = viewModel.getItems(page),
                onSuraClick = viewModel::onSuraClick,
                onFavoriteClick = viewModel::onFavoriteClick,
                onDownloadClick = viewModel::onDownloadClick
            )
        }
    }
}

@Composable
private fun Tab(
    surasFlow: Flow<List<ReciterSura>>,
    onSuraClick: (Int) -> Unit,
    onFavoriteClick: (ReciterSura) -> Unit,
    onDownloadClick: (ReciterSura) -> Unit
) {
    val suras by surasFlow.collectAsStateWithLifecycle(emptyList())

    MyLazyColumn(
        lazyList = {
            items(suras) { sura ->
                SuraCard(
                    sura = sura,
                    onClick = onSuraClick,
                    onFavoriteClick = onFavoriteClick,
                    onDownloadClick = onDownloadClick
                )
            }
        }
    )
}

@Composable
private fun SuraCard(
    sura: ReciterSura,
    onClick: (Int) -> Unit,
    onFavoriteClick: (ReciterSura) -> Unit,
    onDownloadClick: (ReciterSura) -> Unit
) {
    MyClickableSurface(
        onClick = { onClick(sura.id) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MyDownloadButton(
                state = sura.downloadState,
                size = 28.dp,
                onClick = { onDownloadClick(sura) }
            )

            MyText(
                text = sura.suraName,
                modifier = Modifier
                    .weight(1F)
                    .padding(10.dp)
            )

            MyFavoriteButton(
                isFavorite = sura.isFavorite,
                onClick = { onFavoriteClick(sura) }
            )
        }
    }
}