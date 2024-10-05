package bassamalim.hidaya.features.about.domain

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DbUtils
import javax.inject.Inject

class AboutDomain @Inject constructor(
    private val appStateRepository: AppStateRepository
) {

    private var counter by mutableIntStateOf(0)

    fun getLastUpdate() = appStateRepository.getLastDailyUpdateMillis()

    fun rebuildDatabase(activity: Activity) {
        DbUtils.resetDB(activity.applicationContext)

        ActivityUtils.restartApplication(activity)
    }

    fun handleTitleClicks(setDevModeEnabled: () -> Unit) {
        if (++counter >= 5) setDevModeEnabled()
    }

}