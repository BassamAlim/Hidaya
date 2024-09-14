package bassamalim.hidaya.features.prayers.board.ui

import bassamalim.hidaya.features.prayers.settings.ui.PrayerSettings

data class PrayerData(
    val name: String,
    var time: String,
    var settings: PrayerSettings
) {

    fun getText() = "$name: $time"

}