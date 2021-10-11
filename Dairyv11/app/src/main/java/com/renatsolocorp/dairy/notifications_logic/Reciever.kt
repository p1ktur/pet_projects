package com.renatsolocorp.dairy.notifications_logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.view.forEach
import kotlinx.android.synthetic.main.urgent_homework_layout.view.*
import com.renatsolocorp.dairy.*

class Receiver() : BroadcastReceiver() {
    override fun onReceive(context: Context?, receiverIntent: Intent?) {
        var k = 0
        urgentHomework.forEach {
            it.notified_identifier.background = if (k < urgentHomeworkList.size && urgentHomeworkList[k].split(separator)[1].toLong() == 0L) identifierGreen else identifierRed
            k++
        }

        (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()
    }
}