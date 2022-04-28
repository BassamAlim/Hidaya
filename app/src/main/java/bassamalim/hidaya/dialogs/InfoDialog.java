package bassamalim.hidaya.dialogs;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import bassamalim.hidaya.R;

public class InfoDialog extends DialogFragment {

    public static String TAG = "InfoDialog";
    private final String title;
    private final String text;

    public InfoDialog(String title, String text) {
        this.title = title;
        this.text = text;
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.info_dialog,container,false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        TextView titleTv = view.findViewById(R.id.title_tv);
        titleTv.setText(title);

        TextView textTv = view.findViewById(R.id.text_tv);
        textTv.setText(text);

        return view;
    }
}
