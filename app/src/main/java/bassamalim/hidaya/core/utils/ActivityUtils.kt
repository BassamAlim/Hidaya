package bassamalim.hidaya.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.StartAction
import bassamalim.hidaya.core.utils.LangUtils.getLocale
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
        val locale = getLocale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    fun restartActivity(activity: Activity) {
        activity.apply {
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }

    fun restartApplication(activity: Activity) {
        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
        activity.startActivity(
            Intent.makeRestartActivityTask(intent?.component).apply {
                action = StartAction.RESET_DATABASE.name
            }
        )
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