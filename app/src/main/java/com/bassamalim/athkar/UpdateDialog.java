package com.bassamalim.athkar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class UpdateDialog extends Dialog implements View.OnClickListener {

    public Activity c;
    public Button yes, no;
    public String msg = "";

    private final FirebaseRemoteConfig remoteConfig = MainActivity.getInstance().remoteConfig;

    public UpdateDialog(Activity a) {
        super(a);
        this.c = a;
    }

    public UpdateDialog(Activity a, String msg) {
        super(a);
        this.c = a;

        TextView text = findViewById(R.id.textView);
        text.setText(msg);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_dialog);

        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
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
        c.startActivity(i);
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        super.setOnDismissListener(listener);
        // put some funny stuff here
    }

}