package bassamalim.hidaya.features.remembrances.remembrancesMenu.ui

data class RemembrancesItem(
    val id: Int,
    val categoryId: Int,
    val name: String,
    var isFavorite: Boolean
)