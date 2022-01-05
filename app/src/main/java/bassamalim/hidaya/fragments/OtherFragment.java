package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import bassamalim.hidaya.activities.AboutActivity;
import bassamalim.hidaya.activities.FeaturesGuide;
import bassamalim.hidaya.activities.QuizLobbyActivity;
import bassamalim.hidaya.activities.Settings;
import bassamalim.hidaya.activities.TvActivity;
import bassamalim.hidaya.databinding.FragmentOtherBinding;
import bassamalim.hidaya.other.Constants;

public class OtherFragment extends Fragment {

    private FragmentOtherBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentOtherBinding.inflate(inflater, container, false);

        setListeners();

        return binding.getRoot();
    }

    public void setListeners() {
        binding.quranRadio.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CollectionTelawatFragment.class);
            startActivity(intent);
        });

        binding.quiz.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), QuizLobbyActivity.class);
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

        binding.features.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), FeaturesGuide.class);
            startActivity(intent);
        });

        binding.contact.setOnClickListener(v -> {
            String url = "https://api.whatsapp.com/send?phone=" + Constants.MY_NUMBER;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        binding.share.setOnClickListener(v -> {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String appLink = Constants.PLAY_STORE_URL;
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App Share");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, appLink);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        });

        binding.about.setOnClickListener(v -> {
            Intent about = new Intent(getContext(), AboutActivity.class);
            startActivity(about);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}