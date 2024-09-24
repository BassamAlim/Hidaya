package bassamalim.hidaya.features.quiz.test.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
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
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyScaffold
import bassamalim.hidaya.core.ui.components.MySquareButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.core.ui.components.RadioGroup
import bassamalim.hidaya.core.ui.theme.AppTheme
import bassamalim.hidaya.core.ui.theme.Grey

@Composable
fun QuizTestScreen(
    viewModel: QuizTestViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isLoading) return

    MyScaffold(title = "${stringResource(R.string.question)} ${state.titleQuestionNumber}") {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuestionArea(question = state.question)

            // Answers
            RadioGroup(
                options = state.answers,
                selection = state.selection,
                modifier = Modifier
                    .heightIn(1.dp, 400.dp)
                    .verticalScroll(rememberScrollState()),
                onSelect = { index -> viewModel.onAnswerSelected(index) }
            )

            BottomBar(
                questionIdx = state.questionIdx,
                isAllAnswered = state.allAnswered,
                isPreviousButtonEnabled = state.prevBtnEnabled,
                isNextButtonEnabled = state.nextBtnEnabled,
                onPreviousQuestionClick = viewModel::onPreviousQuestionClick,
                onNextQuestionClick = viewModel::onNextQuestionClick
            )
        }
    }
}

@Composable
fun QuestionArea(
    question: String
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
            text = question,
            fontSize = 28.sp,
            textColor = AppTheme.colors.onPrimary,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)
        )
    }
}

@Composable
fun ColumnScope.BottomBar(
    questionIdx: Int,
    isAllAnswered: Boolean,
    isPreviousButtonEnabled: Boolean,
    isNextButtonEnabled: Boolean,
    onPreviousQuestionClick: () -> Unit,
    onNextQuestionClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .weight(1F, false)
            .padding(bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        MySquareButton(
            text = stringResource(R.string.previous_question),
            textColor =
                if (isPreviousButtonEnabled) AppTheme.colors.text
                else Grey,
            innerPadding = PaddingValues(10.dp),
            onClick = onPreviousQuestionClick
        )

        MySquareButton(
            text = stringResource(
                if (questionIdx == 9) {
                    if (isAllAnswered) R.string.finish_quiz
                    else R.string.answer_all_questions
                }
                else R.string.next_question
            ),
            modifier = Modifier.sizeIn(maxWidth = 175.dp),
            textColor =
                if (isNextButtonEnabled) AppTheme.colors.text
                else Grey,
            innerPadding = PaddingValues(10.dp),
            onClick = onNextQuestionClick
        )
    }
}