package bassamalim.hidaya.features.prayers

import bassamalim.hidaya.features.prayerSetting.PrayerSettings

data class PrayerData(
    val name: String,
    var time: String,
    var settings: PrayerSettings
) {

    fun getText() = "$name: $time"

}