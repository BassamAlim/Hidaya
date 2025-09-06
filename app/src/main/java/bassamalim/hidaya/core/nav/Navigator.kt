package bassamalim.hidaya.core.nav

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import bassamalim.hidaya.core.data.repositories.AnalyticsRepository
import bassamalim.hidaya.core.models.AnalyticsEvent
import java.util.Stack
import javax.inject.Inject

class Navigator @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {

    private val callbacks = Stack<(Bundle?) -> Unit>()
    private var navController: NavController? = null

    fun setController(navController: NavController) {
        this.navController = navController
    }

    fun navigate(destination: Screen) {
        navController?.navigate(destination.route)

        analyticsRepository.trackEvent(AnalyticsEvent.ScreenView(destination.route))
    }

    fun navigate(destination: Screen, builder: NavOptionsBuilder.() -> Unit) {
        navController?.navigate(route = destination.route, builder = builder)

        analyticsRepository.trackEvent(AnalyticsEvent.ScreenView(destination.route))
    }

    fun popBackStack() {
        navController?.popBackStack()

        if (!callbacks.isEmpty()) callbacks.pop()
    }

    fun popBackStack(destination: Screen, inclusive: Boolean = false) {
        navController?.popBackStack(route = destination.route, inclusive = inclusive)
    }

    fun navigateForResult(destination: Screen, onResult: (Bundle?) -> Unit) {
        navController?.navigate(destination.route)

        callbacks.push(onResult)

        analyticsRepository.trackEvent(AnalyticsEvent.ScreenView(destination.route))
    }

    fun navigateBackWithResult(data: Bundle?) {
        if (!callbacks.isEmpty()) {
            callbacks.pop()?.invoke(data)
        }

        navController?.popBackStack()
    }

    fun getContext() = navController?.context

    fun clear() {
        navController = null
    }

}