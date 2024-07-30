package bassamalim.hidaya.features.main.data

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val resources: Resources,
    private val appSettingsPrefsRepo: AppSettingsPreferencesDataSource
) {

    suspend fun getNumeralsLanguage() = appSettingsPrefsRepo.getNumeralsLanguage().first()

    fun getDateOffset() = appSettingsPrefsRepo.getDateOffset()

    fun getWeekDays(): Array<String> = resources.getStringArray(R.array.week_days)

    fun getHijriMonths(): Array<String> = resources.getStringArray(R.array.hijri_months)

    fun getGregorianMonths(): Array<String> = resources.getStringArray(R.array.gregorian_months)

}