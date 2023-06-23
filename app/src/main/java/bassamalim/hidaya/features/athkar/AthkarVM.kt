package bassamalim.hidaya.features.athkar

import androidx.lifecycle.ViewModel
import bassamalim.hidaya.core.enums.ListType
import bassamalim.hidaya.features.destinations.AthkarListUIDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class AthkarVM : ViewModel() {

    fun onAllAthkarClick(navigator: DestinationsNavigator) {
        navigator.navigate(
            AthkarListUIDestination(
                type = ListType.All
            )
        )
    }

    fun onFavoriteAthkarClick(navigator: DestinationsNavigator) {
        navigator.navigate(
            AthkarListUIDestination(
                type = ListType.Favorite
            )
        )
    }

    fun onCategoryClick(navigator: DestinationsNavigator, category: Int) {
        navigator.navigate(
            AthkarListUIDestination(
                type = ListType.Custom,
                category = category
            )
        )
    }

}