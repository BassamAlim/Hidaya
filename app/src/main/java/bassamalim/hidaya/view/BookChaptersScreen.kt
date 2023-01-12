package bassamalim.hidaya.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.state.BookChaptersState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.viewmodel.BookChaptersVM

@Composable
fun BookChaptersUI(
    navController: NavController = rememberNavController(),
    viewModel: BookChaptersVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MyScaffold(state.title) {
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite)
            ),
            searchComponent = {
                SearchComponent(
                    value = viewModel.searchText,
                    hint = stringResource(R.string.search),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        ) { pageNum ->
            viewModel.onListTypeChange(pageNum)

            Tab(viewModel, state, navController)
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
                        MyFavBtn(viewModel.favs[item.id]) {
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