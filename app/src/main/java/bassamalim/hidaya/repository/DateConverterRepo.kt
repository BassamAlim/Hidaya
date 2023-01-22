package bassamalim.hidaya.repository

import android.content.Context
import android.content.SharedPreferences
import bassamalim.hidaya.R
import javax.inject.Inject

class DateConverterRepo @Inject constructor(
    private val context: Context,
    val pref: SharedPreferences
) {

    fun getHijriMonths(): Array<String> {
        return context.resources.getStringArray(R.array.numbered_hijri_months)
    }

    fun getGregorianMonths(): Array<String> {
        return context.resources.getStringArray(R.array.numbered_gregorian_months)
    }

}