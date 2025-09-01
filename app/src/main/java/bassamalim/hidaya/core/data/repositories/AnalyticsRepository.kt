package bassamalim.hidaya.core.data.repositories

import android.os.Bundle
import bassamalim.hidaya.core.models.AnalyticsEvent
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.logger.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    private val logger = Logger.getLogger(tag = "AnalyticsRepository")

    fun trackEvent(event: AnalyticsEvent) {
        try {
            val bundle = Bundle().apply {
                event.parameters.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> putString(key, value.toString())
                    }
                }
            }
            firebaseAnalytics.logEvent(event.name, bundle)
            logger.debug("Analytics", "Event tracked: ${event.name}")
        } catch (e: Exception) {
            logger.error("Analytics", "Failed to track event: ${event.name}", e)
        }
    }

}