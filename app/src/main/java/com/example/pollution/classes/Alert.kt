package com.example.pollution.classes

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.example.pollution.R
import com.example.pollution.ui.MapsActivity

class Alert {
    companion object {
        fun dangerAlert(context: Context, channel_id: String) { // Send the alert.
            val intent = Intent(context, MapsActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val builder =
                NotificationCompat.Builder(context, channel_id) // The builder contains the notification attributes.
                    .setSmallIcon(R.drawable.menu_alert)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_desc))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(0, builder.build()) // Send the notification with the builder defined above.
            }
        }
    }
}