package bassamalim.hidaya.core.receivers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.core.Activity
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Prayer
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.other.Global
import bassamalim.hidaya.core.services.AthanService
import bassamalim.hidaya.features.quran.reader.domain.QuranTarget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    private lateinit var ctx: Context
    private lateinit var action: String
    private lateinit var prayer: Prayer
    @Inject lateinit var notificationsRepository: NotificationsRepository
    @Inject lateinit var quranRepository: QuranRepository
    private var notificationId = 0
    private var channelId = ""
    private var time = 0L

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        ctx = context.applicationContext

        prayer = Prayer.valueOf(intent.getStringExtra("id")!!)
        time = intent.getLongExtra("time", 0L)

        GlobalScope.launch {
            if (isOnTime() || !isAlreadyNotified()) {
                action = intent.action!!
                when (intent.action) {
                    "prayer" -> handlePrayer()
                    "reminder" -> handlePrayerReminder()
                    "devotion" -> handleDevotionReminder()
                }
            }
            else Log.i(Global.TAG, "notification receiver: not on time or already notified")
        }
    }

    private suspend fun handlePrayer() {
        Log.i(Global.TAG, "in notification receiver for $prayer prayer")

        notificationId = prayer.ordinal

        val notificationType =
            notificationsRepository.getNotificationType(prayer.toReminder()).first()

        if (notificationType != NotificationType.NONE) {
            if (notificationType == NotificationType.ATHAN) startService()
            else showNotification(true, notificationType)
        }
    }

    private suspend fun handlePrayerReminder() {
        Log.i(Global.TAG, "in notification receiver for $prayer reminder")

        notificationId = prayer.ordinal + 10
        showNotification(false)
    }

    private suspend fun handleDevotionReminder() {
        Log.i(Global.TAG, "in notification receiver for $prayer extra")

        notificationId = prayer.ordinal

        val notificationType =
            notificationsRepository.getNotificationType(prayer.toReminder()).first()

        showNotification(false, notificationType)
    }

    private fun isOnTime(): Boolean {
        val max = time + 120000
        return System.currentTimeMillis() <= max
    }

    private suspend fun isAlreadyNotified(): Boolean {
        val lastDate =
            notificationsRepository.getLastNotificationDates().first()[prayer.toReminder()]!!
        return lastDate == Calendar.getInstance()[Calendar.DAY_OF_YEAR]
    }

    private suspend fun showNotification(
        isPrayer: Boolean,
        notificationType: NotificationType = NotificationType.NOTIFICATION
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

        markAsNotified()
    }

    private fun startService() {
        val intent = Intent(ctx, AthanService::class.java)
        intent.action = Global.PLAY_ATHAN
        intent.putExtra("pid", prayer.name)
        intent.putExtra("time", time)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)
    }

    private suspend fun build(
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
        builder.setContentIntent(onClick(prayer.toReminder()))

        if (notificationType == NotificationType.SILENT)
            builder.setSilent(true)

        return builder.build()
    }

    private fun getTitle(): String {
        return if (prayer == Prayer.DHUHR &&
            Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
            if (action == "reminder") ctx.resources.getString(R.string.jumuah_reminder_title)
            else ctx.resources.getString(R.string.jumuah_title)
        }
        else ctx.resources.getStringArray(R.array.prayer_titles)[notificationId]
    }

    private suspend fun getSubtitle(): String {
        return if (action == "reminder") {
            val offset =notificationsRepository.getPrayerExtraReminderTimeOffsets()
                .first()[prayer.toReminder()]!!
            String.format(
                format =
                    if (offset < 0) ctx.resources.getString(R.string.reminder_before)
                    else ctx.resources.getString(R.string.reminder_after),
                    if (prayer == Prayer.DHUHR &&
                        Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                        ctx.resources.getString(R.string.jumuah)
                    else ctx.resources.getStringArray(R.array.prayer_names)[prayer.ordinal],
                abs(offset) // to remove - sign
            )
        }
        else {
            if (prayer == Prayer.DHUHR &&
                Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                ctx.resources.getString(R.string.jumuah_subtitle)
            else ctx.resources.getStringArray(R.array.prayer_subtitles)[notificationId]
        }
    }

    private suspend fun onClick(reminder: Reminder): PendingIntent {
        val intent = Intent(ctx, Activity::class.java)

        val route = when (reminder) {
            Reminder.Devotional.MorningRemembrances -> {
                Screen.RemembranceReader(0.toString()).route
            }
            Reminder.Devotional.EveningRemembrances -> {
                Screen.RemembranceReader(1.toString()).route
            }
            Reminder.Devotional.DailyWerd -> {
                Screen.QuranReader(
                    targetType = QuranTarget.PAGE.name,
                    targetValue = quranRepository.getWerdPageNum().first().toString()
                ).route
            }
            Reminder.Devotional.FridayKahf -> {
                Screen.QuranReader(
                    targetType = QuranTarget.SURA.name,
                    targetValue = 17.toString() // surat al-kahf
                ).route
            }
            else -> Screen.Main.route
        }
        intent.putExtra("start_route", route)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        return PendingIntent.getActivity(
            ctx, prayer.ordinal, intent,
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

    private suspend fun markAsNotified() {
        notificationsRepository.setLastNotificationDate(
            reminder = prayer.toReminder(),
            dayOfYear = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        )
    }

}