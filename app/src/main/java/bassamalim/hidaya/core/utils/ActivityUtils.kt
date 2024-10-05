package bassamalim.hidaya.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.StartAction
import bassamalim.hidaya.core.enums.Theme
import java.util.Locale

object ActivityUtils {

    fun bootstrapApp(
        context: Context,
        applicationContext: Context = context.applicationContext,
        language: Language,
        theme: Theme
    ) {
        onActivityCreateSetLocale(context = context, language = language)
        onActivityCreateSetTheme(context = context, theme = theme)
        onActivityCreateSetLocale(context = applicationContext, language = language)
        onActivityCreateSetTheme(context = applicationContext, theme = theme)
    }

    private fun onActivityCreateSetTheme(context: Context, theme: Theme) {
        when (theme) {
            Theme.LIGHT -> context.setTheme(R.style.Theme_HidayaL)
            Theme.DARK -> context.setTheme(R.style.Theme_HidayaM)
            Theme.NIGHT -> context.setTheme(R.style.Theme_HidayaN)
        }
    }

    fun onActivityCreateSetLocale(context: Context, language: Language) {
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

    fun restartApplication(activity: Activity) {
        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        mainIntent.setAction(StartAction.RESET_DATABASE.name)
        activity.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    fun clearAppData(context: Context) {
        try {
            context.getSystemService(ActivityManager::class.java).clearApplicationUserData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}