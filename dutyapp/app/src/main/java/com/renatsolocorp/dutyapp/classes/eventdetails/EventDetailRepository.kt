package com.renatsolocorp.dutyapp.classes.eventdetails

import android.app.Application
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.classes.editableclass.memorisedPairId
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.eventdb.*
import com.renatsolocorp.dutyapp.extensions.getCurrentDate
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.selectedClass
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EventDetailRepository(application: Application, val context: Context) {
    val internalRepository = EventRepository(application)
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    fun updateData(events: MutableList<DutyEvent>){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!
                events.forEach { event ->
                    val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(event.classId)
                    classSnapshot.child(DUTY_LIST).child(memorisedPairId.toString()).child(EVENT_LIST).children.forEach {
                        if (events.map{elem -> elem.id}.contains(it.key.toString().toInt())){
                            it.ref.child(EVENT_NAME).setValue(events[events.map{elem -> elem.id}.indexOf(it.key.toString().toInt())].event)
                            it.ref.child(EVENT_TIME).setValue(events[events.map{elem -> elem.id}.indexOf(it.key.toString().toInt())].date)
                        } else {
                            it.ref.removeValue()
                        }
                    }
                }
            }
        }
    }

    fun addEvent(index: Int, eventName: String, date: String = ""){
        GlobalScope.launch {
            if (eventName == SET_ON_DUTY_EVENT_NAME && date != ""){
                val setEvent = DutyEvent(id = 0, event = eventName, date = getCurrentDate(eventName), index, selectedClass.id)
                setEvent.id = internalRepository.eventDao.getPairEventsAsList(setEvent.pairId, setEvent.classId).size
                internalRepository.addEvent(DutyEvent(setEvent.id, setEvent.event, setEvent.date, setEvent.pairId, setEvent.classId))

                db.getReference(USERS).get().addOnCompleteListener { utask ->
                    if (utask.isSuccessful && utask.result != null){
                        val result = utask.result!!
                        val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(setEvent.classId)
                        val listSnapshot = classSnapshot.child(DUTY_LIST).child(index.toString()).child(EVENT_LIST)
                        val newEventId = listSnapshot.childrenCount.toString()
                        listSnapshot.child(newEventId).child(EVENT_NAME).ref.setValue(setEvent.event)
                        listSnapshot.child(newEventId).child(EVENT_TIME).ref.setValue(setEvent.date)
                    }
                }
            } else {
                val newEvent = if (date == "") {
                    DutyEvent(id = 0, event = eventName, date = getCurrentDate(eventName), index, selectedClass.id)
                } else {
                    DutyEvent(id = 0, event = eventName, date = date, index, selectedClass.id)
                }

                newEvent.id = internalRepository.eventDao.getPairEventsAsList(index, selectedClass.id).size
                internalRepository.addEvent(DutyEvent(newEvent.id, newEvent.event, newEvent.date, newEvent.pairId, newEvent.classId))
                db.getReference(USERS).get().addOnCompleteListener { utask ->
                    if (utask.isSuccessful && utask.result != null){
                        val result = utask.result!!
                        val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(newEvent.classId)
                        val listSnapshot = classSnapshot.child(DUTY_LIST).child(index.toString()).child(EVENT_LIST)
                        val newEventId = listSnapshot.childrenCount.toString()
                        listSnapshot.child(newEventId).child(EVENT_NAME).ref.setValue(newEvent.event)
                        listSnapshot.child(newEventId).child(EVENT_TIME).ref.setValue(newEvent.date)
                    }
                }
            }
        }

    }

    fun clearEvents(index: Int){
        internalRepository.deletePairEvents(index, selectedClass.id)

        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(selectedClass.id)
                classSnapshot.child(DUTY_LIST).child(index.toString()).child(EVENT_LIST).ref.removeValue()
            }
        }
    }
}