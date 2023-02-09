package bassamalim.hidaya.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.components.RadioGroup
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.Grey
import bassamalim.hidaya.viewmodel.QuizVM

@Composable
fun QuizUI(
    nc: NavController = rememberNavController(),
    vm: QuizVM = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    MyScaffold(
        title = state.questionNumText
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(1.dp, 200.dp)
                    .verticalScroll(rememberScrollState())
                    .background(AppTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                MyText(
                    text = state.question,
                    fontSize = 28.sp,
                    textColor = AppTheme.colors.onPrimary,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)
                )
            }

            RadioGroup(
                options = state.answers,
                selection = state.selection,
                modifier = Modifier
                    .heightIn(1.dp, 400.dp)
                    .verticalScroll(rememberScrollState())
            ) { index ->
                vm.answered(index, nc)
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1F, false)
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MyButton(
                    text = stringResource(R.string.previous_question),
                    textColor =
                    if (state.prevBtnEnabled) Grey
                    else AppTheme.colors.text,
                    innerPadding = PaddingValues(10.dp)
                ) {
                    vm.previousQ()
                }

                MyButton(
                    text = stringResource(state.nextBtnTextResId),
                    modifier = Modifier.sizeIn(maxWidth = 175.dp),
                    textColor =
                        if (state.nextBtnEnabled) AppTheme.colors.text
                        else Grey,
                    innerPadding = PaddingValues(10.dp)
                ) {
                    vm.nextQ(nc)
                }
            }
        }
    }
}