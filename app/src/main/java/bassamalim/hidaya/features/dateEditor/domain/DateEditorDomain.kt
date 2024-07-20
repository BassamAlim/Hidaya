package bassamalim.hidaya.features.dateEditor.domain

import bassamalim.hidaya.features.dateEditor.data.DateEditorRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DateEditorDomain @Inject constructor(
    private val repository: DateEditorRepository
) {

    private var dateOffset = 0

    suspend fun assignDateOffset() {
        dateOffset = repository.getDateOffset().first()
    }

    fun getDateOffset() = dateOffset

    fun incrementDateOffset() {
        dateOffset++
    }

    fun decrementDateOffset() {
        dateOffset--
    }

    suspend fun saveDateOffset() {
        repository.updateDateOffset(dateOffset)
    }

    suspend fun getNumeralsLanguage() = repository.getNumeralsLanguage()

}