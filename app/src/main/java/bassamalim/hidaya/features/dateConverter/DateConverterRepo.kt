package bassamalim.hidaya.features.dateConverter

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import javax.inject.Inject

class DateConverterRepo @Inject constructor(
    private val resources: Resources,
    private val preferencesDS: PreferencesDataSource
) {

    fun numeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getHijriMonths(): Array<String> =
        resources.getStringArray(R.array.numbered_hijri_months)

    fun getGregorianMonths(): Array<String> =
        resources.getStringArray(R.array.numbered_gregorian_months)

}