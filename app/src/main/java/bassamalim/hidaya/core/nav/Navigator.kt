package bassamalim.hidaya.core.nav

import android.os.Parcelable
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

class Navigator {

    private val callbacks = mutableMapOf<String, (Parcelable?) -> Unit>()
    private var navController: NavController? = null

    fun setController(navController: NavController) {
        this.navController = navController
    }

    fun navigate(destination: Screen) {
        navController?.navigate(
            destination.route
        )
    }

    fun navigate(destination: Screen, builder: NavOptionsBuilder.() -> Unit) {
        navController?.navigate(
            destination.route,
            builder
        )
    }

    fun popBackStack() {
        navController?.popBackStack()
    }

    fun popBackStack(destination: Screen, inclusive: Boolean = false) {
        navController?.popBackStack(destination.route, inclusive)
    }

    fun navigateForResult(
        destination: Screen,
        key: String,
        onResult: (Parcelable?) -> Unit
    ) {
        navController?.navigate(destination.route)

        callbacks[key] = onResult
    }

    fun navigateBackWithResult(key: String, data: Parcelable?) {
        callbacks[key]?.invoke(data)
        callbacks.remove(key)

        navController?.popBackStack()
    }

    fun getContext() = navController?.context

    fun clear() {
        navController = null
    }

}