package bassamalim.hidaya.features.bookChapters

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.BookChapter
import bassamalim.hidaya.core.ui.components.*
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BookChaptersUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: BookChaptersVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(st.title) {
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite)
            ),
            searchComponent = {
                SearchComponent(
                    value = st.searchText,
                    hint = stringResource(R.string.search),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    vm.onSearchTextChange(it)
                }
            }
        ) { page ->
            Tab(vm, st, nc, vm.getItems(page))
        }
    }
}

@Composable
private fun Tab(
    viewModel: BookChaptersVM,
    state: BookChaptersState,
    navController: NavController,
    items: List<BookChapter>
) {
    MyLazyColumn(
        lazyList = {
            items(items) { item ->
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