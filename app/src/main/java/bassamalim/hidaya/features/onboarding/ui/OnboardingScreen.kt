package bassamalim.hidaya.features.onboarding.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.ui.components.MyRectangleButton
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.features.settings.ui.AppearanceSettings

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current!!

    Box(
        Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = stringResource(R.string.welcome_message),
                fontSize = 26.sp
            )

            AppearanceSettings(
                selectedLanguage = state.language,
                onLanguageChange = { language -> viewModel.onLanguageChange(language, activity) },
                selectedNumeralsLanguage = state.numeralsLanguage,
                onNumeralsLanguageChange = viewModel::onNumeralsLanguageChange,
                selectedTimeFormat = state.timeFormat,
                onTimeFormatChange = viewModel::onTimeFormatChange,
                selectedTheme = state.theme,
                onThemeChange = viewModel::onThemeChange,
                numeralsLanguage = state.numeralsLanguage
            )

            MyRectangleButton(
                text = stringResource(R.string.save),
                fontSize = 24.sp,
                innerPadding = PaddingValues(vertical = 2.dp, horizontal = 25.dp),
                modifier = Modifier.padding(bottom = 10.dp),
                onClick = viewModel::onSaveClick
            )
        }
    }
}