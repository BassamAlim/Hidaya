package bassamalim.hidaya.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.models.QuizResultQuestion
import bassamalim.hidaya.ui.components.*
import bassamalim.hidaya.ui.theme.AppTheme
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils

class QuizResultActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private var score = 0
    private lateinit var questions: List<QuizQuestionsDB>
    private lateinit var cAnswers: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.onActivityCreateSetLocale(this)

        db = DBUtils.getDB(this)

        val intent = intent
        score = intent.getIntExtra("score", 10)
        cAnswers = intent.getIntArrayExtra("cAnswers")!!
        questions = intent.getSerializableExtra("questions") as List<QuizQuestionsDB>

        setContent {
            AppTheme {
                UI()
            }
        }
    }

    private fun getQuestionItems(): List<QuizResultQuestion> {
        val items = ArrayList<QuizResultQuestion>()
        for (i in 0..9) {
            val answers = db.quizAnswerDao().getAnswers(questions[i].getQuestionId())
            val answersText = List(answers.size) { answers[it].answer_text!! }

            items.add(
                QuizResultQuestion(
                    i, questions[i].getQuestionText(), questions[i].getCorrectAnswerId(),
                    cAnswers[i], answersText
                )
            )
        }
        return items
    }

    @Composable
    private fun UI() {
        MyScaffold(stringResource(R.string.quiz_result)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(it),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    MyText(
                        text = "${getString(R.string.your_score_is)} ${score * 10}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp)
                    )
                }

                MyLazyColumn(lazyList = {
                    items(
                        items = getQuestionItems()
                    ) { item ->
                        Question(item)
                    }
                })
            }
        }
    }

    @Composable
    fun Question(question: QuizResultQuestion) {
        MySurface {
            Column(
                Modifier.padding(vertical = 5.dp, horizontal = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Question number
                MyText(
                    text = "${stringResource(id = R.string.question)} ${question.questionNum+1}",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 3.dp)
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
                        ansNum = i,
                        ansText = question.answers[i],
                        correctAns = question.correctAns,
                        chosenAns = question.chosenAns
                    )
                }
            }
        }
    }

    @Composable
    private fun Answer(ansNum: Int, ansText: String, correctAns: Int, chosenAns: Int) {
        if (ansNum != 0) MyHorizontalDivider(modifier = Modifier.padding(horizontal = 5.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MyText(text = ansText, fontSize = 18.sp)

            Image(
                painter = painterResource(
                    if (ansNum == correctAns) R.drawable.ic_check
                    else R.drawable.ic_wrong
                ),
                contentDescription = "",
                Modifier
                    .size(25.dp)
                    .alpha(if (ansNum == chosenAns || ansNum == correctAns) 1F else 0F)
            )
        }
    }

}