package bassamalim.hidaya.features.recitations.surasMenu.domain

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.ReciterSura
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecitationSurasMenuDomain @Inject constructor(
    private val app: Application,
    private val recitationsRepository: RecitationsRepository,
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    fun observeSuras(reciterId: Int, narrationId: Int, language: Language) =
        quranRepository.observeAllSuras(language).map {
            it.map { sura ->
                ReciterSura(
                    id = sura.id,
                    suraName = sura.decoratedName,
                    searchName = sura.plainName,
                    isFavorite = sura.isFavorite,
                    downloadState = getDownloadState(reciterId, narrationId, sura.id)
                )
            }
        }

    fun download(
        reciterId: Int,
        narrationId: Int,
        suraId: Int,
        suraSearchName: String,
        server: String
    ) {
        recitationsRepository.downloadSura(reciterId, narrationId, suraId, suraSearchName, server)
    }

    fun delete(reciterId: Int, narrationId: Int, suraId: Int) {
        recitationsRepository.deleteSura(reciterId, narrationId, suraId)
    }

    fun popFromDownloading(downloadId: Long) = recitationsRepository.popFromDownloading(downloadId)

    fun getDownloadState(reciterId: Int, narrationId: Int, suraId: Int): DownloadState {
        return recitationsRepository.getSuraDownloadState(reciterId, narrationId, suraId)
    }

    fun getDownloadStates(reciterId: Int, narrationId: Int) = (0..113).associateWith {
        getDownloadState(reciterId, narrationId, it)
    }

    fun registerDownloadReceiver(onComplete: BroadcastReceiver) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                app.registerReceiver(
                    onComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED
                )
            else
                app.registerReceiver(
                    onComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
        } catch (e: IllegalArgumentException) {
                e.printStackTrace()
        }
    }

    fun unregisterDownloadReceiver(onComplete: BroadcastReceiver) {
        try {
            app.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getDecoratedSuraNames(language: Language) =
        quranRepository.getDecoratedSuraNames(language)

    suspend fun getPlainSuraNames() = quranRepository.getPlainSuraNames()

    suspend fun getReciterName(id: Int, language: Language) =
        recitationsRepository.getSuraReciterName(id, language)

    suspend fun getNarration(reciterId: Int, narrationId: Int, language: Language) =
        recitationsRepository.getNarration(reciterId, narrationId, language)

    suspend fun setFavoriteStatus(suraId: Int, value: Boolean) {
        quranRepository.setSuraFavoriteStatus(suraId, value)
    }

}