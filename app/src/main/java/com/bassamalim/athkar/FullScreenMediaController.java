package com.bassamalim.athkar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;

public class FullScreenMediaController extends MediaController {

    private String isFullScreen;

    public FullScreenMediaController(Context context) {
        super(context);
    }

    @Override
    public void setAnchorView(View view) {
        super.setAnchorView(view);

        ImageButton fullScreen = new ImageButton(super.getContext());

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        params.rightMargin = 80;
        addView(fullScreen, params);

        //fullscreen indicator from intent
        isFullScreen =  ((Activity) getContext()).getIntent().getStringExtra("fullScreenInd");

        if("y".equals(isFullScreen))
            fullScreen.setImageResource(R.drawable.ic_fullscreen_exit);
        else
            fullScreen.setImageResource(R.drawable.ic_fullscreen);

        //add listener to image button to handle full screen and exit full screen events
        fullScreen.setOnClickListener(v ->  {
            Intent intent = new Intent(getContext(), FullScreenVideoActivity.class);

            if("y".equals(isFullScreen))
                intent.putExtra("fullScreenInd", "");
            else
                intent.putExtra("fullScreenInd", "y");

            FullScreenMediaController.this.getContext().startActivity(intent);
        });
    }

}

