package bassamalim.hidaya.features.recitations.surasMenu

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.ui.components.CustomSearchBar
import bassamalim.hidaya.core.ui.components.MyDownloadButton
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyListItem
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyTopBar
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.core.ui.theme.dimensions
import kotlinx.coroutines.flow.Flow

@Composable
fun RecitationSurasMenuScreen(viewModel: RecitationSurasViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose(viewModel::onStop)
    }

    var isConfirmingDownload by remember { mutableStateOf(false) }
    if (isConfirmingDownload) {
        ConfirmationDialog(
            text = stringResource(R.string.download_all_confirmation),
            confirmText = stringResource(R.string.download_description),
            onConfirm = viewModel::onDownloadAllClick,
            onDismiss = { isConfirmingDownload = false }
        )
    }

    var isConfirmingDelete by remember { mutableStateOf(false) }
    if (isConfirmingDelete) {
        ConfirmationDialog(
            text = stringResource(R.string.delete_downloads_confirmation),
            confirmText = stringResource(R.string.delete),
            onConfirm = viewModel::onDeleteAllClick,
            onDismiss = { isConfirmingDelete = false }
        )
    }

    MyScaffold(
        title = state.title,
        topBar = {
            MyTopBar(
                title = state.title,
                onBack = viewModel::onBackPressed,
                actions = {
                    if (state.downloadStates.containsValue(DownloadState.DOWNLOADED)) {
                        MyIconButton(
                            imageVector = Icons.Default.DeleteSweep,
                            description = stringResource(R.string.delete_downloads),
                            onClick = { isConfirmingDelete = true }
                        )
                    }

                    MyIconButton(
                        imageVector = Icons.Default.DownloadForOffline,
                        description = stringResource(R.string.download_all),
                        onClick = { isConfirmingDownload = true }
                    )
                }
            )
        }
    ) { padding ->
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorites),
                stringResource(R.string.downloaded)
            ),
            modifier = Modifier.padding(padding),
            searchComponent = {
                CustomSearchBar(
                    query = state.searchText,
                    hint = stringResource(R.string.suras_search_hint),
                    modifier = Modifier.fillMaxWidth(),
                    onQueryChange = viewModel::onSearchChange
                )
            }
        ) { page ->
            Tab(
                surasFlow = viewModel.getItems(page),
                downloadStates = state.downloadStates,
                onSuraClick = viewModel::onSuraClick,
                onFavoriteClick = viewModel::onFavoriteClick,
                onDownloadClick = viewModel::onDownloadClick
            )
        }
    }
}

@Composable
private fun ConfirmationDialog(
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onConfirm()
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun Tab(
    surasFlow: Flow<List<ReciterSura>>,
    downloadStates: Map<Int, DownloadState>,
    onSuraClick: (Int) -> Unit,
    onFavoriteClick: (ReciterSura) -> Unit,
    onDownloadClick: (ReciterSura) -> Unit
) {
    val suras by surasFlow.collectAsStateWithLifecycle(emptyList())

    MyLazyColumn(
        lazyList = {
            items(suras, key = { it.id }) { sura ->
                SuraRow(
                    sura = sura,
                    downloadState = downloadStates[sura.id] ?: DownloadState.NOT_DOWNLOADED,
                    onClick = onSuraClick,
                    onFavoriteClick = onFavoriteClick,
                    onDownloadClick = onDownloadClick
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.dimensions.spaceLg),
                    thickness = MaterialTheme.dimensions.dividerThickness,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    )
}

@Composable
private fun SuraRow(
    sura: ReciterSura,
    downloadState: DownloadState,
    onClick: (Int) -> Unit,
    onFavoriteClick: (ReciterSura) -> Unit,
    onDownloadClick: (ReciterSura) -> Unit
) {
    MyListItem(
        headline = sura.suraName,
        onClick = { onClick(sura.id) },
        trailing = {
            Row {
                MyFavoriteButton(
                    isFavorite = sura.isFavorite,
                    onClick = { onFavoriteClick(sura) },
                    size = MaterialTheme.dimensions.iconMd
                )

                MyDownloadButton(
                    state = downloadState,
                    iconSize = MaterialTheme.dimensions.iconMd,
                    onClick = { onDownloadClick(sura) }
                )
            }
        }
    )
}
