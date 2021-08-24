package com.bassamalim.athkar.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bassamalim.athkar.Settings;
import com.bassamalim.athkar.databinding.OtherFragmentBinding;
import com.bassamalim.athkar.views.TvView;

public class OtherFragment extends Fragment {

    private OtherFragmentBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = OtherFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setListeners();

        return root;
    }

    public void setListeners() {
        binding.channels.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), TvView.class);
            startActivity(intent);
        });
        binding.settings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), Settings.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}