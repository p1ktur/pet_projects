package com.renatsolocorp.dutyapp.database.pairdb

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DutyPair::class], version = 1)
abstract class PairDatabase: RoomDatabase(){
    companion object {
        fun get(application: Application): PairDatabase {
            return Room.databaseBuilder(application, PairDatabase::class.java, "PairDatabase").build()
        }
    }

    abstract fun getDao(): PairDao
}