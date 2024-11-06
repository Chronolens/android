package com.example.chronolens.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.chronolens.R

// TODO: ask for notification permission
fun createNotificationChannels(context: Context) {
    val syncChannel = NotificationChannel(
        Notifications.SYNC_CHANNEL_ID,
        Notifications.SYNC_PROGRESS,
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "Shows progress of syncing local and remote assets"
    }

    val uploadChannel = NotificationChannel(
        Notifications.UPLOAD_CHANNEL_ID,
        Notifications.UPLOAD_PROGRESS,
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "Shows progress of uploading media files"
    }

    val finishedChannel = NotificationChannel(
        Notifications.FINISHED_CHANNEL_ID,
        Notifications.FINISHED,
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "Shows upload has finished"
    }

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(syncChannel)
    notificationManager.createNotificationChannel(uploadChannel)
    notificationManager.createNotificationChannel(finishedChannel)
}

fun showSyncNotification(context: Context) {
    val syncNotification = NotificationCompat.Builder(context, Notifications.SYNC_CHANNEL_ID)
        .setSmallIcon(R.drawable.el_gato)
        .setContentTitle(context.resources.getString(R.string.notification_sync_message))
        .setContentText(context.resources.getString(R.string.notification_sync_message_init))
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(1, syncNotification)
}

fun showUploadNotification(context: Context, progress: Int, max: Int) {
    val uploadNotification = NotificationCompat.Builder(context, Notifications.UPLOAD_CHANNEL_ID)
        .setSmallIcon(R.drawable.el_gato)
        .setContentTitle(context.resources.getString(R.string.notification_upload_message))
        .setContentText(context.resources.getString(R.string.notification_upload_message_init))
        .setProgress(max, progress, false)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(2, uploadNotification)
}

fun showFinishedNotification(context: Context, uploaded: Int) {
    val finishedNotification =
        NotificationCompat.Builder(context, Notifications.FINISHED_CHANNEL_ID)
            .setSmallIcon(R.drawable.el_gato)
            .setContentTitle(context.resources.getString(R.string.notification_finished))
            .setContentText(
                context.resources.getQuantityString(
                    R.plurals.notification_finished_message,
                    uploaded,
                    uploaded
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(3, finishedNotification)
}

fun updateSyncNotificationProgress(context: Context, progress: Int, max: Int) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val syncNotification = NotificationCompat.Builder(context, Notifications.SYNC_CHANNEL_ID)
        .setSmallIcon(R.drawable.el_gato)
        .setContentTitle(context.resources.getString(R.string.notification_sync_message))
        .setContentText(
            context.resources.getString(
                R.string.notification_sync_message_progress,
                progress,
                max
            )
        )
        .setProgress(max, progress, false)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()
    notificationManager.notify(1, syncNotification)
}

fun updateUploadNotificationProgress(context: Context, progress: Int, max: Int) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val uploadNotification = NotificationCompat.Builder(context, Notifications.UPLOAD_CHANNEL_ID)
        .setSmallIcon(R.drawable.el_gato)
        .setContentTitle(context.resources.getString(R.string.notification_upload_message))
        .setContentText(
            context.resources.getString(
                R.string.notification_upload_message_progress,
                progress,
                max
            )
        )
        .setProgress(max, progress, false)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .build()
    notificationManager.notify(2, uploadNotification)
}
