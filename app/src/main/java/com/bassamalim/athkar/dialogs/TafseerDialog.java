package com.bassamalim.athkar.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.databinding.TafseerDialogBinding;

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

        ScrollView scrollView = new ScrollView(requireContext());
        LinearLayout layout = new LinearLayout(requireContext());
        TextView nameScreen = new TextView(requireContext());
        TextView screen = new TextView(requireContext());

        scrollView.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.dialog_bg, requireContext().getTheme()));

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        Objects.requireNonNull(getDialog()).getWindow()
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
        screen.setTextColor(getResources().getColor(R.color.accent));

        layout.addView(nameScreen);
        layout.addView(screen);
        scrollView.addView(layout);

        return scrollView;
    }

    /*@NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        View v = LayoutInflater.from(requireContext()).inflate(R.layout.tafseer_dialog, null);
        ScrollView scroll = v.findViewById(R.id.dialog_scroll);
        TextView textView = v.findViewById(R.id.tafseer_screen);
        textView.setText(tafseer);
        builder.setView(scroll);

        AlertDialog dialog = builder.create();

        //dialog.getWindow().setLayout(100, 100);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return dialog;
    }*/

}
