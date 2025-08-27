package bassamalim.hidaya.core.data.dataSources.preferences.objects

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat
import kotlinx.serialization.Serializable

@Serializable
data class AppSettingsPreferences(
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC,
    val theme: Theme = Theme.LIGHT, // TODO add system theme
    val timeFormat: TimeFormat = TimeFormat.TWELVE,
    val dateOffset: Int = 0,
)