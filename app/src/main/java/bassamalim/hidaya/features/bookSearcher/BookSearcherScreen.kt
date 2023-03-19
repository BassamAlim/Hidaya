package bassamalim.hidaya.features.bookSearcher

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
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun BookSearcherUI(
    vm: BookSearcherVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsState()

    MyScaffold(stringResource(R.string.books_searcher)) { padding ->
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
                val highlightColor = AppTheme.colors.accent

                MyText(
                    text = stringResource(R.string.search_in_books),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                SearchComponent(
                    value = vm.searchText.value,
                    hint = stringResource(R.string.search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    onValueChange = { vm.onSearchTextChange(it) },
                    onSubmit = { vm.search(highlightColor) }
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
                            if (st.filtered) AppTheme.colors.secondary
                            else AppTheme.colors.weakText
                    ) {
                        vm.onFilterClick()
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
                        selectedIndex = vm.maxMatchesItems.indexOf(
                            st.maxMatches.toString()
                        ),
                        items = vm.translatedMaxMatchesItems
                    ) { index ->
                        vm.onMaxMatchesIndexChange(index)
                    }
                }
            }

            if (st.noResultsFound) {
                MyText(
                    text = stringResource(R.string.books_no_matches),
                    modifier = Modifier.padding(top = 100.dp)
                )
            }
            else {
                MyLazyColumn(
                    lazyList = {
                        items(st.matches) { item ->
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
            itemTitles = vm.bookTitles,
            itemSelections = vm.bookSelections,
            shown = st.filterDialogShown
        ) { selections ->
            vm.onFilterDialogDismiss(selections)
        }
    }
}