package com.bassamalim.athkar.ui.Quran;

import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bassamalim.athkar.AthkarView;
import com.bassamalim.athkar.Quran;
import com.bassamalim.athkar.QuranView;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.Utils;
import com.bassamalim.athkar.databinding.QuranFragmentBinding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

public class QuranFragment extends Fragment {

    private QuranViewModel quranViewModel;
    private QuranFragmentBinding binding;
    private Gson gson = new Gson();
    JSONObject jsonObject;
    private static final String TAG = "QuranFragment";
    JSONObject data;
    JSONArray surahs;
    String jsonFileString;
    private static final int numOfSurahs = 114;
    private LinearLayout linear;
    private String[] surahNames;
    private String basmalah = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ";


    public static QuranFragment newInstance() {
        return new QuranFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        quranViewModel = new ViewModelProvider(this).get(QuranViewModel.class);

        binding = QuranFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
        //return inflater.inflate(R.layout.quran_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        jsonFileString = Utils.getJsonFromAssets(requireContext(), "quran.json");

        try {
            jsonObject = new JSONObject(jsonFileString);

            data = jsonObject.getJSONObject("data");

            surahs = data.getJSONArray("surahs");

            Log.i(TAG, "Nailed it");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG, "Failed it");
        }

        getSurahNames();

        setUp();
    }

    public void setUp() {
        linear = binding.linear;

        for (int i = 0; i < numOfSurahs; i++) {
            Button button = button();
            button.setText(surahNames[i]);
            linear.addView(button);

            int finalI = i;
            button.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), QuranView.class);
                intent.putExtra("key", buildSurah(finalI));
                intent.putExtra("title", surahNames[finalI]);
                startActivity(intent);
            });
        }

    }

    public Button button() {
        Button button = new Button(getContext());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(0,0,0,10);
        button.setLayoutParams(buttonParams);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundColor(getResources().getColor(R.color.secondary, requireContext().getTheme()));
        button.setText("سورة");
        button.setTextSize(20);
        button.setTextColor(getResources().getColor(R.color.primary, requireContext().getTheme()));

        return button;
    }

    public void getSurahNames() {
        String[] names = new String[numOfSurahs];
        try {
            for (int i = 0; i < numOfSurahs; i++)
                names[i] = surahs.getJSONObject(i).getString("name");
        }
        catch (JSONException e) {
            Toast.makeText(getContext(), "Failed in getting surah names", Toast.LENGTH_SHORT).show();
        }

        surahNames = names;
    }

    public String buildSurah(int num){
        StringBuilder surahText = new StringBuilder();
        JSONObject surah;
        JSONArray ayahs;
        try {
            surahText.append(basmalah).append("\n");
            surah = surahs.getJSONObject(num);
            ayahs = surah.getJSONArray("ayahs");
            for (int i = 0; i < surah.getJSONArray("ayahs").length(); i++) {
                surahText.append(ayahs.getJSONObject(i).getString("text"));
                if (i == surah.getJSONArray("ayahs").length()-1)
                    surahText.append(".");
                else
                    surahText.append("  ۞  ");
            }
        }
        catch (JSONException e) {
            Log.i(TAG, "oops in setting up surah");
        }

        return surahText.toString();
    }

}
