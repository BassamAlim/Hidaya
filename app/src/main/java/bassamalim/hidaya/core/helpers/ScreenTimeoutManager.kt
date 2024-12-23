package bassamalim.hidaya.core.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.view.WindowManager

class ScreenTimeoutManager(private val activity: Activity) {
    private var originalTimeout: Int = 0
    private var wakeLock: PowerManager.WakeLock? = null

    /**
     * Keeps the screen on indefinitely for the current activity
     */
    fun keepScreenOn() {
        // Method 1: Using Window Flags
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Method 2: Using WakeLock (alternative approach)
        val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
            "MyApp::ScreenLockTag"
        ).apply {
            acquire(10*60*1000L /*10 minutes*/)
        }
    }

    /**
     * Sets a custom screen timeout duration for the current activity
     * @param timeoutMs timeout duration in milliseconds
     */
    fun setCustomTimeout(timeoutMs: Int) {
        // Store original timeout
        originalTimeout = Settings.System.getInt(
            activity.contentResolver,
            Settings.System.SCREEN_OFF_TIMEOUT,
            30000 // Default 30 seconds
        )

        // Check if we have permission to modify system settings
        if (Settings.System.canWrite(activity)) {
            Settings.System.putInt(
                activity.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                timeoutMs
            )
        }
        else {
            requestWriteSettingsPermission()
        }
    }

    /**
     * Requests permission to modify system settings
     */
    private fun requestWriteSettingsPermission() {
        Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
            activity.startActivity(this)
        }
    }

    /**
     * Restores original screen timeout settings
     */
    fun restoreDefaultSettings() {
        // Remove window flag
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Release WakeLock if it exists and is held
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }

        // Restore original timeout if we changed it
        if (Settings.System.canWrite(activity)) {
            Settings.System.putInt(
                activity.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                originalTimeout
            )
        }
    }

    companion object {
        // Define timeout constants
        const val ONE_MINUTE = 60_000
        const val FIVE_MINUTES = 300_000
        const val TEN_MINUTES = 600_000
    }
}