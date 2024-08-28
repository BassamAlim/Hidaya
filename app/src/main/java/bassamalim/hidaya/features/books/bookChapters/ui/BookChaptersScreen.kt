package bassamalim.hidaya.features.books.bookChapters.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyButtonSurface
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout
import kotlinx.coroutines.flow.Flow

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
                chaptersFlow = viewModel.getItems(page),
                onItemClick = viewModel::onItemClick,
                onFavClick = viewModel::onFavoriteClick,
            )
        }
    }
}

@Composable
private fun Tab(
    chaptersFlow: Flow<List<BookChapter>>,
    onItemClick: (BookChapter) -> Unit,
    onFavClick: (Int) -> Unit
) {
    val chapters by chaptersFlow.collectAsStateWithLifecycle(emptyList())

    MyLazyColumn(
        lazyList = {
            items(chapters) { chapter ->
                MyButtonSurface(
                    text = chapter.title,
                    iconButton = {
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