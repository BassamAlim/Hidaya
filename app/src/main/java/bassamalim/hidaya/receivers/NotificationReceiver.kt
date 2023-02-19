package bassamalim.hidaya.receivers

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
import bassamalim.hidaya.MainActivity
import bassamalim.hidaya.data.Prefs
import bassamalim.hidaya.R
import bassamalim.hidaya.nav.Screen
import bassamalim.hidaya.enums.NotificationType
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.AthanService
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.PrefUtils
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    private lateinit var ctx: Context
    private lateinit var sp: SharedPreferences
    private lateinit var pid: PID
    private var isPrayer = false
    private var channelId = ""
    private var type = NotificationType.None
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
        isPrayer = intent.action == "prayer"

        Log.i(Global.TAG, "in notification receiver for $pid")

        val typeName = PrefUtils.getString(sp, Prefs.NotificationType(pid))
        type = NotificationType.valueOf(typeName)

        if (type != NotificationType.None) prepare()
    }

    private fun prepare() {
        val max = time + 120000
        if (System.currentTimeMillis() <= max) {
            if (type == NotificationType.Athan) startService()
            else showNotification()
        }
        else Log.i(Global.TAG, "$pid Passed")
    }

    private fun showNotification() {
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
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(ctx).notify(pid.ordinal, build())
        }
    }

    private fun startService() {
        val serviceIntent = Intent(ctx, AthanService::class.java)
        serviceIntent.action = Global.PLAY_ATHAN
        serviceIntent.putExtra("pid", pid.name)
        serviceIntent.putExtra("time", time)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(serviceIntent)
        else ctx.startService(serviceIntent)
    }

    private fun build(): Notification {
        val builder = NotificationCompat.Builder(ctx, channelId)
        builder.setSmallIcon(R.drawable.ic_athan)
        builder.setTicker(ctx.resources.getString(R.string.app_name))

        var i = pid.ordinal
        if (pid == PID.DHUHR && Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
            i = 10
        builder.setContentTitle(ctx.resources.getStringArray(R.array.prayer_titles)[i])
        builder.setContentText(ctx.resources.getStringArray(R.array.prayer_subtitles)[i])

        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setAutoCancel(true)
        builder.setOnlyAlertOnce(true)
        builder.color = ctx.getColor(R.color.surface_M)
        builder.setContentIntent(onClick(pid))

        if (type == NotificationType.Silent) builder.setSilent(true)

        return builder.build()
    }

    private fun onClick(pid: PID?): PendingIntent {
        val intent = Intent(ctx, MainActivity::class.java)

        val route: String = when (pid) {
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
                    page = PrefUtils.getInt(sp, Prefs.TodayWerdPage).toString(),
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
            channelId = ctx.resources.getStringArray(R.array.channel_ids)[pid.ordinal]
            val name = ctx.resources.getStringArray(R.array.reminders)[pid.ordinal]

            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = "description"
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = ctx.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}