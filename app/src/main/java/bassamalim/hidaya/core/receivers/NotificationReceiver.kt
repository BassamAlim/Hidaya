package bassamalim.hidaya.core.receivers

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bassamalim.hidaya.core.Activity
import bassamalim.hidaya.R
import bassamalim.hidaya.core.data.Prefs
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.services.AthanService
import bassamalim.hidaya.core.utils.ActivityUtils
import bassamalim.hidaya.core.utils.PrefUtils
import java.util.*
import kotlin.math.abs

class NotificationReceiver : BroadcastReceiver() {

    private lateinit var ctx: Context
    private lateinit var sp: SharedPreferences
    private lateinit var action: String
    private lateinit var pid: PID
    private var notificationId = 0
    private var channelId = ""
    private var time = 0L

    override fun onReceive(context: Context, intent: Intent) {
        ctx = context.applicationContext
        sp = PrefUtils.getPreferences(ctx)

        try {  // remove after a while
            ActivityUtils.onActivityCreateSetLocale(ctx)
        } catch (e: Exception) {
            Log.e(Global.TAG, "Neuralyzing", e)
            ActivityUtils.clearAppData(ctx)
        }

        pid = PID.valueOf(intent.getStringExtra("id")!!)
        time = intent.getLongExtra("time", 0L)

        action = intent.action!!
        when (intent.action) {
            "prayer" -> handlePrayer()
            "reminder" -> handleReminder()
            "extra" -> handleExtra()
        }
    }

    private fun handlePrayer() {
        Log.i(Global.TAG, "in notification receiver for $pid prayer")

        notificationId = pid.ordinal

        val notificationType = NotificationType.valueOf(
            PrefUtils.getString(sp, Prefs.NotificationType(pid))
        )

        if (notificationType != NotificationType.None && isOnTime()) {
            if (notificationType == NotificationType.Athan) startService()
            else showNotification(true, notificationType)
        }
    }

    private fun handleReminder() {
        Log.i(Global.TAG, "in notification receiver for $pid reminder")

        notificationId = pid.ordinal + 10
        if (isOnTime()) showNotification(false)
    }

    private fun handleExtra() {
        Log.i(Global.TAG, "in notification receiver for $pid extra")

        notificationId = pid.ordinal

        val notificationType = NotificationType.valueOf(
            PrefUtils.getString(sp, Prefs.NotificationType(pid))
        )

        if (isOnTime()) showNotification(false, notificationType)
    }

    private fun isOnTime(): Boolean {
        val max = time + 120000
        return System.currentTimeMillis() <= max
    }

    private fun showNotification(
        isPrayer: Boolean,
        notificationType: NotificationType = NotificationType.Notification,
    ) {
        createNotificationChannel()

        if (isPrayer && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val am = ctx.getSystemService(Service.AUDIO_SERVICE) as AudioManager
            // Request audio focus
            am.requestAudioFocus(                   // Request permanent focus
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    ).build()
            )
        }

        if (ActivityCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {
            val notification = build(notificationType)
            NotificationManagerCompat.from(ctx).notify(notificationId, notification)
        }
    }

    private fun startService() {
        val intent = Intent(ctx, AthanService::class.java)
        intent.action = Global.PLAY_ATHAN
        intent.putExtra("pid", pid.name)
        intent.putExtra("time", time)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)
    }

    private fun build(
        notificationType: NotificationType
    ): Notification {
        val builder = NotificationCompat.Builder(ctx, channelId)
        builder.setSmallIcon(R.drawable.ic_athan)
        builder.setTicker(ctx.resources.getString(R.string.app_name))

        builder.setContentTitle(getTitle())
        builder.setContentText(getSubtitle())

        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setAutoCancel(true)
        builder.setOnlyAlertOnce(true)
        builder.color = ctx.getColor(R.color.surface_M)
        builder.setContentIntent(onClick(pid))

        if (notificationType == NotificationType.Silent)
            builder.setSilent(true)

        return builder.build()
    }

    private fun getTitle(): String {
        return if (pid == PID.DHUHR &&
            Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
            if (action == "reminder") ctx.resources.getString(R.string.jumuah_reminder_title)
            else ctx.resources.getString(R.string.jumuah_title)
        }
        else ctx.resources.getStringArray(R.array.prayer_titles)[notificationId]
    }

    private fun getSubtitle(): String {
        return if (action == "reminder") {
            val offset = PrefUtils.getInt(sp, Prefs.ReminderOffset(pid))
            String.format(
                format =
                if (offset < 0) ctx.resources.getString(R.string.reminder_before)
                else ctx.resources.getString(R.string.reminder_after),
                if (pid == PID.DHUHR &&
                    Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                    ctx.resources.getString(R.string.jumuah)
                else ctx.resources.getStringArray(R.array.prayer_names)[pid.ordinal],
                abs(offset) // to remove - sign
            )
        }
        else {
            if (pid == PID.DHUHR &&
                Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                ctx.resources.getString(R.string.jumuah_subtitle)
            else ctx.resources.getStringArray(R.array.prayer_subtitles)[notificationId]
        }
    }

    private fun onClick(pid: PID?): PendingIntent {
        val intent = Intent(ctx, Activity::class.java)

        val route = when (pid) {
            PID.MORNING -> {
                Screen.AthkarViewer(
                    0.toString(),
                ).route
            }
            PID.EVENING -> {
                Screen.AthkarViewer(
                    1.toString(),
                ).route
            }
            PID.DAILY_WERD -> {
                Screen.QuranViewer(
                    "by_page",
                    page = PrefUtils.getInt(sp, Prefs.WerdPage).toString()
                ).route
            }
            PID.FRIDAY_KAHF -> {
                Screen.QuranViewer(
                    "by_sura",
                    suraId = 17.toString() // surat al-kahf
                ).route
            }
            else -> Screen.Main.route
        }
        intent.putExtra("start_route", route)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        return PendingIntent.getActivity(
            ctx, pid!!.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = ctx.resources.getStringArray(R.array.channel_ids)[notificationId]
            val name = ctx.resources.getStringArray(R.array.reminders)[notificationId]

            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = "description"
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = ctx.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}