package bassamalim.hidaya.features.quiz.result.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class QuizResultDomain @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {

    suspend fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage().first()

}