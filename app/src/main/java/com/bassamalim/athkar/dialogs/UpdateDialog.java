package com.bassamalim.athkar.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bassamalim.athkar.other.Constants;
import com.bassamalim.athkar.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class UpdateDialog extends Dialog implements View.OnClickListener {

    private final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

    public UpdateDialog(Context c) {
        super(c);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_dialog);

        Button yes = findViewById(R.id.yes);
        Button no = findViewById(R.id.no);
        yes.setOnClickListener(v -> startUpdate());
        no.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public void startUpdate() {
        String url = remoteConfig.getString(Constants.UPDATE_URL);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        getContext().startActivity(i);
    }

}