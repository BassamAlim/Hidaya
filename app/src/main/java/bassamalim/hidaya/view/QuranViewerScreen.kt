package bassamalim.hidaya.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.enum.QViewType.*
import bassamalim.hidaya.models.Ayah
import bassamalim.hidaya.state.QuranViewerState
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.nsp
import bassamalim.hidaya.ui.theme.uthmanic
import bassamalim.hidaya.utils.LangUtils.translateNums
import bassamalim.hidaya.viewmodel.QuranViewerVM
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun QuranViewerUI(
    navController: NavController = rememberNavController(),
    viewModel: QuranViewerVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    DisposableEffect(key1 = viewModel) {
        viewModel.onStart()
        onDispose { viewModel.onStop() }
    }

    MyScaffold(
        title = "",
        backgroundColor = AppTheme.colors.quranBG,
        topBar = {
            TopAppBar(
                backgroundColor = AppTheme.colors.primary,
                elevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MyText(
                        text = "${stringResource(R.string.sura)} ${state.suraName}",
                        fontSize = 18.nsp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        textColor = AppTheme.colors.onPrimary
                    )

                    MyText(
                        text = "${stringResource(R.string.page)} " +
                                translateNums(
                                    viewModel.numeralsLanguage,
                                    state.pageNum.toString()
                                ),
                        fontSize = 18.nsp,
                        fontWeight = FontWeight.Bold,
                        textColor = AppTheme.colors.onPrimary
                    )

                    MyText(
                        text = "${stringResource(R.string.juz)} " +
                                translateNums(
                                    viewModel.numeralsLanguage,
                                    state.juzNum.toString()
                                ),
                        fontSize = 18.nsp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        textColor = AppTheme.colors.onPrimary
                    )
                }
            }
        },
        bottomBar = {
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
                    MyIconBtn(
                        iconId =
                            if (state.isBookmarked) R.drawable.ic_bookmarked
                            else R.drawable.ic_bookmark,
                        description = stringResource(R.string.bookmark_page_button_description),
                        tint = AppTheme.colors.onPrimary,
                        size = 40.dp
                    ) {
                        viewModel.onBookmarkClick()
                    }

                    Row {
                        MyImageButton(
                            imageResId = R.drawable.ic_aya_backward,
                            description = stringResource(R.string.rewind_btn_description)
                        ) {
                            viewModel.onRewindClick()
                        }

                        MyPlayerBtn(
                            state = state.playerState,
                            size = 50.dp,
                            padding = 5.dp,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            viewModel.onPlayPauseClick()
                        }

                        MyImageButton(
                            imageResId = R.drawable.ic_aya_forward,
                            description = stringResource(R.string.fast_forward_btn_description)
                        ) {
                            viewModel.onFastForwardClick()
                        }
                    }

                    // Preferences btn
                    MyIconBtn(
                        iconId = R.drawable.ic_preferences,
                        description = stringResource(R.string.settings),
                        tint = AppTheme.colors.onPrimary,
                        size = 44.dp
                    ) {
                        viewModel.onSettingsClick()
                    }
                }
            }
        }
    ) {
        val pagerState = rememberPagerState(viewModel.initialPage-1)
        HorizontalPagerScreen(
            count = 604,
            pagerState = pagerState,
            modifier = Modifier.padding(it)
        ) { page ->
            val isCurrentPage = page == pagerState.currentPage

            val scrollState = rememberScrollState()

            viewModel.onPageChange(pagerState.currentPage, page)

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val ayas =
                    if (isCurrentPage) state.ayas
                    else viewModel.buildPage(page + 1)

                if (state.viewType == List) ListItems(ayas, isCurrentPage, viewModel, state)
                else PageItems(ayas, isCurrentPage, viewModel, state)

                if (page == pagerState.currentPage)
                    LaunchedEffect(null) {
                        scrollState.animateScrollTo(viewModel.scrollTo.toInt())
                        viewModel.onScrolled()
                    }
            }
        }
    }

    TutorialDialog(
        shown = state.tutorialDialogShown,
        textResId = R.string.quran_tips,
        onDismiss = { doNotShowAgain -> viewModel.onTutorialDialogDismiss(doNotShowAgain) }
    )

    InfoDialog(
        shown = state.infoDialogShown,
        title = stringResource(R.string.tafseer),
        text = state.infoDialogText
    )

    QuranSettingsDialog(
        startState = state,
        pref = viewModel.pref,
        reciterNames = viewModel.reciterNames,
        onDone = viewModel::onSettingsDialogDone  // :: gives the reference to the function
    )
}

