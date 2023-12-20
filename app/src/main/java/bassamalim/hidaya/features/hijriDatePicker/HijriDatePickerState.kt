package bassamalim.hidaya.features.hijriDatePicker

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar

data class HijriDatePickerState(
    val selectorMode: SelectorMode = SelectorMode.DAY_MONTH,
    val selected: UmmalquraCalendar = UmmalquraCalendar(),
)