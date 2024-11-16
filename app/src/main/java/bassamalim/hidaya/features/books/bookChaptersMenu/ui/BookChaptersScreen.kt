package bassamalim.hidaya.features.books.bookChaptersMenu.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.Book
import bassamalim.hidaya.core.ui.components.MyButtonSurface
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout
import kotlinx.coroutines.flow.Flow

@Composable
fun BookChaptersScreen(viewModel: BookChaptersViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyScaffold(title = state.title) { padding ->
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite)
            ),
            modifier = Modifier.padding(padding),
            searchComponent = {
                SearchComponent(
                    value = state.searchText,
                    modifier = Modifier.fillMaxWidth(),
                    hint = stringResource(R.string.search),
                    onValueChange = viewModel::onSearchTextChange
                )
            }
        ) { page ->
            Tab(
                chaptersFlow = viewModel.getItems(page),
                onItemClick = viewModel::onItemClick,
                onFavClick = viewModel::onFavoriteClick
            )
        }
    }
}

@Composable
private fun Tab(
    chaptersFlow: Flow<List<Book.Chapter>>,
    onItemClick: (Book.Chapter) -> Unit,
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