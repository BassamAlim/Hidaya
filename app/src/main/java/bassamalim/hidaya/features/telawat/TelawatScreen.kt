package bassamalim.hidaya.features.telawat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.models.Reciter
import bassamalim.hidaya.core.ui.components.FilterDialog
import bassamalim.hidaya.core.ui.components.MyDownloadBtn
import bassamalim.hidaya.core.ui.components.MyFavBtn
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun TelawatUI(
    vm: TelawatVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose { vm.onStop() }
    }

    MyScaffold(
        stringResource(R.string.recitations),
        onBack = { vm.onBackPressed() }
    ) {
        Column {
            MySquareButton(
                text = st.continueListeningText,
                fontSize = 18.sp,
                textColor = AppTheme.colors.accent,
                modifier = Modifier.fillMaxWidth(),
                innerPadding = PaddingValues(vertical = 4.dp)
            ) {
                vm.onContinueListeningClick()
            }

            TabLayout(
                pageNames = listOf(
                    stringResource(R.string.all),
                    stringResource(R.string.favorite),
                    stringResource(R.string.downloaded)
                ),
                searchComponent = {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchComponent(
                            value = st.searchText,
                            hint = stringResource(R.string.reciters_hint),
                            modifier = Modifier.weight(1F),
                            onValueChange = { vm.onSearchTextCh(it) }
                        )

                        MyIconButton(
                            iconId = R.drawable.ic_filter,
                            modifier = Modifier.padding(end = 10.dp),
                            description = stringResource(R.string.filter_search_description),
                            size = 32.dp,
                            tint =
                                if (st.isFiltered) AppTheme.colors.secondary
                                else AppTheme.colors.weakText
                        ) {
                            vm.onFilterClk()
                        }
                    }
                }
            ) { page ->
                Tab(vm, st, vm.getItems(page))
            }
        }

        FilterDialog(
            shown = st.filterDialogShown,
            title = stringResource(R.string.choose_rewaya),
            itemTitles = vm.rewayat.toList(),
            itemSelections = st.selectedVersions.toTypedArray(),
            onDismiss = { vm.onFilterDialogDismiss(it) }
        )
    }
}

@Composable
private fun Tab(
    vm: TelawatVM,
    st: TelawatState,
    items: List<Reciter>
) {
    MyLazyColumn(
        lazyList = {
            items(items) { item ->
                ReciterCard(reciter = item, vm, st)
            }
        }
    )
}

@Composable
private fun ReciterCard(
    reciter: Reciter,
    vm: TelawatVM,
    st: TelawatState
) {
    Surface(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 8.dp),
        elevation = 10.dp,
        shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
        color = AppTheme.colors.surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 3.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(reciter.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                MyFavBtn(reciter.fav.value) {
                    reciter.fav.value = (reciter.fav.value + 1) % 2
                    vm.onFavClk(reciter.id, reciter.fav.value)
                }
            }

            MyHorizontalDivider(thickness = 2.dp)

            Column(
                Modifier.fillMaxWidth()
            ) {
                for (version in reciter.versions) {
                    VersionCard(
                        reciterId = reciter.id,
                        version = version,
                        vm = vm,
                        st = st
                    )
                }
            }
        }
    }
}

@Composable
private fun VersionCard(
    reciterId: Int,
    version: Reciter.RecitationVersion,
    vm: TelawatVM,
    st: TelawatState
) {
    if (version.versionId != 0)
        MyHorizontalDivider()

    Box(
        Modifier.clickable {
            vm.onVersionClk(reciterId, version.versionId)
        }
    ) {
        Box(Modifier.padding(start = 10.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MyText(text = version.rewaya, fontSize = 18.sp)

                MyDownloadBtn(
                    state =
                        if (st.downloadStates.isEmpty()) DownloadState.NotDownloaded
                        else st.downloadStates[reciterId][version.versionId],
                    path = "${vm.prefix}$reciterId/${version.versionId}",
                    size = 28.dp,
                    deleted = { vm.onDeleteClk(reciterId, version.versionId) },
                    download = { vm.onDownloadClk(reciterId, version) }
                )
            }
        }
    }
}