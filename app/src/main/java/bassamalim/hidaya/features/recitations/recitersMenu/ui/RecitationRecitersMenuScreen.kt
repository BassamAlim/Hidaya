package bassamalim.hidaya.features.recitations.recitersMenu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyDownloadButton
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.features.recitations.recitersMenu.domain.Recitation
import kotlinx.coroutines.flow.Flow

@Composable
fun RecitationRecitersMenuScreen(viewModel: RecitationRecitersMenuViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose(viewModel::onStop)
    }

    MyScaffold(
        title = stringResource(R.string.recitations),
        onBack = viewModel::onBackPressed
    ) { padding ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            MyRectangleButton(
                text =
                    if (state.lastPlayedMedia != null) {
                        "${stringResource(R.string.last_play)}: " +
                                "${stringResource(R.string.sura)} ${state.lastPlayedMedia!!.suraName} " +
                                stringResource(R.string.for_reciter) +
                                " ${state.lastPlayedMedia!!.reciterName}" +
                                stringResource(R.string.in_narration_of) +
                                " ${state.lastPlayedMedia!!.narrationName}"
                    }
                    else stringResource(R.string.no_last_play),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                innerPadding = PaddingValues(vertical = 4.dp),
                onClick = viewModel::onContinueListeningClick
            )

            TabLayout(
                pageNames = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.favorite),
                    stringResource(R.string.downloaded)
                ),
                searchComponent = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchComponent(
                            value = viewModel.searchText,
                            hint = stringResource(R.string.reciters_search_hint),
                            modifier = Modifier.weight(1F),
                            onValueChange = viewModel::onSearchTextChange
                        )

                        MyIconButton(
                            iconId = R.drawable.ic_filter,
                            modifier = Modifier.padding(end = 10.dp),
                            description = stringResource(R.string.filter_search_description),
                            size = 32.dp,
                            tint =
                                if (state.isFiltered) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.outline,
                            onClick = viewModel::onFilterClick
                        )
                    }
                }
            ) { page ->
                Tab(
                    itemsFlow = viewModel.getItems(page),
                    onFavoriteClick = viewModel::onFavoriteClick,
                    onNarrationClick = viewModel::onNarrationClick,
                    onDownloadNarrationClick = viewModel::onDownloadNarrationClick
                )
            }
        }
    }
}

@Composable
private fun Tab(
    itemsFlow: Flow<List<Recitation>>,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadNarrationClick: (Int, Recitation.Narration, String) -> Unit
) {
    val items by itemsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    MyLazyColumn(
        lazyList = {
            items(items) { item ->
                ReciterCard(
                    reciter = item,
                    onFavoriteClick = onFavoriteClick,
                    onNarrationClick = onNarrationClick,
                    onDownloadNarrationClick = onDownloadNarrationClick
                )
            }
        }
    )
}

@Composable
private fun ReciterCard(
    reciter: Recitation,
    onFavoriteClick: (Int, Boolean) -> Unit,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadNarrationClick: (Int, Recitation.Narration, String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 3.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(
                    text = reciter.reciterName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                MyFavoriteButton(
                    isFavorite = reciter.isFavoriteReciter,
                    onClick = { onFavoriteClick(reciter.reciterId, reciter.isFavoriteReciter) }
                )
            }

            MyHorizontalDivider(thickness = 2.dp)

            Column(
                Modifier.fillMaxWidth()
            ) {
                reciter.narrations.forEachIndexed { idx, narration ->
                    NarrationsCard(
                        idx = idx,
                        reciterId = reciter.reciterId,
                        narration = narration,
                        onNarrationClick = onNarrationClick,
                        onDownloadClick = onDownloadNarrationClick
                    )
                }
            }
        }
    }
}

@Composable
private fun NarrationsCard(
    idx: Int,
    reciterId: Int,
    narration: Recitation.Narration,
    onNarrationClick: (Int, Int) -> Unit,
    onDownloadClick: (Int, Recitation.Narration, String) -> Unit
) {
    val suraString = stringResource(R.string.sura)

    if (idx != 0)
        MyHorizontalDivider()

    Box(
        Modifier.clickable {
            onNarrationClick(reciterId, narration.id)
        }
    ) {
        Box(
            Modifier.padding(start = 10.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(
                    text = narration.name,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1F)
                )

                MyDownloadButton(
                    state = narration.downloadState,
                    size = 28.dp,
                    onClick = { onDownloadClick(reciterId, narration, suraString) }
                )
            }
        }
    }
}