package bassamalim.hidaya.view

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import bassamalim.hidaya.R
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.viewmodel.QuizLobbyVM

@Composable
fun QuizLobbyUI(
    navController: NavController = rememberNavController(),
    viewModel: QuizLobbyVM = hiltViewModel()
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
                viewModel.onStartQuizClick(navController)
            }
        }
    }
}