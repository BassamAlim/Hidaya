package bassamalim.hidaya.features.quran.reader.versePlayer

interface PlayerCallback {
    fun getPbState(): Int
    fun updatePbState(state: Int)
    fun track(verseId: Int)
}