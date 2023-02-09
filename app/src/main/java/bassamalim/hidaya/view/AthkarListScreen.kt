package bassamalim.hidaya.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.viewmodel.AthkarListVM

@Composable
fun AthkarListUI(
    nc: NavController = rememberNavController(),
    vm: AthkarListVM = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    MyScaffold(state.title) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
        ) {

            SearchComponent (
                value = vm.searchText,
                hint = stringResource(R.string.athkar_hint),
                modifier = Modifier.fillMaxWidth()
            )

            MyLazyColumn(
                lazyList = {
                    items(state.items) { item ->
                        MyBtnSurface(
                            text = item.name,
                            iconBtn = {
                                MyFavBtn(item.favorite.value) {
                                    vm.onFavoriteCLick(item)
                                }
                            }
                        ) {
                            vm.onItemClick(nc, item)
                        }
                    }
                }
            )
        }
    }
}