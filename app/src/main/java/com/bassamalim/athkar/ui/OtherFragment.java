package com.bassamalim.athkar.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bassamalim.athkar.Constants;
import com.bassamalim.athkar.activities.Settings;
import com.bassamalim.athkar.databinding.OtherFragmentBinding;
import com.bassamalim.athkar.activities.TvActivity;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class OtherFragment extends Fragment {

    private OtherFragmentBinding binding;
    private final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

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
            Intent intent = new Intent(getContext(), TvActivity.class);
            startActivity(intent);
        });
        binding.settings.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), Settings.class);
            startActivity(intent);
        });
        binding.update.setOnClickListener(v -> {
            String url = remoteConfig.getString(Constants.UPDATE_URL);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        binding.contact.setOnClickListener(v -> {
            String url = "https://api.whatsapp.com/send?phone=" + Constants.MY_NUMBER;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}