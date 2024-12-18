package bassamalim.hidaya.features.quran.surasMenu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import bassamalim.hidaya.core.ui.components.TabLayout
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.Bookmark1Color
import bassamalim.hidaya.core.ui.theme.Bookmark2Color
import bassamalim.hidaya.core.ui.theme.Bookmark3Color
import bassamalim.hidaya.core.ui.theme.Bookmark4Color
import bassamalim.hidaya.core.ui.theme.hafs_smart
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
                QuranSearchBar(
                    searchSurasAndPages = viewModel::searchSurasAndPages,
                    searchVerses = viewModel::searchVerses,
                    onSuraClick = viewModel::onSuraClick,
                    onPageClick = viewModel::onPageClick,
                    onVerseClick = viewModel::onVerseClick
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranSearchBar(
    searchSurasAndPages: (String) -> List<SearchMatch>,
    searchVerses: (String, Color) -> List<VerseMatch>,
    onSuraClick: (Int) -> Unit,
    onPageClick: (String) -> Unit,
    onVerseClick: (Int) -> Unit
) {
    val state = rememberTextFieldState()
    var expanded by remember { mutableStateOf(false) }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                state = state,
                onSearch = {},
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { MyText(stringResource(R.string.quran_search_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search)
                    )
                },
                trailingIcon = {
                    if (state.text.isNotEmpty()) {
                        IconButton(onClick = state::clearText) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                }
            )
        },
        expanded = expanded,
        onExpandedChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp)
            .padding(horizontal = if (expanded) 0.dp else 6.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        val suraAndPageMatches = searchSurasAndPages(state.text.toString())
        val highlightColor = MaterialTheme.colorScheme.primary
        val verseMatches = searchVerses(state.text.toString(), highlightColor)

        SearchBarContent(
            suraAndPageMatches = suraAndPageMatches,
            verseMatches = verseMatches,
            onSuraClick = onSuraClick,
            onPageClick = onPageClick,
            onVerseClick = onVerseClick
        )
    }
}

@Composable
private fun SearchBarContent(
    suraAndPageMatches: List<SearchMatch>,
    verseMatches: List<VerseMatch>,
    onSuraClick: (Int) -> Unit,
    onPageClick: (String) -> Unit,
    onVerseClick: (Int) -> Unit
) {
    MyText(
        text = stringResource(R.string.suras_and_pages),
        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 17.dp),
        fontSize = 16.sp,
        textAlign = TextAlign.Start
    )

    if (suraAndPageMatches.isNotEmpty()) {
        MyLazyColumn(
            state = rememberLazyListState(),
            lazyList = {
                items(suraAndPageMatches) { match ->
                    when (match) {
                        is SuraMatch -> {
                            ListItem(
                                headlineContent = {
                                    MyText(
                                        text = "${stringResource(R.string.sura)} ${match.decoratedName}",
                                        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 16.dp),
                                        textAlign = TextAlign.Start
                                    )
                                },
                                modifier = Modifier.clickable { onSuraClick(match.id) }
                            )
                        }
                        is PageMatch -> {
                            ListItem(
                                headlineContent = {
                                    MyText(
                                        text = "${stringResource(R.string.page)} ${match.num}",
                                        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 16.dp),
                                        textAlign = TextAlign.Start
                                    )
                                },
                                modifier = Modifier.clickable { onPageClick(match.num) },
                                supportingContent = {
                                    MyText(
                                        text = match.suraName,
                                        modifier = Modifier.padding(top = 6.dp, bottom = 6.dp, start = 16.dp),
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            )
                        }
                    }

                    HorizontalDivider(thickness = 0.3.dp)
                }
            }
        )
    }
    else {
        MyText(
            text = stringResource(R.string.no_matches),
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 16.dp),
            textAlign = TextAlign.Start
        )
    }

    MyText(
        text = stringResource(R.string.verses),
        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 16.dp),
        fontSize = 16.sp,
        textAlign = TextAlign.Start
    )

    if (verseMatches.isNotEmpty()) {
        MyLazyColumn(
            state = rememberLazyListState(),
            lazyList = {
                items(verseMatches) { verse ->
                    VerseMatchListItem(item = verse, onVerseClick = onVerseClick)

                    HorizontalDivider(thickness = 0.3.dp)
                }
            }
        )
    }
    else {
        MyText(
            text = stringResource(R.string.no_matches),
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 20.dp),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun VerseMatchListItem(item: VerseMatch, onVerseClick: (Int) -> Unit) {
    ListItem(
        headlineContent = {
            MyText(
                text = "${stringResource(R.string.sura)} ${item.suraName}, " +
                        "${stringResource(R.string.verse_number)} ${item.verseNum}",
                modifier = Modifier.padding(top = 6.dp, start = 16.dp)
            )
        },
        modifier = Modifier.clickable { onVerseClick(item.id) },
        supportingContent = {
            MyText(
                text = item.text,
                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp, start = 16.dp),
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                fontFamily = hafs_smart
            )
        }
    )
}

@Composable
private fun BookmarksFab(
    isExpanded: Boolean,
    bookmarks: QuranBookmarks,
    onBookmarksClick: () -> Unit,
    onBookmarkOptionClick: (Int?) -> Unit
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