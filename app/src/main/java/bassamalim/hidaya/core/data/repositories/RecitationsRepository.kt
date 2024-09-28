package bassamalim.hidaya.core.data.repositories

import android.app.Application
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.RecitationsPreferencesDataSource
import bassamalim.hidaya.core.data.dataSources.room.daos.RecitationNarrationsDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RecitationRecitersDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VerseRecitationsDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VerseRecitersDao
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.DownloadState
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.core.models.Narration
import bassamalim.hidaya.core.models.Recitation
import bassamalim.hidaya.core.models.Reciter
import bassamalim.hidaya.features.recitations.recitersMenu.domain.LastPlayedMedia
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RecitationsRepository @Inject constructor(
    app: Application,
    private val recitationsPreferencesDataSource: RecitationsPreferencesDataSource,
    private val recitationRecitersDao: RecitationRecitersDao,
    private val verseRecitationsDao: VerseRecitationsDao,
    private val verseRecitersDao: VerseRecitersDao,
    private val recitationNarrationsDao: RecitationNarrationsDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) {

    val prefix = "/Telawat/"
    val dir = "${app.getExternalFilesDir(null)}$prefix"
    private val downloading = mutableMapOf<Long, Pair<Int, Int>>()

    fun observeAllReciters(language: Language) =
        recitationRecitersDao.observeAll().map {
            it.map { reciter ->
                Reciter(
                    id = reciter.id,
                    name = when (language) {
                        Language.ARABIC -> reciter.nameAr
                        Language.ENGLISH -> reciter.nameEn
                    },
                    isFavorite = reciter.isFavorite != 0
                )
            }
        }

    suspend fun getAllReciters(language: Language) = withContext(dispatcher) {
        recitationRecitersDao.getAll().map {
            it.let {
                Reciter(
                    id = it.id,
                    name = when (language) {
                        Language.ARABIC -> it.nameAr
                        Language.ENGLISH -> it.nameEn
                    },
                    isFavorite = it.isFavorite != 0
                )
            }
        }
    }

    private fun getReciterFavoriteStatuses() = recitationRecitersDao.observeFavoriteStatuses().map {
        it.map { (id, isFavorite) -> id to (isFavorite == 1) }.toMap()
    }

    suspend fun setReciterFavorite(reciterId: Int, isFavorite: Boolean) {
        withContext(dispatcher) {
            recitationRecitersDao.setFavoriteStatus(
                id = reciterId,
                value = if (isFavorite) 1 else 0
            )
        }
        updateReciterFavoritesBackup(getReciterFavoriteStatuses().first())
    }

    suspend fun setReciterFavorites(favorites: Map<Int, Boolean>) {
        favorites.map { (id, isFavorite) ->
            setReciterFavorite(id, isFavorite)
        }
    }

    suspend fun getReciterNames(language: Language) = withContext(dispatcher) {
        when (language) {
            Language.ARABIC -> recitationRecitersDao.getNamesAr()
            Language.ENGLISH -> recitationRecitersDao.getNamesEn()
        }
    }

    fun getReciterFavoritesBackup() = recitationsPreferencesDataSource.getReciterFavorites()

    private suspend fun updateReciterFavoritesBackup(favorites: Map<Int, Boolean>) {
        recitationsPreferencesDataSource.updateReciterFavorites(favorites.toPersistentMap())
    }

    suspend fun getAllNarrations(language: Language) = withContext(dispatcher) {
        recitationNarrationsDao.getAll().map {
            Narration(
                id = it.id,
                reciterId = it.reciterId,
                name = when (language) {
                    Language.ARABIC -> it.nameAr
                    Language.ENGLISH -> it.nameEn
                },
                server = it.url,
                availableSuras = it.availableSuras
            )
        }
    }

    suspend fun getReciterNarrations(reciterId: Int, language: Language) = withContext(dispatcher) {
        recitationNarrationsDao.getReciterNarrations(reciterId).let { narrations ->
            narrations.map {
                Recitation.Narration(
                    id = it.id,
                    name = when (language) {
                        Language.ARABIC -> it.nameAr
                        Language.ENGLISH -> it.nameEn
                    },
                    server = it.url,
                    availableSuras = it.availableSuras
                )
            }
        }
    }

    suspend fun getNarrationSelections(language: Language): Map<String, Boolean> {
        val narrations = getAllNarrations(language).distinct()
        val selections = recitationsPreferencesDataSource.getNarrationSelections()
            .first().toMutableMap()

        var added = false
        for (narration in narrations) {
            if (!selections.containsKey(narration.name)) {
                selections[narration.name] = true
                added = true
            }
        }
        if (added) setNarrationSelections(selections)

        return selections
    }

    suspend fun setNarrationSelections(selections: Map<String, Boolean>) {
        recitationsPreferencesDataSource.updateNarrationSelections(selections.toPersistentMap())
    }

    private fun isFileExists(reciterId: Int, narrationId: Int) =
        File("${dir}${reciterId}/${narrationId}").exists()

    fun getNarrationDownloadStates(ids: Map<Int, List<Int>>): Map<Int, Map<Int, DownloadState>> {
        return ids.map { (reciterId, narrationIds) ->
            reciterId to narrationIds.associateWith { narrationId ->
                if (isFileExists(reciterId, narrationId)) {
                    if (isDownloading(reciterId, narrationId)) DownloadState.DOWNLOADING
                    else DownloadState.DOWNLOADED
                }
                else DownloadState.NOT_DOWNLOADED
            }
        }.toMap()
    }

    private fun isDownloading(reciterId: Int, narrationId: Int) =
        downloading.containsValue(Pair(reciterId, narrationId))

    fun addToDownloading(downloadId: Long, reciterId: Int, narrationId: Int) {
        downloading[downloadId] = Pair(reciterId, narrationId)
    }

    fun removeFromDownloading(downloadId: Long) {
        downloading.remove(downloadId)
    }

    fun getRepeatMode() = recitationsPreferencesDataSource.getRepeatMode()

    suspend fun setRepeatMode(mode: Int) {
        recitationsPreferencesDataSource.updateRepeatMode(mode)
    }

    fun getShuffleMode() = recitationsPreferencesDataSource.getShuffleMode()

    suspend fun setShuffleMode(mode: Int) {
        recitationsPreferencesDataSource.updateShuffleMode(mode)
    }

    fun getLastPlayedMedia() = recitationsPreferencesDataSource.getLastPlayedMedia()

    suspend fun setLastPlayedMedia(lastPlayed: LastPlayedMedia) {
        recitationsPreferencesDataSource.updateLastPlayedMedia(lastPlayed)
    }

    fun getVerseReciterId() = recitationsPreferencesDataSource.getVerseReciterId()

    suspend fun setVerseReciterId(verseReciterId: Int) {
        recitationsPreferencesDataSource.updateVerseReciterId(verseReciterId)
    }

    fun getVerseRepeatMode() = recitationsPreferencesDataSource.getVerseRepeatMode()

    suspend fun setVerseRepeatMode(verseRepeatMode: VerseRepeatMode) {
        recitationsPreferencesDataSource.updateVerseRepeatMode(verseRepeatMode)
    }

    fun getShouldStopOnSuraEnd() = recitationsPreferencesDataSource.getShouldStopOnSuraEnd()

    suspend fun setShouldStopOnSuraEnd(shouldStopOnSuraEnd: Boolean) {
        recitationsPreferencesDataSource.updateShouldStopOnSuraEnd(shouldStopOnSuraEnd)
    }

    fun getShouldStopOnPageEnd() = recitationsPreferencesDataSource.getShouldStopOnPageEnd()

    suspend fun setShouldStopOnPageEnd(shouldStopOnPageEnd: Boolean) {
        recitationsPreferencesDataSource.updateShouldStopOnPageEnd(shouldStopOnPageEnd)
    }

    suspend fun getVerseReciterNames() = withContext(dispatcher) {
        verseRecitersDao.getNames()
    }

    suspend fun getReciter(id: Int, language: Language) = withContext(dispatcher) {
        recitationRecitersDao.getReciter(id).let {
            Reciter(
                id = it.id,
                name = when (language) {
                    Language.ARABIC -> it.nameAr
                    Language.ENGLISH -> it.nameEn
                },
                isFavorite = it.isFavorite != 0
            )
        }
    }

    suspend fun getReciterName(id: Int, language: Language) = withContext(dispatcher) {
        when (language) {
            Language.ARABIC -> recitationRecitersDao.getNameAr(id)
            Language.ENGLISH -> recitationRecitersDao.getNameEn(id)
        }
    }

    suspend fun getNarration(reciterId: Int, narrationId: Int) = withContext(dispatcher) {
        recitationNarrationsDao.getNarration(reciterId, narrationId)
    }

    suspend fun getAllVerseRecitations() = withContext(dispatcher) {
        verseRecitationsDao.getAll()
    }

    suspend fun getReciterVerseRecitations(reciterId: Int) = withContext(dispatcher) {
        verseRecitationsDao.getReciterRecitations(reciterId)
    }

}