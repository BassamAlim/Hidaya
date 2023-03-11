package bassamalim.hidaya.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.features.telawatSuar.TelawatSuarState
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.features.telawatSuar.TelawatSuarVM
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TelawatSuarUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: TelawatSuarVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsState()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose { vm.onStop() }
    }

    MyScaffold(
        st.title,
        onBack = { vm.onBackPressed(nc) }
    ) {
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite),
                stringResource(R.string.downloaded)
            ),
            searchComponent = {
                SearchComponent(
                    value = vm.searchText,
                    hint = stringResource(R.string.suar_hint),
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { vm.onSearchChange(it) }
                )
            }
        ) { page, currentPage ->
            vm.onPageChg(page, currentPage)

            Tab(vm, st, nc)
        }
    }
}

@Composable
private fun Tab(
    vm: TelawatSuarVM,
    st: TelawatSuarState,
    nc: NavController
) {
    MyLazyColumn(
        lazyList = {
            items(st.items) { item ->
                SuraCard(item, vm, st, nc)
            }
        }
    )
}

@Composable
private fun SuraCard(
    sura: ReciterSura,
    viewModel: TelawatSuarVM,
    state: TelawatSuarState,
    navController: NavController
) {
    MyClickableSurface(
        onClick = { viewModel.onItemClk(navController, sura) }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp, start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            MyDownloadBtn(
                state = state.downloadStates[sura.num],
                path = "${viewModel.prefix}${sura.num}.mp3",
                size = 28.dp,
                deleted = { viewModel.onDelete(sura.num) }
            ) {
                viewModel.onDownload(sura)
            }

            MyText(
                text = sura.surahName,
                modifier = Modifier
                    .weight(1F)
                    .padding(10.dp)
            )

            MyFavBtn(state.favs[sura.num]) {
                viewModel.onFavClk(sura.num)
            }
        }
    }
}