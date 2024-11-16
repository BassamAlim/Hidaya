package bassamalim.hidaya.features.remembrances.reader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.InfoDialog
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyReadingBottomBar
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText

@Composable
fun RemembranceReaderScreen(viewModel: RemembranceReaderViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = state.title,
        bottomBar = {
            MyReadingBottomBar(
                textSize = state.textSize,
                onSeek = viewModel::onTextSizeSliderChange
            )
        }
    ) {
        // remembrances list
        MyLazyColumn(
            modifier = Modifier.padding(it),
            lazyList = {
                items(state.items) { item ->
                    RemembrancePassageCard(
                        passage = item,
                        textSize = state.textSize,
                        onInfoClick = viewModel::onInfoClick
                    )
                }
            }
        )

        // source dialog
        InfoDialog(
            title = stringResource(R.string.reference),
            text = state.infoDialogText,
            shown = state.isInfoDialogShown,
            onDismiss = viewModel::onInfoDialogDismiss
        )
    }
}

@Composable
private fun RemembrancePassageCard(
    passage: RemembrancePassage,
    textSize: Float,
    onInfoClick: (String) -> Unit
) {
    val textSizeMargin = 15

    MySurface {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1F),
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

                // virtue
                if (passage.isVirtueAvailable) {
                    HorizontalDivider()

                    MyText(
                        text = passage.virtue!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = (textSize + textSizeMargin - 8).sp,
                        textColor = MaterialTheme.colorScheme.primary
                    )
                }

                // reference
                if (passage.isReferenceAvailable) {
                    HorizontalDivider()

                    MyIconButton(
                        iconId = R.drawable.ic_help,
                        description = stringResource(R.string.source_btn_description),
                        modifier = Modifier.padding(2.dp),
                        innerPadding = 6.dp,
                        tint = MaterialTheme.colorScheme.onSurface,
                        size = 26.dp,
                        onClick = { onInfoClick(passage.reference!!) }
                    )
                }
            }

            // repetition
            if (passage.isRepetitionAvailable) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                MyText(
                    text = passage.repetition,
                    modifier = Modifier
                        .padding(10.dp)
                        .widthIn(10.dp, 100.dp)
                        .align(Alignment.CenterVertically),
                    fontSize = (textSize + textSizeMargin).sp,
                    textColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}