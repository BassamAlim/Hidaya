package bassamalim.hidaya.features.prayers

import bassamalim.hidaya.core.enums.NotificationType

data class PrayerData(
    val name: String,
    var time: String,
    var notificationType: NotificationType,
    var timeOffset: Int,
    var reminderOffset: Int
) {

    fun getText() = "$name: $time"

}