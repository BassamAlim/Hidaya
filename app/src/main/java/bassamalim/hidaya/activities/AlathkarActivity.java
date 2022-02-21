package bassamalim.hidaya.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import java.util.List;
import java.util.Objects;

import bassamalim.hidaya.R;
import bassamalim.hidaya.database.AppDatabase;
import bassamalim.hidaya.database.dbs.ThikrsDB;
import bassamalim.hidaya.databinding.ActivityAlathkarBinding;
import bassamalim.hidaya.other.Util;

public class AlathkarActivity extends AppCompatActivity {

    private ActivityAlathkarBinding binding;
    private int textSize;
    private List<ThikrsDB> thikrs;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.onActivityCreateSetTheme(this);
        binding = ActivityAlathkarBinding.inflate(getLayoutInflater());
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(binding.getRoot());

        setSupportActionBar(binding.nameBar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "HidayaDB")
                .createFromAsset("databases/HidayaDB.db").allowMainThreadQueries().build();

        Intent intent = getIntent();
        int id = intent.getIntExtra("thikr_id", 0);

        thikrs = getThikrs(id);

        binding.thikrName.setText(db.athkarDao().getName(id));

        textSize = getSize();

        insert();
    }

    private List<ThikrsDB> getThikrs(int id) {
        return db.thikrsDao().getThikrs(id);
    }

    private void insert() {
        LinearLayout ll = binding.linear;
        for (int i = 0; i < thikrs.size(); i++) {
            CardView card = card(thikrs.get(i));
            ll.addView(card);
        }
    }

    private CardView card(ThikrsDB model) {
        CardView card = new CardView(this);
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(20, 15, 20, 15);
        cardParams.gravity = Gravity.CENTER;
        card.setLayoutParams(cardParams);
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.myClick, value, true);
        card.setCardBackgroundColor(value.data);
        card.setRadius(40);
        card.setElevation(15);
        card.setPreventCornerOverlap(true);

        LinearLayout mainLL = new LinearLayout(this);
        LinearLayout.LayoutParams mainLLParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mainLL.setLayoutParams(mainLLParams);
        card.addView(mainLL);

        if (!model.getRepetition().equals("1")) {
            LinearLayout repetitionLL = new LinearLayout(this);
            LinearLayout.LayoutParams horizontalLLParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            repetitionLL.setLayoutParams(horizontalLLParams);
            repetitionLL.setOrientation(LinearLayout.VERTICAL);
            repetitionLL.setGravity(Gravity.CENTER);
            mainLL.addView(repetitionLL);
            TextView repetition = repetitionScreen(model.getRepetition());
            repetitionLL.addView(repetition);

            mainLL.addView(separator(false));
        }

        LinearLayout verticalLL = new LinearLayout(this);
        LinearLayout.LayoutParams verticalLLParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        verticalLLParams.setMargins(10, 10, 10, 15);
        verticalLLParams.gravity = Gravity.CENTER;
        verticalLL.setLayoutParams(verticalLLParams);
        verticalLL.setOrientation(LinearLayout.VERTICAL);
        verticalLL.setGravity(Gravity.CENTER);
        mainLL.addView(verticalLL);

        if (model.getTitle().length() > 0) {
            TextView title = titleScreen(model.getTitle());
            verticalLL.addView(title);
        }
        if (model.getText().length() > 0) {
            TextView text = textScreen(model.getText());
            verticalLL.addView(text);
        }
        if (model.getFadl().length() > 0) {
            verticalLL.addView(separator(true));

            TextView fadl = fadlScreen(model.getFadl());
            verticalLL.addView(fadl);
        }
        if (model.getReference().length() > 0) {
            verticalLL.addView(separator(true));

            TextView reference = referenceScreen(model.getReference());
            verticalLL.addView(reference);
        }

        return card;
    }

    private TextView titleScreen(String title) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        screen.setPadding(10, 0, 10, 0);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize);
        screen.setTypeface(Typeface.DEFAULT_BOLD);
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.myText, value, true);
        screen.setTextColor(value.data);

        screen.setText(title);
        return screen;
    }

    private TextView textScreen(String text) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        screen.setPadding(10, 10, 10, 20);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize);
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.myText, value, true);
        screen.setTextColor(value.data);

        screen.setText(text);
        return screen;
    }

    private TextView repetitionScreen(String repetition) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.myOtherAccent, value, true);
        screen.setTextColor(value.data);
        screen.setPadding(15, 0, 15, 0);
        screen.setMaxWidth(300);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize-5);

        screen.setText(repetition);
        return screen;
    }

    private TextView fadlScreen(String fadl) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.myOtherAccent, value, true);
        screen.setTextColor(value.data);
        screen.setPadding(10, 0, 10, 0);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize-8);

        screen.setText(fadl);
        return screen;
    }

    private TextView referenceScreen(String reference) {
        TextView screen = new TextView(this);
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        screenParams.gravity = Gravity.CENTER;
        screen.setLayoutParams(screenParams);
        screen.setTextColor(getResources().getColor(R.color.black));
        screen.setPadding(10, 0, 10, 0);
        screen.setGravity(Gravity.CENTER);
        screen.setTextSize(textSize-8);

        screen.setText(reference);
        return screen;
    }

    private View separator(boolean horizontal) {
        View separator = new View(this);

        LinearLayout.LayoutParams params;
        if (horizontal)
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3);
        else
            params = new LinearLayout.LayoutParams(3, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 10, 0, 10);

        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(R.attr.mySeparator, value, true);
        separator.setBackgroundColor(value.data);

        separator.setLayoutParams(params);
        return separator;
    }

    private int getSize() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.alathkar_text_size_key), 25);
    }
}
