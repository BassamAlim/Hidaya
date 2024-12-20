package bassamalim.hidaya.features.quran.surasMenu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.models.QuranBookmarks
import bassamalim.hidaya.core.models.Sura
import bassamalim.hidaya.core.ui.components.LoadingScreen
import bassamalim.hidaya.core.ui.components.MyClickableSurface
import bassamalim.hidaya.core.ui.components.MyFavoriteButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.SearchComponent
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.Bookmark1Color
import bassamalim.hidaya.core.ui.theme.Bookmark2Color
import bassamalim.hidaya.core.ui.theme.Bookmark3Color
import bassamalim.hidaya.core.ui.theme.Bookmark4Color
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranSurasScreen(
    viewModel: QuranSurasViewModel,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val noBookmarkMessage = stringResource(R.string.no_bookmarked_page)

    if (state.isLoading) return LoadingScreen()

    MyScaffold(
        title = "",
        topBar = {},  // override the default top bar
        floatingActionButton = {
            AnimatedVisibility(
                visible = !lazyListState.isScrollInProgress && lazyListState.canScrollForward
            ) {
                BookmarksFab(
                    isExpanded = state.isBookmarksExpanded,
                    bookmarks = state.bookmarks,
                    onBookmarksClick = viewModel::onBookmarksClick,
                    onBookmarkOptionClick = { verseId ->
                        viewModel.onBookmarkOptionClick(
                            verseId = verseId,
                            snackbarHostState = snackbarHostState,
                            message = noBookmarkMessage
                        )
                    }
                )
            }
        },
        // TODO: fix snackbar not showing
        snackBarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        TabLayout(
            pageNames = listOf(
                stringResource(R.string.all),
                stringResource(R.string.favorite)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            searchComponent = {
                val pageDoesNotExistMessage = stringResource(R.string.page_does_not_exist)
                SearchComponent(
                    value = state.searchText,
                    hint = stringResource(R.string.quran_search_hint),
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = viewModel::onSearchTextChange,
                    onSubmit = {
                        viewModel.onSearchSubmit(
                            snackbarHostState = snackbarHostState,
                            message = pageDoesNotExistMessage
                        )
                    }
                )
            }
        ) { page ->
            Tab(
                surasFlow = viewModel.getItems(page),
                onSuraClick = viewModel::onSuraClick,
                onFavoriteClick = viewModel::onFavoriteClick,
                lazyListState = lazyListState
            )
        }
    }

    TutorialDialog(
        shown = state.isTutorialDialogShown,
        text = stringResource(R.string.quran_menu_tips),
        onDismissRequest = viewModel::onTutorialDialogDismiss
    )
}

@Composable
private fun Tab(
    surasFlow: Flow<List<Sura>>,
    onSuraClick: (Int) -> Unit,
    onFavoriteClick: (Int, Boolean) -> Unit,
    lazyListState: LazyListState
) {
    val suras by surasFlow.collectAsStateWithLifecycle(emptyList())

    MyLazyColumn(
        state = lazyListState,
        lazyList = {
            items(suras) { item ->
                MyClickableSurface(
                    modifier = Modifier.padding(2.dp),
                    elevation = 6.dp,
                    onClick = { onSuraClick(item.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 10.dp, start = 14.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MyText(
                            text = "${stringResource(R.string.sura)} ${item.decoratedName}",
                            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 20.dp),
                            textAlign = TextAlign.Start
                        )

                        MyFavoriteButton(
                            isFavorite = item.isFavorite,
                            onClick = { onFavoriteClick(item.id, item.isFavorite) },
                            size = 28.dp
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun BookmarksFab(
    isExpanded: Boolean,
    bookmarks: QuranBookmarks,
    onBookmarksClick: () -> Unit,
    onBookmarkOptionClick: (Int?) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Bookmark1 FAB
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { height -> (height * 5.2).toInt() }),
                exit = slideOutVertically(targetOffsetY = { height -> (height * 5.2).toInt() })
            ) {
                FloatingActionButton(
                    onClick = { onBookmarkOptionClick(bookmarks.bookmark1VerseId) },
                    contentColor = Bookmark1Color
                ) {
                    Icon(
                        imageVector =
                            if (bookmarks.bookmark1VerseId != null) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(R.string.bookmarked_verse)
                    )
                }
            }

            // Bookmark2 FAB
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { height -> (height * 3.9).toInt() }),
                exit = slideOutVertically(targetOffsetY = { height -> (height * 3.9).toInt() })
            ) {
                FloatingActionButton(
                    onClick = { onBookmarkOptionClick(bookmarks.bookmark2VerseId) },
                    contentColor = Bookmark2Color
                ) {
                    Icon(
                        imageVector =
                            if (bookmarks.bookmark2VerseId != null) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(R.string.bookmarked_verse)
                    )
                }
            }

            // Bookmark3 FAB
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { height -> (height * 2.6).toInt() }),
                exit = slideOutVertically(targetOffsetY = { height -> (height * 2.6).toInt() })
            ) {
                FloatingActionButton(
                    onClick = { onBookmarkOptionClick(bookmarks.bookmark3VerseId) },
                    contentColor = Bookmark3Color
                ) {
                    Icon(
                        imageVector =
                            if (bookmarks.bookmark3VerseId != null) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(R.string.bookmarked_verse)
                    )
                }
            }

            // Bookmark4 FAB
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { height -> (height * 1.3).toInt() }),
                exit = slideOutVertically(targetOffsetY = { height -> (height * 1.3).toInt() })
            ) {
                FloatingActionButton(
                    onClick = { onBookmarkOptionClick(bookmarks.bookmark4VerseId) },
                    contentColor = Bookmark4Color
                ) {
                    Icon(
                        imageVector =
                            if (bookmarks.bookmark4VerseId != null) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(R.string.bookmarked_verse)
                    )
                }
            }

            // Main FAB
            FloatingActionButton(onClick = onBookmarksClick) {
                Icon(
                    imageVector = Icons.Default.Bookmarks,
                    contentDescription = stringResource(R.string.bookmarked_verses)
                )
            }
        }
    }
}