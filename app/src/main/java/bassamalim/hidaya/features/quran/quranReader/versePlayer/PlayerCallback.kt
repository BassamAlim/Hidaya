package bassamalim.hidaya.features.quranReader.ayaPlayer

interface PlayerCallback {
    fun getPbState(): Int
    fun updatePbState(state: Int)
    fun track(verseId: Int)
}