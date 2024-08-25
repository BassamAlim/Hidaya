package bassamalim.hidaya.core.data.repositories

import android.app.Application
import bassamalim.hidaya.core.data.database.daos.RecitationNarrationsDao
import bassamalim.hidaya.core.data.database.daos.RecitationRecitersDao
import bassamalim.hidaya.core.data.database.daos.VerseRecitationsDao
import bassamalim.hidaya.core.data.database.daos.VerseRecitersDao
import bassamalim.hidaya.core.data.preferences.dataSources.RecitationsPreferencesDataSource
import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.VerseRepeatMode
import bassamalim.hidaya.core.models.Recitation
import bassamalim.hidaya.core.models.Reciter
import bassamalim.hidaya.features.recitations.recitationRecitersMenu.domain.LastPlayedRecitation
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecitationsRepository @Inject constructor(
    app: Application,
    private val recitationsPreferencesDataSource: RecitationsPreferencesDataSource,
    private val recitationRecitersDao: RecitationRecitersDao,
    private val verseRecitationsDao: VerseRecitationsDao,
    private val verseRecitersDao: VerseRecitersDao,
    private val recitationNarrationsDao: RecitationNarrationsDao
) {

    val prefix = "/Telawat/"
    val dir = "${app.getExternalFilesDir(null)}$prefix"

    fun observeAllReciters(language: Language) =
        recitationRecitersDao.observeAll().map {
            it.map { reciter ->
                Reciter(
                    id = reciter.id,
                    name = if (language == Language.ARABIC) reciter.nameAr else reciter.nameEn,
                    isFavorite = reciter.isFavorite != 0
                )
            }
        }

    fun getAllReciters(language: Language) = recitationRecitersDao.getAll().map {
        it.let {
            Reciter(
                id = it.id,
                name = if (language == Language.ARABIC) it.nameAr else it.nameEn,
                isFavorite = it.isFavorite != 0
            )
        }
    }

    fun getReciterFavorites() = recitationsPreferencesDataSource.flow.map {
        it.reciterFavorites.toMap()
    }

    suspend fun setReciterFavorites(favorites: Map<Int, Int>) {
        recitationsPreferencesDataSource.update { it.copy(
            reciterFavorites = favorites.toPersistentMap()
        )}
    }

    suspend fun setReciterIsFavorite(reciterId: Int, isFavorite: Int) {
        recitationsPreferencesDataSource.update { it.copy(
            reciterFavorites = it.reciterFavorites.put(reciterId, isFavorite)
        )}
    }

    fun getAllNarrations(language: Language) = recitationNarrationsDao.getAll().map {
        Recitation.Narration(
            id = it.id,
            name = if (language == Language.ARABIC) it.nameAr else it.nameEn,
            server = it.url,
            availableSuras = it.availableSuras
        )
    }

    fun getReciterWithNarrations(reciterId: Int, language: Language): Recitation {
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

    fun getAllRecitations(language: Language): List<Recitation> {
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

    fun getReciterNarrations(reciterId: Int, language: Language) =
        recitationNarrationsDao.getReciterNarrations(reciterId).let { narrations ->
            narrations.map {
                Recitation.Narration(
                    id = it.id,
                    name = if (language == Language.ARABIC) it.nameAr else it.nameEn,
                    server = it.url,
                    availableSuras = it.availableSuras
                )
            }
        }

    fun getNarrationSelections() = recitationsPreferencesDataSource.flow.map {
        val selections = it.narrationSelections.toMap()
        selections.ifEmpty {
            val narrations = recitationNarrationsDao.getAll()
            narrations.associate { it.id to true }
        }
    }

    suspend fun setNarrationSelections(selections: Map<Int, Boolean>) {
        recitationsPreferencesDataSource.update { it.copy(
            narrationSelections = selections.toPersistentMap()
        )}
    }

    fun getRepeatMode() = recitationsPreferencesDataSource.flow.map {
        it.repeatMode
    }

    suspend fun setRepeatMode(mode: Int) {
        recitationsPreferencesDataSource.update { it.copy(
            repeatMode = mode
        )}
    }

    fun getShuffleMode() = recitationsPreferencesDataSource.flow.map {
        it.shuffleMode
    }

    suspend fun setShuffleMode(mode: Int) {
        recitationsPreferencesDataSource.update { it.copy(
            shuffleMode = mode
        )}
    }

    fun getLastPlayed() = recitationsPreferencesDataSource.flow.map {
        it.lastPlayed
    }

    suspend fun setLastPlayedMediaId(lastPlayed: LastPlayedRecitation) {
        recitationsPreferencesDataSource.update { it.copy(
            lastPlayed = lastPlayed
        )}
    }

    fun getVerseReciterId() = recitationsPreferencesDataSource.flow.map {
        it.verseReciterId
    }

    suspend fun setVerseReciterId(verseReciterId: Int) {
        recitationsPreferencesDataSource.update { it.copy(
            verseReciterId = verseReciterId
        )}
    }

    fun getVerseRepeatMode() = recitationsPreferencesDataSource.flow.map {
        it.verseRepeatMode
    }

    suspend fun setVerseRepeatMode(verseRepeatMode: VerseRepeatMode) {
        recitationsPreferencesDataSource.update { it.copy(
            verseRepeatMode = verseRepeatMode
        )}
    }

    fun getShouldStopOnSuraEnd() = recitationsPreferencesDataSource.flow.map {
        it.shouldStopOnSuraEnd
    }

    suspend fun setShouldStopOnSuraEnd(shouldStopOnSuraEnd: Boolean) {
        recitationsPreferencesDataSource.update { it.copy(
            shouldStopOnSuraEnd = shouldStopOnSuraEnd
        )}
    }

    fun getShouldStopOnPageEnd() = recitationsPreferencesDataSource.flow.map {
        it.shouldStopOnPageEnd
    }

    suspend fun setShouldStopOnPageEnd(shouldStopOnPageEnd: Boolean) {
        recitationsPreferencesDataSource.update { it.copy(
            shouldStopOnPageEnd = shouldStopOnPageEnd
        )}
    }

    fun getVerseReciterNames() = verseRecitersDao.getNames()

    fun getReciter(id: Int, language: Language) =
        recitationRecitersDao.getReciter(id).let {
            Reciter(
                id = it.id,
                name = if (language == Language.ARABIC) it.nameAr else it.nameEn,
                isFavorite = it.isFavorite != 0
            )
        }

    fun getReciterName(id: Int, language: Language) =
        if (language == Language.ARABIC) recitationRecitersDao.getNameAr(id)
        else recitationRecitersDao.getNameEn(id)

    fun getNarration(reciterId: Int, narrationId: Int) =
        recitationNarrationsDao.getNarration(reciterId, narrationId)

    fun getNarrations() = recitationNarrationsDao.getAll()

}