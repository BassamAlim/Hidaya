package bassamalim.hidaya.features.misbaha.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import javax.inject.Inject

class MisbahaDomain @Inject constructor(
    val appSettingsRepository: AppSettingsRepository
) {

    fun getNumeralsLanguage() = appSettingsRepository.getNumeralsLanguage()

}