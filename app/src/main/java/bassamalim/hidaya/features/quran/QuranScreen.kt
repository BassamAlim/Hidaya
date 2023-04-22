package bassamalim.hidaya.features.quran

import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.core.ui.theme.AppTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuranUI(
    vm: QuranVM,
    nc: NavController = rememberAnimatedNavController()
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose {}
    }

    MyScaffold(
        topBar = {},
        fab = {
            MyFloatingActionButton(
                iconId = R.drawable.ic_quran_search,
                description = stringResource(R.string.search_in_quran)
            ) {
                vm.onQuranSearcherClick(nc)
            }
        }
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            MyButton(
                text = st.bookmarkedPageText,
                fontSize = 18.sp,
                textColor = AppTheme.colors.accent,
                modifier = Modifier.fillMaxWidth(),
                innerPadding = PaddingValues(vertical = 4.dp)
            ) {
                vm.onBookmarkedPageClick(nc)
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
                        onSubmit = { vm.onSearchSubmit(nc) }
                    )
                }
            ) { page ->
                Tab(vm, st, nc, vm.getItems(page))
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
    nc: NavController,
    items: List<Sura>
) {
    MyLazyColumn(
        lazyList = {
            items(items) { item ->
                MyClickableSurface(
                    modifier = Modifier.padding(2.dp),
                    elevation = 6.dp,
                    onClick = { vm.onSuraClick(item.id, nc) }
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