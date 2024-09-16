package bassamalim.hidaya.features.about.domain

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import bassamalim.hidaya.core.data.repositories.AppStateRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.data.repositories.RemembrancesRepository
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.DbUtils
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AboutDomain @Inject constructor(
    private val appStateRepository: AppStateRepository,
    private val quranRepository: QuranRepository,
    private val recitationsRepository: RecitationsRepository,
    private val remembrancesRepository: RemembrancesRepository
) {

    private var counter by mutableIntStateOf(0)

    fun getLastUpdate() = appStateRepository.getLastDailyUpdateMillis()

    suspend fun rebuildDatabase(activity: Activity) {
        DbUtils.resetDB(activity.applicationContext)

        DbUtils.restoreDbData(
            suraFavorites = quranRepository.getSuraFavoritesBackup().first(),
            setSuraFavorites = quranRepository::setSuraFavorites,
            reciterFavorites = recitationsRepository.getReciterFavoritesBackup().first(),
            setReciterFavorites = recitationsRepository::setReciterFavorites,
            remembranceFavorites = remembrancesRepository.getFavoritesBackup().first(),
            setRemembranceFavorites = remembrancesRepository::setFavorites,
        )

        ActivityUtils.triggerRebirth(activity)
    }

    fun handleTitleClicks(setDevModeEnabled: () -> Unit) {
        if (++counter >= 5) setDevModeEnabled()
    }

}