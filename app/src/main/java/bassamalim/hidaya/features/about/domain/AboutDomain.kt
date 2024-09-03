package bassamalim.hidaya.features.about.domain

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.DbUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AboutDomain @Inject constructor(
    private val app: Application,
    private val appStateRepository: AppStateRepository,
    private val quranRepository: QuranRepository,
    private val recitationsRepository: RecitationsRepository,
    private val remembrancesRepository: RemembrancesRepository
) {

    private var counter by mutableIntStateOf(0)

    fun getLastUpdate() = appStateRepository.getLastDailyUpdateMillis()

    suspend fun rebuildDatabase() {
        app.deleteDatabase("HidayaDB")

        Log.i(Global.TAG, "Database Rebuilt")

        DbUtils.reviveDB(
            context = app,
            suraFavorites = quranRepository.getSuraFavorites().first(),
            setSuraFavorites = quranRepository::setSuraFavorites,
            reciterFavorites = recitationsRepository.getReciterFavorites(),
            setReciterFavorites = recitationsRepository::setReciterFavorites,
            remembranceFavorites = remembrancesRepository.getFavoriteStatusesBackup().first(),
            setRemembranceFavorites = remembrancesRepository::setFavoriteStatuses,
            setLastDbVersion = appStateRepository::setLastDbVersion
        )
    }

    fun handleTitleClicks(setDevModeEnabled: () -> Unit){
        if (++counter >= 5) setDevModeEnabled()
    }

}