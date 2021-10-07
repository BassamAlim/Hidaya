package com.bassamalim.athkar.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bassamalim.athkar.R;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Objects;

public class CompassCalibrate extends DialogFragment {

    public static String TAG = "CompassCalibrationGif";
    private final Context context;

    public CompassCalibrate(Context gContext) {
        context = gContext;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Objects.requireNonNull(getDialog()).getWindow()
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(getDialog()).getWindow().setLayout(10, 10);

        View view = getLayoutInflater().inflate(R.layout.compass_calibration_gif,
                new LinearLayout(context));
        ImageView screen = view.findViewById(R.id.gif_screen);
        screen.setOnClickListener(v -> dismiss());

        Glide.with(context).load(R.drawable.compass_calibration).into(screen);

        return view;
    }

}
