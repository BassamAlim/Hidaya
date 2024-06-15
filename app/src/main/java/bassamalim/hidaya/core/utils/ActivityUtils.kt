package bassamalim.hidaya.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.database.AppDatabase
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.LocationType
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.other.Global
import java.util.Locale

object ActivityUtils {

    fun bootstrapApp(
        context: Context,
        applicationContext: Context = context.applicationContext,
        preferencesDS: PreferencesDataSource = PreferencesDataSource(
            PreferenceManager.getDefaultSharedPreferences(context)
        ),
        db: AppDatabase = DBUtils.getDB(context),
        isFirstLaunch: Boolean = false
    ) {
        onActivityCreateSetLocale(context)
        onActivityCreateSetTheme(context)
        onActivityCreateSetLocale(applicationContext)
        onActivityCreateSetTheme(applicationContext)

        if (isFirstLaunch) {
            try {  // remove after a while
                LocationType.valueOf(preferencesDS.getString(Preference.LocationType))
            } catch (e: Exception) {
                Log.e(Global.TAG, "Neuralyzing", e)
                clearAppData(context)
            }

            if (DBUtils.needsRevival(preferencesDS, db))
                DBUtils.reviveDB(context, preferencesDS)
        }
    }

    fun onActivityCreateSetTheme(
        context: Context,
        preferencesDS: PreferencesDataSource = PreferencesDataSource(
            PreferenceManager.getDefaultSharedPreferences(context)
        )
    ) {
        when (preferencesDS.getTheme()) {
            Theme.LIGHT -> context.setTheme(R.style.Theme_HidayaL)
            Theme.DARK -> context.setTheme(R.style.Theme_HidayaM)
            Theme.NIGHT -> context.setTheme(R.style.Theme_HidayaN)
        }
    }

    fun onActivityCreateSetLocale(
        context: Context,
        preferencesDS: PreferencesDataSource = PreferencesDataSource(
            PreferenceManager.getDefaultSharedPreferences(context)
        )
    ) {
        val language = preferencesDS.getLanguage()

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
    }

    fun restartActivity(activity: Activity) {
        activity.apply {
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }

    fun clearAppData(ctx: Context) {
        try {
            ctx.getSystemService(ActivityManager::class.java).clearApplicationUserData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}