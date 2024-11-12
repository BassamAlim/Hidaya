package bassamalim.hidaya.core.models

import kotlinx.serialization.Serializable
import java.util.Calendar

@Serializable
data class TimeOfDay(
    val hour: Int,
    val minute: Int,
    val second: Int = 0
) {
    companion object {
        fun fromCalendar(calendar: Calendar) = TimeOfDay(
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            minute = calendar.get(Calendar.MINUTE)
        )
    }
}
