package bassamalim.hidaya.core.data.database.dbs

data class TelawatDB(
    val reciter_id: Int,
    val version_id: Int,
    val reciter_name: String,
    val rewaya: String,
    val url: String,
    val count: Int,
    val suras: String
)