package bassamalim.hidaya.features.about.domain

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.DBUtils
import javax.inject.Inject

class AboutDomain @Inject constructor(
    private val app: Application,
    private val appStateRepo: AppStateRepository
) {

    private var counter by mutableIntStateOf(0)

    fun getLastUpdate() = appStateRepo.getLastDailyUpdateMillis()

    fun rebuildDatabase() {
        app.deleteDatabase("HidayaDB")

        Log.i(Global.TAG, "Database Rebuilt")

        DBUtils.reviveDB(app)
    }

    fun handleTitleClicks(setDevModeEnabled: () -> Unit){
        if (++counter >= 5) setDevModeEnabled()
    }

}