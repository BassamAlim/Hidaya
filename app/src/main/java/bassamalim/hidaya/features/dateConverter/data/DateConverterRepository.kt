package bassamalim.hidaya.features.dateConverter.data

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DateConverterRepository @Inject constructor(
    private val resources: Resources,
    private val appSettingsPrefsRepo: AppSettingsPreferencesDataSource
) {

    suspend fun getNumeralsLanguage() = appSettingsPrefsRepo.getNumeralsLanguage().first()

    fun getHijriMonths(): Array<String> =
        resources.getStringArray(R.array.numbered_hijri_months)

    fun getGregorianMonths(): Array<String> =
        resources.getStringArray(R.array.numbered_gregorian_months)

}