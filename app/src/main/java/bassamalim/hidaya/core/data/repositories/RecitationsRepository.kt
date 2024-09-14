package bassamalim.hidaya.core.data.repositories

import android.app.Application
import bassamalim.hidaya.core.data.dataSources.preferences.dataSources.RecitationsPreferencesDataSource
import bassamalim.hidaya.core.data.dataSources.room.daos.RecitationNarrationsDao
import bassamalim.hidaya.core.data.dataSources.room.daos.RecitationRecitersDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VerseRecitationsDao
import bassamalim.hidaya.core.data.dataSources.room.daos.VerseRecitersDao
import bassamalim.hidaya.core.di.DefaultDispatcher
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.core.models.Recitation
import bassamalim.hidaya.core.models.Reciter
import bassamalim.hidaya.features.recitations.recitersMenu.domain.LastPlayedMedia
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecitationsRepository @Inject constructor(
    app: Application,
    private val recitationsPreferencesDataSource: RecitationsPreferencesDataSource,
    private val recitationRecitersDao: RecitationRecitersDao,
    private val verseRecitationsDao: VerseRecitationsDao,
    private val verseRecitersDao: VerseRecitersDao,
    private val recitationNarrationsDao: RecitationNarrationsDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher
) {

    val prefix = "/Telawat/"
    val dir = "${app.getExternalFilesDir(null)}$prefix"

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

    fun getReciterFavoriteStatuses() = recitationRecitersDao.observeFavoriteStatuses().map {
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

    suspend fun getReciterWithNarrations(reciterId: Int, language: Language): Recitation {
        val reciter = getReciter(reciterId, language)
        val narrations = getReciterNarrations(reciterId, language)

        return Recitation(
            reciterId = reciter.id,
            reciterName = reciter.name,
            reciterIsFavorite = reciter.isFavorite,
            narrations = narrations.map {
                Recitation.Narration(
                    id = it.id,
                    name = it.name,
                    server = it.server,
                    availableSuras = it.availableSuras
                )
            }
        )
    }

    suspend fun getAllRecitations(language: Language): List<Recitation> {
        val reciters = getAllReciters(language)
        return reciters.map {
            Recitation(
                reciterId = it.id,
                reciterName = it.name,
                reciterIsFavorite = it.isFavorite,
                narrations = getReciterNarrations(it.id, language)
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

    fun getNarrationSelections() = recitationsPreferencesDataSource.getNarrationSelections().map {
        it.ifEmpty {
            withContext(dispatcher) {
                val narrations = recitationNarrationsDao.getAll()
                narrations.associate { narration -> narration.id to true }
            }
        }
    }

    suspend fun setNarrationSelections(selections: Map<Int, Boolean>) {
        recitationsPreferencesDataSource.updateNarrationSelections(selections.toPersistentMap())
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

    private suspend fun getReciter(id: Int, language: Language) = withContext(dispatcher) {
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

    suspend fun getAllNarrations() = withContext(dispatcher) {
        recitationNarrationsDao.getAll()
    }

    suspend fun getAllVerseRecitations() = withContext(dispatcher) {
        verseRecitationsDao.getAll()
    }

    suspend fun getReciterVerseRecitations(reciterId: Int) = withContext(dispatcher) {
        verseRecitationsDao.getReciterRecitations(reciterId)
    }

}