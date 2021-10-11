package com.renatsolocorp.dutyapp.database.classdb

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ClassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addClass(klass: DutyClass)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addClasses(classes: MutableList<DutyClass>)

    @Update
    fun updateClass(klass: DutyClass)

    @Query("SELECT * FROM DutyClass WHERE creatorId = :userUid")
    fun getAllClasses(userUid: String): LiveData<MutableList<DutyClass>>

    @Query("SELECT * FROM DutyClass WHERE creatorId = :userUid")
    fun getAllClassesAsList(userUid: String): MutableList<DutyClass>

    @Query("SELECT * FROM DutyClass WHERE id = :id")
    fun getClassesWithId(id: String): MutableList<DutyClass>

    @Query("SELECT * FROM DutyClass WHERE isPinnedByCurrentUser = :t")
    fun getPinnedClasses(t: Boolean = true): LiveData<MutableList<DutyClass>>

    @Query("SELECT * FROM DutyClass WHERE isPinnedByCurrentUser = :t")
    fun getPinnedClassesAsList(t: Boolean = true): MutableList<DutyClass>

    @Delete
    fun deleteClass(klass: DutyClass)

    @Query("DELETE FROM DutyClass WHERE id = :classId")
    fun deleteClassWithId(classId: String)

    @Query("DELETE FROM DutyClass")
    fun clearAllClasses()
}