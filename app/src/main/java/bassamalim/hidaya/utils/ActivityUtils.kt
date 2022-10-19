package bassamalim.hidaya.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.dialogs.TutorialDialog
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

    fun checkFirstTime(
        context: Context,
        supportFragmentManager: FragmentManager,
        prefKey: String,
        textResId: Int,
        pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    ) {
        if (pref.getBoolean(prefKey, true))
            TutorialDialog.newInstance(
                context.getString(textResId), prefKey
            ).show(supportFragmentManager, TutorialDialog.TAG)
    }

    fun getListType(ordinal: Int): ListType {
        return when (ordinal) {
            1 -> ListType.Favorite
            2 -> ListType.Downloaded
            else -> ListType.All
        }
    }

}