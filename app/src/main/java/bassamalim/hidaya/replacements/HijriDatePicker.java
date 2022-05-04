package bassamalim.hidaya.replacements;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;

import java.util.Calendar;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.other.Utils;

public class HijriDatePicker extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;
    private NumberPicker monthPicker;
    private NumberPicker yearPicker;
    private NumberPicker dayPicker;
    private final UmmalquraCalendar cal = new UmmalquraCalendar();

    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(getDialog()).getWindow().setBackgroundDrawableResource(R.color.bg_M);

        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                getResources().getColor(R.color.text_M, requireContext().getTheme()));
        ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                getResources().getColor(R.color.text_M, requireContext().getTheme()));
    }

    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialog = requireActivity().getLayoutInflater()
                .inflate(R.layout.date_picker, null);
        yearPicker = dialog.findViewById(R.id.picker_year);
        monthPicker = dialog.findViewById(R.id.picker_month);
        dayPicker = dialog.findViewById(R.id.picker_day);

        int MAX_YEAR = 2000;
        String[] tempArray = new String[MAX_YEAR];
        for (int i = 0; i < MAX_YEAR; i++)
            tempArray[i] = Utils.translateNumbers(getContext(), String.valueOf(i+1));

        yearPicker.setMinValue(1);
        yearPicker.setMaxValue(MAX_YEAR);
        yearPicker.setDisplayedValues(tempArray);
        yearPicker.setValue(cal.get(Calendar.YEAR));
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            cal.set(Calendar.YEAR, newVal);
            dayPicker.setMaxValue(cal.lengthOfMonth());
        });

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(cal.get(Calendar.MONTH)+1);
        monthPicker.setDisplayedValues(getResources()
                .getStringArray(R.array.numbered_hijri_months));
        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            cal.set(Calendar.MONTH, newVal-1);
            dayPicker.setMaxValue(cal.lengthOfMonth());
        });

        String[] daysNums = new String[30];
        for (int i = 0; i < daysNums.length; i++)
            daysNums[i] = Utils.translateNumbers(getContext(), String.valueOf(i+1));

        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(cal.lengthOfMonth());
        dayPicker.setValue(cal.get(Calendar.DATE));
        dayPicker.setDisplayedValues(daysNums);
        dayPicker.setOnValueChangedListener(
                (picker, oldVal, newVal) -> cal.set(Calendar.DATE, newVal));

        return new AlertDialog.Builder(requireActivity()).setView(dialog)
                .setPositiveButton("حفظ", (dialog1, id) -> {
                    int year = yearPicker.getValue();
                    listener.onDateSet(null, year,
                            monthPicker.getValue(), dayPicker.getValue());
                })
                .setNegativeButton(R.string.cancel, (dialog12, id) -> Objects.requireNonNull(
                        HijriDatePicker.this.getDialog()).cancel()).create();
    }
}
