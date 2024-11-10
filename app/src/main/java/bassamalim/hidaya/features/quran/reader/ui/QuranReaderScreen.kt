package bassamalim.hidaya.features.quran.reader.ui

import android.app.Activity
import android.widget.Toast
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Verse
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyIconPlayerBtn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.hafs
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.core.ui.theme.uthmanic

@Composable
fun QuranReaderScreen(viewModel: QuranReaderViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalContext.current as Activity

    if (state.isLoading) return

    val pagerState = rememberPagerState(
        initialPage = viewModel.pageNum - 1,
        pageCount = { Global.NUM_OF_QURAN_PAGES }
    )

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(pagerState, coroutineScope)
        onDispose { viewModel.onStop(activity) }
    }

    MyScaffold(
        title = "",
        backgroundColor = AppTheme.colors.quranBG,
        topBar = {
            TopBar(
                suraName = state.suraName,
                pageNumText = state.pageNum,
                juzNumText = state.juzNum
            )
        },
        bottomBar = {
            BottomBar(
                activity = activity,
                isBookmarked = state.isBookmarked,
                playerState = state.playerState,
                onBookmarkClick = viewModel::onBookmarkClick,
                onPreviousVerseClick = viewModel::onPreviousVerseClick,
                onPlayPauseClick = viewModel::onPlayPauseClick,
                onNextVerseClick = viewModel::onNextVerseClick,
                onSettingsClick = viewModel::onSettingsClick
            )
        }
    ) {
        PageContent(
            viewType = state.viewType,
            selectedVerse = state.selectedVerse,
            trackedVerseId = state.trackedVerseId,
            textSize = state.textSize.toInt(),
            language = viewModel.language,
            scrollTo = viewModel.scrollTo,
            onScrolled = viewModel::onScrolled,
            pagerState = pagerState,
            padding = it,
            onPageChange = viewModel::onPageChange,
            buildPage = viewModel::buildPage,
            onSuraHeaderGloballyPositioned = viewModel::onSuraHeaderGloballyPositioned,
            onVerseGloballyPositioned = viewModel::onVerseGloballyPositioned,
            onVerseClick = viewModel::onVerseClick,
            onVersePressed = viewModel::onVersePressed,
            onVerseReleased = viewModel::onVerseReleased
        )
    }

    TutorialDialog(
        shown = state.isTutorialDialogShown,
        text = stringResource(R.string.suras_reader_tips),
        onDismiss = viewModel::onTutorialDialogDismiss
    )

    if (state.isPlayerNotSupportedShown) {
        PlayerNotSupportedToast()
    }
}

@Composable
private fun TopBar(suraName: String, pageNumText: String, juzNumText: String) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        backgroundColor = AppTheme.colors.quranBG,
        elevation = 4.dp
    ) {
        Row(
            Modifier
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
                textAlign = TextAlign.Start,
                textColor = AppTheme.colors.onPrimary
            )

            // Page number
            MyText(
                text = "${stringResource(R.string.page)} $pageNumText",
                fontSize = 18.nsp,
                fontWeight = FontWeight.Medium,
                textColor = AppTheme.colors.onPrimary
            )

            // Juz number
            MyText(
                text = "${stringResource(R.string.juz)} $juzNumText",
                fontSize = 18.nsp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End,
                textColor = AppTheme.colors.onPrimary
            )
        }
    }
}

