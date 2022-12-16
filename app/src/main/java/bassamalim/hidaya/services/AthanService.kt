package bassamalim.hidaya.services

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.activities.Splash
import bassamalim.hidaya.enums.PID
import bassamalim.hidaya.other.Global
import bassamalim.hidaya.utils.ActivityUtils
import java.util.*

class AthanService : Service() {

    private lateinit var pid: PID
    private var channelId = ""
    private lateinit var mediaPlayer: MediaPlayer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        pid = PID.valueOf(intent?.getStringExtra("pid")!!)

        ActivityUtils.onActivityCreateSetLocale(this)

        Log.i(Global.TAG, "In athan service for $pid")

        createNotificationChannel()
        startForeground(pid.ordinal, build())


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            // Request audio focus
            am.requestAudioFocus(             // Request permanent focus.
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    ).build()
            )
        }

        play()

        return START_NOT_STICKY
    }

    private fun build(): Notification {
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_athan)
        builder.setTicker(resources.getString(R.string.app_name))

        var i = pid.ordinal
        if (pid == PID.DUHR && Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) i = 10
        builder.setContentTitle(resources.getStringArray(R.array.prayer_titles)[i])
        builder.setContentText(resources.getStringArray(R.array.prayer_subtitles)[i])

        builder.addAction(0, getString(R.string.stop_athan), getStopIntent())
        builder.setContentIntent(getStopAndOpenIntent())
        builder.setDeleteIntent(getStopIntent())
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setAutoCancel(true)
        builder.setOnlyAlertOnce(true)
        builder.color = getColor(R.color.surface_M)

        return builder.build()
    }

    private fun getStopIntent(): PendingIntent {
        return PendingIntent.getService(
            this, 11, Intent(this, AthanService::class.java)
                .setAction(Global.STOP_ATHAN).setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getStopAndOpenIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this, 12, Intent(this, Splash::class.java)
                .setAction(Global.STOP_ATHAN),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence
            val description = ""

            channelId = "Athan"
            name = getString(R.string.prayer_alerts)

            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = description
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun play() {
        Log.i(Global.TAG, "Playing Athan")

        mediaPlayer = MediaPlayer.create(this, R.raw.athan1)
        mediaPlayer.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA).build()
        )
        mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
        mediaPlayer.setOnCompletionListener { stopMyService() }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun stopMyService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
        else stopForeground(true)
        stopSelf()
        mediaPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMyService()
    }

}