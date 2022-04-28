package bassamalim.hidaya.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import bassamalim.hidaya.R;

public class TutorialDialog extends DialogFragment {

    public static String TAG = "TutorialDialog";
    private final SharedPreferences pref;
    private View view;
    private final String text;
    private final String prefKey;

    public TutorialDialog(Context context, String text, String prefKey) {
        this.text = text;
        this.prefKey = prefKey;

        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.dialog_tutorial,container,false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        TextView textTv = view.findViewById(R.id.text_tv);
        textTv.setText(text);

        return view;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        CheckBox do_not_show_again_checkbox = view.findViewById(R.id.do_not_show_again_cb);
        if (do_not_show_again_checkbox.isChecked()) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(prefKey, false);
            editor.apply();
        }
    }
}
