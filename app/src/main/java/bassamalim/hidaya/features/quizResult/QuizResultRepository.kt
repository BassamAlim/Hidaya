package bassamalim.hidaya.features.quizResult

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.database.dbs.QuizQuestionsDB
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class QuizResultRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getQuestions(ids: IntArray): List<QuizQuestionsDB> =
        ids.map { id -> db.quizQuestionDao().getQuestion(id) }

    fun getAnswers(questionId: Int) = db.quizAnswerDao().getAnswers(questionId)

}