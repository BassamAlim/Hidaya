package bassamalim.hidaya.features.dateEditor.domain

import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DateEditorDomain @Inject constructor(
    private val appSettingsRepo: AppSettingsRepository
) {

    private var dateOffset = 0

    suspend fun assignDateOffset() {
        dateOffset = appSettingsRepo.getDateOffset().first()
    }

    fun getDateOffset() = dateOffset

    fun incrementDateOffset() {
        dateOffset++
    }

    fun decrementDateOffset() {
        dateOffset--
    }

    suspend fun saveDateOffset() {
        appSettingsRepo.setDateOffset(dateOffset)
    }

    suspend fun getNumeralsLanguage() = appSettingsRepo.getNumeralsLanguage().first()

}