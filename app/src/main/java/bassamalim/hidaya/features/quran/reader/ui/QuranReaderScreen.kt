package bassamalim.hidaya.features.quran.reader.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Verse
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyIconPlayerButton
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.hafs_smart
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.core.ui.theme.uthmanic_hafs

@Composable
fun QuranReaderScreen(viewModel: QuranReaderViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalContext.current as Activity
    val configuration = LocalConfiguration.current

    if (state.isLoading) return

    val pagerState = rememberPagerState(
        initialPage = viewModel.pageNum - 1,
        pageCount = { Global.NUM_OF_QURAN_PAGES }
    )

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(pagerState, coroutineScope)
        onDispose { viewModel.onStop(activity) }
    }

    EnforcePortrait(activity)

    Scaffold(
        topBar = {
            TopBar(
                suraName = state.suraName,
                pageNumText = state.pageNum,
                juzNumText = state.juzNum
            )
        },
        bottomBar = {
            val featureNotFoundMessage = stringResource(R.string.feature_not_supported)
            BottomBar(
                activity = activity,
                playerState = state.playerState,
                onBookmarksClick = viewModel::onBookmarksClick,
                onPreviousVerseClick = viewModel::onPreviousVerseClick,
                onPlayPauseClick = {
                    viewModel.onPlayPauseClick(
                        activity = activity,
                        snackbarHostState = snackbarHostState,
                        message = featureNotFoundMessage
                    )
                },
                onNextVerseClick = viewModel::onNextVerseClick,
                onSettingsClick = viewModel::onSettingsClick
            )
        },
        floatingActionButton = {
//            BookmarkOptionButtons(
//                isExpanded = state.bookmarkOptionButtonsExpanded,
//                onReadingBookmarkClick = ,
//                onMemorizationBookmarkClick = ,
//                onRevisionBookmarkClick =
//            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PageContent(
            viewType = state.viewType,
            fillPage = state.fillPage,
            selectedVerse = state.selectedVerse,
            trackedVerseId = state.trackedVerseId,
            textSize = state.textSize.toInt(),
            language = viewModel.language,
            scrollTo = viewModel.scrollTo,
            onScrolled = viewModel::onScrolled,
            pagerState = pagerState,
            padding = padding,
            onPageChange = viewModel::onPageChange,
            buildPage = viewModel::buildPage,
            buildListPage = viewModel::buildListPage,
            onSuraHeaderGloballyPositioned = viewModel::onSuraHeaderGloballyPositioned,
            onVerseGloballyPositioned = viewModel::onVerseGloballyPositioned,
            onVersePointerInput = viewModel::onVersePointerInput,
            configuration = configuration
        )
    }

    TutorialDialog(
        shown = state.isTutorialDialogShown,
        text = stringResource(R.string.suras_reader_tips),
        onDismiss = viewModel::onTutorialDialogDismiss
    )
}

