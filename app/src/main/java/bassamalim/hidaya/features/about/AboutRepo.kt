package bassamalim.hidaya.features.about

import androidx.datastore.core.DataStore
import bassamalim.hidaya.core.data.preferences.objects.AppStatePreferences
import javax.inject.Inject

class AboutRepo @Inject constructor(
    private val appStatePrefs: DataStore<AppStatePreferences>
) {

    fun getLastUpdate() =
        appStatePrefs.data

}