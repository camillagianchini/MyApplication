package it.progmob.myconcerts.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import it.progmob.myconcerts.R

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        Log.d("NotificationWorker", "doWork called")

        val title = inputData.getString("title") ?: "Upcoming concert!"
        val message = inputData.getString("message") ?: "Don't forget your concert 🎶"

        Log.d("NotificationWorker", "Title: $title, Message: $message")

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "concert_reminder_channel",
                "Concert Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
            Log.d("NotificationWorker", "Notification channel created")
        }

        val notification = NotificationCompat.Builder(applicationContext, "concert_reminder_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notifications)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)

        Log.d("NotificationWorker", "Notification sent")

        return Result.success()
    }
}
