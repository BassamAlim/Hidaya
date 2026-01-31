package bassamalim.hidaya.features.quiz.test.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.RadioGroup

@Composable
fun QuizTestScreen(viewModel: QuizTestViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyScaffold(title = stringResource(R.string.quiz_title)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressSection(
                currentQuestion = state.questionIdx + 1,
                totalQuestions = viewModel.totalQuestions
            )

            MyHorizontalDivider()

            QuestionArea(
                questionNumber = state.titleQuestionNumber,
                question = state.question
            )

            MyHorizontalDivider()

            RadioGroup(
                options = state.answers,
                selection = state.selection,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(1.dp, 400.dp)
                    .verticalScroll(rememberScrollState()),
                fontSize = 18.sp,
                onSelect = { index -> viewModel.onAnswerSelected(index) }
            )

            MyHorizontalDivider()

            BottomBar(
                questionIdx = state.questionIdx,
                isAllAnswered = state.allAnswered,
                isPreviousButtonEnabled = state.previousButtonEnabled,
                isNextButtonEnabled = state.nextButtonEnabled,
                onPreviousQuestionClick = viewModel::onPreviousQuestionClick,
                onNextQuestionClick = viewModel::onNextQuestionClick
            )
        }
    }
}

@Composable
private fun ProgressSection(currentQuestion: Int, totalQuestions: Int) {
    val progress = currentQuestion.toFloat() / totalQuestions

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Spacer(modifier = Modifier.height(6.dp))

        MyText(
            text = "$currentQuestion / $totalQuestions",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestionArea(questionNumber: String, question: String) {
    MySurface(
        padding = PaddingValues(vertical = 12.dp, horizontal = 16.dp),
        cornerRadius = 12.dp,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .heightIn(1.dp, 200.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = "${stringResource(R.string.question)} $questionNumber",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            MyText(
                text = question,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BottomBar(
    questionIdx: Int,
    isAllAnswered: Boolean,
    isPreviousButtonEnabled: Boolean,
    isNextButtonEnabled: Boolean,
    onPreviousQuestionClick: () -> Unit,
    onNextQuestionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilledTonalButton(
            onClick = onPreviousQuestionClick,
            modifier = Modifier.weight(1f),
            enabled = isPreviousButtonEnabled
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(4.dp))
            MyText(
                text = stringResource(R.string.previous),
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        FilledTonalButton(
            onClick = onNextQuestionClick,
            modifier = Modifier.weight(1f),
            enabled = isNextButtonEnabled
        ) {
            MyText(
                text = stringResource(
                    if (questionIdx == 9) {
                        if (isAllAnswered) R.string.finish_quiz
                        else R.string.answer_all_questions
                    }
                    else R.string.next
                ),
                modifier = Modifier.padding(vertical = 8.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}