@Composable
private fun PageItems(
    ayas: List<Ayah>,
    isCurrentPage: Boolean,
    viewModel: QuranViewerVM,
    state: QuranViewerState
) {
    var text = StringBuilder()
    var sequence = ArrayList<Ayah>()
    var lastSura = ayas[0].surahNum

    NewSura(ayas[0], isCurrentPage, viewModel, state)
    for (aya in ayas) {
        if (aya.surahNum != lastSura) {
            PageItem(text = text.toString(), sequence = sequence, viewModel, state)

            NewSura(aya, isCurrentPage, viewModel, state)

            text = StringBuilder()
            sequence = ArrayList()
        }

        aya.start = text.length
        text.append(aya.text)
        aya.end = text.length
        sequence.add(aya)

        lastSura = aya.surahNum
    }
    PageItem(text = text.toString(), sequence = sequence, viewModel, state)
}

@Composable
private fun PageItem(
    text: String,
    sequence: List<Ayah>,
    viewModel: QuranViewerVM,
    state: QuranViewerState
) {
    val annotatedString = buildAnnotatedString {
        append(text)

        for (seqAya in sequence) {
            addStyle(
                style = SpanStyle(
                    color =
                        if (viewModel.selected.value == seqAya) AppTheme.colors.highlight
                        else if (viewModel.tracked.value == seqAya) AppTheme.colors.track
                        else AppTheme.colors.strongText
                ),
                start = seqAya.start,
                end = seqAya.end
            )
        }
    }

    Screen(annotatedString = annotatedString, ayaId = sequence[0].id, viewModel, state)
}

@Composable
private fun ListItems(
    ayas: List<Ayah>,
    isCurrentPage: Boolean,
    viewModel: QuranViewerVM,
    state: QuranViewerState
) {
    for (aya in ayas) {
        NewSura(aya, isCurrentPage, viewModel, state)

        val annotatedString = AnnotatedString(aya.text!!)
        Screen(annotatedString, aya.id, viewModel, state)

        MyText(
            text = aya.translation!!,
            fontSize = (state.textSize - 5).sp,
            modifier = Modifier.padding(6.dp)
        )

        MyHorizontalDivider()
    }
}

@Composable
private fun Screen(
    annotatedString: AnnotatedString,
    ayaId: Int,
    viewModel: QuranViewerVM,
    state: QuranViewerState
) {
    ClickableText(
        text = annotatedString,
        style = TextStyle(
            fontFamily = uthmanic,
            fontSize = state.textSize.sp,
            color = AppTheme.colors.strongText,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
        onClick = { offset ->
            viewModel.onAyaClick(ayaId, offset)
        }
    )
}

@Composable
private fun NewSura(
    aya: Ayah,
    isCurrentPage: Boolean,
    viewModel: QuranViewerVM,
    state: QuranViewerState
) {
    if (aya.ayahNum == 1) {
        SuraHeader(aya, isCurrentPage, viewModel, state)
        // surat al-fatiha and At-Taubah
        if (aya.surahNum != 1 && aya.surahNum != 9) Basmalah(state)
    }
}

@Composable
private fun SuraHeader(
    aya: Ayah,
    isCurrentPage: Boolean,
    viewModel: QuranViewerVM,
    state: QuranViewerState
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height((state.textSize * 2.6).dp)
            .padding(top = 5.dp, bottom = 10.dp, start = 5.dp, end = 5.dp)
            .onGloballyPositioned { layoutCoordinates ->
                viewModel.onSuraHeaderGloballyPositioned(aya, isCurrentPage, layoutCoordinates)
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.surah_header),
            contentDescription = aya.surahName,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        MyText(
            text = aya.surahName,
            fontSize = (state.textSize + 2).sp,
            fontWeight = FontWeight.Bold,
            textColor = AppTheme.colors.onPrimary
        )
    }
}

@Composable
private fun Basmalah(state: QuranViewerState) {
    MyText(
        text = stringResource(R.string.basmalah),
        fontSize = (state.textSize - 3).sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 5.dp)
    )
}