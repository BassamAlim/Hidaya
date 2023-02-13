package bassamalim.hidaya.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.state.BookChaptersState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.viewmodel.BookChaptersVM
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BookChaptersUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: BookChaptersVM = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    MyScaffold(state.title) {
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite)
            ),
            searchComponent = {
                SearchComponent(
                    value = vm.searchText,
                    hint = stringResource(R.string.search),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    vm.onSearchTextChange(it)
                }
            }
        ) { page, currentPage ->
            vm.onListTypeChange(page, currentPage)

            Tab(vm, state, nc)
        }
    }
}

@Composable
private fun Tab(
    viewModel: BookChaptersVM,
    state: BookChaptersState,
    navController: NavController
) {
    MyLazyColumn(
        lazyList = {
            items(state.items) { item ->
                MyBtnSurface(
                    text = item.title,
                    iconBtn = {
                        MyFavBtn(state.favs[item.id]) {
                            viewModel.onFavClick(item.id)
                        }
                    }
                ) {
                    viewModel.onItemClick(item, navController)
                }
            }
        }
    )
}