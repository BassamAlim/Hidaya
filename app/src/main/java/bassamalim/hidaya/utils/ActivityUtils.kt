package bassamalim.hidaya.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import bassamalim.hidaya.R
import java.util.*

object ActivityUtils {

    fun myOnActivityCreated(activity: Activity) {
        onActivityCreateSetTheme(activity)
        onActivityCreateSetLocale(activity)
    }

    fun onActivityCreateSetTheme(activity: Activity): String {
        val theme = PrefUtils.getTheme(activity)
        when (theme) {
            "ThemeM" -> activity.setTheme(R.style.Theme_HidayaM)
            "ThemeR" -> activity.setTheme(R.style.Theme_HidayaN)
            else -> activity.setTheme(R.style.Theme_HidayaL)
        }
        return theme
    }

    fun onActivityCreateSetLocale(context: Context): String {
        val language = PrefUtils.getLanguage(context)

        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources

        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return language
    }

    fun restartActivity(activity: Activity) {
        val intent: Intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }

}