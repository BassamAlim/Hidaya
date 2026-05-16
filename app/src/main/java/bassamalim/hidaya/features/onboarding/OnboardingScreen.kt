package bassamalim.hidaya.features.onboarding

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.features.settings.AppearanceSettings
import bassamalim.hidaya.features.settings.MenuSetting

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current!!

    Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyText(
                text = stringResource(R.string.welcome_message),
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onSurface
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

            MenuSetting(
                selection = state.calculationMethod,
                items = PrayerTimeCalculationMethod.entries.toTypedArray(),
                entries = stringArrayResource(R.array.prayer_times_calc_method_entries),
                title = stringResource(R.string.calculation_method_title),
                onSelection = viewModel::onCalculationMethodChange
            )

            MenuSetting(
                selection = state.juristicMethod,
                items = PrayerTimeJuristicMethod.entries.toTypedArray(),
                entries = stringArrayResource(R.array.juristic_method_entries),
                title = stringResource(R.string.juristic_method_title),
                onSelection = viewModel::onJuristicMethodChange
            )

            MenuSetting(
                selection = state.highLatitudesAdjustment,
                items = HighLatitudesAdjustmentMethod.entries.toTypedArray(),
                entries = stringArrayResource(R.array.high_lat_adjustment_entries),
                title = stringResource(R.string.high_lat_adjustment_title),
                onSelection = viewModel::onHighLatitudesAdjustmentChange
            )

            Button(onClick = viewModel::onSaveClick) {
                MyText(
                    text = stringResource(R.string.save),
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 24.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}