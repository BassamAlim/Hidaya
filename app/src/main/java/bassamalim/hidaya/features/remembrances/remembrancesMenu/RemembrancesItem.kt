package bassamalim.hidaya.features.remembrances.remembrancesMenu

data class RemembrancesItem(
    val id: Int,
    val categoryId: Int,
    val name: String,
    var isFavorite: Boolean
)