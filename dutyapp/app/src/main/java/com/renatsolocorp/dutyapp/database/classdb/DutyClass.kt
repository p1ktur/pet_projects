package com.renatsolocorp.dutyapp.database.classdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class DutyClass(
    var name: String,
    var dutyAmount: String,
    var creatorName: String,
    @PrimaryKey var id: String,
    var creatorId: String,
    var grade: String,
    var gradeShow: Boolean = true,
    var show: Boolean = true,
    var isPinnedByCurrentUser: Boolean = false,
    var pinnedTime: Long = 0L,
    var createdTime: Long = 0L
) {
    override fun toString(): String {
        return "name: $name; id: $id; creatorName: $creatorName; creatorId: $creatorId; isPBCU: $isPinnedByCurrentUser; show: $show; pinnedTime: $pinnedTime; createdTime: $createdTime "
    }

    fun clear(){
        this.name = ""
        this.dutyAmount = ""
        this.creatorName = ""
        this.creatorId = ""
        this.id = ""
        this.grade = ""
        this.gradeShow = true
        this.show = true
        this.isPinnedByCurrentUser = false
    }

    fun copy(): DutyClass{
        return DutyClass(
            name = name,
            dutyAmount = dutyAmount,
            creatorName = creatorName,
            id = id,
            creatorId = creatorId,
            grade = grade,
            gradeShow = gradeShow,
            show = show,
            isPinnedByCurrentUser = isPinnedByCurrentUser,
            pinnedTime = pinnedTime,
            createdTime = createdTime
        )
    }
}