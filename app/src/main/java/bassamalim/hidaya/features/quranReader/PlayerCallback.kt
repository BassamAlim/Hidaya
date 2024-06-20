package bassamalim.hidaya.features.quranReader

interface PlayerCallback {
    fun getPbState(): Int
    fun updatePbState(state: Int)
    fun track(ayaId: Int)
}