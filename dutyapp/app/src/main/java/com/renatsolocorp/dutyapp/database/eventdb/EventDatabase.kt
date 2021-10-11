package com.renatsolocorp.dutyapp.database.eventdb

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DutyEvent::class], version = 1)
abstract class EventDatabase: RoomDatabase() {
    companion object {
        fun get(application: Application): EventDatabase{
            return Room.databaseBuilder(application, EventDatabase::class.java, "EventDatabase").build()
        }
    }

    abstract fun getDao(): EventDao
}