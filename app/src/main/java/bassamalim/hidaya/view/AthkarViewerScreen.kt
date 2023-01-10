package bassamalim.hidaya.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.models.Thikr
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.viewmodel.AthkarViewerVM

@Composable
fun AthkarViewerUI(
    navController: NavController = rememberNavController(),
    viewModel: AthkarViewerVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MyScaffold(
        title = state.title,
        bottomBar = {
            MyReadingBottomBar(
                textSizeState = state.textSize
            ) {
                viewModel.onTextSizeChange(it)
            }
        }
    ) {
        MyLazyColumn(
            modifier = Modifier.padding(it),
            lazyList = {
                items(state.items) { item ->
                    ThikrCard(
                        viewModel = viewModel,
                        thikr = item,
                        textSize = state.textSize
                    )
                }
            }
        )

        InfoDialog(
            title = stringResource(R.string.reference),
            text = state.infoDialogText,
            shown = state.infoDialogShown
        ) {
            viewModel.onInfoDialogDismiss()
        }
    }
}

@Composable
private fun ThikrCard(
    viewModel: AthkarViewerVM,
    thikr: Thikr,
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
                if (viewModel.shouldShowTitle(thikr))
                    MyText(
                        text = thikr.title!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = (textSize + textSizeMargin).sp,
                        fontWeight = FontWeight.Bold
                    )

                MyText(
                    text = thikr.text,
                    modifier = Modifier.padding(10.dp),
                    fontSize = (textSize + textSizeMargin).sp,
                    textColor = AppTheme.colors.strongText
                )

                if (viewModel.shouldShowTranslation(thikr))
                    MyText(
                        text = thikr.textTranslation!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = (textSize + textSizeMargin).sp
                    )

                if (viewModel.shouldShowFadl(thikr)) {
                    Divider()

                    MyText(
                        text = thikr.fadl!!,
                        modifier = Modifier.padding(10.dp),
                        fontSize = (textSize + textSizeMargin - 8).sp,
                        textColor = AppTheme.colors.accent
                    )
                }

                if (viewModel.shouldShowReference(thikr)) {
                    Divider()

                    MyIconBtn(
                        iconId = R.drawable.ic_help,
                        description = stringResource(R.string.source_btn_description),
                        tint = AppTheme.colors.text,
                        size = 26.dp
                    ) {
                        viewModel.showInfoDialog(thikr.reference!!)
                    }
                }
            }

            if (viewModel.shouldShowRepetition(thikr)) {
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                MyText(
                    text = thikr.repetition,
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