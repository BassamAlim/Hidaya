package bassamalim.hidaya.features.prayerSetting

import android.content.SharedPreferences
import android.content.res.Resources
import bassamalim.hidaya.core.data.database.AppDatabase
import javax.inject.Inject

class PrayerSettingRepo @Inject constructor(
    private val res: Resources,
    private val sp: SharedPreferences,
    private val db: AppDatabase
) {

}