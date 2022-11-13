package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.ui.components.RadioGroup
import bassamalim.hidaya.ui.components.MyButton
import bassamalim.hidaya.ui.components.MyScaffold
import bassamalim.hidaya.ui.components.MyText
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.ui.theme.Grey
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils
import java.io.Serializable
import java.util.*

class QuizActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var questions: List<QuizQuestionsDB>
    private val cAnswers = IntArray(10)
    private val current = mutableStateOf(0)
    private val currentAs = mutableStateListOf("", "", "", "")
    private val selection = mutableStateOf(-1)
    private val allAnswered = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetLocale(this)

        db = DBUtils.getDB(this)

        getQuestions()

        Arrays.fill(cAnswers, -1)

        ask(current.value)

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    private fun getQuestions() {
        val rawQuestions = db.quizQuestionDao().all.toMutableList()
        rawQuestions.shuffle()
        questions = ArrayList(rawQuestions.subList(0, 10))
    }

    private fun ask(num: Int) {
        current.value = num

        val q = questions[num]
        val answers = db.quizAnswerDao().getAnswers(q.getQuestionId())
        for (i in currentAs.indices) currentAs[i] = answers[i].answer_text!!

        selection.value = cAnswers[current.value]
    }

    private fun answered(a: Int) {
        cAnswers[current.value] = a

        allAnswered.value = allAnswered()
        if (current.value != 9) nextQ()
    }

    private fun nextQ() {
        if (current.value == 9) endQuiz()
        else ask(++current.value)
    }

    private fun previousQ() {
        if (current.value > 0) ask(--current.value)
    }

    private fun allAnswered(): Boolean {
        for (cAnswer in cAnswers) {
            if (cAnswer == -1) return false
        }
        return true
    }

    private fun endQuiz() {
        var score = 0
        for (i in 0..9) {
            if (cAnswers[i] == questions[i].getCorrectAnswerId()) score++
        }

        val intent = Intent(this, QuizResultActivity::class.java)
        intent.putExtra("cAnswers", cAnswers)
        intent.putExtra("score", score)
        intent.putExtra("questions", questions as Serializable?)
        startActivity(intent)

        finish()
    }

    @Composable
    private fun UI() {
        MyScaffold("${getString(R.string.question)} ${current.value + 1}") {
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
                        text = questions[current.value].getQuestionText(),
                        fontSize = 28.sp,
                        textColor = AppTheme.colors.onPrimary,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)
                    )
                }

                RadioGroup(
                    options = currentAs,
                    selection = selection,
                    modifier = Modifier
                        .heightIn(1.dp, 400.dp)
                        .verticalScroll(rememberScrollState())
                ) { index ->
                    answered(index)
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
                            if (current.value == 0) Grey
                            else AppTheme.colors.text,
                        innerPadding = PaddingValues(10.dp)
                    ) {
                        if (current.value != 0) previousQ()
                    }

                    MyButton(
                        text = stringResource(
                            if (current.value == 9) {
                                if (allAnswered.value) R.string.finish_quiz
                                else R.string.answer_all_questions
                            }
                            else R.string.next_question
                        ),
                        modifier = Modifier.sizeIn(maxWidth = 175.dp),
                        textColor =
                            if (current.value == 9) {
                                if (allAnswered.value) AppTheme.colors.text
                                else Grey
                            }
                            else AppTheme.colors.text,
                        innerPadding = PaddingValues(10.dp)
                    ) {
                        if (current.value == 9) {
                            if (allAnswered.value) nextQ()
                        }
                        else nextQ()
                    }
                }
            }
        }
    }

}