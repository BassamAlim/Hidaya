package bassamalim.hidaya.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.AthkarViewer
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.activities.Splash
import bassamalim.hidaya.enum.NotificationType
import bassamalim.hidaya.enum.PID
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.AthanService
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.PrefUtils
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    private lateinit var context: Context
    private lateinit var pref: SharedPreferences
    private lateinit var pid: PID
    private var isPrayer = false
    private var channelId = ""
    private var type = NotificationType.None
    private var time = 0L

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        pref = PrefUtils.getPreferences(context)

        ActivityUtils.onActivityCreateSetLocale(context as Activity)

        pid = PID.valueOf(intent.getStringExtra("id")!!)
        time = intent.getLongExtra("time", 0L)
        isPrayer = intent.action == "prayer"

        Log.i(Global.TAG, "in notification receiver for $pid")

        val defaultType =
            if (pid == PID.SUNRISE) NotificationType.None
            else NotificationType.Notification
        val typeName = PrefUtils.getString(pref, "$pid notification_type", defaultType.name)
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
            val am = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
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

        NotificationManagerCompat.from(context).notify(pid.ordinal, build())
    }

    private fun startService() {
        val serviceIntent = Intent(context, AthanService::class.java)
        serviceIntent.action = Global.PLAY_ATHAN
        serviceIntent.putExtra("pid", pid.name)
        serviceIntent.putExtra("time", time)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(serviceIntent)
        else context.startService(serviceIntent)
    }

    private fun build(): Notification {
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(R.drawable.ic_athan)
        builder.setTicker(context.resources.getString(R.string.app_name))

        var i = pid.ordinal
        if (pid == PID.DHUHR && Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
            i = 10
        builder.setContentTitle(context.resources.getStringArray(R.array.prayer_titles)[i])
        builder.setContentText(context.resources.getStringArray(R.array.prayer_subtitles)[i])

        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setAutoCancel(true)
        builder.setOnlyAlertOnce(true)
        builder.color = context.getColor(R.color.surface_M)
        builder.setContentIntent(onClick(pid))

        if (type == NotificationType.Silent) builder.setSilent(true)

        return builder.build()
    }

    private fun onClick(pid: PID?): PendingIntent {
        val intent: Intent

        when (pid) {
            PID.MORNING -> {
                intent = Intent(context, AthkarViewer::class.java)
                intent.putExtra("category", 0)
                intent.putExtra("thikr_id", 0)
                intent.putExtra("title", "أذكار الصباح")
            }
            PID.EVENING -> {
                intent = Intent(context, AthkarViewer::class.java)
                intent.putExtra("category", 0)
                intent.putExtra("thikr_id", 1)
                intent.putExtra("title", "أذكار المساء")
            }
            PID.DAILY_WERD -> {
                intent = Intent(context, QuranViewer::class.java)
                intent.action = "by_page"
                intent.putExtra("page",
                    PrefUtils.getInt(
                        pref, "today_werd_page", Random().nextInt(Global.QURAN_PAGES-1)
                    )
                )
            }
            PID.FRIDAY_KAHF -> {
                intent = Intent(context, QuranViewer::class.java)
                intent.action = "by_surah"
                intent.putExtra("surah_id", 17) // surat al-kahf
            }
            else -> intent = Intent(context, Splash::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        return PendingIntent.getActivity(
            context, pid!!.ordinal, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = context.resources.getStringArray(R.array.channel_ids)[pid.ordinal]
            val name = context.resources.getStringArray(R.array.reminders)[pid.ordinal]

            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = "description"
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}