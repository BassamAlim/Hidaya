package bassamalim.hidaya.features.recitationSurasMenu.domain

import android.app.Application
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.Language
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

class RecitationSurasMenuDomain @Inject constructor(
    private val app: Application,
    private val recitationsRepository: RecitationsRepository,
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    private val downloading = HashMap<Long, Int>()

    private fun checkIsDownloaded(suraNum: Int): Boolean {
        return File("${recitationsRepository.dir}$suraNum.mp3").exists()
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    fun getDecoratedSuraNames(language: Language) =
        quranRepository.getDecoratedSuraNames(language)

    fun getPlainSuraNames() = quranRepository.getPlainSuraNames()

    fun getReciterName(id: Int, language: Language) =
        recitationsRepository.getReciterName(id, language)

    fun getNarration(reciterId: Int, narrationId: Int) =
        recitationsRepository.getNarration(reciterId, narrationId)

    fun getIsFavorites() = recitationsRepository.getReciterFavorites()

    suspend fun setFav(suraId: Int, value: Boolean) {
        quranRepository.setSuraIsFavorite(suraId, value)
    }

}