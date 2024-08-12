package bassamalim.hidaya.core.data.database.models

data class Recitation(
    val reciter_id: Int,
    val reciter_name_ar: String,
    val reciter_name_en: String,
    val rewayah_id: Int,
    val rewayah_name_ar: String,
    val rewayah_name_en: String,
    val rewayah_url: String,
    val rewayah_surah_total: Int,
    val rewayah_surah_list: String
)