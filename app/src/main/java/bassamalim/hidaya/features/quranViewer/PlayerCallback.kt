package bassamalim.hidaya.features.quranViewer

interface PlayerCallback {
    fun getPbState(): Int
    fun updatePbState(state: Int)
    fun track(ayaId: Int)
}