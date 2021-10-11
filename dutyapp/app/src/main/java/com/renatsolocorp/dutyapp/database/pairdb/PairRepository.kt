package com.renatsolocorp.dutyapp.database.pairdb

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.MutableData
import com.renatsolocorp.dutyapp.classes.editableclass.*
import com.renatsolocorp.dutyapp.classes.eventdetails.EventDetailRepository
import com.renatsolocorp.dutyapp.classes.viewedclass.viewedViewModel
import com.renatsolocorp.dutyapp.database.EVENT_LIST
import com.renatsolocorp.dutyapp.database.eventdb.EventDatabase
import com.renatsolocorp.dutyapp.database.eventdb.EventRepository
import com.renatsolocorp.dutyapp.database.eventdb.WAS_DUTY_EVENT_NAME
import com.renatsolocorp.dutyapp.extensions.getCurrentDate
import com.renatsolocorp.dutyapp.extensions.getIndexOfPair
import com.renatsolocorp.dutyapp.extensions.unFormatDate
import com.renatsolocorp.dutyapp.main.selectedClass
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class PairRepository(val application: Application) {
    val pairDao = PairDatabase.get(application).getDao()
    val eventDao = EventDatabase.get(application).getDao()
    val pairs = pairDao.getAllPairs(selectedClass.id)

    fun addEmptyPair(pairs: MutableList<DutyPair>){
        GlobalScope.launch {
            val uid = if (pairDao.getLastUidPair().size != 0) pairDao.getLastUidPair()[0].uid + 1 else 1
            pairs[0].uid = uid
            pairDao.addPair(pairs[0])
        }
    }

    fun addPair(pair: DutyPair){
        GlobalScope.launch {
            pairDao.addPair(pair)
        }
    }

    fun addAllPairs(pairs: MutableList<DutyPair>){
        GlobalScope.launch {
            pairDao.addPairs(pairs)
        }
    }

    fun updatePair(pair: DutyPair){
        GlobalScope.launch {
            pairDao.updatePair(pair)
        }
    }

    fun updatePairEventsInTheInternet(pair: DutyPair, context: Context){
        GlobalScope.launch {
            val events = eventDao.getPairEventsAsList(pair.id, pair.classId)
            EventDetailRepository(application, context).updateData(events)
        }
    }

    fun updateAllPairs(pairs: MutableList<DutyPair>){
        GlobalScope.launch {
            pairs.forEach {
                pairDao.updatePair(it)
            }
        }
    }

    fun deletePair(pair: DutyPair, index: Int? = null, newPair: DutyPair? = null){
        GlobalScope.launch {
            eventDao.getPairEventsAsList(pair.id, selectedClass.id).forEach {
                eventDao.deleteSinglePairEvent(it)
            }
            //can optimize if i can
            val condition1 = pair.id == currentPair.id
            val tPairs = pairDao.getAllPairsAsList(selectedClass.id)
            val dIndex = getIndexOfPair(tPairs, pair)
            pairDao.deletePair(tPairs[dIndex])
            tPairs.removeAt(dIndex)
            if (tPairs.size == 0){
                if (newPair == null) {
                    tPairs.add(DutyPair("empty", id = 0, isCurrent = true, dutyTime = 0L, classId = selectedClass.id))
                } else {
                    tPairs.add(newPair)
                    initFinished = false
                }
            }

            if (index != null && tPairs.size != 0 && index < tPairs.size){
                selectedPair = tPairs[index]
                if (!tPairs.map { it.isCurrent }.contains(true)){
                    currentPair = if (condition1) {
                        selectedPair.isCurrent = true
                        tPairs[index].isCurrent = true
                        selectedPair
                    } else currentPair.apply { id-- }
                    currentPair.isCurrent = true
                    if (currentPair.dutyTime == 0L) currentPair.dutyTime = unFormatDate(getCurrentDate(WAS_DUTY_EVENT_NAME, true)).toLong()
                    if (selectedPair.isCurrent && selectedPair.dutyTime == 0L) selectedPair.dutyTime = unFormatDate(getCurrentDate(WAS_DUTY_EVENT_NAME, true)).toLong()
                    if (tPairs[index].isCurrent && tPairs[index].dutyTime == 0L) tPairs[index].dutyTime = unFormatDate(getCurrentDate(WAS_DUTY_EVENT_NAME, true)).toLong()
                }
            }

            pairDao.getAllPairsAsList(selectedClass.id).forEach {
                pairDao.deletePair(it)
            }

            if (tPairs.size != 0) for (i in 0 until tPairs.size){
                eventDao.getPairEventsAsList(tPairs[i].id, selectedClass.id).forEach {
                    val newEvent = it.apply { pairId = i }
                    eventDao.updateSinglePairEvent(newEvent)
                }
                val toAdd = tPairs[i].apply { id = i }
                pairDao.addPair(toAdd)
            }
        }
    }

    fun deleteClassPairs(classId: String){
        GlobalScope.launch {
            pairDao.getAllPairsAsList(classId).forEach { pair ->
                eventDao.getPairEventsAsList(pair.id, classId).forEach{ event ->
                    eventDao.deleteSinglePairEvent(event)
                }
                pairDao.deletePair(pair)
            }
            if (selectedClass.id != "") pairDao.addPair(DutyPair("empty", id = 0, isCurrent = true, dutyTime = 0L, classId = selectedClass.id))
        }
    }

    fun getViewedPairs(classId: String){
        GlobalScope.launch {
            val pairs = pairDao.getAllPairs(classId)
            viewedViewModel.pairsListData = pairs
        }
    }
}