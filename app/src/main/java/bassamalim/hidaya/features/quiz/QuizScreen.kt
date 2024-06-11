package bassamalim.hidaya.features.quiz

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
import bassamalim.hidaya.core.utils.LangUtils

@Composable
fun QuizUI(
    vm: QuizVM
) {
    val st by vm.uiState.collectAsStateWithLifecycle()

    MyScaffold(
        title = "${stringResource(R.string.question)} ${
            LangUtils.translateNums(st.numeralsLanguage, (st.questionIdx + 1).toString())
        }",
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuestionArea(st)

            // Answers
            RadioGroup(
                options = st.answers,
                selection = st.selection,
                modifier = Modifier
                    .heightIn(1.dp, 400.dp)
                    .verticalScroll(rememberScrollState()),
                onSelect = { index -> vm.answered(index) }
            )

            BottomBar(vm, st)
        }
    }
}

@Composable
fun QuestionArea(
    st: QuizState
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
}

@Composable
fun ColumnScope.BottomBar(
    vm: QuizVM,
    st: QuizState
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
                if (st.prevBtnEnabled) AppTheme.colors.text
                else Grey,
            innerPadding = PaddingValues(10.dp)
        ) {
            vm.previousQ()
        }

        MySquareButton(
            text = stringResource(
                if (st.questionIdx == 9) {
                    if (st.allAnswered) R.string.finish_quiz
                    else R.string.answer_all_questions
                }
                else R.string.next_question
            ),
            modifier = Modifier.sizeIn(maxWidth = 175.dp),
            textColor =
                if (st.nextBtnEnabled) AppTheme.colors.text
                else Grey,
            innerPadding = PaddingValues(10.dp)
        ) {
            vm.nextQ()
        }
    }
}