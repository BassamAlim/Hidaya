package bassamalim.hidaya.features.recitations.recitationRecitersMenu.domain

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.Recitation
import bassamalim.hidaya.core.utils.FileUtils
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Locale
import javax.inject.Inject

class RecitationRecitersMenuDomain @Inject constructor(
    private val app: Application,
    private val recitationsRepository: RecitationsRepository,
    private val quranRepository: QuranRepository,
    private val settingsRepository: AppSettingsRepository
) {

    private val downloading = HashMap<Long, Int>()

    fun registerDownloadReceiver(onComplete: BroadcastReceiver) {
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
    }

    fun unregisterDownloadReceiver(onComplete: BroadcastReceiver) {
        try {
            app.unregisterReceiver(onComplete)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    fun cleanFiles() {
        val mainDir = File(recitationsRepository.dir)
        FileUtils.deleteDirRecursive(mainDir)
    }

    fun downloadNarration(
        reciterId: Int,
        narration: Recitation.Narration,
        suraNames: List<String>,
        language: Language,
        suraString: String
    ) {
        Thread {
            val downloadManager = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            var request: DownloadManager.Request
            var posted = false
            for (i in 0..113) {
                if (narration.availableSuras.contains("," + (i + 1) + ",")) {
                    val link = String.format(
                        Locale.US,
                        "%s/%03d.mp3",
                        narration.server,
                        i + 1
                    )
                    val uri = Uri.parse(link)

                    request = DownloadManager.Request(uri)
                    request.setTitle(
                        "${getReciterName(reciterId, language)} ${narration.name}" +
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
                        downloading[downloadId] = narration.id
                        posted = true
                    }
                }
            }
        }.start()
    }

    fun getAllRecitations(language: Language) = recitationsRepository.getAllRecitations(language)

    fun deleteNarration(reciterId: Int, narration: Recitation.Narration) {
        FileUtils.deleteFile(
            context = app,
            path = "${recitationsRepository.prefix}$reciterId/${narration.id}"
        )
    }

    suspend fun getLanguage() = settingsRepository.getLanguage().first()

    fun getFavs() = recitationsRepository.getReciterFavorites()

    suspend fun setFav(reciterId: Int, fav: Int) {
        recitationsRepository.setReciterIsFavorite(reciterId, fav)
    }

    fun getDownloadingNarrationId(downloadId: Long) = downloading[downloadId]!!

    fun removeDownloading(downloadId: Long) {
        downloading.remove(downloadId)
    }

    fun observeAllReciters(language: Language) =
        recitationsRepository.observeAllReciters(language)

    fun getAllReciters(language: Language) =
        recitationsRepository.getAllReciters(language)

    fun getReciterNarrations(reciterId: Int, language: Language) =
        recitationsRepository.getReciterNarrations(reciterId, language)

    fun getAllNarrations(language: Language) = recitationsRepository.getAllNarrations(language)

    fun getNarration(reciterId: Int, narrationId: Int) =
        recitationsRepository.getNarration(reciterId, narrationId)

    fun getReciterName(reciterId: Int, language: Language) =
        recitationsRepository.getReciterName(reciterId, language)

    fun getSuraNames(language: Language) = quranRepository.getDecoratedSuraNames(language)

    fun getNarrationSelections() = recitationsRepository.getNarrationSelections()

    fun getLastPlayed() = recitationsRepository.getLastPlayed()

    suspend fun setFavorite(reciterId: Int, isFavorite: Boolean) {
        recitationsRepository.setReciterIsFavorite(
            reciterId = reciterId,
            isFavorite = if (isFavorite) 1 else 0
        )
    }

    fun checkIsDownloaded(reciterId: Int, narrationId: Int) =
        File("${recitationsRepository.dir}${reciterId}/${narrationId}").exists()

}