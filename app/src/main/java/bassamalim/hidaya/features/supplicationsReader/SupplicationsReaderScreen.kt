package bassamalim.hidaya.features.supplicationsReader

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
import androidx.compose.material.Divider
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
import bassamalim.hidaya.core.models.RemembrancePassage
import bassamalim.hidaya.core.ui.components.InfoDialog
import bassamalim.hidaya.core.ui.components.MyIconButton
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyReadingBottomBar
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.AppTheme

@Composable
fun AthkarViewerUI(
    vm: SupplicationsReaderViewModel
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = st.title,
        bottomBar = {
            MyReadingBottomBar(
                textSize = st.textSize,
                onSeek = { vm.onTextSizeChange(it) }
            )
        }
    ) {
        // athkar list
        MyLazyColumn(
            modifier = Modifier.padding(it),
            lazyList = {
                items(st.items) { item ->
                    ThikrCard(
                        vm = vm,
                        remembrancePassage = item,
                        textSize = st.textSize
                    )
                }
            }
        )

        // source dialog
        InfoDialog(
            title = stringResource(R.string.reference),
            text = st.infoDialogText,
            shown = st.infoDialogShown,
            onDismiss = { vm.onInfoDialogDismiss() }
        )
    }
}

@Composable
private fun ThikrCard(
    vm: SupplicationsReaderViewModel,
    remembrancePassage: RemembrancePassage,
    textSize: Float
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
                if (vm.shouldShowTitle(remembrancePassage)) {
                    MyText(
                        text = remembrancePassage.title!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = (textSize + textSizeMargin).sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // text
                MyText(
                    text = remembrancePassage.text,
                    modifier = Modifier.padding(10.dp),
                    fontSize = (textSize + textSizeMargin).sp,
                    textColor = AppTheme.colors.strongText
                )

                // translation
                if (vm.shouldShowTranslation(remembrancePassage)) {
                    MyText(
                        text = remembrancePassage.textTranslation!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = (textSize + textSizeMargin).sp
                    )
                }

                // fadl
                if (vm.shouldShowFadl(remembrancePassage)) {
                    Divider()

                    MyText(
                        text = remembrancePassage.fadl!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = (textSize + textSizeMargin - 8).sp,
                        textColor = AppTheme.colors.accent
                    )
                }

                // reference
                if (vm.shouldShowReference(remembrancePassage)) {
                    Divider()

                    MyIconButton(
                        iconId = R.drawable.ic_help,
                        description = stringResource(R.string.source_btn_description),
                        modifier = Modifier.padding(2.dp),
                        innerPadding = 6.dp,
                        tint = AppTheme.colors.text,
                        size = 26.dp
                    ) {
                        vm.showInfoDialog(remembrancePassage.reference!!)
                    }
                }
            }

            // repetition
            if (vm.shouldShowRepetition(remembrancePassage)) {
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                MyText(
                    text = remembrancePassage.repetition,
                    modifier = Modifier
                        .padding(10.dp)
                        .widthIn(10.dp, 100.dp)
                        .align(Alignment.CenterVertically),
                    fontSize = (textSize + textSizeMargin).sp,
                    textColor = AppTheme.colors.accent
                )
            }
        }
    }
}