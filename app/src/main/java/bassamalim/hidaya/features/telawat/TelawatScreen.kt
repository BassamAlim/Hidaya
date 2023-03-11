package bassamalim.hidaya.view

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.models.Reciter
import bassamalim.hidaya.features.telawat.TelawatState
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.features.telawat.TelawatVM
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TelawatUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: TelawatVM = hiltViewModel()
) {
    val st by vm.uiState.collectAsState()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose { vm.onStop() }
    }

    MyScaffold(
        stringResource(R.string.recitations),
        onBack = { vm.onBackPressed(nc) }
    ) {
        Column {
            MyButton(
                text = st.continueListeningText,
                fontSize = 18.sp,
                textColor = AppTheme.colors.accent,
                modifier = Modifier.fillMaxWidth(),
                innerPadding = PaddingValues(vertical = 4.dp)
            ) {
                vm.onContinueListeningClick(nc)
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
                            value = vm.searchText,
                            hint = stringResource(R.string.reciters_hint),
                            modifier = Modifier.weight(1F),
                            onValueChange = { vm.onSearchTextCh(it) }
                        )

                        MyIconBtn(
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
            ) { page, currentPage ->
                vm.onPageChg(page, currentPage)

                Tab(vm, st, nc)
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
    nc: NavController
) {
    MyLazyColumn(
        lazyList = {
            items(
                items = st.items.filter { item ->
                    item.name.contains(vm.searchText, ignoreCase = true)
                }
            ) { item ->
                ReciterCard(reciter = item, vm, st, nc)
            }
        }
    )
}

@Composable
private fun ReciterCard(
    reciter: Reciter,
    vm: TelawatVM,
    st: TelawatState,
    nc: NavController
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

                MyFavBtn(st.favs[reciter.id]) {
                    vm.onFavClk(reciter.id)
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
                        st = st,
                        nc = nc
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
    st: TelawatState,
    nc: NavController
) {
    if (version.versionId != 0) MyHorizontalDivider()

    Box(
        Modifier.clickable {
            vm.onVersionClk(reciterId, version.versionId, nc)
        }
    ) {
        Box(Modifier.padding(horizontal = 10.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp),
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
                    deleted = { vm.onDeleted(reciterId, version.versionId) }
                ) {
                    vm.onDownloadClk(reciterId, version)
                }
            }
        }
    }
}