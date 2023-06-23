package bassamalim.hidaya.features.prayers

import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID

data class PrayerData(
    val pid: PID,
    val name: String,
    var time: String,
    var notificationType: NotificationType,
    var timeOffset: Int,
    var reminderOffset: Int
) {

    fun getText() = "$name: $time"

}