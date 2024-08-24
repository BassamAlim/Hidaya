@file:OptIn(ExperimentalFoundationApi::class)

package bassamalim.hidaya.features.quranReader.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.models.Verse
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.components.InfoDialog
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyIconPlayerBtn
import bassamalim.hidaya.core.ui.components.MyRow
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.TutorialDialog
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.core.ui.theme.uthmanic
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quranReader.ui.QuranViewType.LIST
import bassamalim.hidaya.features.quranSettings.ui.QuranSettingsDlg

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuranViewerUI(
    viewModel: QuranReaderViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = viewModel.pageNum - 1,
        pageCount = { Global.QURAN_PAGES }
    )
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart(pagerState, coroutineScope)
        onDispose { viewModel.onStop() }
    }

    MyScaffold(
        title = "",
        backgroundColor = AppTheme.colors.quranBG,
        topBar = {
            TopBar(
                suraName = state.suraName,
                pageNumText = state.pageNum,
                juzNumText = state.juzNum,
                numeralsLanguage = viewModel.numeralsLanguage
            )
        },
        bottomBar = { BottomBar(viewModel, state) }
    ) {
        PageContent(
            pageVerses = state.pageVerses,
            viewType = state.viewType,
            selectedVerse = state.selectedVerse!!,
            trackedAyaId = state.trackedVerseId,
            textSize = state.textSize.toInt(),
            language = viewModel.language,
            theme = viewModel.theme,
            scrollTo = viewModel.scrollTo,
            onScrolled = viewModel::onScrolled,
            setScrollState = viewModel::setScrollState,
            pagerState = pagerState,
            padding = it,
            onPageChange = viewModel::onPageChange,
            buildPage = viewModel::buildPage,
            onSuraHeaderGloballyPositioned = viewModel::onSuraHeaderGloballyPositioned,
            onAyaGloballyPositioned = viewModel::onVerseGloballyPositioned,
            onAyaScreenClick = viewModel::onVerseClick
        )
    }

    TutorialDialog(
        shown = state.isTutorialDialogShown,
        textResId = R.string.quran_tips,
        onDismiss = viewModel::onTutorialDialogDismiss
    )

    InfoDialog(
        shown = state.isInfoDialogShown,
        title = stringResource(R.string.interpretation),
        text = state.infoDialogText,
        onDismiss = viewModel::onInfoDialogDismiss  // :: gives the reference to the function
    )

    QuranSettingsDlg(
        viewModel = hiltViewModel(),
        shown = state.isSettingsDialogShown,
        onDone = viewModel::onSettingsDialogDismiss
    )

    if (state.isPlayerNotSupportedShown) {
        PlayerNotSupportedToast()
    }
}

@Composable
private fun TopBar(
    suraName: String,
    pageNumText: String,
    juzNumText: String,
    numeralsLanguage: Language
) {
    TopAppBar(
        backgroundColor = AppTheme.colors.primary,
        modifier = Modifier.fillMaxWidth(),
        elevation = 8.dp
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sura name
            MyText(
                text = "${stringResource(R.string.sura)} $suraName",
                fontSize = 18.nsp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                textColor = AppTheme.colors.onPrimary
            )

            // Page number
            MyText(
                text = "${stringResource(R.string.page)} " +
                        translateNums(
                            numeralsLanguage = numeralsLanguage,
                            string = pageNumText
                        ),
                fontSize = 18.nsp,
                fontWeight = FontWeight.Bold,
                textColor = AppTheme.colors.onPrimary
            )

            // Juz number
            MyText(
                "${stringResource(R.string.juz)} " +
                        translateNums(
                            numeralsLanguage = numeralsLanguage,
                            string = juzNumText
                        ),
                fontSize = 18.nsp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                textColor = AppTheme.colors.onPrimary
            )
        }
    }
}

@Composable
private fun BottomBar(
    vm: QuranReaderViewModel,
    st: QuranReaderUiState
) {
    BottomAppBar(
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
                    if (st.isBookmarked) R.drawable.ic_bookmarked
                    else R.drawable.ic_bookmark,
                description = stringResource(R.string.bookmark_page_button_description),
                tint = AppTheme.colors.onPrimary,
                size = 40.dp,
                onClick = { vm.onBookmarkClick() }
            )

            MyRow {
                // Rewind btn
                MyIconButton(
                    iconId = R.drawable.ic_skip_previous,
                    description = stringResource(R.string.rewind_btn_description),
                    size = 40.dp,
                    tint = AppTheme.colors.onPrimary,
                    onClick = { vm.onPreviousVerseClick() }
                )

                // Play/Pause btn
                MyIconPlayerBtn(
                    state = st.playerState,
                    size = 50.dp,
                    padding = 5.dp,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    tint = AppTheme.colors.onPrimary,
                    onClick = { vm.onPlayPauseClick() }
                )

                // Fast Forward btn
                MyIconButton(
                    iconId = R.drawable.ic_skip_next,
                    description = stringResource(R.string.fast_forward_btn_description),
                    size = 40.dp,
                    tint = AppTheme.colors.onPrimary,
                    onClick = { vm.onNextVerseClick() }
                )
            }

            // Preference btn
            MyIconButton(
                iconId = R.drawable.ic_preferences,
                description = stringResource(R.string.settings),
                tint = AppTheme.colors.onPrimary,
                size = 44.dp,
                onClick = { vm.onSettingsClick() }
            )
        }
    }
}

