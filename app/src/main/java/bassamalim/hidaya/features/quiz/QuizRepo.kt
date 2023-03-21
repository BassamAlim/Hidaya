package bassamalim.hidaya.features.quiz

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.utils.PrefUtils
import javax.inject.Inject

class QuizRepo @Inject constructor(
    private val resources: Resources,
    private val pref: SharedPreferences,
    private val db: AppDatabase
) {

    fun getNumeralsLanguage() = PrefUtils.getNumeralsLanguage(pref)

    fun getQuestions() = db.quizQuestionDao().all

    fun getAnswers(questionId: Int) = db.quizAnswerDao().getAnswers(questionId)

    fun getQuestionStr() = resources.getString(R.string.question)

}