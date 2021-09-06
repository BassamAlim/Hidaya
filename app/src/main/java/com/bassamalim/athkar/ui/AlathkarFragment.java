package com.bassamalim.athkar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bassamalim.athkar.views.AlathkarView;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.AlathkarFragmentBinding;

public class AlathkarFragment extends Fragment {

    private AlathkarFragmentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = AlathkarFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setListeners();

        return root;
    }

    private void setListeners() {
        binding.morning.setOnClickListener(v -> showThikrs(R.array.morning, binding.morning.getText()));
        binding.night.setOnClickListener(v -> showThikrs(R.array.night, binding.night.getText()));
        binding.postPrayer.setOnClickListener(v -> showThikrs(R.array.post_prayer, binding.postPrayer.getText()));
        binding.preSleep.setOnClickListener(v -> showThikrs(R.array.pre_sleep, binding.preSleep.getText()));
        binding.wakeup.setOnClickListener(v -> showThikrs(R.array.wakeup, binding.wakeup.getText()));
        binding.quranConclude.setOnClickListener(v -> showThikrs(R.array.quran_conclude, binding.quranConclude.getText()));
        binding.athan.setOnClickListener(v -> showThikrs(R.array.athan, binding.athan.getText()));
        binding.wudu.setOnClickListener(v -> showThikrs(R.array.ablution, binding.wudu.getText()));
        binding.bathroom.setOnClickListener(v -> showThikrs(R.array.bathroom, binding.bathroom.getText()));
        binding.travel.setOnClickListener(v -> showThikrs(R.array.travel, binding.travel.getText()));
    }

    public void showThikrs(int givenThikrs, CharSequence title) {
        String[] thikrs = getResources().getStringArray(givenThikrs);
        Intent intent = new Intent(getContext(), AlathkarView.class);
        intent.putExtra("thikrs", thikrs);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}