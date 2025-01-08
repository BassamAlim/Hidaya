package bassamalim.hidaya.features.remembrances.reader.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.ReaderBottomBar

@Composable
fun RemembranceReaderScreen(viewModel: RemembranceReaderViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = state.title,
        bottomBar = {
            ReaderBottomBar(
                textSize = state.textSize,
                onSeek = viewModel::onTextSizeSliderChange
            )
        }
    ) { padding ->
        // remembrances list
        MyLazyColumn(
            modifier = Modifier.padding(padding),
            lazyList = {
                items(state.items) { item ->
                    RemembrancePassageCard(
                        passage = item,
                        textSize = state.textSize,
                        onRepetitionClick = viewModel::onRepetitionClick
                    )
                }
            }
        )
    }
}

@Composable
private fun RemembrancePassageCard(
    passage: RemembrancePassage,
    textSize: Float,
    onRepetitionClick: (Int) -> Unit
) {
    val textSizeMargin = 10
    var expandedState by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(if (expandedState) 180f else 0f, label = "")

    MySurface(
        Modifier.animateContentSize(
            animationSpec = TweenSpec(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // title
            if (passage.isTitleAvailable) {
                MyText(
                    text = passage.title!!,
                    modifier = Modifier.padding(10.dp),
                    fontSize = (textSize + textSizeMargin).sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // text
            MyText(
                text = passage.text,
                modifier = Modifier.padding(10.dp),
                fontSize = (textSize + textSizeMargin).sp,
                textColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // translation
            if (passage.isTranslationAvailable) {
                MyText(
                    text = passage.translation!!,
                    modifier = Modifier.padding(10.dp),
                    fontSize = (textSize + textSizeMargin).sp
                )
            }

            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // repetition
                FilledTonalButton(
                    onClick = { onRepetitionClick(passage.id) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp)
                ) {
                    MyText(
                        text = passage.repetitionText,
                        fontSize = (textSize + textSizeMargin).sp,
                        textColor =
                            if (passage.repetitionTotal != null
                                && passage.repetitionTotal != passage.repetitionCurrent)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                    )
                }

                FilledTonalIconButton(
                    onClick = { expandedState = !expandedState },
                    modifier = Modifier
                        .weight(1f)
                        .height(IntrinsicSize.Min)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.title_more),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            if (expandedState) {
                // virtue
                if (passage.isVirtueAvailable) {
                    HorizontalDivider()

                    MyText(
                        text = stringResource(R.string.virtue),
                        modifier = Modifier.padding(top = 10.dp),
                        fontSize = (textSize + textSizeMargin - 8).sp,
                        fontWeight = FontWeight.Bold
                    )

                    MyText(
                        text = passage.virtue!!,
                        modifier = Modifier.padding(bottom = 10.dp),
                        fontSize = (textSize + textSizeMargin - 8).sp
                    )
                }

                // reference
                if (passage.isReferenceAvailable) {
                    HorizontalDivider()

                    MyText(
                        text = stringResource(R.string.reference),
                        modifier = Modifier.padding(top = 10.dp),
                        fontSize = (textSize + textSizeMargin - 8).sp,
                        fontWeight = FontWeight.Bold
                    )

                    MyText(
                        text = passage.reference!!,
                        modifier = Modifier.padding(bottom = 10.dp),
                        fontSize = (textSize + textSizeMargin - 8).sp
                    )
                }
            }
        }
    }
}