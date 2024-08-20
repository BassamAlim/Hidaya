package bassamalim.hidaya.features.recitationsRecitersMenu.domain

import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecitationRecitersMenuDomain @Inject constructor(
    private val recitationsRepository: RecitationsRepository,
    private val quranRepository: QuranRepository,
    private val settingsRepository: AppSettingsRepository
) {

    suspend fun getLanguage() = settingsRepository.getLanguage().first()

    fun getFavs() = recitationsRepository.getReciterFavorites()

    suspend fun setFav(reciterId: Int, fav: Int) {
        recitationsRepository.setReciterFavorite(reciterId, fav)
    }

    fun observeReciters() = recitationsRepository.observeAllReciters()

    fun getReciterRecitations(reciterId: Int) =
        recitationsRepository.getReciterRecitations(reciterId)

    fun getAllVersions() = recitationsRepository.getAllVersions()

    fun getVersion(reciterId: Int, versionId: Int) =
        recitationsRepository.getVersion(reciterId, versionId)

    fun getReciterName(reciterId: Int, language: Language) =
        recitationsRepository.getReciterName(id = reciterId, language = language)

    fun getSuraNames(language: Language) =
        quranRepository.getDecoratedSuraNames(language)

    fun getNarrationSelections(): recitationsRepository.getNarrationSelections()

    fun getLastPlayedMediaId() =
        preferencesDS.getString(Preference.LastPlayedMediaId)

    fun updateFavorites() {
        val recitersJson = gson.toJson(db.recitationRecitersDao().getIsFavorites())
        preferencesDS.setString(Preference.FavoriteReciters, recitersJson)
    }

    fun getRewayat(): Array<String> =
        res.getStringArray(R.array.rewayat)

    fun getLastPlayStr() = res.getString(R.string.last_play)
    fun getSuraStr() = res.getString(R.string.sura)
    fun getForReciterStr() = res.getString(R.string.for_reciter)
    fun getInRewayaOfStr() = res.getString(R.string.in_rewaya_of)
    fun getNoLastPlayStr() = res.getString(R.string.no_last_play)

}