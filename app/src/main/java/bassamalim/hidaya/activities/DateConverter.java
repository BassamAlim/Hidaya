package bassamalim.hidaya.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;

import java.util.Calendar;

import bassamalim.hidaya.R;
import bassamalim.hidaya.databinding.ActivityDateConverterBinding;
import bassamalim.hidaya.other.Global;
import bassamalim.hidaya.other.Utils;
import bassamalim.hidaya.replacements.HijriDatePicker;

public class DateConverter extends AppCompatActivity {

    private ActivityDateConverterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityDateConverterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.home.setOnClickListener(v -> onBackPressed());

        setListeners();
    }

    private void setListeners() {
        binding.hijriToGregorianBtn.setOnClickListener(v -> pickHijri());
        binding.gregorianToHijriBtn.setOnClickListener(v -> pickGregorian());
    }

    private void pickGregorian() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, day) -> {
            Calendar choice = Calendar.getInstance();
            choice.set(Calendar.YEAR, year);
            choice.set(Calendar.MONTH, month);    // starts from 0
            choice.set(Calendar.DATE, day);

            show(gregorianToHijri(choice), choice);
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));
        datePicker.setButton(DatePickerDialog.BUTTON_POSITIVE, ("حفظ"), datePicker);
        datePicker.setButton(DatePickerDialog.BUTTON_NEGATIVE, ("إلغاء"), datePicker);
        datePicker.show();
    }

    private void pickHijri() {
        HijriDatePicker hijriPicker = new HijriDatePicker();

        hijriPicker.setListener((view, year, month, day) -> {
            Log.d(Global.TAG, "Here");
            Calendar choice = new UmmalquraCalendar();
            choice.set(Calendar.YEAR, year);
            choice.set(Calendar.MONTH, month-1);    // starts from 0
            choice.set(Calendar.DATE, day);

            show(choice, hijriToGregorian(choice));
        });

        hijriPicker.show(getSupportFragmentManager(), "HijriDatePicker");
    }

    private Calendar gregorianToHijri(Calendar gregorian) {
        Calendar hijri = new UmmalquraCalendar();
        hijri.setTime(gregorian.getTime());
        return hijri;
    }

    private Calendar hijriToGregorian(Calendar hijri) {
        Calendar gregorian = Calendar.getInstance();
        gregorian.setTime(hijri.getTime());
        return gregorian;
    }

    private void show(Calendar hijri, Calendar gregorian) {
        binding.hijriYearTv.setText(Utils.translateNumbers(
                String.valueOf(hijri.get(Calendar.YEAR))));
        binding.hijriMonthTv.setText(getResources().getStringArray(
                R.array.numbered_hijri_months)[hijri.get(Calendar.MONTH)]);
        binding.hijriDayTv.setText(Utils.translateNumbers(
                String.valueOf(hijri.get(Calendar.DATE))));

        binding.gregorianYearTv.setText(Utils.translateNumbers(
                String.valueOf(gregorian.get(Calendar.YEAR))));
        binding.gregorianMonthTv.setText(getResources().getStringArray(
                R.array.numbered_gregorian_months)[gregorian.get(Calendar.MONTH)]);
        binding.gregorianDayTv.setText(Utils.translateNumbers(
                String.valueOf(gregorian.get(Calendar.DATE))));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}