package bassamalim.hidaya.features.bookChapters.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.features.bookChapters.domain.BookChapter
import bassamalim.hidaya.core.ui.components.MyBtnSurface
import bassamalim.hidaya.core.ui.components.MyFavBtn
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout

@Composable
fun BookChaptersUI(
    viewModel: BookChaptersViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(state.title) {
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite)
            ),
            searchComponent = {
                SearchComponent(
                    value = state.searchText,
                    hint = stringResource(R.string.search),
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { viewModel.onSearchTextChange(it) }
                )
            }
        ) { page ->
            Tab(
                items = viewModel.getItems(page),
                favs = state.favs,
                onItemClick = { viewModel.onItemClick(it) },
                onFavClick = { viewModel.onFavClick(it) },
            )
        }
    }
}

@Composable
private fun Tab(
    items: List<BookChapter>,
    favs: Map<Int, Int>,
    onItemClick: (BookChapter) -> Unit,
    onFavClick: (Int) -> Unit
) {
    MyLazyColumn(
        lazyList = {
            items(items) { item ->
                MyBtnSurface(
                    text = item.title,
                    iconBtn = {
                        MyFavBtn(
                            fav = favs[item.id]!!,
                            onClick = { onFavClick(item.id) }
                        )
                    },
                    onClick = { onItemClick(item) }
                )
            }
        }
    )
}