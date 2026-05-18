package bassamalim.hidaya.features.prayers.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.ui.components.DialogDismissButton
import bassamalim.hidaya.core.ui.components.DialogTitle
import bassamalim.hidaya.core.ui.components.MyCheckbox
import bassamalim.hidaya.core.ui.components.MyHorizontalDivider
import bassamalim.hidaya.core.ui.components.MyOutlinedButton
import bassamalim.hidaya.core.ui.components.MyText

enum class ReportStep { CHECKS, FORM, RESULT }

@Composable
fun PrayerTimesReportDialog(
    state: PrayersBoardUiState,
    onDismiss: () -> Unit,
    onOpenCalculationSettings: () -> Unit,
    onOpenLocator: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onTogglePrayer: (Prayer) -> Unit,
    onOpenCorrectTimePicker: (Prayer) -> Unit,
    onCorrectTimePickerDismiss: () -> Unit,
    onCorrectTimePickerConfirm: (Int, Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            DialogTitle(stringResource(reportTitleRes(state.report.step)))
        },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                when (state.report.step) {
                    ReportStep.CHECKS -> ChecksStep(
                        currentMethodName = state.report.currentMethodName,
                        locationName = state.locationName,
                        isAutoLocation = state.report.isAutoLocation,
                        onOpenCalculationSettings = onOpenCalculationSettings,
                        onOpenLocator = onOpenLocator
                    )
                    ReportStep.FORM -> ReportFormStep(
                        prayerNames = state.report.prayerNames,
                        computedTimes = state.report.computedTimes,
                        wrongPrayers = state.report.wrongPrayers,
                        correctTimes = state.report.correctTimes,
                        notes = state.report.notes,
                        onTogglePrayer = onTogglePrayer,
                        onOpenCorrectTimePicker = onOpenCorrectTimePicker,
                        onNotesChange = onNotesChange
                    )
                    ReportStep.RESULT -> ResultStep(
                        submitting = state.report.submitting,
                        success = state.report.submitted,
                        errorMessage = state.report.error
                    )
                }
            }
        },
        confirmButton = {
            when (state.report.step) {
                ReportStep.CHECKS -> {
                    TextButton(onClick = onNext) {
                        MyText(stringResource(R.string.report_still_wrong))
                    }
                }
                ReportStep.FORM -> {
                    TextButton(
                        onClick = onSubmit,
                        enabled = state.report.wrongPrayers.isNotEmpty() && !state.report.submitting
                    ) {
                        MyText(stringResource(R.string.report_submit))
                    }
                }
                ReportStep.RESULT -> {
                    TextButton(onClick = onDismiss) {
                        MyText(stringResource(R.string.close))
                    }
                }
            }
        },
        dismissButton = {
            when (state.report.step) {
                ReportStep.FORM -> {
                    TextButton(onClick = onBack) {
                        MyText(stringResource(R.string.back))
                    }
                }
                ReportStep.CHECKS -> {
                    DialogDismissButton(onDismiss = onDismiss)
                }
                else -> Unit
            }
        }
    )
}

@Composable
fun CorrectTimePickerHost(
    target: Prayer?,
    existing: Map<Prayer, String>,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    if (target == null) return
    val parts = existing[target]?.split(":")
    val initialHour = parts?.getOrNull(0)?.toIntOrNull() ?: 12
    val initialMinute = parts?.getOrNull(1)?.toIntOrNull() ?: 0
    bassamalim.hidaya.core.ui.components.TimePickerDialog(
        initialHour = initialHour,
        initialMinute = initialMinute,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

private fun reportTitleRes(step: ReportStep) = when (step) {
    ReportStep.CHECKS -> R.string.report_step_calc_title
    ReportStep.FORM -> R.string.report_step_form_title
    ReportStep.RESULT -> R.string.report_step_result_title
}

@Composable
private fun ChecksStep(
    currentMethodName: String,
    locationName: String,
    isAutoLocation: Boolean,
    onOpenCalculationSettings: () -> Unit,
    onOpenLocator: () -> Unit
) {
    MyText(
        text = stringResource(R.string.report_intro),
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        fontSize = 14.sp
    )

    CheckRow(
        label = stringResource(R.string.report_calc_method_label),
        value = currentMethodName,
        actionText = stringResource(R.string.change),
        onAction = onOpenCalculationSettings
    )

    MyHorizontalDivider(Modifier.padding(vertical = 8.dp))

    CheckRow(
        label = stringResource(R.string.report_location_label),
        value = locationName,
        valueIcon =
            if (isAutoLocation) Icons.Default.CheckCircle
            else Icons.Default.ErrorOutline,
        valueIconTint =
            if (isAutoLocation) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
        actionText = stringResource(R.string.change),
        onAction = onOpenLocator
    )
}

@Composable
private fun CheckRow(
    label: String,
    value: String,
    actionText: String,
    onAction: () -> Unit,
    valueIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    valueIconTint: androidx.compose.ui.graphics.Color =
        androidx.compose.ui.graphics.Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            MyText(
                text = label,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 13.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (valueIcon != null) {
                    Icon(
                        imageVector = valueIcon,
                        contentDescription = null,
                        tint = valueIconTint,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 4.dp)
                    )
                }
                MyText(
                    text = value,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        TextButton(onClick = onAction) {
            MyText(text = actionText, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ReportFormStep(
    prayerNames: Map<Prayer, String>,
    computedTimes: Map<Prayer, String>,
    wrongPrayers: Set<Prayer>,
    correctTimes: Map<Prayer, String>,
    notes: String,
    onTogglePrayer: (Prayer) -> Unit,
    onOpenCorrectTimePicker: (Prayer) -> Unit,
    onNotesChange: (String) -> Unit
) {
    MyText(
        text = stringResource(R.string.report_step_form_hint),
        textAlign = TextAlign.Start,
        modifier = Modifier.fillMaxWidth()
    )

    Column(Modifier.padding(top = 8.dp)) {
        prayerNames.forEach { (prayer, name) ->
            val isChecked = prayer in wrongPrayers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyCheckbox(
                    isChecked = isChecked,
                    onCheckedChange = { onTogglePrayer(prayer) }
                )

                MyText(
                    text = "$name (${computedTimes[prayer] ?: ""})",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = TextAlign.Start
                )
            }

            if (isChecked) {
                val current = correctTimes[prayer].orEmpty()
                MyOutlinedButton(
                    text =
                        if (current.isNotEmpty())
                            stringResource(R.string.report_correct_time_value, current)
                        else
                            stringResource(R.string.report_correct_time_set),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 36.dp, end = 8.dp, bottom = 6.dp),
                    onClick = { onOpenCorrectTimePicker(prayer) }
                )
            }
        }
    }

    MyHorizontalDivider(Modifier.padding(vertical = 8.dp))

    var localNotes by remember { mutableStateOf(notes) }
    OutlinedTextField(
        value = localNotes,
        onValueChange = {
            localNotes = it
            onNotesChange(it)
        },
        label = { MyText(stringResource(R.string.report_notes_label)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        minLines = 2,
        maxLines = 5
    )
}

@Composable
private fun ResultStep(
    submitting: Boolean,
    success: Boolean,
    errorMessage: String?
) {
    MyText(
        text = when {
            submitting -> stringResource(R.string.report_submitting)
            success -> stringResource(R.string.report_submitted)
            errorMessage != null -> errorMessage
            else -> ""
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )
}
