package com.bassamalim.athkar.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.bassamalim.athkar.popups.UpdateDialog;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class Update {

    private final String UPDATE_AVAILABLE = "update_available";
    private final String LATEST_VERSION = "latest_app_version";
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
                boolean available = remoteConfig.getBoolean(UPDATE_AVAILABLE);
                remoteConfig.fetchAndActivate();
                if (available) {
                    Log.i(TAG, "Update available");
                    String latestVersion = remoteConfig.getString(LATEST_VERSION);
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
        UpdateDialog updateDialog = new UpdateDialog(context.getApplicationContext());
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
