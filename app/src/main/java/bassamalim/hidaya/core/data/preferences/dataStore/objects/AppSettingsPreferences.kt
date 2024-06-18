package bassamalim.hidaya.core.data.preferences.dataStore.objects

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsPreferences(
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC,
    val timeFormat: TimeFormat = TimeFormat.TWELVE,
    val theme: Theme = Theme.DARK, // TODO add system theme
    val dateOffset: Int = 0,
)