package com.renatsolocorp.dutyapp.database.eventdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.renatsolocorp.dutyapp.R

const val ADDED_EVENT_NAME = "Pair was added " //time _
const val SKIPPED_EVENT_NAME = "Pair was skipped " //time (+ 1 debt)
const val WAS_DUTY_EVENT_NAME = "Pair was on duty " //date (+ 1 past duty)
const val IS_DUTY_EVENT_NAME = "Pair is currently on duty " //nothing _
const val SET_ON_DUTY_EVENT_NAME = "Pair was set on duty " //time
const val PAIR_CHANGED_EVENT_NAME = "Pair's name was changed to " //time $name

@Entity
class DutyEvent(
        var id: Int,
        var event: String = "",
        var date: String = "",
        var pairId: Int,
        var classId: String,
        @PrimaryKey(autoGenerate = true) var uid: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        return if (other is DutyEvent){
            this.pairId == other.pairId && this.classId == other.classId && this.event == other.event && this.date == other.date
        } else {
            false
        }
    }

    override fun toString(): String {
        return "event id = $id | name = $event | time = $date;"
    }

    fun resetIds(list: MutableList<DutyEvent>){
        for (i in 0 until list.size){
            list[i].id = i
        }
    }
}