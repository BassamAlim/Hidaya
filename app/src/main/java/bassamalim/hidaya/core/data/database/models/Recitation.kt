package bassamalim.hidaya.core.data.database.models

data class Recitation(
    val reciter_id: Int,
    val reciter_name_ar: String,
    val reciter_name_en: String,
    val narration_id: Int,
    val narration_name_ar: String,
    val narration_name_en: String,
    val narration_url: String,
    val narration_available_suras: String
)