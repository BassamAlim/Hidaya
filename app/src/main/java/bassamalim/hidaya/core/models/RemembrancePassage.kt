package bassamalim.hidaya.core.models

data class RemembrancePassage(
    val id: Int,
    val title: String?,
    val text: String,
    val translation: String? = null,
    val virtue: String?,
    val reference: String?,
    val repetition: String,
    val isTitleAvailable: Boolean,
    val isTranslationAvailable: Boolean = false,
    val isVirtueAvailable: Boolean,
    val isReferenceAvailable: Boolean,
    val isRepetitionAvailable: Boolean
)