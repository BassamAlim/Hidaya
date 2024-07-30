package bassamalim.hidaya.core.data.repositories

import bassamalim.hidaya.core.data.preferences.dataSources.RecitationsPreferencesDataSource
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecitationsRepository @Inject constructor(
    private val recitationsPreferencesDataSource: RecitationsPreferencesDataSource
) {

    fun getReciterFavorites() = recitationsPreferencesDataSource.flow.map {
        it.reciterFavorites.toMap()
    }
    suspend fun setReciterFavorites(favorites: Map<Int, Int>) {
        recitationsPreferencesDataSource.update { it.copy(
            reciterFavorites = favorites.toPersistentMap()
        )}
    }

    fun getNarrationSelections() = recitationsPreferencesDataSource.flow.map {
        it.narrationSelections.toMap()
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

    fun getShuffle() = recitationsPreferencesDataSource.flow.map {
        it.shuffleMode
    }
    suspend fun setShuffle(shuffle: Int) {
        recitationsPreferencesDataSource.update { it.copy(
            shuffleMode = shuffle
        )}
    }

    fun getLastPlayedMediaId() = recitationsPreferencesDataSource.flow.map {
        it.lastPlayedMediaId
    }
    suspend fun setLastPlayedMediaId(mediaId: String) {
        recitationsPreferencesDataSource.update { it.copy(
            lastPlayedMediaId = mediaId
        )}
    }

    fun getLastProgress() = recitationsPreferencesDataSource.flow.map {
        it.lastProgress
    }
    suspend fun setLastProgress(progress: Long) {
        recitationsPreferencesDataSource.update { it.copy(
            lastProgress = progress
        )}
    }

}