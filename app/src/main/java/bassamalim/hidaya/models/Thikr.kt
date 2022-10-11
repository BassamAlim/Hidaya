package bassamalim.hidaya.models

data class Thikr(
    val id: Int,
    val title: String?,
    val text: String,
    val textTranslation: String?,
    val fadl: String?,
    val reference: String?,
    val repetition: String
)