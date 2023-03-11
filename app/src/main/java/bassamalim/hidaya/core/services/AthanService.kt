package bassamalim.hidaya.core.services

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bassamalim.hidaya.core.MainActivity
import bassamalim.hidaya.R
import bassamalim.hidaya.core.enums.PID
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.utils.ActivityUtils
import java.util.*

class AthanService : Service() {

    private val iBinder = LocalBinder()
    private lateinit var pid: PID
    private var channelId = ""
    private lateinit var mediaPlayer: MediaPlayer

    inner class LocalBinder : Binder() {
        val service: AthanService
            get() = this@AthanService
    }

    override fun onCreate() {
        super.onCreate()
        ActivityUtils.onActivityCreateSetLocale(applicationContext)

        createNotificationChannel()
        startForeground(243, build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        pid = PID.valueOf(intent?.getStringExtra("pid")!!)

        Log.i(Global.TAG, "In athan service for $pid")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
            // Request audio focus
            am.requestAudioFocus(             // Request permanent focus.
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
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
        if (pid == PID.DHUHR && Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
            i = 10
        builder.setContentTitle(resources.getStringArray(R.array.prayer_titles)[i])
        builder.setContentText(resources.getStringArray(R.array.prayer_subtitles)[i])

        builder.addAction(0, getString(R.string.stop_athan), getStopIntent())
        builder.clearActions()
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
            this, 11,
            Intent(this, AthanService::class.java)
                .setAction(Global.STOP_ATHAN)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getStopAndOpenIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this, 12,
            Intent(this, MainActivity::class.java)
                .setAction(Global.STOP_ATHAN),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "Athan"
            val name = getString(R.string.prayer_alerts)

            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, name, importance)
            notificationChannel.description = "Athan"
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
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
        )
        mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
        mediaPlayer.setOnCompletionListener {
            showReminderNotification()
            onDestroy()
        }
    }

    private fun showReminderNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val am = getSystemService(AUDIO_SERVICE) as AudioManager
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
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(pid.ordinal, build())
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return iBinder
    }

    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
        else stopForeground(true)

        mediaPlayer.stop()

        stopSelf()
    }

}