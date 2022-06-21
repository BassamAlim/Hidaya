package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.adapters.QuizResultQuestionAdapter
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.QuizAnswersDB
import bassamalim.hidaya.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.databinding.ActivityQuizResultBinding
import bassamalim.hidaya.models.QuizResultQuestion
import bassamalim.hidaya.other.Utils

class QuizResultActivity : AppCompatActivity() {

    private var binding: ActivityQuizResultBinding? = null
    private var db: AppDatabase? = null
    private var score = 0
    private var cAnswers: IntArray? = null
    private var questions: List<QuizQuestionsDB>? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: QuizResultQuestionAdapter? = null
    private var questionCards: ArrayList<QuizResultQuestion>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }

        db = Room.databaseBuilder(this, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()

        val intent: Intent = intent
        score = intent.getIntExtra("score", 10)
        cAnswers = intent.getIntArrayExtra("cAnswers")
        questions = intent.getSerializableExtra("questions") as List<QuizQuestionsDB>

        questionCards = makeQuestionCards()

        setupRecycler()

        show()
    }

    private fun makeQuestionCards(): ArrayList<QuizResultQuestion> {
        val cards: ArrayList<QuizResultQuestion> = ArrayList()
        for (i in 0..9) {
            val answers: List<QuizAnswersDB> = getAnswers(questions!![i].getQuestionId())

            cards.add(
                QuizResultQuestion(
                    i, questions!![i].getQuestionText(), questions!![i].getCorrectAnswerId(),
                    cAnswers!![i], answers[0].answer_text!!, answers[1].answer_text!!,
                    answers[2].answer_text!!, answers[3].answer_text!!
                )
            )
        }
        return cards
    }

    private fun getAnswers(qId: Int): List<QuizAnswersDB> {
        return db!!.quizAnswerDao().getAnswers(qId)
    }

    private fun setupRecycler() {
        recyclerView = binding!!.recycler
        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        adapter = QuizResultQuestionAdapter(this, questionCards!!)
        recyclerView!!.adapter = adapter
    }

    private fun show() {
        val resultText: String =
            getString(R.string.your_score_is) + " " + score * 10 + "%"
        binding!!.resultScreen.text = resultText
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        recyclerView!!.adapter = null
        adapter = null
    }
}