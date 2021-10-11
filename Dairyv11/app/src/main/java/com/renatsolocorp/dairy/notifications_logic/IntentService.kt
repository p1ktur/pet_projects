package com.renatsolocorp.dairy.notifications_logic

import android.app.IntentService
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import com.renatsolocorp.dairy.*

const val notificationServiceName = "NotificationIntentService"
const val unSortedUrgentPosition = "UnSortedUrgentPosition"

class NotificationIntentService: IntentService(notificationServiceName){
    override fun onHandleIntent(intent: Intent?) {
        if (sortNotifications().size != 0){
            for (i in sortNotifications().indices){
                if (sortNotifications()[i].split(separator)[1].toLong() != 0L){
                    Log.d("sd", "it = $i")
                    callNotifications(i)
                }
            }
        }
    }

    fun callNotifications(iterator: Int){
        notificationId++
        val sortedNotifications = sortNotifications()
        if (sortedNotifications[iterator].split(separator)[1].toLong() - System.currentTimeMillis() > 0){
            Thread.sleep(sortedNotifications[iterator].split(separator)[1].toLong() - System.currentTimeMillis())
        }
        Log.d("sd", "SERVICE STARTED")
        var contentTitle = "Homework"
        val displayedText = sortedNotifications[iterator].split(separator)[0].split(smallSeparator)[0] + " " + sortedNotifications[iterator].split(separator)[0].split(smallSeparator)[1]

        val intent = Intent(context, WeekActivity::class.java)
        for (i in dayOfWeekNames.indices){
            if (sortedNotifications[iterator].split(separator)[0].split(smallSeparator)[0].contains(dayOfWeekNames[i])){
                for (j in urgentHomeworkList.indices){
                    if (urgentHomeworkList[j] == sortedNotifications[iterator]){
                        intent.putExtra(unSortedUrgentPosition, j)
                    }
                }
                intent.putExtra(intentOpenDayId, i)
                intent.putExtra(numberAnimation, iterator.toString())
            }
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = Notification.Builder(context, channelId)
                .setContentTitle(contentTitle)
                .setContentText(displayedText)
                .setSmallIcon(R.drawable.dairy_icon_round)
                .setLargeIcon(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.dairy_icon))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            builder = Notification.Builder(context)
                .setContentTitle(contentTitle)
                .setContentText(displayedText)
                .setSmallIcon(R.drawable.dairy_icon_round)
                .setLargeIcon(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.dairy_icon))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }

        val receiverIntent = Intent("UpdateUrgents")
        sendBroadcast(receiverIntent)

        notificationManager.notify(notificationId, builder.build())
        for (i in urgentHomeworkList.indices){
            if (urgentHomeworkList[i] == sortedNotifications[iterator]){
                urgentHomeworkList[i] = emptyUrgentTime(sortedNotifications[iterator])
                break
            }
        }
        Log.d("sd", "NOTIFICATION DELIVERED ${sortedNotifications}")
    }
}
