package bassamalim.hidaya.features.onboarding

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.HighLatitudesAdjustmentMethod
import bassamalim.hidaya.core.enums.PrayerTimeCalculationMethod
import bassamalim.hidaya.core.enums.PrayerTimeJuristicMethod
import bassamalim.hidaya.core.ui.components.MySurface
import bassamalim.hidaya.core.ui.components.MyText
import bassamalim.hidaya.features.settings.AppearanceSettings
import bassamalim.hidaya.features.settings.CategoryTitle
import bassamalim.hidaya.features.settings.MenuSetting

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current!!

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp)
                .padding(top = 40.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeHeader()

            Spacer(Modifier.height(20.dp))

            MySurface {
                Column(Modifier.padding(vertical = 8.dp)) {
                    CategoryTitle(stringResource(R.string.appearance))

                    AppearanceSettings(
                        selectedLanguage = state.language,
                        onLanguageChange = { language ->
                            viewModel.onLanguageChange(language, activity)
                        },
                        selectedNumeralsLanguage = state.numeralsLanguage,
                        onNumeralsLanguageChange = viewModel::onNumeralsLanguageChange,
                        selectedTimeFormat = state.timeFormat,
                        onTimeFormatChange = viewModel::onTimeFormatChange,
                        selectedTheme = state.theme,
                        onThemeChange = viewModel::onThemeChange,
                        numeralsLanguage = state.numeralsLanguage
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            MySurface {
                Column(Modifier.padding(vertical = 8.dp)) {
                    CategoryTitle(stringResource(R.string.prayer_time_settings))

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
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = viewModel::onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                MyText(
                    text = stringResource(R.string.save),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WelcomeHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(colorResource(R.color.ic_launcher_background)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .padding(12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        MyText(
            text = stringResource(R.string.welcome_message),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
