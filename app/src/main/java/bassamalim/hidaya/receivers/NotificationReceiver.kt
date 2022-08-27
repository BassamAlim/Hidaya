package bassamalim.hidaya.receivers

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.AthkarViewer
import bassamalim.hidaya.activities.QuranViewer
import bassamalim.hidaya.activities.Splash
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.services.AthanService
import bassamalim.hidaya.utils.ActivityUtils
import bassamalim.hidaya.utils.PTUtils
import java.util.*

class NotificationReceiver : BroadcastReceiver() {

    private lateinit var context: Context
    private lateinit var pid: PID
    private var isPrayer = false
    private var channelId = ""
    private var type = 0
    private var time: Long = 0

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context

        pid = PTUtils.mapID(intent.getIntExtra("id", 0))!!
        time = intent.getLongExtra("time", 0)
        isPrayer = intent.action == "prayer"

        ActivityUtils.onActivityCreateSetLocale(context)

        Log.i(Global.TAG, "in notification receiver for $pid")

        val defaultType =
            if (pid == PID.SHOROUQ) 0
            else 2

        type = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt("$pid notification_type", defaultType)

        if (type != 0) prepare()
    }

    private fun prepare() {
        val max = time + 120000
        if (System.currentTimeMillis() <= max) {
            if (type == 3) startService()
            else showNotification()
        }
        else Log.i(Global.TAG, "$pid Passed")
    }

    private fun showNotification() {
        createNotificationChannel()

        if (isPrayer && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val am = context.getSystemService(Service.AUDIO_SERVICE) as AudioManager
            // Request audio focus
            am.requestAudioFocus(                   // Request permanent focus.
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    )
                    .build()
            )
        }

        NotificationManagerCompat.from(context).notify(pid.ordinal, build())
    }

    private fun startService() {
        val intent1 = Intent(context, AthanService::class.java)
        intent1.action = Global.PLAY_ATHAN
        intent1.putExtra("id", pid)
        intent1.putExtra("time", time)

        if (Build.VERSION.SDK_INT >= 26)
            context.startForegroundService(intent1)
        else
            context.startService(intent1)
    }

    private fun build(): Notification {
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(R.drawable.ic_athan)
        builder.setTicker(context.resources.getString(R.string.app_name))

        var i: Int = pid.ordinal
        if (pid == PID.DUHR && Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
            i = 10
        builder.setContentTitle(context.resources.getStringArray(R.array.prayer_titles)[i])
        builder.setContentText(context.resources.getStringArray(R.array.prayer_subtitles)[i])

        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setAutoCancel(true)
        builder.setOnlyAlertOnce(true)
        builder.color = context.getColor(R.color.surface_M)
        builder.setContentIntent(onClick(pid))

        if (type == 1) builder.setSilent(true)

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
                intent.action = "random"
            }
            PID.FRIDAY_KAHF -> {
                intent = Intent(context, QuranViewer::class.java)
                intent.action = "by_surah"
                intent.putExtra("surah_id", 17) // alkahf
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
            val name: CharSequence
            val description = ""

            channelId = context.resources.getStringArray(R.array.channel_ids)[pid.ordinal]
            name = context.resources.getStringArray(R.array.reminders)[pid.ordinal]

            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = description
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

}