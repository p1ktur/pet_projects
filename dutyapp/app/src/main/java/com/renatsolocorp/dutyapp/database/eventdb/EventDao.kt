package com.renatsolocorp.dutyapp.database.eventdb

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addEvent(event: DutyEvent)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addEvents(events: MutableList<DutyEvent>)

    @Query("SELECT * FROM DutyEvent WHERE pairId = :pairId AND classId = :classId")
    fun getPairEvents(pairId: Int, classId: String): LiveData<MutableList<DutyEvent>>

    @Query("SELECT * FROM DutyEvent WHERE pairId = :pairId AND classId = :classId")
    fun getPairEventsAsList(pairId: Int, classId: String): MutableList<DutyEvent>

    @Delete
    fun deleteSinglePairEvent(event: DutyEvent)

    @Update
    fun updateSinglePairEvent(event: DutyEvent)

    @Query("DELETE FROM DutyEvent")
    fun clearAllEvents()
}