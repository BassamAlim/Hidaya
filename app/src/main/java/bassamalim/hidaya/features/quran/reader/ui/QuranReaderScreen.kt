package bassamalim.hidaya.features.quran.reader.ui

import android.app.Activity
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import bassamalim.hidaya.core.ui.components.MyIconPlayerBtn
import bassamalim.hidaya.core.ui.components.MyRow
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

    Scaffold(
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
            fillPage = state.fillPage,
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
        modifier = Modifier.fillMaxSize()
    ) { pageIdx ->
        val isCurrentPage = pageIdx == pagerState.currentPage
        val scrollState = rememberScrollState()

        onPageChange(pagerState.currentPage, pageIdx, scrollState)

        val modifier =
            if (viewType == QuranViewType.PAGE && fillPage) Modifier.fillMaxSize()
            else Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)

        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (viewType) {
                QuranViewType.PAGE -> {
                    val pageContent = buildPage(
                        pageIdx + 1,
                        AppTheme.colors.strongText,
                        AppTheme.colors.highlight,
                        AppTheme.colors.track
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
                        AppTheme.colors.strongText,
                        AppTheme.colors.highlight,
                        AppTheme.colors.track
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
// TODO: handle <aya> in tafseer
// TODO: find a way to center filled page text
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
                        numOfLines = section.numOfLines,
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

                if (language != Language.ARABIC) {
                    MyText(
                        text = section.translation,
                        modifier = Modifier.padding(6.dp),
                        fontSize = (textSize - 5).sp
                    )
                }

                if (section != sections.last())
                    MyHorizontalDivider()
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
            fontFamily = hafs,
            fontSize = textSize.sp,
            color = AppTheme.colors.strongText,
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
            fontFamily = hafs,
            color = AppTheme.colors.strongText,
            lineHeight = (lineHeight.value * 0.95).sp,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.None
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
            fontFamily = hafs,
            fontSize = textSize.sp,
            color =
                if (selectedVerse?.id == verseId) AppTheme.colors.highlight
                else if (trackedVerseId == verseId) AppTheme.colors.track
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
    height: Dp? = null,
    onGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit
) {
    Box(
        Modifier
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
            colorFilter = ColorFilter.tint(AppTheme.colors.text)
        )

        MyText(
            text = "${stringResource(R.string.sura)} $suraName",
            fontSize = (textSize * 0.9).sp,
            textColor = AppTheme.colors.strongText,
            fontFamily = uthmanic
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

private fun getLineHeight(padding: PaddingValues, configuration: Configuration): Dp {
    val screenHeightPx = configuration.screenHeightDp.dp
    val topBarHeight = 36.dp

    val availableHeight = screenHeightPx -
            topBarHeight - padding.calculateTopPadding() - padding.calculateBottomPadding()
    val lineHeight = availableHeight / 15f
    println("in getLineHeight, availableHeight: $availableHeight, lineHeight: $lineHeight, padding: $padding")
    return lineHeight
}