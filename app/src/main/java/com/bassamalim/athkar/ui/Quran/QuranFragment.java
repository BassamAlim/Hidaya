package com.bassamalim.athkar.ui.Quran;

import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.QuranView;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.Utils;
import com.bassamalim.athkar.databinding.QuranFragmentBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuranFragment extends Fragment {

    private QuranViewModel quranViewModel;
    private QuranFragmentBinding binding;
    JSONObject jsonObject;
    JSONObject data;
    JSONArray surahs;
    String jsonFileString;
    private LinearLayout linear;
    private String[] surahNames;
    private Intent intent;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        quranViewModel = new ViewModelProvider(this).get(QuranViewModel.class);

        binding = QuranFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupJson();

        getSurahNames();

        setUp();

        return root;
    }

    private void setupJson() {
        jsonFileString = Utils.getJsonFromAssets(requireContext(), "quran.json");
        try {
            assert jsonFileString != null;
            jsonObject = new JSONObject(jsonFileString);
            data = jsonObject.getJSONObject("data");
            surahs = data.getJSONArray("surahs");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setUp() {
        linear = binding.linear;
        for (int i = 0; i < Constants.NUMBER_OF_SURAHS; i++) {
            Button button = button();
            button.setText(surahNames[i]);
            linear.addView(button);

            int finalI = i;
            button.setOnClickListener(v -> {
                intent = new Intent(getContext(), QuranView.class);
                intent.putExtra("surah index", finalI);
                startActivity(intent);
            });
        }
    }

    private Button button() {
        Button button = new Button(getContext());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0,0,0,10);
        button.setLayoutParams(buttonParams);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundColor(getResources().getColor(R.color.accent, requireContext().getTheme()));
        button.setText("سورة");
        button.setTextSize(20);
        button.setTextColor(getResources().getColor(R.color.secondary, requireContext().getTheme()));

        return button;
    }

    public void getSurahNames() {
        String[] names = new String[Constants.NUMBER_OF_SURAHS];
        String surahNamesJson = Utils.getJsonFromAssets(requireContext(), "surah_names.json");
        try {
            assert surahNamesJson != null;
            jsonObject = new JSONObject(surahNamesJson);
            JSONArray array = jsonObject.getJSONArray("names");
            for (int i=0; i<Constants.NUMBER_OF_SURAHS; i++) {
                JSONObject object = array.getJSONObject(i);
                names[i] = object.getString("name");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        surahNames = names;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
