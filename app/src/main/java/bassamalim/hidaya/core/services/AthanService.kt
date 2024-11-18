package bassamalim.hidaya.core.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.core.Activity
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.enums.StartAction
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.ui.theme.colorSchemeO
import bassamalim.hidaya.core.utils.ActivityUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AthanService : Service() {

    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var notificationsRepository: NotificationsRepository
    private var notificationId = 0
    private var channelId = ""
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent): IBinder? { return null }  // Not used

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch {
            ActivityUtils.configure(
                context = application,
                applicationContext = applicationContext,
                language = appSettingsRepository.getLanguage().first(),
            )
            createNotificationChannel()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == StartAction.STOP_ATHAN.name) {
            onDestroy()
            return START_NOT_STICKY
        }

        val reminder = Reminder.getById(intent!!.getIntExtra("id", -1))
        notificationId = reminder.id

        GlobalScope.launch {
            startForeground(243, build(reminder))

            Log.i(Global.TAG, "In athan service for $reminder")

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

            play(reminder)
        }

        return START_NOT_STICKY
    }

    private fun build(reminder: Reminder): Notification {
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.drawable.ic_athan)
        builder.setTicker(resources.getString(R.string.app_name))

        builder.setContentTitle(getTitle(reminder))
        builder.setContentText(getSubtitle(reminder))

        builder.addAction(0, getString(R.string.stop_athan), getStopIntent())
        builder.clearActions()
        builder.setContentIntent(getStopAndOpenIntent())
        builder.setDeleteIntent(getStopIntent())
        builder.priority = NotificationCompat.PRIORITY_MAX
        builder.setAutoCancel(true)
        builder.setOnlyAlertOnce(true)
        builder.color = colorSchemeO.surfaceContainer.value.toInt() // TODO: get theme color

        return builder.build()
    }

    private fun getTitle(reminder: Reminder): String {
        return if ((reminder == Reminder.Prayer.Dhuhr || reminder == Reminder.PrayerExtra.Dhuhr) &&
            Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
            resources.getString(R.string.jumuah_title)
        }
        else resources.getStringArray(R.array.prayer_titles)[reminder.id-1]
    }

    private fun getSubtitle(reminder: Reminder): String {
        return if (reminder == Reminder.Prayer.Dhuhr &&
            Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
            resources.getString(R.string.jumuah_subtitle)
        else resources.getStringArray(R.array.prayer_subtitles)[reminder.id-1]
    }

    private fun getStopIntent(): PendingIntent {
        return PendingIntent.getService(
            this, 11,
            Intent(this, AthanService::class.java)
                .setAction(StartAction.STOP_ATHAN.name)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getStopAndOpenIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this, 12,
            Intent(this, Activity::class.java)
                .setAction(StartAction.STOP_ATHAN.name),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "athan"
            val channel = NotificationChannel(
                channelId,
                getString(R.string.prayer_notification_channel),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Athan notification channel"
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun play(reminder: Reminder) {
        Log.i(Global.TAG, "Playing Athan")

        GlobalScope.launch {
            val athanAudio = getAthanAudio()
            mediaPlayer = MediaPlayer.create(this@AthanService, athanAudio)
            mediaPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            mediaPlayer!!.setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
            )
            mediaPlayer!!.setOnPreparedListener { mediaPlayer?.start() }
            mediaPlayer!!.setOnCompletionListener {
                GlobalScope.launch {
                    showReminderNotification(reminder)
                    onDestroy()
                }
            }
        }
    }

    private suspend fun getAthanAudio(): Int {
        val athanAudioId = prayersRepository.getAthanAudioId().first()
        return when(athanAudioId) {
            1 -> R.raw.athan1
            2 -> R.raw.athan2
            3 -> R.raw.athan3
            else -> R.raw.athan1
        }
    }

    private suspend fun showReminderNotification(reminder: Reminder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            // Request audio focus
            audioManager.requestAudioFocus(                   // Request permanent focus
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    ).build()
            )
        }

        val havePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        if (havePermission)
            NotificationManagerCompat.from(this).notify(notificationId, build(reminder))
    }

    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
        else stopForeground(true)

        mediaPlayer?.stop()

        stopSelf()
    }

}