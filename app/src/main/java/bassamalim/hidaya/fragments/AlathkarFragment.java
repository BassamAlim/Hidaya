package bassamalim.hidaya.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import bassamalim.hidaya.activities.AthkarListActivity;
import bassamalim.hidaya.databinding.FragmentAthkarBinding;

public class AlathkarFragment extends Fragment {

    private FragmentAthkarBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAthkarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setListeners();

        return root;
    }

    private void setListeners() {
        binding.allThikrs.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AthkarListActivity.class);
            intent.setAction("all");
            startActivity(intent);
        });
        binding.favoriteAthkar.setOnClickListener(view -> {
            Intent intent = new Intent(getContext(), AthkarListActivity.class);
            intent.setAction("favorite");
            startActivity(intent);
        });
        binding.dayAndNight.setOnClickListener(v -> showThikrs(0));
        binding.prayers.setOnClickListener(v -> showThikrs(1));
        binding.quran.setOnClickListener(v -> showThikrs(2));
        binding.actions.setOnClickListener(v -> showThikrs(3));
        binding.events.setOnClickListener(v -> showThikrs(4));
        binding.emotions.setOnClickListener(v -> showThikrs(5));
        binding.places.setOnClickListener(v -> showThikrs(6));
        binding.more.setOnClickListener(v -> showThikrs(7));
    }

    public void showThikrs(int category) {
        Intent intent = new Intent(getContext(), AthkarListActivity.class);
        intent.setAction("category");
        intent.putExtra("category", category);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}