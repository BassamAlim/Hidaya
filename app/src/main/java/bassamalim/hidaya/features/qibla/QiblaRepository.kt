package bassamalim.hidaya.features.qibla

import bassamalim.hidaya.core.data.preferences.Preference
import bassamalim.hidaya.core.data.preferences.PreferencesDataSource
import bassamalim.hidaya.core.utils.LocUtils
import javax.inject.Inject

class QiblaRepository @Inject constructor(
    private val preferencesDS: PreferencesDataSource
) {

    fun numeralsLanguage() = preferencesDS.getNumeralsLanguage()

    fun getLocation() =
        LocUtils.retrieveLocation(preferencesDS.getString(Preference.StoredLocation))

}