package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen

class AthkarVM : ViewModel() {

    fun onAllAthkarClick(navController: NavController) {
        navController.navigate(
            Screen.AthkarList("all").route
        )
    }

    fun onFavoriteAthkarClick(navController: NavController) {
        navController.navigate(
            Screen.AthkarList("favorite").route
        )
    }

    fun onCategoryClick(navController: NavController, category: Int) {
        navController.navigate(
            Screen.AthkarList("category", category.toString()).route
        )
    }

}