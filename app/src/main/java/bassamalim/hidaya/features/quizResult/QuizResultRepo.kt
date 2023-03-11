package bassamalim.hidaya.features.quizResult

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.core.utils.PrefUtils
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