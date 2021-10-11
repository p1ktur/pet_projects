package com.renatsolocorp.dutyapp.database.classdb

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DutyClass::class], version = 1)
abstract class ClassDatabase: RoomDatabase() {
    companion object {
        fun get(application: Application): ClassDatabase {
            return Room.databaseBuilder(application, ClassDatabase::class.java, "ClassDatabase").build()
        }
    }

    abstract fun getDao(): ClassDao
}