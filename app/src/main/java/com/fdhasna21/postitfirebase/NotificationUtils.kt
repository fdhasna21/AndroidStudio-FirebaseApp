package com.fdhasna21.postitfirebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fdhasna21.postitfirebase.dataclass.Profile

class NotificationUtils(val context: Context, private val channelID : String, private val importance : Int, val name : String, private val description : String) {

    private val notificationId = 101

    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = description
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(title : String,  text : String, profile:Profile){
        val chatIntent = Intent(context, (context as AppCompatActivity)::class.java).apply {
            putExtra("target", profile)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val browserIntent = Intent().apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
            action = Intent.ACTION_VIEW
            data = Uri.parse(profile.url)
        }

        val gotoChatIntent = PendingIntent.getActivity(context, 0 , chatIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val gotoBrowserIntent = PendingIntent.getActivity(context, 0, browserIntent, 0)

        val bitmap : Bitmap? = BitmapFactory.decodeResource(context.resources, R.drawable.profile_pict)
        val builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setLargeIcon(bitmap)
//            .setStyle(NotificationCompat.BigTextStyle().bigText("test"))
            .setContentIntent(gotoChatIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(R.drawable.ic_attach, "Photo Profile", gotoBrowserIntent)

        with(NotificationManagerCompat.from(context)){
            notify(notificationId, builder.build())
        }
    }
}