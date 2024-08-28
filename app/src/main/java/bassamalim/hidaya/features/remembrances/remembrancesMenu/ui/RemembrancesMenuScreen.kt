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
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.ui.components.MyButtonSurface
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.features.remembrances.remembrancesMenu.RemembrancesItem

@Composable
fun RemembrancesListScreen(
    viewModel: RemembrancesMenuViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = when (state.listType) {
            ListType.FAVORITES -> stringResource(R.string.favorite_remembrances)
            ListType.CUSTOM -> state.categoryTitle
            else -> stringResource(R.string.all_remembrances)
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            SearchBar(
                searchText = state.searchText,
                onSearchValueChange = viewModel::onSearchChange
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
private fun SearchBar(
    searchText: String,
    onSearchValueChange: (String) -> Unit
) {
    SearchComponent (
        value = searchText,
        hint = stringResource(R.string.remembrances_search_hint),
        modifier = Modifier.fillMaxWidth(),
        onValueChange = onSearchValueChange
    )
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