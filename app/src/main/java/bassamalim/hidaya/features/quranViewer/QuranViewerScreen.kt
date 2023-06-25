package bassamalim.hidaya.features.quranViewer

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.QViewType.*
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.models.Ayah
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.core.ui.theme.uthmanic
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quranSettings.QuranSettingsDlg
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun QuranViewerUI(
    vm: QuranViewerVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    DisposableEffect(key1 = vm) {
        onDispose { vm.onStop() }
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
                        text = "${stringResource(R.string.sura)} ${st.suraName}",
                        fontSize = 18.nsp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        textColor = AppTheme.colors.onPrimary
                    )

                    MyText(
                        text = "${stringResource(R.string.page)} " +
                                translateNums(
                                    vm.numeralsLanguage,
                                    st.pageNum.toString()
                                ),
                        fontSize = 18.nsp,
                        fontWeight = FontWeight.Bold,
                        textColor = AppTheme.colors.onPrimary
                    )

                    MyText(
                        text = "${stringResource(R.string.juz)} " +
                                translateNums(
                                    vm.numeralsLanguage,
                                    st.juzNum.toString()
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
                            if (st.isBookmarked) R.drawable.ic_bookmarked
                            else R.drawable.ic_bookmark,
                        description = stringResource(R.string.bookmark_page_button_description),
                        tint = AppTheme.colors.onPrimary,
                        size = 40.dp
                    ) {
                        vm.onBookmarkClick()
                    }

                    Row {
                        MyImageButton(
                            imageResId = R.drawable.ic_aya_backward,
                            description = stringResource(R.string.rewind_btn_description)
                        ) {
                            vm.onRewindClick()
                        }

                        MyPlayerBtn(
                            state = st.playerState,
                            size = 50.dp,
                            padding = 5.dp,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            vm.onPlayPauseClick()
                        }

                        MyImageButton(
                            imageResId = R.drawable.ic_aya_forward,
                            description = stringResource(R.string.fast_forward_btn_description)
                        ) {
                            vm.onFastForwardClick()
                        }
                    }

                    // Preferences btn
                    MyIconBtn(
                        iconId = R.drawable.ic_preferences,
                        description = stringResource(R.string.settings),
                        tint = AppTheme.colors.onPrimary,
                        size = 44.dp
                    ) {
                        vm.onSettingsClick()
                    }
                }
            }
        }
    ) {
        val pagerState = rememberPagerState(vm.initialPage-1)
        HorizontalPagerScreen(
            count = Global.QURAN_PAGES,
            pagerState = pagerState,
            modifier = Modifier.padding(it)
        ) { page ->
            val isCurrentPage = page == pagerState.currentPage

            val scrollState = rememberScrollState()

            vm.onPageChange(pagerState.currentPage, page)

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val ayas =
                    if (isCurrentPage) st.ayas
                    else vm.buildPage(page + 1)

                if (st.viewType == List) ListItems(ayas, isCurrentPage, vm, st)
                else PageItems(ayas, isCurrentPage, vm, st)

                if (page == pagerState.currentPage) {
                    LaunchedEffect(null) {
                        scrollState.animateScrollTo(vm.scrollTo.toInt())
                        vm.onScrolled()
                    }
                }
            }
        }
    }

    TutorialDialog(
        shown = st.tutorialDialogShown,
        textResId = R.string.quran_tips,
        onDismiss = { doNotShowAgain -> vm.onTutorialDialogDismiss(doNotShowAgain) }
    )

    InfoDialog(
        shown = st.infoDialogShown,
        title = stringResource(R.string.tafseer),
        text = st.infoDialogText,
        onDismiss = vm::onInfoDialogDismiss  // :: gives the reference to the function
    )

    QuranSettingsDlg(
        vm = hiltViewModel(),
        shown = st.settingsDialogShown,
        mainOnDone = { vm.onSettingsDialogDismiss() }
    )
}

@Composable
private fun PageItems(
    ayas: List<Ayah>,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    var text = StringBuilder()
    var sequence = ArrayList<Ayah>()
    var lastSura = ayas[0].surahNum

    NewSura(ayas[0], isCurrentPage, vm, st)
    for (aya in ayas) {
        if (aya.surahNum != lastSura) {
            PageItem(text = text.toString(), sequence = sequence, vm, st)

            NewSura(aya, isCurrentPage, vm, st)

            text = StringBuilder()
            sequence = ArrayList()
        }

        aya.start = text.length
        text.append(aya.text)
        aya.end = text.length
        sequence.add(aya)

        lastSura = aya.surahNum
    }
    PageItem(text = text.toString(), sequence = sequence, vm, st)
}

@Composable
private fun PageItem(
    text: String,
    sequence: List<Ayah>,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    val annotatedString = buildAnnotatedString {
        append(text)

        for (seqAya in sequence) {
            addStyle(
                style = SpanStyle(
                    color =
                        if (vm.selected.value == seqAya) AppTheme.colors.highlight
                        else if (vm.tracked.value == seqAya) AppTheme.colors.track
                        else AppTheme.colors.strongText
                ),
                start = seqAya.start,
                end = seqAya.end
            )
        }
    }

    Screen(annotatedString = annotatedString, aya = sequence[0], vm, st)
}

@Composable
private fun ListItems(
    ayas: List<Ayah>,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    for (aya in ayas) {
        NewSura(aya, isCurrentPage, vm, st)

        val annotatedString = AnnotatedString(aya.text!!)
        Screen(annotatedString, aya, vm, st)

        if (vm.language != Language.ARABIC) {
            MyText(
                text = aya.translation!!,
                fontSize = (st.textSize - 5).sp,
                modifier = Modifier.padding(6.dp)
            )
        }

        MyHorizontalDivider()
    }
}

@Composable
private fun Screen(
    annotatedString: AnnotatedString,
    aya: Ayah,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    ClickableText(
        text = annotatedString,
        style = TextStyle(
            fontFamily = uthmanic,
            fontSize = st.textSize.sp,
            color = AppTheme.colors.strongText,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
        onClick = { offset ->
            vm.onAyaClick(aya.id, offset)
        }
    )
}

@Composable
private fun NewSura(
    aya: Ayah,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    if (aya.ayahNum == 1) {
        SuraHeader(aya, isCurrentPage, vm, st)
        // surat al-fatiha and At-Taubah
        if (aya.surahNum != 1 && aya.surahNum != 9) Basmalah(st)
    }
}

@Composable
private fun SuraHeader(
    aya: Ayah,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height((st.textSize * 2.6).dp)
            .padding(top = 5.dp, bottom = 10.dp, start = 5.dp, end = 5.dp)
            .onGloballyPositioned { layoutCoordinates ->
                vm.onSuraHeaderGloballyPositioned(aya, isCurrentPage, layoutCoordinates)
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                if (vm.theme == Theme.LIGHT) R.drawable.surah_header_light
                else R.drawable.surah_header
            ),
            contentDescription = aya.surahName,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        MyText(
            text = aya.surahName,
            fontSize = (st.textSize + 2).sp,
            fontWeight = FontWeight.Bold,
            textColor = AppTheme.colors.strongText
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