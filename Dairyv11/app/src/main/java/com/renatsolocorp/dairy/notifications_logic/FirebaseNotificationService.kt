package com.renatsolocorp.dairy.notifications_logic

import android.content.Intent
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.os.Handler
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class FirebaseNotificationService: FirebaseMessagingService() {
    private var broadcaster: LocalBroadcastManager? = null

    override fun onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this)
    }

    //TODO somewhen in a batter world to make internet pushed notifications

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        handleMessage(remoteMessage)
    }

    private fun handleMessage(remoteMessage: RemoteMessage) {
        val handler = Handler(Looper.getMainLooper())

        handler.post {
            Toast.makeText(baseContext, "handle notification now, whatever that means", Toast.LENGTH_LONG).show()

            remoteMessage.notification?.let {
                val intent = Intent("MyData")
                intent.putExtra("message", it.body);
                broadcaster?.sendBroadcast(intent);
            }
        }
    }

}