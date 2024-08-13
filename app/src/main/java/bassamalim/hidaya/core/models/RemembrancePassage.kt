package bassamalim.hidaya.core.models

data class RemembrancePassage(
    val id: Int,
    val title: String?,
    val text: String,
    val textTranslation: String?,
    val fadl: String?,
    val reference: String?,
    val repetition: String
)