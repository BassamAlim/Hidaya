package bassamalim.hidaya.features.recitations.recitationSurasMenu.domain

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
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.models.ReciterSura
import bassamalim.hidaya.core.utils.FileUtils
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.Locale
import javax.inject.Inject

class RecitationSurasMenuDomain @Inject constructor(
    private val app: Application,
    private val recitationsRepository: RecitationsRepository,
    private val quranRepository: QuranRepository,
    private val appSettingsRepository: AppSettingsRepository
) {

    private val downloading = HashMap<Long, Int>()

    fun getDownloadingSuraId(downloadId: Long): Int = downloading[downloadId]!!

    fun download(sura: ReciterSura, server: String) {
        Thread {
            val link = String.format(Locale.US, "%s/%03d.mp3", server, sura.id + 1)
            val uri = Uri.parse(link)

            val request = DownloadManager.Request(uri)
            request.setTitle(sura.searchName)
            FileUtils.createDir(app, recitationsRepository.prefix)
            request.setDestinationInExternalFilesDir(
                app,
                recitationsRepository.prefix,
                "${sura.id}.mp3"
            )
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

            val downloadId = (app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager)
                .enqueue(request)
            downloading[downloadId] = sura.id
        }.start()
    }

    fun delete(reciterId: Int, narrationId: Int, suraId: Int) {
        FileUtils.deleteFile(
            context = app,
            path = "${recitationsRepository.prefix}$reciterId/$narrationId/$suraId.mp3"
        )
    }

    fun removeFromDownloading(downloadId: Long) {
        downloading.remove(downloadId)
    }

    fun checkIsDownloaded(suraNum: Int): Boolean {
        return File("${recitationsRepository.dir}$suraNum.mp3").exists()
    }

    fun getDownloadStates() = (0..113).associateWith {
        if (checkIsDownloaded(it)) DownloadState.DOWNLOADED
        else DownloadState.NOT_DOWNLOADED
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

    fun getSuraFavorites() = quranRepository.getSuraFavorites()

    suspend fun getLanguage() = appSettingsRepository.getLanguage().first()

    fun getDecoratedSuraNames(language: Language) =
        quranRepository.getDecoratedSuraNames(language)

    fun getPlainSuraNames() = quranRepository.getPlainSuraNames()

    fun getReciterName(id: Int, language: Language) =
        recitationsRepository.getReciterName(id, language)

    fun getNarration(reciterId: Int, narrationId: Int) =
        recitationsRepository.getNarration(reciterId, narrationId)

    fun getIsFavorites() = recitationsRepository.getReciterFavoritesBackup()

    suspend fun setIsFavorite(suraId: Int, value: Boolean) {
        quranRepository.setSuraIsFavorite(suraId, value)
    }

}