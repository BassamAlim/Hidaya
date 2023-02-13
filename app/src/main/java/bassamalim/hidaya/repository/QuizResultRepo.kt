package bassamalim.hidaya.repository

import android.content.SharedPreferences
import bassamalim.hidaya.data.database.AppDatabase
import bassamalim.hidaya.data.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.utils.PrefUtils
import javax.inject.Inject

class QuizResultRepo @Inject constructor(
    pref: SharedPreferences,
    private val db: AppDatabase
) {

    val numeralsLanguage = PrefUtils.getNumeralsLanguage(pref)

    fun getQuestions(ids: IntArray): List<QuizQuestionsDB> {
        return ids.map { id -> db.quizQuestionDao().getQuestion(id) }
    }

    fun getAnswers(questionId: Int) = db.quizAnswerDao().getAnswers(questionId)

}