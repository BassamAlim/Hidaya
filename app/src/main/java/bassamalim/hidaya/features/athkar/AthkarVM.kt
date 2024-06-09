package bassamalim.hidaya.features.athkar

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.core.nav.Navigator
import bassamalim.hidaya.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AthkarVM @Inject constructor(
    private val navigator: Navigator
): ViewModel() {

    fun onAllAthkarClick() {
        navigator.navigate(
            Screen.AthkarList(ListType.All.name)
        )
    }

    fun onFavoriteAthkarClick() {
        navigator.navigate(
            Screen.AthkarList(ListType.Favorite.name)
        )
    }

    fun onCategoryClick(category: Int) {
        navigator.navigate(
            Screen.AthkarList(
                type = ListType.Custom.name,
                category = category.toString()
            )
        )
    }

}