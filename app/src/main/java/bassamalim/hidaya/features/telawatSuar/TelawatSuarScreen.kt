package bassamalim.hidaya.features.telawatSuar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyDownloadBtn
import bassamalim.hidaya.core.ui.components.MyFavBtn
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout

@Composable
fun TelawatSuarUI(
    vm: TelawatSuarVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose { vm.onStop() }
    }

    MyScaffold(
        st.title,
        onBack = { vm.onBackPressed() }
    ) {
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite),
                stringResource(R.string.downloaded)
            ),
            searchComponent = {
                SearchComponent(
                    value = st.searchText,
                    hint = stringResource(R.string.suar_hint),
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { vm.onSearchChange(it) }
                )
            }
        ) { page ->
            Tab(vm, st, vm.getItems(page))
        }
    }
}

@Composable
private fun Tab(
    vm: TelawatSuarVM,
    st: TelawatSuarState,
    items: List<ReciterSura>
) {
    MyLazyColumn(
        lazyList = {
            items(items) { item ->
                SuraCard(item, vm, st)
            }
        }
    )
}

@Composable
private fun SuraCard(
    sura: ReciterSura,
    viewModel: TelawatSuarVM,
    state: TelawatSuarState
) {
    MyClickableSurface(
        onClick = { viewModel.onItemClk(sura) }
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
                text = sura.suraName,
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