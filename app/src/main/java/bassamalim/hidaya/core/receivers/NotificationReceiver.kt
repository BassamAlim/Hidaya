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
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bassamalim.hidaya.R
import bassamalim.hidaya.core.Activity
import bassamalim.hidaya.core.Globals
import bassamalim.hidaya.core.data.repositories.AppSettingsRepository
import bassamalim.hidaya.core.data.repositories.NotificationsRepository
import bassamalim.hidaya.core.data.repositories.QuranRepository
import bassamalim.hidaya.core.enums.NotificationType
import bassamalim.hidaya.core.enums.Reminder
import bassamalim.hidaya.core.enums.ThemeColor
import bassamalim.hidaya.core.nav.Screen
import bassamalim.hidaya.core.services.AthanService
import bassamalim.hidaya.core.ui.theme.getThemeColor
import bassamalim.hidaya.core.utils.ActivityUtils
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
    @Inject lateinit var notificationsRepository: NotificationsRepository
    @Inject lateinit var quranRepository: QuranRepository
    @Inject lateinit var appSettingsRepository: AppSettingsRepository
    private var notificationId = 0
    private var channelId = ""

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        ctx = context.applicationContext

        val reminder = Reminder.getById(intent.getIntExtra("id", -1))
        Log.d(Globals.TAG, "notification receiver: received $reminder")
        val time = intent.getLongExtra("time", 0L)
        notificationId = reminder.id

        GlobalScope.launch {
            ActivityUtils.configure(
                context = context,
                applicationContext = context.applicationContext,
                language = appSettingsRepository.getLanguage().first()
            )

            if (!isOnTime(time) || isAlreadyNotified(reminder)) {
                Log.i(Globals.TAG, "notification receiver: not on time or already notified")
                return@launch
            }

            when (reminder) {
                is Reminder.Prayer -> handlePrayerReminder(reminder, time)
                is Reminder.PrayerExtra -> handlePrayerExtraReminder(reminder)
                is Reminder.Devotional -> handleDevotionalReminder(reminder)
            }
        }
    }

    private suspend fun handlePrayerReminder(reminder: Reminder.Prayer, time: Long) {
        Log.i(Globals.TAG, "in notification receiver for $reminder prayer")

        val notificationType = notificationsRepository.getNotificationType(reminder).first()

        if (notificationType != NotificationType.OFF) {
            if (notificationType == NotificationType.ATHAN) startService(reminder, time)
            else showNotification(reminder, notificationType)
        }
    }

    private suspend fun handlePrayerExtraReminder(reminder: Reminder.PrayerExtra) {
        Log.i(Globals.TAG, "in notification receiver for $reminder reminder")

        showNotification(reminder)
    }

    private suspend fun handleDevotionalReminder(reminder: Reminder.Devotional) {
        Log.i(Globals.TAG, "in notification receiver for $reminder extra")

        showNotification(reminder)
    }

    private fun isOnTime(time: Long): Boolean {
        val max = time + 120000
        return System.currentTimeMillis() <= max
    }

    private suspend fun isAlreadyNotified(reminder: Reminder): Boolean {
        val lastDate = notificationsRepository.getLastNotificationDates().first()[reminder]
        return lastDate == Calendar.getInstance()[Calendar.DAY_OF_YEAR]
    }

    private suspend fun showNotification(
        reminder: Reminder,
        notificationType: NotificationType = NotificationType.NOTIFICATION
    ) {
        createNotificationChannel(reminder)

        if (reminder is Reminder.Prayer && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioManager = ctx.getSystemService(Service.AUDIO_SERVICE) as AudioManager
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

        if (ActivityCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {
            val notification = build(reminder, notificationType)
            NotificationManagerCompat.from(ctx).notify(notificationId, notification)
        }

        markAsNotified(reminder)
    }

    private fun startService(reminder: Reminder.Prayer, time: Long) {
        val intent = Intent(ctx, AthanService::class.java).apply {
            action = Globals.PLAY_ATHAN
            putExtra("id", reminder.id)
            putExtra("time", time)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent)
        else
            ctx.startService(intent)

        markAsNotified(reminder)
    }

    private suspend fun build(
        reminder: Reminder,
        notificationType: NotificationType
    ): Notification {
        return NotificationCompat.Builder(ctx, channelId).apply {
            setSmallIcon(R.drawable.ic_athan)
            setTicker(ctx.resources.getString(R.string.app_name))

            setContentTitle(getTitle(reminder))
            setContentText(getSubtitle(reminder))

            priority = NotificationCompat.PRIORITY_MAX
            setAutoCancel(true)
            setOnlyAlertOnce(true)
            this.color = getThemeColor(
                color = ThemeColor.SURFACE_CONTAINER,
                theme = appSettingsRepository.getTheme().first()
            ).toArgb()
            setContentIntent(onClick(reminder))

            if (notificationType == NotificationType.SILENT)
                setSilent(true)
        }.build()
    }

    private fun getTitle(reminder: Reminder): String {
        return if ((reminder == Reminder.Prayer.Dhuhr || reminder == Reminder.PrayerExtra.Dhuhr) &&
            Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY) {
            if (reminder is Reminder.PrayerExtra)
                ctx.resources.getString(R.string.jumuah_reminder_title)
            else
                ctx.resources.getString(R.string.jumuah_title)
        }
        else ctx.resources.getStringArray(R.array.prayer_titles)[reminder.id-1]
    }

    private suspend fun getSubtitle(reminder: Reminder): String {
        return if (reminder is Reminder.PrayerExtra) {
            val offset = notificationsRepository.getPrayerExtraReminderTimeOffsets()
                .first()[reminder]!!
            String.format(
                format =
                    if (offset < 0) ctx.resources.getString(R.string.reminder_before)
                    else ctx.resources.getString(R.string.reminder_after),
                    if (reminder == Reminder.PrayerExtra.Dhuhr &&
                        Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                        ctx.resources.getString(R.string.jumuah)
                    else
                        ctx.resources.getStringArray(R.array.prayer_names)
                            [reminder.toPrayer().toReminder().id-1],
                abs(offset) // to remove - sign
            )
        }
        else {
            if (reminder == Reminder.Prayer.Dhuhr &&
                Calendar.getInstance()[Calendar.DAY_OF_WEEK] == Calendar.FRIDAY)
                ctx.resources.getString(R.string.jumuah_subtitle)
            else ctx.resources.getStringArray(R.array.prayer_subtitles)[reminder.id-1]
        }
    }

    private suspend fun onClick(reminder: Reminder): PendingIntent {
        val intent = Intent(ctx, Activity::class.java)

        val route = when (reminder) {
            Reminder.Devotional.MorningRemembrances -> Screen.RemembranceReader(0.toString()).route
            Reminder.Devotional.EveningRemembrances -> Screen.RemembranceReader(1.toString()).route
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
            ctx,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel(reminder: Reminder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = getChannelId(reminder)
            val channel = NotificationChannel(
                channelId,
                getChannelName(reminder),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "description"
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val notificationManager = ctx.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getChannelId(reminder: Reminder): String {
        return when (reminder) {
            is Reminder.Prayer -> "prayers_channel"
            is Reminder.PrayerExtra -> "prayers_extra_channel"
            is Reminder.Devotional -> {
                when (reminder) {
                    Reminder.Devotional.MorningRemembrances,
                    Reminder.Devotional.EveningRemembrances ->
                        "morning_and_evening_remembrances_channel"
                    Reminder.Devotional.DailyWerd -> "daily_werd_channel"
                    Reminder.Devotional.FridayKahf -> "friday_kahf_channel"
                }
            }
        }
    }

    private fun getChannelName(reminder: Reminder): String {
        return when (reminder) {
            is Reminder.Prayer -> ctx.getString(R.string.prayer_notification_channel)
            is Reminder.PrayerExtra -> ctx.getString(R.string.prayer_extra_notification_channel)
            is Reminder.Devotional -> {
                when (reminder) {
                    Reminder.Devotional.MorningRemembrances,
                    Reminder.Devotional.EveningRemembrances -> ctx.getString(
                        R.string.morning_and_evening_remembrances_notification_channel
                    )
                    Reminder.Devotional.DailyWerd -> ctx.getString(
                        R.string.daily_werd_notification_channel
                    )
                    Reminder.Devotional.FridayKahf -> ctx.getString(
                        R.string.friday_kahf_notification_channel
                    )
                }
            }
        }
    }

    private fun markAsNotified(reminder: Reminder) {
        notificationsRepository.setLastNotificationDate(
            reminder = reminder,
            dayOfYear = Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        )
    }

}