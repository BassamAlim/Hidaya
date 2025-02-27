package bassamalim.hidaya.features.recitations.recitersMenu.domain

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.helpers.ReceiverWrapper
import bassamalim.hidaya.core.utils.FileUtils
import bassamalim.hidaya.features.quran.surasMenu.ui.LastPlayedMedia
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.Locale
import javax.inject.Inject

class RecitationRecitersMenuDomain @Inject constructor(
    private val app: Application,
    private val recitationsRepository: RecitationsRepository,
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    private val downloadReceiver = ReceiverWrapper(
        context = app,
        intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                try {
                    recitationsRepository.popFromDownloading(downloadId)
                } catch (_: RuntimeException) {}
            }
        }
    )

    suspend fun observeRecitersWithNarrations(language: Language): Flow<Map<Int, Recitation>> {
        val allReciters = recitationsRepository.observeAllSuraReciters(language)
        val allNarrations = recitationsRepository.getAllNarrations(language)
        val downloadStates = recitationsRepository.getNarrationDownloadStates(
            ids = allReciters.first().associate { reciter ->
                reciter.id to allNarrations.filter { narration ->
                    narration.reciterId == reciter.id
                }.map { narration ->
                    narration.id
                }
            }
        )

        return allReciters.map {
            it.map { reciter ->
                reciter.id to Recitation(
                    reciterId = reciter.id,
                    reciterName = reciter.name,
                    isFavoriteReciter = reciter.isFavorite,
                    narrations = allNarrations.filter { narration ->
                        narration.reciterId == reciter.id
                    }.map { narration ->
                        narration.id to Recitation.Narration(
                            id = narration.id,
                            name = narration.name,
                            server = narration.server,
                            availableSuras = narration.availableSuras,
                            downloadState = downloadStates[reciter.id]?.get(narration.id)!!
                        )
                    }.toMap()
                )
            }.toMap()
        }
    }

    fun registerDownloadReceiver() {
        downloadReceiver.register()
    }

    fun unregisterDownloadReceiver() {
        downloadReceiver.unregister()
    }

    fun cleanFiles() {
        val mainDir = File(recitationsRepository.dir)
        FileUtils.deleteDirRecursive(mainDir)
    }

    suspend fun downloadNarration(
        reciterId: Int,
        narration: Recitation.Narration,
        suraNames: List<String>,
        language: Language,
        suraString: String
    ) {
        val reciterNames = recitationsRepository.getSuraReciterNames(language)
        Thread {
            val downloadManager = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var request: DownloadManager.Request
            var posted = false
            for (i in 0..113) {
                if (narration.availableSuras.contains(i+1)) {
                    val link = String.format(
                        Locale.US,
                        "%s/%03d.mp3",
                        narration.server,
                        i + 1
                    )
                    val uri = Uri.parse(link)

                    request = DownloadManager.Request(uri)
                    request.setTitle(
                        "${reciterNames[reciterId]} ${narration.name}" +
                                " $suraString ${suraNames[i]}"
                    )
                    val suffix = "${recitationsRepository.prefix}$reciterId/${narration.id}"
                    FileUtils.createDir(app, suffix)
                    request.setDestinationInExternalFilesDir(app, suffix, "$i.mp3")
                    request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                    )

                    val downloadId = downloadManager.enqueue(request)
                    if (!posted) {
                        recitationsRepository.addToDownloading(downloadId, reciterId, narration.id)
                        posted = true
                    }
                }
            }
        }.start()
    }

    fun deleteNarration(reciterId: Int, narration: Recitation.Narration) {
        FileUtils.deleteFile(
            context = app,
            path = "${recitationsRepository.prefix}$reciterId/${narration.id}"
        )
    }

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    fun setFavorite(reciterId: Int, value: Boolean) {
        recitationsRepository.setReciterFavorite(
            reciterId = reciterId,
            isFavorite = value
        )
    }

    suspend fun getLastPlayedMedia(mediaId: String): LastPlayedMedia? {
        if (mediaId.isEmpty() || mediaId == "00000000") return null  // added the second part to prevent errors due to change in db
        Log.d("RecitationsRecitersMenuViewModel", "continueListeningMediaId: $mediaId")

        val reciterId = mediaId.substring(0, 3).toInt()
        val narrationId = mediaId.substring(3, 6).toInt()
        val suraId = mediaId.substring(6).toInt()
        Log.d(
            "RecitationsRecitersMenuViewModel",
            "reciterId: $reciterId, narrationId: $narrationId, suraIndex: $suraId"
        )

        val language = getLanguage()
        return LastPlayedMedia(
            reciterName = getReciterName(reciterId, language),
            suraName = getSuraNames(language)[suraId],
            narrationName = getNarration(reciterId, narrationId, language).name
        )
    }

    suspend fun getNarration(reciterId: Int, narrationId: Int, language: Language) =
        recitationsRepository.getNarration(reciterId, narrationId, language)

    suspend fun getReciterName(reciterId: Int, language: Language) =
        recitationsRepository.getSuraReciterName(reciterId, language)

    suspend fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    suspend fun getNarrationSelections(language: Language) =
        recitationsRepository.getNarrationSelections(language)

    fun getLastPlayed() = recitationsRepository.getLastPlayedMedia()

}