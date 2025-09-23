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
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.core.Activity
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.PrayersRepository
import bassamalim.hidaya.core.di.ApplicationScope
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.enums.StartAction
import bassamalim.hidaya.core.enums.ThemeColor
import bassamalim.hidaya.core.ui.theme.getThemeColor
import bassamalim.hidaya.core.utils.ActivityUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class AthanService : Service() {

    @Inject @ApplicationScope lateinit var scope: CoroutineScope
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    @Inject lateinit var prayersRepository: PrayersRepository
    @Inject lateinit var notificationsRepository: NotificationsRepository
    private var notificationId = 0
    private var channelId = ""
    private var mediaPlayer: MediaPlayer? = null
    private var athanAudio: Int? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        scope.launch {
            ActivityUtils.configure(
                context = application,
                applicationContext = applicationContext,
                language = appSettingsRepository.getLanguage().first(),
            )

            athanAudio = getAthanAudio()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == StartAction.STOP_ATHAN.name) {
            onDestroy()
            return START_NOT_STICKY
        }

        val reminder = Reminder.getById(intent!!.getIntExtra("id", -1))
        val time = intent.getLongExtra("time", 0L)
        Log.i(Globals.TAG, "In athan service for $reminder")
        notificationId = reminder.id

        runBlocking {
            if (!isOnTime(time) || isAlreadyNotified(reminder)) {
                Log.i(Globals.TAG, "notification receiver: not on time or already notified")
                return@runBlocking
            }

            startForeground(notificationId, build(reminder))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (getSystemService(AUDIO_SERVICE) as AudioManager)
                    .requestAudioFocus(
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

    override fun onBind(intent: Intent): IBinder? = null

    private fun isOnTime(time: Long): Boolean {
        val max = time + 120000
        return System.currentTimeMillis() <= max
    }

    private suspend fun isAlreadyNotified(reminder: Reminder): Boolean {
        val lastDate = notificationsRepository.getLastNotificationDates().first()[reminder]
        return lastDate == Calendar.getInstance()[Calendar.DAY_OF_YEAR]
    }

    private suspend fun build(reminder: Reminder): Notification {
        return NotificationCompat.Builder(this, channelId).apply {
            setSmallIcon(R.drawable.small_launcher_foreground)
            setTicker(resources.getString(R.string.app_name))

            setContentTitle(getTitle(reminder))
            setContentText(getSubtitle(reminder))

            addAction(0, getString(R.string.stop_athan), getStopIntent())
            clearActions()
            setContentIntent(getStopAndOpenIntent())
            setDeleteIntent(getStopIntent())
            priority = NotificationCompat.PRIORITY_MAX
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            color = getThemeColor(
                color = ThemeColor.SURFACE_CONTAINER,
                theme = appSettingsRepository.getTheme().first()
            ).toArgb()
        }.build()
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
            this,
            11,
            Intent(this, AthanService::class.java)
                .setAction(StartAction.STOP_ATHAN.name)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getStopAndOpenIntent(): PendingIntent {
        return PendingIntent.getActivity(
            this,
            12,
            Intent(this, Activity::class.java).setAction(StartAction.STOP_ATHAN.name),
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
            ).apply {
                description = "Athan notification channel"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun play(reminder: Reminder) {
        Log.i(Globals.TAG, "Playing Athan")

        if (athanAudio == null) {
            Log.e(Globals.TAG, "Athan audio is null")
            return
        }

        mediaPlayer = MediaPlayer.create(this@AthanService, athanAudio!!).apply {
            setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
            setAudioAttributes(
                AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
            )
            setOnPreparedListener { mediaPlayer?.start() }
            setOnCompletionListener {
                scope.launch {
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
            (getSystemService(AUDIO_SERVICE) as AudioManager)
                .requestAudioFocus(                   // Request permanent focus
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
        if (havePermission) {
            NotificationManagerCompat
                .from(this)
                .notify(notificationId, build(reminder))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(STOP_FOREGROUND_REMOVE)
        else stopForeground(true)

        mediaPlayer?.stop()

        stopSelf()
    }

}