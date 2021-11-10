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

import com.bassamalim.athkar.activities.AboutActivity;
import com.bassamalim.athkar.activities.RadioActivity;
import com.bassamalim.athkar.other.Constants;
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
        binding.quranRadio.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RadioActivity.class);
            startActivity(intent);
        });
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
        binding.about.setOnClickListener(v -> {
            Intent about = new Intent(getContext(), AboutActivity.class);
            startActivity(about);
        });
        binding.share.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String appLink = Constants.APP_URL;
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App Share");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, appLink);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}