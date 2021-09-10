package com.bassamalim.athkar;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class Update extends AppCompatActivity {

    private final Context context;
    private final String TAG = "Update";
    private final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    private final String currentVersion;

    public Update(Context gContext) {
        context = gContext;
        currentVersion = getAppVersion();

        checkForUpdate();
    }

    private void checkForUpdate() {
        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.i(TAG, "remote config is fetched.");
                boolean available = remoteConfig.getBoolean(Constants.UPDATE_AVAILABLE);
                remoteConfig.fetchAndActivate();
                if (available) {
                    Log.i(TAG, "Update available");
                    String latestVersion = remoteConfig.getString(Constants.LATEST_VERSION);
                    Log.i(TAG, "Latest: " + latestVersion);
                    Log.i(TAG, "current: " + currentVersion);
                    if (removePoint(currentVersion) < removePoint(latestVersion)) {
                        Log.i(TAG, "Update needed");
                        showUpdatePrompt();
                    }
                }
            }
            else
                Log.i(TAG, "Fetch Failed");
        });
    }

    private int removePoint(String with) {
        String without;
        without = with.replace(".", "");
        return Integer.parseInt(without);
    }

    public void showUpdatePrompt() {
        UpdateDialog updateDialog = new UpdateDialog(context);
        updateDialog.show();
    }

    private String getAppVersion() {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0).versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }

}
