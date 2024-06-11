package bassamalim.hidaya.features.quiz

import android.content.SharedPreferences
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class QuizRepo @Inject constructor(
    private val sp: SharedPreferences,
    private val db: AppDatabase
) {

    fun getNumeralsLanguage() = PrefUtils.getNumeralsLanguage(sp)

    fun getQuestions() = db.quizQuestionDao().all

    fun getAnswers(questionId: Int) = db.quizAnswerDao().getAnswers(questionId)

}