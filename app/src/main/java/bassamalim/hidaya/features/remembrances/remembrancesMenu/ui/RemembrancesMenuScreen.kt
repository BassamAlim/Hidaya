package bassamalim.hidaya.features.remembrances.remembrancesMenu.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.MenuType
import bassamalim.hidaya.core.ui.components.CustomSearchBar
import bassamalim.hidaya.core.ui.components.MyButtonSurface
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold

@Composable
fun RemembrancesMenuScreen(viewModel: RemembrancesMenuViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyScaffold(
        title = when (state.menuType) {
            MenuType.FAVORITES -> stringResource(R.string.favorite_remembrances)
            MenuType.CUSTOM -> state.categoryTitle
            else -> stringResource(R.string.all_remembrances)
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            CustomSearchBar(
                query = state.searchText,
                modifier = Modifier.fillMaxWidth(),
                hint = stringResource(R.string.remembrances_search_hint),
                onQueryChange = viewModel::onSearchTextChange
            )

            RemembrancesList(
                remembrances = state.remembrances,
                onItemClick = viewModel::onItemClick,
                onFavoriteClick = viewModel::onFavoriteCLick
            )
        }
    }
}

@Composable
private fun RemembrancesList(
    remembrances: List<RemembrancesItem>,
    onItemClick: (RemembrancesItem) -> Unit,
    onFavoriteClick: (RemembrancesItem) -> Unit
) {
    MyLazyColumn(
        lazyList = {
            items(remembrances) { remembrance ->
                MyButtonSurface(
                    text = remembrance.name,
                    iconButton = {
                        MyFavoriteButton(
                            isFavorite = remembrance.isFavorite,
                            onClick = { onFavoriteClick(remembrance) }
                        )
                    },
                    onClick = { onItemClick(remembrance) }
                )
            }
        }
    )
}