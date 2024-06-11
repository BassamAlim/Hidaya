package bassamalim.hidaya.features.books

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.dbs.BooksDB
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.ui.components.MyBtnSurface
import bassamalim.hidaya.core.ui.components.MyCircularProgressIndicator
import bassamalim.hidaya.core.ui.components.MyFloatingActionButton
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.utils.FileUtils

@Composable
fun BooksUI(
    vm: BooksVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = vm) {
        vm.onStart()
        onDispose {}
    }

    MyScaffold(
        title = stringResource(R.string.hadeeth_books),
        fab = {
            MyFloatingActionButton(
                iconId = R.drawable.ic_quran_search,
                description = stringResource(R.string.search_in_books),
                onClick = { vm.onFabClick() }
            )
        }
    ) {
        // books list
        MyLazyColumn(
            Modifier.padding(vertical = 5.dp),
            lazyList = {
                items(st.items) { item ->
                    BookCard(vm, st, item)
                }
            }
        )

        // tutorial dialog
        TutorialDialog(
            textResId = R.string.books_activity_tips,
            shown = st.tutorialDialogShown,
            onDismiss = { vm.onTutorialDialogDismiss(it) }
        )

        if (st.shouldShowWait != 0) {
            WaitMessage(st)
        }
    }
}

@Composable
private fun BookCard(
    vm: BooksVM,
    st: BooksState,
    item: BooksDB
) {
    MyBtnSurface(
        text = item.title,
        innerVPadding = 15.dp,
        fontSize = 22.sp,
        modifier = Modifier.padding(vertical = 2.dp),
        iconBtn = {
            DownloadBtn(
                state =
                    if (st.downloadStates.isEmpty()) DownloadState.NotDownloaded
                    else st.downloadStates[item.id],
                onClick = { vm.onDownloadButtonClick(item) }
            )
        },
        onClick = { vm.onItemClick(item) }
    )
}

@Composable
fun DownloadBtn(
    state: DownloadState,
    onClick: () -> Unit,
) {
    Box(
        Modifier.padding(end = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (state == DownloadState.Downloading)
            MyCircularProgressIndicator(Modifier.size(32.dp))
        else {
            MyIconButton(
                iconId =
                    if (state == DownloadState.Downloaded) R.drawable.ic_downloaded
                    else R.drawable.ic_download,
                description = stringResource(R.string.download_description),
                size = 32.dp,
                innerPadding = 6.dp,
                tint = AppTheme.colors.accent,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun WaitMessage(
    st: BooksState
) {
    val ctx = LocalContext.current
    LaunchedEffect(st.shouldShowWait) {
        FileUtils.showWaitMassage(ctx)
    }
}