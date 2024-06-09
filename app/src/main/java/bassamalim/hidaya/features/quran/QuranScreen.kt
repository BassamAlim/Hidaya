package bassamalim.hidaya.features.quran

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyFavBtn
import bassamalim.hidaya.core.ui.components.MyFloatingActionButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.core.ui.components.TutorialDialog

@Composable
fun QuranUI(
    vm: QuranVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose {}
    }

    MyScaffold(
        title = "",
        topBar = {},  // override the default top bar
        fab = {
            MyFloatingActionButton(
                iconId = R.drawable.ic_quran_search,
                description = stringResource(R.string.search_in_quran)
            ) {
                vm.onQuranSearcherClick()
            }
        }
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            MySquareButton(
                text = st.bookmarkedPageText,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth(),
                innerPadding = PaddingValues(vertical = 4.dp)
            ) {
                vm.onBookmarkedPageClick()
            }

            TabLayout(
                pageNames = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.favorite)
                ),
                searchComponent = {
                    SearchComponent(
                        value = st.searchText,
                        hint = stringResource(R.string.quran_query_hint),
                        modifier = Modifier.fillMaxWidth(),
                        onValueChange = { vm.onSearchTextChange(it) },
                        onSubmit = { vm.onSearchSubmit() }
                    )
                }
            ) { page ->
                Tab(vm, st, vm.getItems(page))
            }
        }
    }

    TutorialDialog(
        textResId = R.string.quran_fragment_tips,
        shown = st.tutorialDialogShown
    ) {
        vm.onTutorialDialogDismiss(it)
    }

    if (st.shouldShowPageDNE != 0) {
        LaunchedEffect(st.shouldShowPageDNE) {
                Toast.makeText(
                    ctx,
                    ctx.getString(R.string.page_does_not_exist),
                    Toast.LENGTH_SHORT
                ).show()
        }
    }

}

@Composable
private fun Tab(
    vm: QuranVM,
    st: QuranState,
    items: List<Sura>
) {
    MyLazyColumn(
        lazyList = {
            items(items) { item ->
                MyClickableSurface(
                    modifier = Modifier.padding(2.dp),
                    elevation = 6.dp,
                    onClick = { vm.onSuraClick(item.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            top = 10.dp, bottom = 10.dp, start = 14.dp, end = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                if (item.tanzeel == 0) R.drawable.ic_kaaba
                                else R.drawable.ic_madina
                            ),
                            contentDescription = stringResource(R.string.tanzeel_view_description)
                        )

                        MyText(
                            text = item.suraName,
                            modifier = Modifier
                                .weight(1F)
                                .padding(10.dp)
                        )

                        MyFavBtn(st.favs[item.id]) {
                            vm.onFavClick(item.id)
                        }
                    }
                }
            }
        }
    )
}