package bassamalim.hidaya.features.prayers.prayersMain.ui

import bassamalim.hidaya.features.prayers.prayerSettings.ui.PrayerSettings

data class PrayerData(
    val name: String,
    var time: String,
    var settings: PrayerSettings
) {

    fun getText() = "$name: $time"

}