package bassamalim.hidaya.features.books.bookSearcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.integerArrayResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyDropDownMenu
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun BookSearcherScreen(viewModel: BookSearcherViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose {}
    }

    MyScaffold(stringResource(R.string.books_searcher)) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchArea(
                searchText = state.searchText,
                isFiltered = state.filtered,
                maxMatches = state.maxMatches,
                onSearchTextChange = viewModel::onSearchTextChange,
                onSearch = { color -> viewModel.onSearch(color, state.bookSelections) },
                onFilterClick = viewModel::onFilterClick,
                onMaxMatchesIndexChange = viewModel::onMaxMatchesIndexChange
            )

            state.matches?.let {
                if (state.matches!!.isEmpty()) {
                    MyText(
                        text = stringResource(R.string.books_no_matches),
                        modifier = Modifier.padding(top = 100.dp)
                    )
                }
                else {
                    ResultsList(state.matches!!)
                }
            }
        }
    }
}

@Composable
private fun SearchArea(
    searchText: String,
    isFiltered: Boolean,
    maxMatches: Int,
    onSearchTextChange: (String) -> Unit,
    onSearch: (Color) -> Unit,
    onFilterClick: () -> Unit,
    onMaxMatchesIndexChange: (Int) -> Unit,
) {
    val highlightColor = AppTheme.colors.accent

    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MyText(
            text = stringResource(R.string.search_in_books),
            modifier = Modifier.padding(vertical = 6.dp)
        )

        SearchComponent(
            value = searchText,
            hint = stringResource(R.string.search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp),
            onValueChange = onSearchTextChange,
            onSubmit = { onSearch(highlightColor) }
        )

        BooksFilter(
            isFiltered = isFiltered,
            onFilterClick = onFilterClick
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MyText(stringResource(R.string.max_num_of_marches))

            MyDropDownMenu(
                selection = maxMatches,
                items = integerArrayResource(R.array.searcher_matches_items).toTypedArray(),
                entries = stringArrayResource(R.array.searcher_matches_entries),
                onChoice = onMaxMatchesIndexChange
            )
        }
    }
}

@Composable
private fun BooksFilter(
    isFiltered: Boolean,
    onFilterClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MyText(stringResource(R.string.selected_books))

        MyIconButton(
            iconId = R.drawable.ic_filter,
            description = stringResource(R.string.filter_search_description),
            size = 30.dp,
            tint =
                if (isFiltered) AppTheme.colors.secondary
                else AppTheme.colors.weakText,
            onClick = onFilterClick
        )
    }
}

@Composable
private fun ResultsList(
    matches: List<BookSearcherMatch>
) {
    MyLazyColumn(
        lazyList = {
            items(matches) { item ->
                MySurface {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MyText(
                            text = item.bookTitle,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(6.dp)
                        )

                        MyText(
                            text = item.chapterTitle,
                            modifier = Modifier.padding(6.dp)
                        )

                        MyText(
                            text = item.doorTitle,
                            modifier = Modifier.padding(6.dp)
                        )

                        MyText(
                            text = item.text,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    )
}