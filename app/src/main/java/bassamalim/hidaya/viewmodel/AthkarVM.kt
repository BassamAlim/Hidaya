package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.nav.Screen
import bassamalim.hidaya.enums.ListType

class AthkarVM : ViewModel() {

    fun onAllAthkarClick(navController: NavController) {
        navController.navigate(
            Screen.AthkarList(ListType.All.name).route
        )
    }

    fun onFavoriteAthkarClick(navController: NavController) {
        navController.navigate(
            Screen.AthkarList(ListType.Favorite.name).route
        )
    }

    fun onCategoryClick(navController: NavController, category: Int) {
        navController.navigate(
            Screen.AthkarList(
                ListType.Custom.name,
                category.toString()
            ).route
        )
    }

}