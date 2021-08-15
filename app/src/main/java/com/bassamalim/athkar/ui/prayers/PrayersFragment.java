package com.bassamalim.athkar.ui.prayers;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bassamalim.athkar.MainActivity;
import com.bassamalim.athkar.databinding.PrayersFragmentBinding;
import java.util.ArrayList;
import java.util.HashMap;

public class PrayersFragment extends Fragment {

    private PrayersViewModel prayersViewModel;
    private PrayersFragmentBinding binding;
    public static ArrayList<String> times;
    public static String[] translatedTimes;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        prayersViewModel = new ViewModelProvider(this).get(PrayersViewModel.class);

        binding = PrayersFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        times = MainActivity.times;
        translatedTimes = translateNumbers(times);

        String fajrText = "الفجر: " + translatedTimes[0];
        String shorouqText = "الشروق: " + translatedTimes[1];
        String dhuhrText = "الظهر: " + translatedTimes[2];
        String asrText = "العصر: " + translatedTimes[3];
        String maghribText = "المغرب: " + translatedTimes[5];
        String ishaaText = "العشاء: " + translatedTimes[6];

        binding.fajrView.setText(fajrText);
        binding.shorouqView.setText(shorouqText);
        binding.dhuhrView.setText(dhuhrText);
        binding.asrView.setText(asrText);
        binding.maghribView.setText(maghribText);
        binding.ishaaView.setText(ishaaText);


        return root;
    }

    public static String[] translateNumbers(ArrayList<String> english) {
        String[] result = english.toArray(new String[english.size()]);

        HashMap<Character, Character> map = new HashMap<>();
        map.put('0', '٠');
        map.put('1', '١');
        map.put('2', '٢');
        map.put('3', '٣');
        map.put('4', '٤');
        map.put('5', '٥');
        map.put('6', '٦');
        map.put('7', '٧');
        map.put('8', '٨');
        map.put('9', '٩');
        map.put('A', 'ص');
        map.put('P', 'م');

        for (int i = 0; i < english.size(); i++) {
            StringBuilder sb = new StringBuilder(english.get(i));
            if (sb.charAt(sb.length()-1) == 'M')
                sb.deleteCharAt(sb.length()-1);

            english.set(i, sb.toString());

            if (english.get(i).charAt(0) == '0')
                english.set(i, english.get(i).replaceFirst("0", ""));
        }

        for (int i = 0; i < english.size(); i++) {
            StringBuilder temp = new StringBuilder();
            for (int j = 0; j < english.get(i).length(); j++) {
                char t = english.get(i).charAt(j);
                if (map.containsKey(t))
                    t = map.get(t);
                temp.append(t);
            }
            result[i] = temp.toString();
        }
        return result;
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}