package bassamalim.hidaya.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.dialogs.FilterDialog
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.viewmodel.BookSearcherVM

@Composable
fun BookSearcherUI(
    navController: NavController = rememberNavController(),
    viewModel: BookSearcherVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MyScaffold(stringResource(R.string.books_searcher)) { padding ->
        val highlightColor = AppTheme.colors.accent

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    value = viewModel.searchText.value,
                    hint = stringResource(R.string.search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    onSubmit = { viewModel.search(highlightColor) }
                )

                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MyText(stringResource(R.string.selected_books))

                    MyIconBtn(
                        iconId = R.drawable.ic_filter,
                        description = stringResource(R.string.filter_search_description),
                        size = 30.dp,
                        tint =
                            if (state.filtered) AppTheme.colors.secondary
                            else AppTheme.colors.weakText
                    ) {
                        viewModel.onFilterClick()
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MyText(text = stringResource(R.string.max_num_of_marches))

                    MyDropDownMenu(
                        selectedIndex = viewModel.maxMatchesIndex.value,
                        items = viewModel.maxMatchesItems
                    ) { index ->
                        viewModel.onMaxMatchesIndexChange(index)
                    }
                }
            }

            if (state.noResultsFound)
                MyText(
                    text = stringResource(R.string.books_no_matches),
                    modifier = Modifier.padding(top = 100.dp)
                )
            else {
                MyLazyColumn(
                    lazyList = {
                        items(state.matches) { item ->
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
        }

        FilterDialog(
            title = stringResource(R.string.choose_books),
            itemTitles = viewModel.bookTitles,
            itemSelections = viewModel.bookSelections,
            shown = state.filterDialogShown
        ) { selections ->
            viewModel.onFilterDialogDismiss(selections)
        }
    }
}