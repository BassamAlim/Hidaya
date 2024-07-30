package bassamalim.hidaya.features.prayerReminder.data

import android.content.res.Resources
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.preferences.dataSources.AppSettingsPreferencesDataSource
import bassamalim.hidaya.core.data.preferences.dataSources.PrayersPreferencesDataSource
import bassamalim.hidaya.core.enums.PID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrayerReminderRepository @Inject constructor(
    private val res: Resources,
    private val appSettingsPrefsRepo: AppSettingsPreferencesDataSource,
    private val prayersPrefsRepo: PrayersPreferencesDataSource
) {

    suspend fun numeralsLanguage() = appSettingsPrefsRepo.getNumeralsLanguage().first()

    suspend fun getOffset(pid: PID) =
        prayersPrefsRepo.getTimeOffsets().map { it[pid]!! }.first()

    fun getPrayerName(pid: PID) =
        res.getStringArray(R.array.prayer_names)[pid.ordinal]!!

}