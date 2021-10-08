package com.bassamalim.athkar.other;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;

import com.bassamalim.athkar.R;
import com.bassamalim.athkar.activities.FSVideoActivity;

public class FSMediaController extends MediaController {

    private String isFullScreen;

    public FSMediaController(Context context) {
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
            Intent intent = new Intent(getContext(), FSVideoActivity.class);

            if("y".equals(isFullScreen))
                intent.putExtra("fullScreenInd", "");
            else
                intent.putExtra("fullScreenInd", "y");

            FSMediaController.this.getContext().startActivity(intent);
        });
    }

}

