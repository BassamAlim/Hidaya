package bassamalim.hidaya.features.quran.searcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyDropDownMenu
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent

@Composable
fun QuranSearcherScreen(viewModel: QuranSearcherViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(title = stringResource(R.string.quran_searcher)) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val highlightColor = MaterialTheme.colorScheme.primary

            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyText(stringResource(R.string.search_for_quran_text))

                SearchComponent(
                    value = state.searchText,
                    hint = stringResource(R.string.search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    onValueChange = { viewModel.onSearchValueChange(it, highlightColor) }
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MyText(
                        text = stringResource(R.string.max_num_of_marches),
                        fontSize = 18.sp
                    )

                    MyDropDownMenu(
                        selection = state.maxMatches,
                        items = integerArrayResource(R.array.searcher_matches_items).toTypedArray(),
                        entries = stringArrayResource(R.array.searcher_matches_entries),
                        onChoice = viewModel::onMaxMatchesChange
                    )
                }
            }

            if (state.isNoResultsFound) {
                MyText(
                    text = stringResource(R.string.no_matches),
                    modifier = Modifier.padding(top = 100.dp)
                )
            }
            else {
                MyLazyColumn(lazyList = {
                    items(state.matches) { item ->
                        MatchItem(
                            item = item,
                            onGoToPageClick = viewModel::onGotoPageClick
                        )
                    }
                })
            }
        }
    }
}

@Composable
fun MatchItem(
    item: QuranSearcherMatch,
    onGoToPageClick: (QuranSearcherMatch) -> Unit
) {
    MySurface {
        Column(
            Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MyText("${stringResource(R.string.sura)} ${item.suraName}")

                MyText("${stringResource(R.string.page)} ${item.pageNum}")
            }

            MyText(
                text = "${stringResource(R.string.verse_number)} ${item.verseNum}",
                modifier = Modifier.padding(6.dp)
            )
            MyText(
                text = item.text,
                modifier = Modifier.padding(6.dp)
            )
            MyText(
                text = "${stringResource(R.string.interpretation)}: ${item.interpretation}",
                modifier = Modifier.padding(6.dp)
            )

            MyRectangleButton(
                text = stringResource(R.string.go_to_page),
                modifier = Modifier.padding(bottom = 6.dp),
                textColor = MaterialTheme.colorScheme.primary,
                elevation = 0,
                innerPadding = PaddingValues(0.dp),
                onClick = { onGoToPageClick(item) }
            )
        }
    }
}