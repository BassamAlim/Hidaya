package bassamalim.hidaya.features.quizLobby

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.theme.AppTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizLobbyUI(
    nc: NavController = rememberAnimatedNavController(),
    vm: QuizLobbyVM
) {
    MyScaffold(stringResource(R.string.quiz_title)) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyButton(
                text = stringResource(R.string.start_quiz),
                innerPadding = PaddingValues(vertical = 10.dp, horizontal = 25.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textColor = AppTheme.colors.accent
            ) {
                vm.onStartQuizClick(nc)
            }
        }
    }
}