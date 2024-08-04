package bassamalim.hidaya.features.supplicationsMenu

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
import bassamalim.hidaya.core.ui.components.MyBtnSurface
import bassamalim.hidaya.core.ui.components.MyFavBtn
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SearchComponent

@Composable
fun AthkarListScreen(
    vm: SupplicationsMenuViewModel
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = when (st.listType) {
            ListType.FAVORITES -> stringResource(R.string.favorite_athkar)
            ListType.CUSTOM -> vm.getName()
            else -> stringResource(R.string.all_athkar)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            SearchBar(vm, st)

            AthkarList(vm, st)
        }
    }
}

@Composable
private fun SearchBar(
    vm: SupplicationsMenuViewModel,
    st: SupplicationsMenuState
) {
    SearchComponent (
        value = st.searchText,
        hint = stringResource(R.string.athkar_hint),
        modifier = Modifier.fillMaxWidth(),
        onValueChange = { vm.onSearchChange(it) }
    )
}

@Composable
private fun AthkarList(
    vm: SupplicationsMenuViewModel,
    st: SupplicationsMenuState
) {
    MyLazyColumn(
        lazyList = {
            items(st.items) { item ->
                MyBtnSurface(
                    text = item.name,
                    iconBtn = {
                        MyFavBtn(item.favorite.value) {
                            vm.onFavoriteCLick(item)
                        }
                    },
                    onClick = { vm.onItemClick(item) }
                )
            }
        }
    )
}