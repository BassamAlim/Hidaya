package com.bassamalim.athkar.views;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bassamalim.athkar.R;

public class TafseerDialog extends DialogFragment {

    public static String TAG = "TafseerDialog";
    private final String tafseer;

    public TafseerDialog(String gTafseer) {
        tafseer = gTafseer;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        LinearLayout layout = new LinearLayout(requireContext());
        TextView nameScreen = new TextView(requireContext());
        TextView screen = new TextView(requireContext());

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(getResources().getColor(R.color.accent));

        nameScreen.setGravity(Gravity.CENTER);
        nameScreen.setText("التفسير");
        nameScreen.setTextSize(20);
        nameScreen.setTypeface(Typeface.DEFAULT_BOLD);
        nameScreen.setTextColor(Color.WHITE);

        screen.setPadding(20, 20, 20, 20);
        screen.setGravity(Gravity.CENTER);
        screen.setElevation(20);
        screen.setText(tafseer);
        screen.setTextSize(20);
        screen.setTextColor(getResources().getColor(R.color.secondary));

        layout.addView(nameScreen);
        layout.addView(screen);

        return layout;
    }
}
