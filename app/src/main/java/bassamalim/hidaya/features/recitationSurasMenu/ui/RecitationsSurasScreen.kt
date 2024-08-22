package bassamalim.hidaya.features.recitationSurasMenu.ui

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
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyDownloadBtn
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout

@Composable
fun TelawatSurasScreen(
    viewModel: RecitationsSurasViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose(viewModel::onStop)
    }

    MyScaffold(
        title = state.title,
        onBack = viewModel::onBackPressed
    ) {
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite),
                stringResource(R.string.downloaded)
            ),
            searchComponent = {
                SearchComponent(
                    value = state.searchText,
                    hint = stringResource(R.string.suar_hint),
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = viewModel::onSearchChange
                )
            }
        ) { page ->
            Tab(viewModel, state, viewModel.getItems(page))
        }
    }
}

@Composable
private fun Tab(
    vm: RecitationsSurasViewModel,
    st: RecitationsSurasUiState,
    items: List<ReciterSura>
) {
    MyLazyColumn(
        lazyList = {
            items(items) { item ->
                SuraCard(item, vm, st)
            }
        }
    )
}

@Composable
private fun SuraCard(
    sura: ReciterSura,
    onClick: (ReciterSura) -> Unit,
    onFavoriteClick: (Int) -> Unit,
) {
    MyClickableSurface(
        onClick = { onClick(sura) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MyDownloadBtn(
                state =
                    if (st.downloadStates.isEmpty()) DownloadState.NOT_DOWNLOADED
                    else st.downloadStates[sura.num],
                path = "${vm.prefix}${sura.num}.mp3",
                size = 28.dp,
                deleted = { vm.onDelete(sura.num) },
                download = { vm.onDownload(sura) }
            )

            MyText(
                text = sura.suraName,
                modifier = Modifier
                    .weight(1F)
                    .padding(10.dp)
            )

            MyFavoriteButton(
                isFavorite = sura.isFavorite,
                onClick = { onFavoriteClick(sura.) }
            )
        }
    }
}