@Composable
private fun PageContent(
    pageVerses: List<Verse>,
    viewType: QuranViewType,
    selectedVerse: Verse,
    trackedAyaId: Int,
    textSize: Int,
    language: Language,
    theme: Theme,
    pagerState: PagerState,
    padding: PaddingValues,
    scrollTo: Float,
    onScrolled: () -> Unit,
    setScrollState: (ScrollState) -> Unit,
    onPageChange: (Int, Int) -> Unit,
    buildPage: (Int) -> List<Verse>,
    onSuraHeaderGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit,
    onAyaGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit,
    onAyaScreenClick: (Int, Int) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) { pageIdx ->
        val isCurrentPage = pageIdx == pagerState.currentPage
        val scrollState = rememberScrollState()

        if (isCurrentPage) setScrollState(scrollState)

        onPageChange(pagerState.currentPage, pageIdx)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val verses =
                if (isCurrentPage) pageVerses
                else buildPage(pageIdx + 1)

            if (viewType == LIST) {
                ListItems(
                    verses = verses,
                    isCurrentPage = isCurrentPage,
                    selectedVerse = selectedVerse,
                    trackedAyaId = trackedAyaId,
                    textSize = textSize,
                    language = language,
                    theme = theme,
                    onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned,
                    onAyaGloballyPositioned = onAyaGloballyPositioned,
                    onAyaScreenClick = onAyaScreenClick
                )
            }
            else {
                PageItems(
                    verses = verses,
                    isCurrentPage = isCurrentPage,
                    selectedVerse = selectedVerse,
                    trackedAyaId = trackedAyaId,
                    textSize = textSize,
                    theme = theme,
                    onAyaScreenClick = onAyaScreenClick,
                    onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned
                )
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
    verses: List<Verse>,
    isCurrentPage: Boolean,
    selectedVerse: Verse,
    trackedAyaId: Int,
    textSize: Int,
    theme: Theme,
    onAyaScreenClick: (Int, Int) -> Unit,
    onSuraHeaderGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit
) {
    var sequenceText = StringBuilder()
    var sequence = mutableListOf<Verse>()
    var lastSura = verses[0].suraNum

    if (verses[0].num == 1) {
        NewSura(
            verse = verses[0],
            isCurrentPage = isCurrentPage,
            textSize = textSize,
            theme = theme,
            onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned
        )
    }

    for (aya in verses) {
        if (aya.suraNum != lastSura) {
            PageItem(
                text = sequenceText.toString(),
                sequence = sequence,
                selectedVerse = selectedVerse,
                trackedAyaId = trackedAyaId,
                textSize = textSize,
                onAyaScreenClick = onAyaScreenClick
            )

            if (aya.num == 1) {
                NewSura(
                    verse = aya,
                    isCurrentPage = isCurrentPage,
                    textSize = textSize,
                    theme = theme,
                    onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned
                )
            }

            sequenceText = StringBuilder()
            sequence = mutableListOf()
        }

        aya.start = sequenceText.length
        val ayaText =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                aya.text!!
            else {  // reverse aya number if below android 13 (because of a bug)
                val text = aya.text!!
                val reversedNum = text
                    .split(" ")
                    .last()
                    .dropLast(1)
                    .reversed()
                val rest = text.dropLast(reversedNum.length + 1)
                "$rest$reversedNum "
            }
        sequenceText.append(ayaText)
        aya.end = sequenceText.length
        sequence.add(aya)

        Log.d(Global.TAG, "ID: ${aya.id}, Start: ${aya.start}, End: ${aya.end}")

        lastSura = aya.suraNum
    }
    PageItem(
        text = sequenceText.toString(),
        sequence = sequence,
        selectedVerse = selectedVerse,
        trackedAyaId = trackedAyaId,
        textSize = textSize,
        onAyaScreenClick = onAyaScreenClick
    )
}

@Composable
private fun PageItem(
    text: String,
    sequence: List<Verse>,
    selectedVerse: Verse,
    trackedAyaId: Int,
    textSize: Int,
    onAyaScreenClick: (Int, Int) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(text)

        for (seqAya in sequence) {
            addStyle(
                style = SpanStyle(
                    color =
                        if (selectedVerse == seqAya) AppTheme.colors.highlight
                        else if (trackedAyaId == seqAya.id) AppTheme.colors.track
                        else AppTheme.colors.strongText
                ),
                start = seqAya.start,
                end = seqAya.end
            )
        }
    }

    PageViewScreen(
        annotatedString = annotatedString,
        firstVerse = sequence[0],
        textSize = textSize,
        onAyaScreenClick = onAyaScreenClick
    )
}

