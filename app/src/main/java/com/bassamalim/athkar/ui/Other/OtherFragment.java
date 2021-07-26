package com.bassamalim.athkar.ui.Other;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.OtherFragmentBinding;

public class OtherFragment extends Fragment {

    private OtherViewModel otherViewModel;
    private OtherFragmentBinding binding;

    public static OtherFragment newInstance() {
        return new OtherFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        otherViewModel = new ViewModelProvider(this).get(OtherViewModel.class);

        binding = OtherFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();





        return root;
        //return inflater.inflate(R.layout.other_fragment, container, false);
    }


}