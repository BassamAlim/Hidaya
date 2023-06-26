package bassamalim.hidaya.features.bookChapters

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.BookChapter
import bassamalim.hidaya.core.ui.components.MyBtnSurface
import bassamalim.hidaya.core.ui.components.MyFavBtn
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout

@Composable
fun BookChaptersUI(
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
            Tab(vm, st, vm.getItems(page))
        }
    }
}

@Composable
private fun Tab(
    viewModel: BookChaptersVM,
    state: BookChaptersState,
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
                    viewModel.onItemClick(item)
                }
            }
        }
    )
}