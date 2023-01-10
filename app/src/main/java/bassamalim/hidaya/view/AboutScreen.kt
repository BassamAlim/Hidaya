package bassamalim.hidaya.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyHorizontalDivider
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.viewmodel.AboutVM

@Composable
fun AboutUI(
    navController: NavController = rememberNavController(),
    viewModel: AboutVM = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    MyScaffold(stringResource(R.string.about)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 5.dp)
        ) {
            MyText(
                text = stringResource(R.string.thanks),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 15.dp, bottom = 20.dp)
                    .align(Alignment.CenterHorizontally)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        viewModel.onTitleClick()
                    }
            )

            Column(
                Modifier
                    .weight(1F)
                    .verticalScroll(rememberScrollState())
            ) {
                Source(R.string.quran_source)
                MyHorizontalDivider()
                Source(R.string.tafseer_source)
                MyHorizontalDivider()
                Source(R.string.hadeeth_source)
                MyHorizontalDivider()
                Source(R.string.athkar_source)
                MyHorizontalDivider()
                Source(R.string.quiz_source)
            }

            AnimatedVisibility(
                visible = state.isDevModeOn,
                enter = expandVertically()
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MyButton(
                        stringResource(R.string.rebuild_database),
                        Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        viewModel.rebuildDatabase()
                    }

                    MyButton(
                        stringResource(R.string.quick_update),
                        Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        viewModel.quickUpdate()
                    }

                    MyText(
                        state.lastDailyUpdate,
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Source(textResId: Int) {
    MyText(
        text = stringResource(textResId),
        modifier = Modifier.padding(10.dp),
        fontSize = 22.sp,
        textAlign = TextAlign.Start
    )
}