package bassamalim.hidaya.models

import android.view.View

class ReciterSura(
    private val num: Int,
    private val surahName: String,
    private val searchName: String,
    private var favorite: Int,
    private val listener: View.OnClickListener
) {
    fun getSurahName(): String {
        return surahName
    }

    fun getNum(): Int {
        return num
    }

    fun getSearchName(): String {
        return searchName
    }

    fun setFavorite(favorite: Int) {
        this.favorite = favorite
    }

    fun getFavorite(): Int {
        return favorite
    }

    fun getListener(): View.OnClickListener {
        return listener
    }
}