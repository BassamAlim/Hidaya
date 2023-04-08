package bassamalim.hidaya.features.athkarList

import androidx.compose.animation.ExperimentalAnimationApi
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
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.*
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AthkarListUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: AthkarListVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsState()

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
                            onClick = { vm.onItemClick(nc, item) }
                        )
                    }
                }
            )
        }
    }
}