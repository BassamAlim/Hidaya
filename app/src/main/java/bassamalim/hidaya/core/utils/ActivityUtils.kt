package bassamalim.hidaya.core.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import java.util.Locale

object ActivityUtils {

    suspend fun bootstrapApp(
        context: Context,
        applicationContext: Context = context.applicationContext,
        language: Language,
        theme: Theme,
        isFirstLaunch: Boolean = false,
        lastDbVersion: Int,
        setLastDbVersion: suspend (Int) -> Unit,
        favoriteSuraMap: Map<Int, Boolean>,
        setFavoriteSuraMap: suspend (Map<Int, Boolean>) -> Unit,
        favoriteReciterMap: Map<Int, Boolean>,
        setFavoriteReciterMap: suspend (Map<Int, Boolean>) -> Unit,
        favoriteRemembranceMap: Map<Int, Boolean>,
        setFavoriteRemembranceMap: suspend (Map<Int, Boolean>) -> Unit,
        testDb: () -> Unit
    ) {
        onActivityCreateSetLocale(context = context, language = language)
        onActivityCreateSetTheme(context = context, theme = theme)
        onActivityCreateSetLocale(context = applicationContext, language = language)
        onActivityCreateSetTheme(context = applicationContext, theme = theme)

        if (isFirstLaunch) {
            if (DBUtils.needsRevival(lastDbVersion = lastDbVersion, test = testDb))
                DBUtils.reviveDB(
                    context = context,
                    favoriteSuraMap = favoriteSuraMap,
                    setFavoriteSuraMap = setFavoriteSuraMap,
                    favoriteReciterMap = favoriteReciterMap,
                    setFavoriteReciterMap = setFavoriteReciterMap,
                    favoriteRemembranceMap = favoriteRemembranceMap,
                    setFavoriteRemembranceMap = setFavoriteRemembranceMap,
                    setLastDbVersion = setLastDbVersion
                )
        }
    }

    fun onActivityCreateSetTheme(context: Context, theme: Theme) {
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

    fun clearAppData(context: Context) {
        try {
            context.getSystemService(ActivityManager::class.java).clearApplicationUserData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}