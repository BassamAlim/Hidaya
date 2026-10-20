package bassamalim.hidaya.features.quran.reader

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.QuranViewType
import bassamalim.hidaya.core.models.Verse
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.hafs_smart
import bassamalim.hidaya.core.ui.theme.uthmanic_hafs

@Composable
internal fun PageContent(
    viewType: QuranViewType,
    fillPage: Boolean,
    selectedVerse: Verse?,
    trackedVerseId: Int,
    textSize: Int,
    language: Language,
    pagerState: PagerState,
    scrollTo: Float,
    onScrolled: () -> Unit,
    scrollToVersePosition: Float?,
    onScrollToVerseConsumed: () -> Unit,
    onPageChange: (Int, Int) -> Unit,
    buildPage: (Int, Int?, Int, Color, Color, Color) -> List<Section>,
    buildListPage: (Int, Int?, Int, Color, Color, Color) -> List<Section>,
    buildMushafPage: (Int, Int?, Int, Color, Color, Color) -> List<Section?>,
    onSuraHeaderGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onVerseGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit,
    onContentTap: () -> Unit
) {
    val defaultColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val selectedVerseId = selectedVerse?.id

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { pageIdx ->
        val isCurrentPage = pageIdx == pagerState.currentPage
        val scrollState = rememberScrollState()

        LaunchedEffect(pagerState.currentPage) {
            onPageChange(pagerState.currentPage, pageIdx)
        }

        if (isCurrentPage) {
            LaunchedEffect(scrollToVersePosition) {
                scrollToVersePosition?.let { position ->
                    scrollState.animateScrollTo(position.toInt())
                    onScrollToVerseConsumed()
                }
            }
        }

        val columnModifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .pointerInput(Unit) { detectTapGestures { onContentTap() } }

        if (viewType == QuranViewType.PAGE && fillPage) {
            val lines = remember(pageIdx, selectedVerseId, trackedVerseId, defaultColor, primaryColor, tertiaryColor) {
                buildMushafPage(pageIdx + 1, selectedVerseId, trackedVerseId, defaultColor, primaryColor, tertiaryColor)
            }

            MushafPage(
                lines = lines,
                // The two opening pages are printed as short centered lines
                justify = pageIdx >= 2,
                isCurrentPage = isCurrentPage,
                onVersePointerInput = onVersePointerInput,
                onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned,
                onContentTap = onContentTap
            )
        }
        else Column(
            modifier = columnModifier,
            verticalArrangement =
                if (pageIdx == 0 || pageIdx == 1) Arrangement.Top
                else Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (viewType) {
                QuranViewType.PAGE -> {
                    val pageContent = remember(pageIdx, selectedVerseId, trackedVerseId, defaultColor, primaryColor, tertiaryColor) {
                        buildPage(pageIdx + 1, selectedVerseId, trackedVerseId, defaultColor, primaryColor, tertiaryColor)
                    }

                    PageItems(
                        sections = pageContent,
                        isCurrentPage = isCurrentPage,
                        textSize = textSize,
                        onVersePointerInput = onVersePointerInput,
                        onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned
                    )
                }
                QuranViewType.LIST -> {
                    val pageContent = remember(pageIdx, selectedVerseId, trackedVerseId, defaultColor, primaryColor, tertiaryColor) {
                        buildListPage(pageIdx + 1, selectedVerseId, trackedVerseId, defaultColor, primaryColor, tertiaryColor)
                    }

                    ListItems(
                        sections = pageContent,
                        isCurrentPage = isCurrentPage,
                        selectedVerse = selectedVerse,
                        trackedVerseId = trackedVerseId,
                        textSize = textSize,
                        language = language,
                        onSuraHeaderGloballyPositioned = onSuraHeaderGloballyPositioned,
                        onVerseGloballyPositioned = onVerseGloballyPositioned,
                        onVersePointerInput = onVersePointerInput
                    )
                }
            }

            if (isCurrentPage && scrollTo > 0f) {
                LaunchedEffect(scrollTo) {
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
    textSize: Int,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit,
    onSuraHeaderGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit
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
                PageViewScreen(
                    annotatedString = section.annotatedString,
                    textSize = textSize,
                    onVersePointerInput = onVersePointerInput
                )
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
    onVerseGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit
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
                    onVerseGloballyPositioned = onVerseGloballyPositioned,
                    onVersePointerInput = onVersePointerInput
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
private fun PageViewScreen(
    annotatedString: AnnotatedString,
    textSize: Int,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedString,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .pointerInput(Unit) {
                onVersePointerInput(this, layoutResult, annotatedString)
            },
        onTextLayout = { textLayoutResult ->
            layoutResult = textLayoutResult
        },
        style = TextStyle(
            fontFamily = hafs_smart,
            fontSize = textSize.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    )
}

/**
 * The page as printed: fixed line slots sharing the height, one font size for the page (the
 * largest at which its widest line fits), and each line's words spread across the full width.
 */
@Composable
private fun MushafPage(
    lines: List<Section?>,
    justify: Boolean,
    isCurrentPage: Boolean,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit,
    onSuraHeaderGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onContentTap: () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurface

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .pointerInput(Unit) { detectTapGestures { onContentTap() } },
        contentAlignment = Alignment.Center
    ) {
        val lineHeight = maxHeight / QuranPageBuilder.MUSHAF_LINES
        val density = LocalDensity.current
        val fontSize = remember(lines, constraints, density) {
            // Width grows linearly with font size, so measuring each line once at 100sp is enough
            val referenceStyle = TextStyle(fontFamily = uthmanic_hafs, fontSize = 100.sp)
            val widestLine = lines.filterIsInstance<MushafLineSection>().maxOfOrNull { line ->
                textMeasurer.measure(
                    text = line.words.joinToString(" ") { it.text },
                    style = referenceStyle,
                    softWrap = false
                ).size.width
            } ?: 1
            val widthFit = 100f * constraints.maxWidth / widestLine * 0.97f
            // ponytail: height ratio eyeballed for the KFGQPC font, tune if lines touch
            val heightFit = with(density) { (lineHeight * 0.55f).toSp().value }
            minOf(widthFit, heightFit).sp
        }
        val wordStyle = TextStyle(fontFamily = uthmanic_hafs, fontSize = fontSize, color = textColor)

        // Mushaf lines read right to left whatever the app language
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for (line in lines) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(lineHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        when (line) {
                            is MushafLineSection -> Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(unbounded = true),
                                horizontalArrangement =
                                    if (justify) Arrangement.SpaceBetween
                                    else Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (word in line.words) {
                                    MushafWord(
                                        word = word,
                                        style = wordStyle,
                                        onVersePointerInput = onVersePointerInput
                                    )
                                }
                            }
                            is SuraHeaderSection -> SuraHeader(
                                suraNum = line.suraNum,
                                suraName = line.suraName,
                                isCurrentPage = isCurrentPage,
                                textSize = fontSize.value.toInt(),
                                height = lineHeight,
                                onGloballyPositioned = onSuraHeaderGloballyPositioned
                            )
                            is BasmalahSection -> Basmalah(
                                textSize = fontSize.value.toInt(),
                                height = lineHeight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MushafWord(
    word: AnnotatedString,
    style: TextStyle,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = word,
        modifier = Modifier.pointerInput(Unit) {
            onVersePointerInput(this, layoutResult, word)
        },
        onTextLayout = { textLayoutResult ->
            layoutResult = textLayoutResult
        },
        style = style,
        softWrap = false,
        maxLines = 1
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
    onVerseGloballyPositioned: (Int, Boolean, LayoutCoordinates) -> Unit,
    onVersePointerInput: (PointerInputScope, TextLayoutResult?, AnnotatedString) -> Unit
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotatedString,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 6.dp)
            .onGloballyPositioned { layoutCoordinates ->
                onVerseGloballyPositioned(verseId, isCurrentPage, layoutCoordinates)
            }
            .pointerInput(Unit) {
                onVersePointerInput(this, layoutResult, annotatedString)
            },
        onTextLayout = { textLayoutResult ->
            layoutResult = textLayoutResult
        },
        style = TextStyle(
            fontFamily = hafs_smart,
            fontSize = textSize.sp,
            color =
                if (selectedVerse?.id == verseId) MaterialTheme.colorScheme.primary
                else if (trackedVerseId == verseId) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurface,
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
            // Gold frame, as in printed mushafs
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary)
        )

        MyText(
            text = "${stringResource(R.string.sura)} $suraName",
            fontSize = (textSize * 0.9).sp,
            color = MaterialTheme.colorScheme.onSurface,
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
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
    )
}
