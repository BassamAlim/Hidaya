package bassamalim.hidaya.features.quranViewer

import android.app.Activity
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
import bassamalim.hidaya.core.enums.QuranViewTypes.*
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.models.Aya
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.components.*
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.nsp
import bassamalim.hidaya.core.ui.theme.uthmanic
import bassamalim.hidaya.core.utils.LangUtils.translateNums
import bassamalim.hidaya.features.quranSettings.QuranSettingsDlg
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun QuranViewerUI(
    vm: QuranViewerVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity
    val pagerState = rememberPagerState(vm.initialPageNum-1)
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(key1 = vm) {
        vm.onStart(activity, pagerState, coroutineScope)
        onDispose { vm.onStop() }
    }

    MyScaffold(
        title = "",
        backgroundColor = AppTheme.colors.quranBG,
        topBar = { TopBar(vm, st) },
        bottomBar = { BottomBar(vm, st) }
    ) {
        PageContent(
            vm, st,
            pagerState = pagerState,
            padding = it
        )
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
private fun TopBar(
    vm: QuranViewerVM,
    st: QuranViewerState
) {
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
            // Sura name
            MyText(
                text = "${stringResource(R.string.sura)} ${st.suraName}",
                fontSize = 18.nsp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                textColor = AppTheme.colors.onPrimary
            )

            // Page number
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

            // Juz number
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
}

@Composable
private fun BottomBar(
    vm: QuranViewerVM,
    st: QuranViewerState
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
                    onClick = { vm.onPreviousAyaClk() }
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
                    onClick = { vm.onNextAyaClk() }
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

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun PageContent(
    vm: QuranViewerVM,
    st: QuranViewerState,
    pagerState: PagerState,
    padding: PaddingValues
) {

    HorizontalPagerScreen(
        count = Global.QURAN_PAGES,
        pagerState = pagerState,
        modifier = Modifier.padding(padding)
    ) { pageIdx ->
        val isCurrentPage = pageIdx == pagerState.currentPage
        val scrollState = rememberScrollState()

        if (isCurrentPage) vm.setScrollState(scrollState)

        vm.onPageChange(pagerState.currentPage, pageIdx)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val ayat =
                if (isCurrentPage) st.pageAyat
                else vm.buildPage(pageIdx + 1)

            if (st.viewType == List) ListItems(ayat, isCurrentPage, vm, st)
            else PageItems(ayat, isCurrentPage, vm, st)

            if (isCurrentPage && vm.scrollTo > 0f) {
                LaunchedEffect(null) {
                    scrollState.animateScrollTo(vm.scrollTo.toInt())
                    vm.onScrolled()
                }
            }
        }
    }
}

@Composable
private fun PageItems(
    ayat: List<Aya>,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    var text = StringBuilder()
    var sequence = ArrayList<Aya>()
    var lastSura = ayat[0].suraNum

    if (ayat[0].ayaNum == 1) NewSura(ayat[0], isCurrentPage, vm, st)

    for (aya in ayat) {
        if (aya.suraNum != lastSura) {
            PageItem(
                text = text.toString(),
                sequence = sequence,
                isCurrentPage = isCurrentPage,
                vm, st
            )

            if (aya.ayaNum == 1) NewSura(aya, isCurrentPage, vm, st)

            text = StringBuilder()
            sequence = ArrayList()
        }

        aya.start = text.length
        text.append(aya.text)
        aya.end = text.length
        sequence.add(aya)

        lastSura = aya.suraNum
    }
    PageItem(
        text = text.toString(),
        sequence = sequence,
        isCurrentPage = isCurrentPage,
        vm, st
    )
}

@Composable
private fun PageItem(
    text: String,
    sequence: List<Aya>,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    val annotatedString = buildAnnotatedString {
        append(text)

        for (seqAya in sequence) {
            addStyle(
                style = SpanStyle(
                    color =
                        if (st.selectedAya == seqAya) AppTheme.colors.highlight
                        else if (st.trackedAyaId == seqAya.id) AppTheme.colors.track
                        else AppTheme.colors.strongText
                ),
                start = seqAya.start,
                end = seqAya.end
            )
        }
    }

    PageViewScreen(
        annotatedString = annotatedString,
        aya = sequence[0],
        isCurrentPage = isCurrentPage,
        vm, st
    )
}

@Composable
private fun ListItems(
    ayat: List<Aya>,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    for (aya in ayat) {
        if (aya.ayaNum == 1)
            NewSura(aya, isCurrentPage, vm, st)

        val annotatedString = AnnotatedString(aya.text!!)
        ListViewScreen(annotatedString, aya, isCurrentPage, vm, st)

        if (vm.language != Language.ARABIC) {
            MyText(
                text = aya.translation!!,
                fontSize = (st.textSize - 5).sp,
                modifier = Modifier.padding(6.dp)
            )
        }

        if (aya.ayaNum != ayat.last().ayaNum)
            MyHorizontalDivider()
    }
}

@Composable
private fun PageViewScreen(
    annotatedString: AnnotatedString,
    aya: Aya,
    isCurrentPage: Boolean,
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
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp),
        onClick = { offset ->
            vm.onAyaScreenClick(aya.id, offset)
        }
    )
}

@Composable
private fun ListViewScreen(
    annotatedString: AnnotatedString,
    aya: Aya,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    ClickableText(
        text = annotatedString,
        style = TextStyle(
            fontFamily = uthmanic,
            fontSize = st.textSize.sp,
            color =
                if (st.selectedAya == aya) AppTheme.colors.highlight
                else if (st.trackedAyaId == aya.id) AppTheme.colors.track
                else AppTheme.colors.strongText,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .onGloballyPositioned { layoutCoordinates ->
                vm.onAyaGloballyPositioned(aya, isCurrentPage, layoutCoordinates)
            },
        onClick = { offset ->
            vm.onAyaScreenClick(aya.id, offset)
        }
    )
}

@Composable
private fun NewSura(
    aya: Aya,
    isCurrentPage: Boolean,
    vm: QuranViewerVM,
    st: QuranViewerState
) {
    SuraHeader(aya, isCurrentPage, vm, st)
    // surat al-fatiha and At-Taubah
    if (aya.suraNum != 1 && aya.suraNum != 9) Basmalah(st)
}

@Composable
private fun SuraHeader(
    aya: Aya,
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
                if (vm.theme == Theme.LIGHT) R.drawable.sura_header_light
                else R.drawable.sura_header
            ),
            contentDescription = aya.suraName,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        MyText(
            text = aya.suraName,
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