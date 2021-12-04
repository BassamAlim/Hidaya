package com.bassamalim.hidaya.popups;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.bassamalim.hidaya.R;

import java.util.Objects;

public class TafseerDialog extends DialogFragment {

    public static String TAG = "TafseerDialog";
    private final String tafseer;

    public TafseerDialog(String gTafseer) {
        tafseer = gTafseer;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Objects.requireNonNull(getDialog()).getWindow()
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        TextView nameScreen = new TextView(requireContext());
        TextView screen = new TextView(requireContext());

        scrollView.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.dialog_bg, requireContext().getTheme()));

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        nameScreen.setPadding(0,0,0, 10);
        nameScreen.setGravity(Gravity.CENTER);
        nameScreen.setText("التفسير");
        nameScreen.setTextSize(20);
        nameScreen.setTypeface(Typeface.DEFAULT_BOLD);

        screen.setGravity(Gravity.CENTER);
        screen.setText(tafseer);
        screen.setTextSize(20);
        screen.setTextColor(Color.WHITE);

        layout.addView(nameScreen);
        layout.addView(screen);
        scrollView.addView(layout);

        return scrollView;
    }

}
