package com.bassamalim.athkar;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class UpdateDialog extends Dialog implements View.OnClickListener {

    public Activity c;
    public Button yes, no;
    public String msg = "";


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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_dialog);

        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        yes.setOnClickListener(v -> Update.getInstance().startUpdate());
        no.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        super.setOnDismissListener(listener);
        // put some funny stuff here
    }

}