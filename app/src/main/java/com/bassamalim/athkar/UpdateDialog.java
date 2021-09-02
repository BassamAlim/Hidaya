package com.bassamalim.athkar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class UpdateDialog extends Dialog implements View.OnClickListener {

    private final String msg = "";
    private final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

    public UpdateDialog(Context c) {
        super(c);
    }

    public UpdateDialog(Context c, String msg) {
        super(c);

        TextView text = findViewById(R.id.textView);
        text.setText(msg);
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

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        super.setOnDismissListener(listener);
        // put some funny stuff here
    }

}