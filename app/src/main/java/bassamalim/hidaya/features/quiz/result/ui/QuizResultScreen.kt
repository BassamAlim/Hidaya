package bassamalim.hidaya.features.quiz.result.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyLazyColumn
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.theme.Negative
import bassamalim.hidaya.core.ui.theme.Positive

@Composable
fun QuizResultScreen(viewModel: QuizResultViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyScaffold(title = stringResource(R.string.quiz_result)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 5.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                MyText(
                    "${stringResource(R.string.your_score_is)} ${state.score}%",
                    Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            MyLazyColumn(
                lazyList = {
                    items(state.questions) { item ->
                        Question(item)
                    }
                }
            )
        }
    }
}

@Composable
fun Question(question: QuizResultQuestion) {
    MySurface {
        Column(
            modifier = Modifier.padding(top = 5.dp, bottom = 8.dp, start = 5.dp, end = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Question number
            MyText(
                text = "${stringResource(R.string.question)} ${question.questionNum}",
                modifier = Modifier.padding(bottom = 3.dp),
                fontSize = 16.sp
            )

            // Question text
            MyText(
                text = question.questionText,
                modifier = Modifier.padding(vertical = 3.dp)
            )

            MyHorizontalDivider(thickness = 2.dp)

            // Answers
            for (i in 0..3) {
                Answer(
                    answerNum = i,
                    answerText = question.answers[i].text,
                    isCorrectAnswer = question.answers[i].isCorrect,
                    chosenAnswer = question.chosenAnswerId
                )
            }
        }
    }
}

@Composable
private fun Answer(answerNum: Int, answerText: String, isCorrectAnswer: Boolean, chosenAnswer: Int) {
    if (answerNum != 0)
        MyHorizontalDivider(Modifier.padding(horizontal = 5.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MyText(
            text = answerText,
            modifier = Modifier.fillMaxWidth(0.85f),
            fontSize = 18.sp,
            textAlign = TextAlign.Start
        )

        if (answerNum == chosenAnswer || isCorrectAnswer) {
            Icon(
                imageVector =
                    if (isCorrectAnswer) Icons.Default.Check
                    else Icons.Default.Close,
                contentDescription = "",
                modifier = Modifier.size(25.dp),
                tint =
                    if (isCorrectAnswer) Positive
                    else Negative
            )
        }
    }
}