package bassamalim.hidaya.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.StartAction
import java.util.Locale

object ActivityUtils {

    fun configure(
        context: Context,
        applicationContext: Context = context.applicationContext,
        language: Language
    ) {
        onActivityCreateSetLocale(context = context, language = language)
        onActivityCreateSetLocale(context = applicationContext, language = language)
    }

    fun onActivityCreateSetLocale(context: Context, language: Language) {
        val locale = Locale(
            when (language) {
                Language.ARABIC -> "ar"
                Language.ENGLISH -> "en"
            }
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