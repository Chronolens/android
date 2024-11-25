package com.example.chronolens.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.chronolens.R

// TODO: design do icon
enum class Notification{
    SYNC_CHANNEL_ID,
    UPLOAD_CHANNEL_ID,
    FINISHED_CHANNEL_ID,
    SYNC_PROGRESS,
    UPLOAD_PROGRESS,
    FINISHED
}

fun createNotificationChannels(context: Context) {
    val syncChannel = NotificationChannel(
        Notification.SYNC_CHANNEL_ID.name,
        Notification.SYNC_PROGRESS.name,
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "Shows progress of syncing local and remote assets"
    }

    val uploadChannel = NotificationChannel(
        Notification.UPLOAD_CHANNEL_ID.name,
        Notification.UPLOAD_PROGRESS.name,
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "Shows progress of uploading media files"
    }

    val finishedChannel = NotificationChannel(
        Notification.FINISHED_CHANNEL_ID.name,
        Notification.FINISHED.name,
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
    val syncNotification = NotificationCompat.Builder(context, Notification.SYNC_CHANNEL_ID.name)
        .setSmallIcon(R.drawable.el_gato)
        .setContentTitle(context.resources.getString(R.string.notification_sync_message))
        .setContentText(context.resources.getString(R.string.notification_sync_message_init))
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(Notification.SYNC_CHANNEL_ID.ordinal, syncNotification)
}

fun showUploadNotification(context: Context, progress: Int, max: Int) {
    val uploadNotification = NotificationCompat.Builder(context, Notification.UPLOAD_CHANNEL_ID.name)
        .setSmallIcon(R.drawable.el_gato)
        .setContentTitle(context.resources.getString(R.string.notification_upload_message))
        .setContentText(context.resources.getString(R.string.notification_upload_message_init))
        .setProgress(max, progress, false)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(Notification.UPLOAD_CHANNEL_ID.ordinal, uploadNotification)
}

fun showFinishedNotification(context: Context, uploaded: Int) {
    val finishedNotification =
        NotificationCompat.Builder(context, Notification.FINISHED_CHANNEL_ID.name)
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
    notificationManager.notify(Notification.FINISHED_CHANNEL_ID.ordinal, finishedNotification)
}

fun updateSyncNotificationProgress(context: Context, progress: Int, max: Int) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val syncNotification = NotificationCompat.Builder(context, Notification.SYNC_CHANNEL_ID.name)
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
        .build()
    notificationManager.notify(Notification.SYNC_CHANNEL_ID.ordinal, syncNotification)
}

fun updateUploadNotificationProgress(context: Context, progress: Int, max: Int) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val uploadNotification = NotificationCompat.Builder(context, Notification.UPLOAD_CHANNEL_ID.name)
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
        .build()
    notificationManager.notify(Notification.UPLOAD_CHANNEL_ID.ordinal, uploadNotification)
}
