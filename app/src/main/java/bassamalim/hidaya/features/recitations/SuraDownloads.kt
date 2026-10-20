package bassamalim.hidaya.features.recitations

import android.app.Application
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import bassamalim.hidaya.core.data.repositories.RecitationsRepository
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.helpers.ReceiverWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Downloads single suras and tells their download state, for the menus that list the recitations
 * of one sura. Sura ids are 0-based.
 */
class SuraDownloads @Inject constructor(
    app: Application,
    private val recitationsRepository: RecitationsRepository
) {

    private data class Key(val reciterId: Int, val narrationId: Int, val suraId: Int)

    // What the user just asked for, shown until the files catch up: a download only reaches the
    // disk (and the repository's downloading list) a moment after it's requested
    private val pending = MutableStateFlow(emptyMap<Key, DownloadState>())

    /** Emits whenever a state may have changed, so [getState] should be asked again. */
    val changes: Flow<Unit> = pending.map {}

    private val receiver = ReceiverWrapper(
        context = app,
        intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                recitationsRepository.popFromDownloading(
                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                )
                // The files tell the truth from here; the ones still downloading keep saying so
                pending.update { states ->
                    states.filterValues { it != DownloadState.DOWNLOADING }
                }
            }
        }
    )

    fun register() = receiver.register()

    fun unregister() = receiver.unregister()

    /** Checks the disk, so keep it off the main thread when asking for many. */
    fun getState(reciterId: Int, narrationId: Int, suraId: Int) =
        pending.value[Key(reciterId, narrationId, suraId)]
            ?: recitationsRepository.getSuraDownloadState(reciterId, narrationId, suraId)

    /** Downloads the sura, or deletes it if it's downloaded. */
    fun toggle(reciterId: Int, narrationId: Int, suraId: Int, suraName: String, server: String) {
        val key = Key(reciterId, narrationId, suraId)

        when (getState(reciterId, narrationId, suraId)) {
            DownloadState.NOT_DOWNLOADED -> {
                pending.update { it + (key to DownloadState.DOWNLOADING) }
                recitationsRepository.downloadSura(
                    reciterId = reciterId,
                    narrationId = narrationId,
                    suraId = suraId,
                    suraSearchName = suraName,
                    server = server
                )
            }
            DownloadState.DOWNLOADED -> {
                pending.update { it + (key to DownloadState.NOT_DOWNLOADED) }
                recitationsRepository.deleteSura(reciterId, narrationId, suraId)
            }
            DownloadState.DOWNLOADING -> {}
        }
    }

}
