package bassamalim.hidaya.features.locationPicker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyTopBar
import bassamalim.hidaya.core.ui.components.SearchComponent

@Composable
fun LocationPickerScreen(viewModel: LocationPickerViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(coroutineScope, lazyListState)
        onDispose {}
    }

    MyScaffold(
        title = "",
        topBar = {
            MyTopBar(
                title = stringResource(
                    when (state.mode) {
                        LocationPickerMode.COUNTRY -> R.string.select_country
                        LocationPickerMode.CITY -> R.string.select_city
                    }
                ),
                onBack = { viewModel.onBack() }
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchComp(
                mode = state.mode,
                searchText = state.searchText,
                onSearchTextChange = viewModel::onSearchTextChange
            )

            LocationItems(
                lazyListState = lazyListState,
                items = state.items,
                onSelect = viewModel::onSelect
            )
        }
    }
}

@Composable
private fun SearchComp(
    mode: LocationPickerMode,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
) {
    SearchComponent(
        value = searchText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        hint = stringResource(
            when (mode) {
                LocationPickerMode.COUNTRY -> R.string.countries_search_hint
                LocationPickerMode.CITY -> R.string.cities_search_hint
            }
        ),
        onValueChange = onSearchTextChange
    )
}

@Composable
private fun LocationItems(
    lazyListState: LazyListState,
    items: List<LocationPickerItem>,
    onSelect: (Int) -> Unit
) {
    MyLazyColumn(
        state = lazyListState,
        lazyList = {
            items(items) { item ->
                MyRectangleButton(
                    text = item.name,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(item.id) }
                )
            }
        }
    )
}