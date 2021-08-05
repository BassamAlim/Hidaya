package com.bassamalim.athkar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class Update extends AppCompatActivity {

    private static Update instance;
    private final FirebaseRemoteConfig remoteConfig = MainActivity.getInstance().remoteConfig;
    private static final String TAG = "MainActivity";

    public static Update getInstance() {
        return instance;
    }

    public Update() {
        instance = this;

        checkForUpdate();
    }

    private void checkForUpdate() {
        remoteConfig.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "remote config is fetched.");
                remoteConfig.fetchAndActivate();
            }
            boolean available = remoteConfig.getBoolean(Constants.UPDATE_AVAILABLE);
            remoteConfig.fetchAndActivate();
            if (available) {
                Log.i(TAG, "Update available");
                String latestVersion = remoteConfig.getString(Constants.LATEST_VERSION);
                String currentVersion = getAppVersion(MainActivity.getInstance());
                if (!TextUtils.equals(currentVersion, latestVersion)) {
                    showUpdatePrompt();
                }
            }
        });
    }

    public void showUpdatePrompt() {
        UpdateDialog updateDialog = new UpdateDialog(MainActivity.getInstance());
        updateDialog.show();
    }

    private String getAppVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }

    public void startUpdate() {
        String url;
        url = remoteConfig.getString(Constants.UPDATE_URL);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

}
