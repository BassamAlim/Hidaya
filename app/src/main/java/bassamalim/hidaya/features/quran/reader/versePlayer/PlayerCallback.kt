package bassamalim.hidaya.features.quran.reader.versePlayer

interface PlayerCallback {
    fun getPbState(): Int
    fun updatePbState(state: Int)
    suspend fun track(verseId: Int)
}