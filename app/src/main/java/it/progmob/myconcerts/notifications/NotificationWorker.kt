package it.progmob.myconcerts.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import it.progmob.myconcerts.MainActivity
import it.progmob.myconcerts.R

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("NotificationWorker", "doWork called")

        val title = inputData.getString("title") ?: "Upcoming concert!"
        val message = inputData.getString("message") ?: "Don't forget your concert 🎶"
        val concertId = inputData.getString("concertId")

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "concert_reminder_channel",
                "Concert Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 👉 Intent per aprire MainActivity con deep link
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            concertId?.let {
                data = "myconcerts://concert_detail/$it".toUri()
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "concert_reminder_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        Log.d("NotificationWorker", "Notification sent")

        return Result.success()
    }
}
