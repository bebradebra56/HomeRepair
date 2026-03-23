package com.homerapa.repagom.gefr.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.homerapa.repagom.HomeRepairActivity
import com.homerapa.repagom.R
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication

private const val HOME_REPAIR_CHANNEL_ID = "home_repair_notifications"
private const val HOME_REPAIR_CHANNEL_NAME = "HomeRepair Notifications"
private const val HOME_REPAIR_NOT_TAG = "HomeRepair"

class HomeRepairPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                homeRepairShowNotification(it.title ?: HOME_REPAIR_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                homeRepairShowNotification(it.title ?: HOME_REPAIR_NOT_TAG, it.body ?: "", data = null)
            }
        }

        // Обработка data payload
        if (remoteMessage.data.isNotEmpty()) {
            homeRepairHandleDataPayload(remoteMessage.data)
        }
    }

    private fun homeRepairShowNotification(title: String, message: String, data: String?) {
        val homeRepairNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                HOME_REPAIR_CHANNEL_ID,
                HOME_REPAIR_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            homeRepairNotificationManager.createNotificationChannel(channel)
        }

        val homeRepairIntent = Intent(this, HomeRepairActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val homeRepairPendingIntent = PendingIntent.getActivity(
            this,
            0,
            homeRepairIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val homeRepairNotification = NotificationCompat.Builder(this, HOME_REPAIR_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.home_repair_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(homeRepairPendingIntent)
            .build()

        homeRepairNotificationManager.notify(System.currentTimeMillis().toInt(), homeRepairNotification)
    }

    private fun homeRepairHandleDataPayload(data: Map<String, String>) {
        data.forEach { (key, value) ->
            Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Data key=$key value=$value")
        }
    }
}