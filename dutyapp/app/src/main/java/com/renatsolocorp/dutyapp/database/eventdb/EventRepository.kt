package com.renatsolocorp.dutyapp.database.eventdb

import android.app.Application
import com.renatsolocorp.dutyapp.classes.editableclass.memorisedPairId
import com.renatsolocorp.dutyapp.main.selectedClass
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EventRepository(application: Application) {
    val eventDao = EventDatabase.get(application).getDao()
    var events = eventDao.getPairEvents(memorisedPairId, selectedClass.id)

    fun addEvent(event: DutyEvent){
        GlobalScope.launch {
            eventDao.addEvent(event)
        }
    }

    fun deletePairEvents(pairId: Int, classId: String){
        GlobalScope.launch {
            eventDao.getPairEventsAsList(pairId, classId).forEach {
                eventDao.deleteSinglePairEvent(it)
            }
        }
    }

    fun deleteSingleEvent(event: DutyEvent){
        GlobalScope.launch {
            eventDao.deleteSinglePairEvent(event)
        }
    }

    fun updateSinglePairEvent(event: DutyEvent){
        GlobalScope.launch {
            eventDao.updateSinglePairEvent(event)
        }
    }
}