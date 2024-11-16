package bassamalim.hidaya.features.quiz.lobby.ui

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
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyScaffold
import androidx.compose.material3.MaterialTheme

@Composable
fun QuizLobbyScreen(viewModel: QuizLobbyViewModel) {
    MyScaffold(title = stringResource(R.string.quiz_title)) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyRectangleButton(
                text = stringResource(R.string.start_quiz),
                innerPadding = PaddingValues(vertical = 10.dp, horizontal = 25.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textColor = MaterialTheme.colorScheme.primary,
                onClick = viewModel::onStartQuizClick
            )
        }
    }
}