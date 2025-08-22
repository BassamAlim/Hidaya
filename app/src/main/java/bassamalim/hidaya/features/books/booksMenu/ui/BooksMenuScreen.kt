package bassamalim.hidaya.features.books.booksMenu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.ui.components.LoadingScreen
import bassamalim.hidaya.core.ui.components.MyCircularProgressIndicator
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyFloatingActionButton
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.utils.FileUtils

@Composable
fun BooksMenuScreen(viewModel: BooksMenuViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }

    if (state.isLoading) return LoadingScreen()

    MyScaffold(
        title = stringResource(R.string.hadeeth_books),
        floatingActionButton = {
            val noDownloadedBooksMessage = stringResource(R.string.no_downloaded_books)
            MyFloatingActionButton(
                iconId = R.drawable.ic_quran_search,
//                imageVector = Icons.Default.FindInPage,
                description = stringResource(R.string.search_in_books),
                onClick = {
                    viewModel.onSearcherClick(
                        snackBarHostState = snackBarHostState,
                        message = noDownloadedBooksMessage
                    )
                }
            )
        },
        snackBarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { padding ->
        // books list
        MyLazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(vertical = 5.dp),
            lazyList = {
                items(state.books.toList()) { (id, book) ->
                    BookCard(
                        id = id,
                        book = book,
                        onItemClick = viewModel::onItemClick,
                        onDownloadButtonClick = viewModel::onDownloadButtonClick
                    )
                }
            }
        )

        TutorialDialog(
            shown = state.tutorialDialogShown,
            text = stringResource(R.string.books_menu_tips),
            onDismissRequest = viewModel::onTutorialDialogDismiss
        )

        if (state.shouldShowWait != 0)
            WaitMessage(state.shouldShowWait)
    }
}

@Composable
private fun BookCard(
    id: Int,
    book: Book,
    onItemClick: (Int, Book) -> Unit,
    onDownloadButtonClick: (Int, Book) -> Unit,
) {
    MyClickableSurface(
        modifier = Modifier.padding(2.dp),
        onClick = { onItemClick(id, book) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 10.dp, start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(
                text = book.title,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 20.dp),
                textAlign = TextAlign.Start
            )

            DownloadBtn(
                downloadState = book.downloadState,
                onClick = { onDownloadButtonClick(id, book) }
            )
        }
    }
}

@Composable
private fun DownloadBtn(downloadState: DownloadState, onClick: () -> Unit) {
    Box(
        modifier = Modifier.padding(end = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (downloadState == DownloadState.DOWNLOADING)
            MyCircularProgressIndicator(Modifier.size(32.dp))
        else {
            MyIconButton(
                imageVector =
                    if (downloadState == DownloadState.DOWNLOADED) Icons.Default.DownloadDone
                    else Icons.Default.Download,
                description = stringResource(R.string.download_description),
                iconModifier = Modifier.size(32.dp),
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun WaitMessage(shouldShow: Int) {
    val context = LocalContext.current
    LaunchedEffect(shouldShow) {
        FileUtils.showWaitMassage(context)
    }
}