package bassamalim.hidaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import bassamalim.hidaya.Screen

class AthkarVM : ViewModel() {

    fun onAllAthkarClick(navController: NavController) {
        navController.navigate(
            Screen.AthkarList.withArgs(
                "all"
            )
        )
    }

    fun onFavoriteAthkarClick(navController: NavController) {
        navController.navigate(
            Screen.AthkarList.withArgs(
                "favorite"
            )
        )
    }

    fun onCategoryClick(navController: NavController, category: Int) {
        navController.navigate(
            Screen.AthkarList.withArgs(
                "category",
                category.toString()
            )
        )
    }

}