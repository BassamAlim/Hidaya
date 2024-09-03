package bassamalim.hidaya.features.quran.quranReader.versePlayer

interface PlayerCallback {
    fun getPbState(): Int
    fun updatePbState(state: Int)
    fun track(verseId: Int)
}