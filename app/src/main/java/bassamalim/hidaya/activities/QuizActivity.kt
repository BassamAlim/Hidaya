package bassamalim.hidaya.activities

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import bassamalim.hidaya.R
import bassamalim.hidaya.database.AppDatabase
import bassamalim.hidaya.database.dbs.QuizAnswersDB
import bassamalim.hidaya.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.databinding.ActivityQuizBinding
import bassamalim.hidaya.other.Utils
import java.io.Serializable
import java.util.*

class QuizActivity : AppCompatActivity() {

    private var binding: ActivityQuizBinding? = null
    private var db: AppDatabase? = null
    private var questions: List<QuizQuestionsDB>? = null
    private var current = 0
    private val cAnswers = IntArray(10)
    private var nextBtn: Button? = null
    private var prevBtn: Button? = null
    private var radioGroup: RadioGroup? = null
    private val answerBtns: Array<RadioButton?> = arrayOfNulls(4)
    private var colorText: TypedValue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.myOnActivityCreated(this)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        binding!!.home.setOnClickListener { onBackPressed() }

        db = Room.databaseBuilder(this, AppDatabase::class.java, "HidayaDB")
            .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build()

        colorText = TypedValue()
        theme.resolveAttribute(R.attr.myText, colorText, true)

        selectQuestions(getQuestions())

        Arrays.fill(cAnswers, -1)

        initViews()

        setListeners()

        ask(current)
    }

    private fun selectQuestions(rawQuestions: MutableList<QuizQuestionsDB?>) {
        rawQuestions.shuffle()
        questions = ArrayList<QuizQuestionsDB>(rawQuestions.subList(0, 10))
    }

    private fun initViews() {
        nextBtn = binding!!.nextQuestion
        prevBtn = binding!!.previousQuestion

        radioGroup = binding!!.answersRadioGroup

        answerBtns[0] = binding!!.answer1
        answerBtns[1] = binding!!.answer2
        answerBtns[2] = binding!!.answer3
        answerBtns[3] = binding!!.answer4
    }

    private fun setListeners() {
        for (i in answerBtns.indices) {
            answerBtns[i]!!.setOnClickListener {answered(i)}
        }

        prevBtn!!.setOnClickListener { previousQ() }
        nextBtn!!.setOnClickListener { nextQ() }
    }

    private fun ask(num: Int) {
        val q: QuizQuestionsDB = questions!![num]

        val qNum: String = getString(R.string.question) + " " + (current + 1)
        binding!!.topBarTitle.text = qNum

        binding!!.questionScreen.text = questions!![current].getQuestion_text()

        val answers: List<QuizAnswersDB> = getAnswers(q.getQuestion_id())
        for (i in answerBtns.indices) answerBtns[i]!!.text = answers[i].answer_text

        adjustButtons()
    }

    private fun adjustButtons() {
        radioGroup!!.clearCheck()
        if (cAnswers[current] != -1) radioGroup!!.check(answerBtns[cAnswers[current]]!!.id)

        if (current == 0) {
            prevBtn!!.isEnabled = false
            prevBtn!!.setTextColor(resources.getColor(R.color.grey, theme))
        }
        else if (current == 9) {
            if (allAnswered()) {
                nextBtn!!.text = getString(R.string.finish_quiz)
                nextBtn!!.isEnabled = true
                nextBtn!!.setTextColor(colorText!!.data)
            }
            else {
                nextBtn!!.text = getString(R.string.answer_all_questions)
                nextBtn!!.isEnabled = false
                nextBtn!!.setTextColor(resources.getColor(R.color.grey, theme))
            }
        }
        else {
            prevBtn!!.isEnabled = true
            prevBtn!!.setTextColor(colorText!!.data)

            nextBtn!!.isEnabled = true
            nextBtn!!.text = getString(R.string.next_question)
            nextBtn!!.setTextColor(colorText!!.data)
        }
    }

    private fun answered(a: Int) {
        cAnswers[current] = a

        adjustButtons()

        if (current != 9) nextQ()
    }

    private fun nextQ() {
        if (current == 9) endQuiz()
        else ask(++current)
    }

    private fun previousQ() {
        if (current > 0) ask(--current)
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
            if (cAnswers[i] == questions!![i].getCorrect_answer_id()) score++
        }

        val intent = Intent(this, QuizResultActivity::class.java)
        intent.putExtra("cAnswers", cAnswers)
        intent.putExtra("score", score)
        intent.putExtra("questions", questions as Serializable?)
        startActivity(intent)

        finish()
    }

    private fun getQuestions(): MutableList<QuizQuestionsDB?> {
        return db!!.quizQuestionDao().all.toMutableList()
    }

    private fun getAnswers(qId: Int): List<QuizAnswersDB> {
        return db!!.quizAnswerDao().getAnswers(qId)
    }
}