package bassamalim.hidaya.core.helpers

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import bassamalim.hidaya.core.nav.Screen
import java.util.Stack

class Navigator {

    private val callbacks = Stack<(Bundle?) -> Unit>()
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

        if (!callbacks.isEmpty()) callbacks.pop()
    }

    fun popBackStack(destination: Screen, inclusive: Boolean = false) {
        navController?.popBackStack(destination.route, inclusive)
    }

    fun navigateForResult(destination: Screen, onResult: (Bundle?) -> Unit) {
        navController?.navigate(destination.route)

        callbacks.push(onResult)
    }

    fun navigateBackWithResult(data: Bundle?) {
        if (!callbacks.isEmpty()) callbacks.pop()?.invoke(data)

        navController?.popBackStack()
    }

    fun getContext() = navController?.context

    fun clear() {
        navController = null
    }

}