@Composable
private fun BottomBar(
    activity: Activity,
    isBookmarked: Boolean,
    playerState: Int,
    onBookmarkClick: (Boolean) -> Unit,
    onPreviousVerseClick: () -> Unit,
    onPlayPauseClick: (Activity) -> Unit,
    onNextVerseClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        backgroundColor = AppTheme.colors.primary
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Bookmark btn
            MyIconButton(
                iconId =
                    if (isBookmarked) R.drawable.ic_bookmarked
                    else R.drawable.ic_bookmark,
                description = stringResource(R.string.bookmark_page_button_description),
                tint = AppTheme.colors.onPrimary,
                size = 40.dp,
                onClick = { onBookmarkClick(isBookmarked) }
            )

            MyRow {
                // Rewind btn
                MyIconButton(
                    iconId = R.drawable.ic_skip_previous,
                    description = stringResource(R.string.rewind_btn_description),
                    size = 40.dp,
                    tint = AppTheme.colors.onPrimary,
                    onClick = onPreviousVerseClick
                )

                // Play/Pause btn
                MyIconPlayerBtn(
                    state = playerState,
                    size = 50.dp,
                    padding = 5.dp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    tint = AppTheme.colors.onPrimary,
                    onClick = { onPlayPauseClick(activity) }
                )

                // Fast Forward btn
                MyIconButton(
                    iconId = R.drawable.ic_skip_next,
                    description = stringResource(R.string.fast_forward_btn_description),
                    size = 40.dp,
                    tint = AppTheme.colors.onPrimary,
                    onClick = onNextVerseClick
                )
            }

            // Preference btn
            MyIconButton(
                iconId = R.drawable.ic_display_settings,
                description = stringResource(R.string.settings),
                tint = AppTheme.colors.onPrimary,
                size = 40.dp,
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun PageContent(
    viewType: QuranViewType,
    selectedVerse: Verse?,
    trackedVerseId: Int,
    textSize: Int,
    language: Language,
    pagerState: PagerState,
    padding: PaddingValues,
    scrollTo: Float,
    onScrolled: () -> Unit,
    onPageChange: (Int, Int, ScrollState) -> Unit,
    buildPage: (Int) -> List<Section>,
    onSuraHeaderGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onVerseGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit,
    onVerseClick: (Int) -> Unit,
    onVersePressed: (Int) -> Unit,
    onVerseReleased: (Int) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) { pageIdx ->
        val isCurrentPage = pageIdx == pagerState.currentPage
        val scrollState = rememberScrollState()

        onPageChange(pagerState.currentPage, pageIdx, scrollState)

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val t1 = System.nanoTime()
            val pageContent = buildPage(pageIdx + 1)
            val t2 = System.nanoTime()
            println("buildPage took ${t2 - t1} ns")

            when (viewType) {
                QuranViewType.PAGE -> {
                    PageItems(
                        sections = pageContent,
                        isCurrentPage = isCurrentPage,
                        selectedVerse = selectedVerse,
                        trackedVerseId = trackedVerseId,
                        textSize = textSize,
                        onVerseClick = onVerseClick,
                        onVersePressed = onVersePressed,
                        onVerseReleased = onVerseReleased,
                        onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned
                    )
                }
                QuranViewType.LIST -> {
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
    sections: List<Section>,
    isCurrentPage: Boolean,
    selectedVerse: Verse?,
    trackedVerseId: Int,
    textSize: Int,
    onVerseClick: (Int) -> Unit,
    onVersePressed: (Int) -> Unit,
    onVerseReleased: (Int) -> Unit,
    onSuraHeaderGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit
) {
    val lineHeight = getLineHeight()

    for (section in sections) {
        when (section) {
            is SuraHeaderSection -> {
                SuraHeader(
                    suraNum = section.suraNum,
                    suraName = section.suraName,
                    isCurrentPage = isCurrentPage,
                    textSize = textSize,
                    lineHeight = lineHeight,
                    onGloballyPositioned = onSuraHeaderGloballyPositioned
                )
            }
            is BasmalahSection -> {
                Basmalah(textSize = textSize, lineHeight = lineHeight)
            }
            is VersesSection -> {
                PageItem(
                    sequence = section.verses,
                    numOfLines = section.numOfLines,
                    selectedVerse = selectedVerse,
                    trackedVerseId = trackedVerseId,
                    textSize = textSize,
                    lineHeight = lineHeight,
                    onVerseClick = onVerseClick,
                    onVersePressed = onVersePressed,
                    onVerseReleased = onVerseReleased
                )
            }
        }
    }
}

@Composable
private fun PageItem(
    sequence: List<Verse>,
    numOfLines: Int,
    selectedVerse: Verse?,
    trackedVerseId: Int,
    textSize: Int,
    lineHeight: TextUnit,
    onVerseClick: (Int) -> Unit,
    onVersePressed: (Int) -> Unit,
    onVerseReleased: (Int) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        for (seqVerse in sequence) {
            withLink(
                link = LinkAnnotation.Clickable(
                    tag = seqVerse.id.toString(),
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color =
                                if (selectedVerse == seqVerse) AppTheme.colors.highlight
                                else if (trackedVerseId == seqVerse.id) AppTheme.colors.track
                                else AppTheme.colors.strongText
                        )
                    ),
                    linkInteractionListener = { onVerseClick(seqVerse.id) }
                )
            ) {
                append(seqVerse.text)
            }
        }
    }

    PageViewScreen(
        annotatedString = annotatedString,
        numOfLines = numOfLines,
        lineHeight = lineHeight,
        onVersePressed = onVersePressed,
        onVerseReleased = onVerseReleased
    )
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
    onVerseGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit
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
            is VersesSection -> {
                for (verse in section.verses) {
                    ListViewScreen(
                        annotatedString = AnnotatedString(verse.text!!),
                        verse = verse,
                        isCurrentPage = isCurrentPage,
                        textSize = textSize,
                        selectedVerse = selectedVerse,
                        trackedVerseId = trackedVerseId,
                        onVerseGloballyPositioned = onVerseGloballyPositioned
                    )

                    if (language != Language.ARABIC) {
                        MyText(
                            text = verse.translation!!,
                            fontSize = (textSize - 5).sp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }

                    if (verse.num != section.verses.last().num)
                        MyHorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PageViewScreen(
    annotatedString: AnnotatedString,
    numOfLines: Int,
    lineHeight: TextUnit,
    onVersePressed: (Int) -> Unit,
    onVerseReleased: (Int) -> Unit
) {
    var fontSize by remember { mutableStateOf(25.sp) }
    var ready by remember { mutableStateOf(false) }
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedString,
        modifier = Modifier
            .fillMaxWidth()
            .height((lineHeight.value * numOfLines).dp)
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .pointerInput(Unit) {
                println("pointerInput")
                awaitPointerEventScope {
                    println("awaitPointerEventScope")
                    while (true) {
                        val event = awaitPointerEvent()
                        val offset = event.changes[0].position
                        val position = layoutResult?.getOffsetForPosition(offset)
                        val verseId = position?.let {
                            annotatedString.getLinkAnnotations(start = position, end = position)
                                .firstOrNull()?.item?.toString()
                                ?.substringAfter('=')
                                ?.substringBefore(')')
                                ?.toInt()
                        }
                        println("event, event.type: ${event.type}, event.changes.size: ${event.changes.size}, verseId: $verseId")

                        when (event.type) {
                            PointerEventType.Press -> {
                                println("Press")
                                verseId?.let(onVersePressed)
                            }
                            PointerEventType.Release -> {
                                println("Release")
                                verseId?.let(onVerseReleased)
                            }
                        }
                    }
                }
            }
            .drawWithContent {
                println("in drawWithContent, ready: $ready, fontSize: $fontSize, lineHeight: $lineHeight")
//                if (ready)
                    drawContent()
            },
        fontSize = fontSize,
        style = TextStyle(
            fontFamily = hafs,
            color = AppTheme.colors.strongText,
            lineHeight = (lineHeight.value * 0.95).sp,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None
            )
        ),
        textAlign = TextAlign.Justify,
        onTextLayout = { textLayoutResult ->
            println("in onTextLayout, ready: $ready, fontSize: $fontSize, lineHeight: $lineHeight, lineCount: ${textLayoutResult.lineCount}, didOverflowHeight: ${textLayoutResult.didOverflowHeight}")

            layoutResult = textLayoutResult

            if (!ready) {
                when {
                    textLayoutResult.lineCount > numOfLines -> fontSize *= 0.99f
                    textLayoutResult.lineCount < numOfLines -> fontSize *= 1.01f
                    else -> ready = true
                }
            }
        }
    )
}

@Composable
private fun getLineHeight(): TextUnit {
    val configuration = LocalConfiguration.current
    val screenHeightPx = configuration.screenHeightDp.dp

    // Define the heights of top and bottom bars
    val topBarHeight = 36.dp
    val bottomBarHeight = 56.dp

    // Calculate available height for text lines
    val availableHeight = screenHeightPx - topBarHeight - bottomBarHeight

    // Convert Dp to sp for line height calculation
    val lineHeight = with(LocalDensity.current) {
        (availableHeight / 15).toSp()
    }

    println("in getLineHeight, availableHeight: $availableHeight, lineHeight: $lineHeight")

    return lineHeight
}

@Composable
private fun ListViewScreen(
    annotatedString: AnnotatedString,
    verse: Verse,
    isCurrentPage: Boolean,
    selectedVerse: Verse?,
    trackedVerseId: Int,
    textSize: Int,
    onVerseGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit
) {
    Text(
        text = annotatedString,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .onGloballyPositioned { layoutCoordinates ->
                onVerseGloballyPositioned(verse, isCurrentPage, layoutCoordinates)
            },
        style = TextStyle(
            fontFamily = hafs,
            fontSize = textSize.sp,
            color =
                if (selectedVerse == verse) AppTheme.colors.highlight
                else if (trackedVerseId == verse.id) AppTheme.colors.track
                else AppTheme.colors.strongText,
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
    lineHeight: TextUnit? = null,
    onGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit
) {
    val lineHeightDp =
        if (lineHeight != null) with(LocalDensity.current) { lineHeight.toDp() }
        else (textSize * 1.6).dp

    Box(
        Modifier
            .padding(top = 10.dp, bottom = 5.dp, start = 5.dp, end = 5.dp)
            .onGloballyPositioned { layoutCoordinates ->
                onGloballyPositioned(suraNum, isCurrentPage, layoutCoordinates)
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.sura_header),
            contentDescription = suraName,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(Color.Gray)
                .height(lineHeightDp),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(AppTheme.colors.text)
        )

        MyText(
            text = "${stringResource(R.string.sura)} $suraName",
            fontSize = textSize.sp,
            textColor = AppTheme.colors.strongText,
            fontFamily = uthmanic
        )
    }
}

@Composable
private fun Basmalah(textSize: Int, lineHeight: TextUnit? = null) {
    val lineHeightDp =
        if (lineHeight != null) with(LocalDensity.current) { lineHeight.toDp() }
        else (textSize * 1.6).dp

    Image(
        painter = painterResource(R.drawable.basmala),
        contentDescription = stringResource(R.string.basmalah),
        modifier = Modifier
            .fillMaxWidth(0.75f)
            .background(Color.DarkGray)
            .height(lineHeightDp),
        contentScale = ContentScale.FillBounds,
        colorFilter = ColorFilter.tint(AppTheme.colors.strongText)
    )
}

@Composable
private fun PlayerNotSupportedToast() {
    val context = LocalContext.current
    LaunchedEffect(null) {
        Toast.makeText(
            context,
            context.getString(R.string.feature_not_supported),
            Toast.LENGTH_SHORT
        ).show()
    }
}