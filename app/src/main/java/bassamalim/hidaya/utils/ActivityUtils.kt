package bassamalim.hidaya.utils

import android.app.Activity
import android.content.Context
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.Language
import bassamalim.hidaya.enums.Theme
import java.util.*

object ActivityUtils {

    fun onActivityCreateSetTheme(context: Context): Theme {
        val theme = PrefUtils.getTheme(PrefUtils.getPreferences(context))
        when (theme) {
            Theme.LIGHT -> context.setTheme(R.style.Theme_HidayaL)
            Theme.DARK -> context.setTheme(R.style.Theme_HidayaM)
            Theme.NIGHT -> context.setTheme(R.style.Theme_HidayaN)
        }
        return theme
    }

    fun onActivityCreateSetLocale(context: Context): Language {
        val language = PrefUtils.getLanguage(PrefUtils.getPreferences(context))

        val locale = Locale(
            if (language == Language.ENGLISH) "en"
            else "ar"
        )
        Locale.setDefault(locale)
        val resources = context.resources

        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return language
    }

    fun restartActivity(activity: Activity) {
        activity.finish()
        activity.startActivity(activity.intent)
    }

}