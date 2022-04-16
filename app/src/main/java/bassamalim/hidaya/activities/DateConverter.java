package bassamalim.hidaya.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import bassamalim.hidaya.databinding.ActivityDateConverterBinding;
import bassamalim.hidaya.other.Utils;

public class DateConverter extends AppCompatActivity {

    private ActivityDateConverterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.onActivityCreateSetTheme(this);
        binding = ActivityDateConverterBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.topBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

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
        Calendar now = new UmmalquraCalendar();

    }

    private Calendar gregorianToHijri(Calendar gregorian) {
        Calendar gCal = new GregorianCalendar(gregorian.get(Calendar.YEAR),
                gregorian.get(Calendar.MONTH), gregorian.get(Calendar.DATE));

        Calendar hijri = new UmmalquraCalendar();
        hijri.setTime(gCal.getTime());
        return hijri;
    }

    private Calendar hijriToGregorian(Calendar hijri) {
        Calendar gregorian = Calendar.getInstance();
        gregorian.setTime(hijri.getTime());
        return gregorian;
    }

    private void show(Calendar hijri, Calendar gregorian) {
        binding.hijriYearTv.setText(String.valueOf(hijri.get(Calendar.YEAR)));
        binding.hijriMonthTv.setText(String.valueOf(hijri.get(Calendar.MONTH)+1));
        binding.hijriDayTv.setText(String.valueOf(hijri.get(Calendar.DATE)));

        binding.gregorianYearTv.setText(String.valueOf(gregorian.get(Calendar.YEAR)));
        binding.gregorianMonthTv.setText(String.valueOf(gregorian.get(Calendar.MONTH)+1));
        binding.gregorianDayTv.setText(String.valueOf(gregorian.get(Calendar.DATE)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}