@Composable
private fun TopBar(suraName: String, pageNumText: String, juzNumText: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sura name
            MyText(
                text = "${stringResource(R.string.sura)} $suraName",
                fontSize = 18.nsp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start
            )

            // Page number
            MyText(
                text = "${stringResource(R.string.page)} $pageNumText",
                fontSize = 18.nsp,
                fontWeight = FontWeight.Medium
            )

            // Juz number
            MyText(
                text = "${stringResource(R.string.juz)} $juzNumText",
                fontSize = 18.nsp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun BottomBar(
    activity: Activity,
    playerState: Int,
    onBookmarksClick: () -> Unit,
    onPreviousVerseClick: () -> Unit,
    onPlayPauseClick: (Activity) -> Unit,
    onNextVerseClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    BottomAppBar(Modifier.height(56.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bookmark button
            MyIconButton(
                imageVector = Icons.Default.Bookmarks,
                description = stringResource(R.string.bookmark_verse_button_description),
                iconModifier = Modifier
                    .size(36.dp)
                    .padding(2.dp),
                onClick = onBookmarksClick
            )

            MyRow {
                // Skip to previous button
                MyIconButton(
                    iconId = R.drawable.ic_skip_previous,
                    description = stringResource(R.string.rewind_btn_description),
                    iconSize = 40.dp,
                    onClick = onPreviousVerseClick
                )

                // Play/Pause button
                Box (Modifier.padding(horizontal = 4.dp)) {
                    MyIconPlayerButton(
                        state = playerState,
                        onClick = { onPlayPauseClick(activity) },
                        iconSize = 40.dp,
                        filled = false
                    )
                }

                // Skip to next button
                MyIconButton(
                    iconId = R.drawable.ic_skip_next,
                    description = stringResource(R.string.fast_forward_btn_description),
                    iconSize = 40.dp,
                    onClick = onNextVerseClick
                )
            }

            // Preference button
            MyIconButton(
                imageVector = Icons.Default.DisplaySettings,
                description = stringResource(R.string.settings),
                iconModifier = Modifier
                    .size(40.dp)
                    .padding(2.dp),
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun BookmarkOptionButtons(
    isExpanded: Boolean,
    onReadingBookmarkClick: () -> Unit,
    onMemorizationBookmarkClick: () -> Unit,
    onRevisionBookmarkClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reading FAB
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { height -> (height * 3.9).toInt() }),
                exit = slideOutVertically(targetOffsetY = { height -> (height * 3.9).toInt() })
            ) {
                FloatingActionButton(
                    onClick = onReadingBookmarkClick,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(R.string.bookmarked_reading_verse)
                    )
                }
            }

            // Memorization FAB
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { height -> (height * 2.6).toInt() }),
                exit = slideOutVertically(targetOffsetY = { height -> (height * 2.6).toInt() })
            ) {
                FloatingActionButton(
                    onClick = onMemorizationBookmarkClick,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(R.string.bookmarked_memorization_verse)
                    )
                }
            }

            // Revision FAB
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(initialOffsetY = { height -> (height * 1.3).toInt() }),
                exit = slideOutVertically(targetOffsetY = { height -> (height * 1.3).toInt() })
            ) {
                FloatingActionButton(
                    onClick = onRevisionBookmarkClick,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(R.string.bookmarked_revision_verse)
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(
    viewType: QuranViewType,
    fillPage: Boolean,
    selectedVerse: Verse?,
    trackedVerseId: Int,
    textSize: Int,
    language: Language,
    pagerState: PagerState,
    padding: PaddingValues,
    scrollTo: Float,
    onScrolled: () -> Unit,
    onPageChange: (Int, Int, ScrollState) -> Unit,
    buildPage: (Int, Color, Color, Color) -> List<Section>,
    buildListPage: (Int, Color, Color, Color) -> List<Section>,
    onSuraHeaderGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onVerseGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit,
    configuration: Configuration
) {
    val lineHeight = remember { getLineHeight(padding, configuration) }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) { pageIdx ->
        val isCurrentPage = pageIdx == pagerState.currentPage
        val scrollState = rememberScrollState()

        onPageChange(pagerState.currentPage, pageIdx, scrollState)

        var columnModifier = Modifier.fillMaxSize()
        if (!(viewType == QuranViewType.PAGE && fillPage))
            columnModifier = columnModifier.verticalScroll(scrollState)

        Column(
            modifier = columnModifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (viewType) {
                QuranViewType.PAGE -> {
                    val pageContent = buildPage(
                        pageIdx + 1,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )

                    PageItems(
                        fillPage = fillPage,
                        sections = pageContent,
                        isCurrentPage = isCurrentPage,
                        textSize = textSize,
                        lineHeight = lineHeight,
                        onVersePointerInput = onVersePointerInput,
                        onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned
                    )
                }
                QuranViewType.LIST -> {
                    val pageContent = buildListPage(
                        pageIdx + 1,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )

                    ListItems(
                        sections = pageContent,
                        isCurrentPage = isCurrentPage,
                        selectedVerse = selectedVerse,
                        trackedVerseId = trackedVerseId,
                        textSize = textSize,
                        language = language,
                        onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned,
                        onVerseGloballyPositioned = onVerseGloballyPositioned
                    )
                }
            }

            if (isCurrentPage && scrollTo > 0f) {
                LaunchedEffect(null) {
                    scrollState.animateScrollTo(scrollTo.toInt())
                    onScrolled()
                }
            }
        }
    }
}

@Composable
private fun PageItems(
    fillPage: Boolean,
    sections: List<Section>,
    isCurrentPage: Boolean,
    textSize: Int,
    lineHeight: Dp,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit,
    onSuraHeaderGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit
) {
    for (section in sections) {
        if (fillPage) {
            when (section) {
                is SuraHeaderSection -> {
                    SuraHeader(
                        suraNum = section.suraNum,
                        suraName = section.suraName,
                        isCurrentPage = isCurrentPage,
                        textSize = textSize,
                        height = lineHeight,
                        onGloballyPositioned = onSuraHeaderGloballyPositioned
                    )
                }
                is BasmalahSection -> {
                    Basmalah(textSize = textSize, height = lineHeight)
                }
                is VersesSection -> {
                    FilledPageViewScreen(
                        annotatedString = section.annotatedString,
                        numOfLines =
                            if (section.suraNum == 1) section.numOfLines-1
                            else section.numOfLines,
                        lineHeight = lineHeight,
                        onVersePointerInput = onVersePointerInput
                    )
                }
            }
        }
        else {
            when (section) {
                is SuraHeaderSection -> {
                    SuraHeader(
                        suraNum = section.suraNum,
                        suraName = section.suraName,
                        isCurrentPage = isCurrentPage,
                        textSize = textSize,
                        onGloballyPositioned = onSuraHeaderGloballyPositioned
                    )
                }
                is BasmalahSection -> {
                    Basmalah(textSize)
                }
                is VersesSection -> {
                    PageViewScreen(
                        annotatedString = section.annotatedString,
                        textSize = textSize
                    )
                }
            }
        }
    }
}

@Composable
private fun ListItems(
    sections: List<Section>,
    isCurrentPage: Boolean,
    selectedVerse: Verse?,
    trackedVerseId: Int,
    textSize: Int,
    language: Language,
    onSuraHeaderGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onVerseGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit
) {
    for (section in sections) {
        when (section) {
            is SuraHeaderSection -> {
                SuraHeader(
                    suraNum = section.suraNum,
                    suraName = section.suraName,
                    isCurrentPage = isCurrentPage,
                    textSize = textSize,
                    onGloballyPositioned = onSuraHeaderGloballyPositioned
                )
            }
            is BasmalahSection -> {
                Basmalah(textSize)
            }
            is ListVerse -> {
                ListViewScreen(
                    verseId = section.id,
                    annotatedString = section.text,
                    isCurrentPage = isCurrentPage,
                    textSize = textSize,
                    selectedVerse = selectedVerse,
                    trackedVerseId = trackedVerseId,
                    onVerseGloballyPositioned = onVerseGloballyPositioned
                )

                if (language != Language.ARABIC && !section.translation.isNullOrEmpty()) {
                    MyText(
                        text = section.translation,
                        modifier = Modifier.padding(6.dp),
                        fontSize = (textSize - 5).sp
                    )
                }

                if (section != sections.last()) {
                    MyHorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PageViewScreen(annotatedString: AnnotatedString, textSize: Int) {
    Text(
        text = annotatedString,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
        style = TextStyle(
            fontFamily = hafs_smart,
            fontSize = textSize.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
private fun FilledPageViewScreen(
    annotatedString: AnnotatedString,
    numOfLines: Int,
    lineHeight: Dp,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit
) {
    var fontSize by remember { mutableStateOf(25.sp) }
    var ready by remember { mutableStateOf(false) }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val t1 = remember { System.currentTimeMillis() }

    Text(
        text = annotatedString,
        modifier = Modifier
            .fillMaxWidth()
            .height((lineHeight.value * numOfLines).dp)
            .padding(horizontal = 6.dp)
            .pointerInput(Unit) {
                onVersePointerInput(this, layoutResult, annotatedString)
            }
            .drawWithContent {
                if (ready) {
                    drawContent()
                    println("Took ${System.currentTimeMillis() - t1} ms to draw content")
                }
            },
        fontSize = fontSize,
        style = TextStyle(
            fontFamily = hafs_smart,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = lineHeight.value.sp,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both
            ),
            textAlign = TextAlign.Center,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineBreak = LineBreak(
                strategy = LineBreak.Strategy.Balanced,
                strictness = LineBreak.Strictness.Strict,
                wordBreak = LineBreak.WordBreak.Default
            )
        ),
        overflow = TextOverflow.Visible,
        textAlign = TextAlign.Justify,
        onTextLayout = { textLayoutResult ->
            layoutResult = textLayoutResult

            if (!ready) {
                when {
                    textLayoutResult.lineCount > numOfLines -> fontSize = (fontSize.value - 0.5f).sp
                    textLayoutResult.lineCount < numOfLines -> fontSize = (fontSize.value + 0.5f).sp
                    else -> ready = true
                }
            }
        }
    )
}

@Composable
private fun ListViewScreen(
    verseId: Int,
    annotatedString: AnnotatedString,
    isCurrentPage: Boolean,
    selectedVerse: Verse?,
    trackedVerseId: Int,
    textSize: Int,
    onVerseGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit
) {
    Text(
        text = annotatedString,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .onGloballyPositioned { layoutCoordinates ->
                onVerseGloballyPositioned(verseId, isCurrentPage, layoutCoordinates)
            },
        style = TextStyle(
            fontFamily = hafs_smart,
            fontSize = textSize.sp,
            color =
                if (selectedVerse?.id == verseId) MaterialTheme.colorScheme.primary
                else if (trackedVerseId == verseId) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
private fun SuraHeader(
    suraNum: Int,
    suraName: String,
    isCurrentPage: Boolean,
    textSize: Int,
    height: Dp? = null,
    onGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit
) {
// TODO: Implement revelation icon
//                        Icon(
//                            painter = painterResource(
//                                if (item.revelation == 0) R.drawable.ic_kaaba
//                                else R.drawable.ic_madina
//                            ),
//                            contentDescription = stringResource(R.string.revelation_view_description),
//                            modifier = Modifier.size(30.dp),
//                            tint = MaterialTheme.colorScheme.outlineVariant
//                        )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(height ?: (textSize * 1.6).dp)
            .onGloballyPositioned { layoutCoordinates ->
                onGloballyPositioned(suraNum, isCurrentPage, layoutCoordinates)
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.sura_header),
            contentDescription = suraName,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 5.dp, horizontal = 5.dp),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )

        MyText(
            text = "${stringResource(R.string.sura)} $suraName",
            fontSize = (textSize * 0.9).sp,
            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = uthmanic_hafs
        )
    }
}

@Composable
private fun Basmalah(textSize: Int, height: Dp? = null) {
    Image(
        painter = painterResource(R.drawable.basmala),
        contentDescription = stringResource(R.string.basmalah),
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .height(height ?: (textSize * 1.6).dp),
        contentScale = ContentScale.FillBounds,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
    )
}

@Composable
private fun EnforcePortrait(activity: Activity) {
    DisposableEffect(Unit) {
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity.requestedOrientation = originalOrientation
        }
    }
}

private fun getLineHeight(padding: PaddingValues, configuration: Configuration): Dp {
    val screenHeightPx = configuration.screenHeightDp.dp
    val topBarHeight = 36.dp
    val availableHeight = screenHeightPx -
            topBarHeight - padding.calculateTopPadding() - padding.calculateBottomPadding()
    return availableHeight / 15f
}