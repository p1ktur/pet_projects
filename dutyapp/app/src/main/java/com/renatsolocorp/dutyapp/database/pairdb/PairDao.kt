package com.renatsolocorp.dutyapp.database.pairdb

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PairDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPair(pair: DutyPair)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPairs(pairs: MutableList<DutyPair>)

    @Update
    fun updatePair(pair: DutyPair)

    @Query("SELECT * FROM DutyPair WHERE classId = :classId")
    fun getAllPairs(classId: String): LiveData<MutableList<DutyPair>>

    @Query("SELECT * FROM DutyPair WHERE classId = :classId")
    fun getAllPairsAsList(classId: String): MutableList<DutyPair>

    @Query("SELECT * FROM DutyPair ORDER BY uid DESC LIMIT 1")
    fun getLastUidPair(): MutableList<DutyPair>

    @Delete
    fun deletePair(pair: DutyPair)

    @Query("DELETE FROM DutyPair WHERE classId = :classId")
    fun deleteClassPairs(classId: String)

    @Query("DELETE FROM DutyPair")
    fun clearAllPairs()
}