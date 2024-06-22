package bassamalim.hidaya.features.hijriDatePicker

import bassamalim.hidaya.core.enums.Language
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar

data class HijriDatePickerState(
    val selectorMode: SelectorMode = SelectorMode.DAY_MONTH,
    val selected: UmmalquraCalendar = UmmalquraCalendar(),
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC,
)