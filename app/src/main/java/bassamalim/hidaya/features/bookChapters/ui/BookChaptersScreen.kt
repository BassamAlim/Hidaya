package bassamalim.hidaya.features.bookChapters.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyBtnSurface
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout

@Composable
fun BookChaptersUI(
    viewModel: BookChaptersViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(title = state.title) {
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
                    onValueChange = viewModel::onSearchTextChange
                )
            }
        ) { page ->
            Tab(
                chapters = viewModel.getItems(page),
                onItemClick = viewModel::onItemClick,
                onFavClick = viewModel::onFavoriteClick,
            )
        }
    }
}

@Composable
private fun Tab(
    chapters: List<BookChapter>,
    onItemClick: (BookChapter) -> Unit,
    onFavClick: (Int) -> Unit
) {
    MyLazyColumn(
        lazyList = {
            items(chapters) { chapter ->
                MyBtnSurface(
                    text = chapter.title,
                    iconBtn = {
                        MyFavoriteButton(
                            isFavorite = chapter.isFavorite,
                            onClick = { onFavClick(chapter.id) }
                        )
                    },
                    onClick = { onItemClick(chapter) }
                )
            }
        }
    )
}