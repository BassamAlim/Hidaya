package bassamalim.hidaya.features.books

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.core.utils.FileUtils
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun BooksUI(
    vm: BooksVM = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    DisposableEffect(key1 = vm) {  // Like activity callbacks. Provides onStart, onStop, etc.
        vm.onStart()
        onDispose {  /* here we call onStop if needed */  }
    }

    MyScaffold(
        title = stringResource(R.string.hadeeth_books),
        fab = {
            MyFloatingActionButton(
                iconId = R.drawable.ic_quran_search,
                description = stringResource(R.string.search_in_books),
                onClick = { vm.onFabClick(navigator) }
            )
        }
    ) {
        MyLazyColumn(
            Modifier.padding(vertical = 5.dp),
            lazyList = {
                items(st.items) { item ->
                    MyBtnSurface(
                        text = item.title,
                        innerVPadding = 15.dp,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(vertical = 2.dp),
                        iconBtn = {
                            MyDownloadBtn(
                                state =
                                    if (st.downloadStates.isEmpty()) DownloadState.NotDownloaded
                                    else st.downloadStates[item.id],
                                path = vm.getPath(item.id),
                                modifier = Modifier.padding(end = 10.dp),
                                size = 32.dp,
                                download = { vm.onDownloadClk(item) },
                                deleted = { vm.onFileDeleted(item.id) }
                            )
                        },
                        onClick = { vm.onItemClick(item, navigator) }
                    )
                }
            }
        )

        TutorialDialog(
            textResId = R.string.books_activity_tips,
            shown = st.tutorialDialogShown,
            onDismiss = { vm.onTutorialDialogDismiss(it) }
        )

        if (st.shouldShowWait != 0) {
            LaunchedEffect(st.shouldShowWait) {
                FileUtils.showWaitMassage(ctx)
            }
        }
    }
}