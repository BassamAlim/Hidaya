package bassamalim.hidaya.features.quiz

import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class QuizRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource,
    private val db: AppDatabase
) {

    fun getNumeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getQuestions() = db.quizQuestionDao().all

    fun getAnswers(questionId: Int) = db.quizAnswerDao().getAnswers(questionId)

}