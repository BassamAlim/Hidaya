package bassamalim.hidaya.utils

import android.app.Activity
import android.content.Context
import bassamalim.hidaya.R
import bassamalim.hidaya.enums.ListType
import java.util.*

object ActivityUtils {

    fun myOnActivityCreated(activity: Activity) {
        onActivityCreateSetTheme(activity)
        onActivityCreateSetLocale(activity)
    }

    fun onActivityCreateSetTheme(activity: Activity): String {
        val theme = PrefUtils.getTheme(activity)
        when (theme) {
            "Dark" -> activity.setTheme(R.style.Theme_HidayaM)
            "Night" -> activity.setTheme(R.style.Theme_HidayaN)
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
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }

    fun getListType(ordinal: Int): ListType {
        return when (ordinal) {
            1 -> ListType.Favorite
            2 -> ListType.Downloaded
            else -> ListType.All
        }
    }

}