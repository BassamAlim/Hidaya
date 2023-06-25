package bassamalim.hidaya.features.athkarList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyBtnSurface
import bassamalim.hidaya.core.ui.components.MyFavBtn
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SearchComponent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination(navArgsDelegate = AthkarListNavArgs::class)
@Composable
fun AthkarListUI(
    vm: AthkarListVM = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(st.title) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            SearchComponent (
                value = vm.searchText,
                hint = stringResource(R.string.athkar_hint),
                modifier = Modifier.fillMaxWidth(),
                onValueChange = { vm.onSearchChange(it) }
            )

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
                            onClick = { vm.onItemClick(navigator, item) }
                        )
                    }
                }
            )
        }
    }
}