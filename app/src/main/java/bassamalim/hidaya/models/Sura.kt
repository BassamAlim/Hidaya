package bassamalim.hidaya.models

import android.view.View
import java.io.Serializable

data class Sura(
    private val number: Int,
    private val suraName: String,
    private val searchName: String,
    private val tanzeel: Int,
    private var favorite: Int,
    private val cardListener: View.OnClickListener
) : Serializable {

    fun getNumber(): Int {
        return number
    }

    fun getSuraName(): String {
        return suraName
    }

    fun getSearchName(): String {
        return searchName
    }

    fun getTanzeel(): Int {
        return tanzeel
    }

    fun getFavorite(): Int {
        return favorite
    }

    fun getCardListener(): View.OnClickListener {
        return cardListener
    }

    fun setFavorite(favorite: Int) {
        this.favorite = favorite
    }
}