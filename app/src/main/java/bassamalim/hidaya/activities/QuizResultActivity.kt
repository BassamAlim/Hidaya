package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.QuizResultQuestionAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.QuizAnswersDB
import bassamalim.hidaya.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.databinding.ActivityQuizResultBinding
import bassamalim.hidaya.models.QuizResultQuestion
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.DBUtils

class QuizResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizResultBinding
    private lateinit var db: AppDatabase
    private var score = 0
    private lateinit var cAnswers: IntArray
    private lateinit var questions: List<QuizQuestionsDB>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuizResultQuestionAdapter
    private lateinit var questionCards: ArrayList<QuizResultQuestion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityUtils.myOnActivityCreated(this)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.home.setOnClickListener { onBackPressed() }

        db = DBUtils.getDB(this)

        val intent: Intent = intent
        score = intent.getIntExtra("score", 10)
        cAnswers = intent.getIntArrayExtra("cAnswers")!!
        questions = intent.getSerializableExtra("questions") as List<QuizQuestionsDB>

        questionCards = makeQuestionCards()

        setupRecycler()

        show()
    }

    private fun makeQuestionCards(): ArrayList<QuizResultQuestion> {
        val cards: ArrayList<QuizResultQuestion> = ArrayList()
        for (i in 0..9) {
            val answers: List<QuizAnswersDB> = getAnswers(questions[i].getQuestionId())

            cards.add(
                QuizResultQuestion(
                    i, questions[i].getQuestionText(), questions[i].getCorrectAnswerId(),
                    cAnswers[i], answers[0].answer_text!!, answers[1].answer_text!!,
                    answers[2].answer_text!!, answers[3].answer_text!!
                )
            )
        }
        return cards
    }

    private fun getAnswers(qId: Int): List<QuizAnswersDB> {
        return db.quizAnswerDao().getAnswers(qId)
    }

    private fun setupRecycler() {
        recyclerView = binding.recycler
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = QuizResultQuestionAdapter(this, questionCards)
        recyclerView.adapter = adapter
    }

    private fun show() {
        val resultText: String =
            getString(R.string.your_score_is) + " " + score * 10 + "%"
        binding.resultScreen.text = resultText
    }

}