@Composable
private fun ListItems(
    verses: List<Verse>,
    isCurrentPage: Boolean,
    selectedVerse: Verse,
    trackedAyaId: Int,
    textSize: Int,
    language: Language,
    theme: Theme,
    onSuraHeaderGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit,
    onAyaGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit,
    onAyaScreenClick: (Int, Int) -> Unit
) {
    for (aya in verses) {
        if (aya.num == 1)
            NewSura(
                verse = aya,
                isCurrentPage = isCurrentPage,
                textSize = textSize,
                theme = theme,
                onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned
            )

        val ayaText =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                aya.text!!
            else {  // reverse aya number if below android 13 (because of a bug)
                val text = aya.text!!
                val reversedNum = text
                    .split(" ")
                    .last()
                    .dropLast(1)
                    .reversed()
                val rest = text.dropLast(reversedNum.length + 1)
                "$rest$reversedNum "
            }

        ListViewScreen(
            annotatedString = AnnotatedString(ayaText),
            verse = aya,
            isCurrentPage = isCurrentPage,
            textSize = textSize,
            selectedVerse = selectedVerse,
            trackedAyaId = trackedAyaId,
            onAyaGloballyPositioned = onAyaGloballyPositioned,
            onAyaScreenClick = onAyaScreenClick
        )

        if (language != Language.ARABIC) {
            MyText(
                text = aya.translation!!,
                fontSize = (textSize - 5).sp,
                modifier = Modifier.padding(6.dp)
            )
        }

        if (aya.num != verses.last().num)
            MyHorizontalDivider()
    }
}

@Composable
private fun PageViewScreen(
    annotatedString: AnnotatedString,
    firstVerse: Verse,
    textSize: Int,
    onAyaScreenClick: (Int, Int) -> Unit
) {
    ClickableText(
        text = annotatedString,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
        style = TextStyle(
            fontFamily = uthmanic,
            fontSize = textSize.sp,
            color = AppTheme.colors.strongText,
            textAlign = TextAlign.Center
        ),
        onClick = { offset -> onAyaScreenClick(firstVerse.id, offset) }
    )
}

@Composable
private fun ListViewScreen(
    annotatedString: AnnotatedString,
    verse: Verse,
    isCurrentPage: Boolean,
    selectedVerse: Verse,
    trackedAyaId: Int,
    textSize: Int,
    onAyaGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit,
    onAyaScreenClick: (Int, Int) -> Unit
) {
    ClickableText(
        text = annotatedString,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .onGloballyPositioned { layoutCoordinates ->
                onAyaGloballyPositioned(
                    verse,
                    isCurrentPage,
                    layoutCoordinates
                )
            },
        style = TextStyle(
            fontFamily = uthmanic,
            fontSize = textSize.sp,
            color =
                if (selectedVerse == verse) AppTheme.colors.highlight
                else if (trackedAyaId == verse.id) AppTheme.colors.track
                else AppTheme.colors.strongText,
            textAlign = TextAlign.Center
        ),
        onClick = { offset ->
            onAyaScreenClick(verse.id, offset)
        }
    )
}

@Composable
private fun NewSura(
    verse: Verse,
    isCurrentPage: Boolean,
    textSize: Int,
    theme: Theme,
    onSuraHeaderGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit
) {
    SuraHeader(
        verse = verse,
        isCurrentPage = isCurrentPage,
        textSize = textSize,
        theme = theme,
        onGloballyPositioned = onSuraHeaderGloballyPositioned
    )

    // surat al-fatiha and At-Taubah
    if (verse.suraNum != 1 && verse.suraNum != 9) Basmalah(textSize)
}

@Composable
private fun SuraHeader(
    verse: Verse,
    isCurrentPage: Boolean,
    textSize: Int,
    theme: Theme,
    onGloballyPositioned: (Verse, Boolean, LayoutCoordinates) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height((textSize * 2.6).dp)
            .padding(top = 5.dp, bottom = 10.dp, start = 5.dp, end = 5.dp)
            .onGloballyPositioned { layoutCoordinates ->
                onGloballyPositioned(verse, isCurrentPage, layoutCoordinates)
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                if (theme == Theme.LIGHT) R.drawable.sura_header_light
                else R.drawable.sura_header
            ),
            contentDescription = verse.suraName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        MyText(
            text = verse.suraName,
            fontSize = (textSize + 2).sp,
            fontWeight = FontWeight.Bold,
            textColor = AppTheme.colors.strongText
        )
    }
}

@Composable
private fun Basmalah(textSize: Int) {
    MyText(
        text = stringResource(R.string.basmalah),
        modifier = Modifier.padding(bottom = 5.dp),
        fontSize = (textSize - 3).sp,
        fontWeight = FontWeight.Bold
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