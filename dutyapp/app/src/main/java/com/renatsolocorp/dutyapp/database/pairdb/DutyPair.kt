package com.renatsolocorp.dutyapp.database.pairdb

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.renatsolocorp.dutyapp.extensions.getTimeInMillis
import java.util.*

@Entity
class DutyPair(var name: String,
               var debts: Int = 0,
               var id: Int,
               var isCurrent: Boolean = false,
               var dutyTime: Long = 0L,
               var dutiesAmount: Int = 0,
               var classId: String = "",
               @PrimaryKey(autoGenerate = true) var uid: Int = 0
) {

    override fun toString(): String {
        return "$name | $debts | $id | $isCurrent | $dutyTime | $dutiesAmount | $classId | $uid"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is DutyPair) {
            this.name == other.name && this.id == other.id && this.classId == other.classId
        } else {
            false
        }
    }

    fun clear(){
        this.name = "empty"
        this.debts = 0
        this.id = 0
        this.isCurrent = true
        this.dutyTime = 0L
    }
}