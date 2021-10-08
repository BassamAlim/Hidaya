package com.bassamalim.athkar.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bassamalim.athkar.activities.AlathkarList;
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
        binding.allThikrs.setOnClickListener(v -> showThikrs(0));
        binding.dayAndNight.setOnClickListener(v -> showThikrs(1));
        binding.prayers.setOnClickListener(v -> showThikrs(2));
        binding.quran.setOnClickListener(v -> showThikrs(3));
        binding.actions.setOnClickListener(v -> showThikrs(4));
        binding.events.setOnClickListener(v -> showThikrs(5));
        binding.emotions.setOnClickListener(v -> showThikrs(6));
        binding.places.setOnClickListener(v -> showThikrs(7));
        binding.more.setOnClickListener(v -> showThikrs(8));
    }

    public void showThikrs(int index) {
        Intent intent = new Intent(getContext(), AlathkarList.class);
        intent.putExtra("index", index);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}