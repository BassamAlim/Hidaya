package bassamalim.hidaya.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import bassamalim.hidaya.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                MyText(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
                onDismiss()
            }) {
                MyText(stringResource(R.string.select))
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MyDatePickerDialog(
    initialDateMillis: Long,
    onSubmit: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogSubmitButton {
                onSubmit(datePickerState.selectedDateMillis)
                onDismiss()
            }
        },
        dismissButton = {
            DialogDismissButton { onDismiss() }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}