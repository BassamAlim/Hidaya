package bassamalim.hidaya.features.prayers.ui

import bassamalim.hidaya.features.prayerSettings.ui.PrayerSettings

data class PrayerData(
    val name: String,
    var time: String,
    var settings: PrayerSettings
) {

    fun getText() = "$name: $time"

}