package bassamalim.hidaya.features.quiz

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.RadioGroup
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.Grey
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: QuizVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = st.questionNumText
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
                    text = st.question,
                    fontSize = 28.sp,
                    textColor = AppTheme.colors.onPrimary,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)
                )
            }

            RadioGroup(
                options = st.answers,
                selection = st.selection,
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
                        if (st.prevBtnEnabled) AppTheme.colors.text
                        else Grey,
                    innerPadding = PaddingValues(10.dp)
                ) {
                    vm.previousQ()
                }

                MyButton(
                    text = stringResource(st.nextBtnTextResId),
                    modifier = Modifier.sizeIn(maxWidth = 175.dp),
                    textColor =
                        if (st.nextBtnEnabled) AppTheme.colors.text
                        else Grey,
                    innerPadding = PaddingValues(10.dp)
                ) {
                    vm.nextQ(nc)
                }
            }
        }
    }
}