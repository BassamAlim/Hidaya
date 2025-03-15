package bassamalim.hidaya.features.recitations.surasMenu.domain

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.helpers.ReceiverWrapper
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.Globals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.properties.Delegates

class RecitationSurasMenuDomain @Inject constructor(
    app: Application,
    private val recitationsRepository: RecitationsRepository,
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    private var reciterId by Delegates.notNull<Int>()
    private var narrationId by Delegates.notNull<Int>()
    private lateinit var setAsDownloaded: (suraId: Int) -> Unit
    private lateinit var setDownloadStates: (downloadState: Map<Int, DownloadState>) -> Unit

    private val downloadReceiver = ReceiverWrapper(
        context = app,
        intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    val downloadId =
                        intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    val downloadedItem = popFromDownloading(downloadId)!!
                    setAsDownloaded(downloadedItem.suraId)
                } catch (e: Exception) {
                    setDownloadStates(getDownloadStates(reciterId, narrationId))
                }
            }
        }
    )

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

    private fun getDownloadState(reciterId: Int, narrationId: Int, suraId: Int): DownloadState {
        return recitationsRepository.getSuraDownloadState(reciterId, narrationId, suraId)
    }

    fun getDownloadStates(reciterId: Int, narrationId: Int) =
        (0..<Globals.NUM_OF_QURAN_SURAS).associateWith { suraId ->
            getDownloadState(reciterId, narrationId, suraId)
        }

    fun registerDownloadReceiver(
        reciterId: Int,
        narrationId: Int,
        setAsDownloaded: (suraId: Int) -> Unit,
        setDownloadStates: (downloadState: Map<Int, DownloadState>) -> Unit
    ) {
        this.reciterId = reciterId
        this.narrationId = narrationId
        this.setAsDownloaded = setAsDownloaded
        this.setDownloadStates = setDownloadStates

        downloadReceiver.register()
    }

    fun unregisterDownloadReceiver() {
        downloadReceiver.unregister()
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    suspend fun getDecoratedSuraNames(language: Language) =
        quranRepository.getDecoratedSuraNames(language)

    suspend fun getPlainSuraNames() = quranRepository.getPlainSuraNames()

    suspend fun getReciterName(id: Int, language: Language) =
        recitationsRepository.getSuraReciterName(id, language)

    suspend fun getNarration(reciterId: Int, narrationId: Int, language: Language) =
        recitationsRepository.getNarration(reciterId, narrationId, language)

    fun setFavoriteStatus(suraId: Int, value: Boolean) {
        quranRepository.setSuraFavoriteStatus(suraId, value)
    }

}