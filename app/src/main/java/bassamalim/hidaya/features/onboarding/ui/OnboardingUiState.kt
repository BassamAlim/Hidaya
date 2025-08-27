package bassamalim.hidaya.features.onboarding.ui

import bassamalim.hidaya.core.enums.Language
import bassamalim.hidaya.core.enums.Theme
import bassamalim.hidaya.core.enums.TimeFormat

data class OnboardingUiState(
    val language: Language = Language.ARABIC,
    val numeralsLanguage: Language = Language.ARABIC,
    val timeFormat: TimeFormat = TimeFormat.TWELVE,
    val theme: Theme = Theme.LIGHT
)