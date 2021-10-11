package com.renatsolocorp.dutyapp.classes.eventdetails

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.classes.editableclass.memorisedPairId
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.eventdb.DutyEvent
import com.renatsolocorp.dutyapp.database.eventdb.EventRepository
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.mainRefreshLayout
import com.renatsolocorp.dutyapp.main.selectedClass
import com.renatsolocorp.dutyapp.profile.appIsOffline

class EventDetailViewModel: ViewModel() {
    lateinit var localEventsList: LiveData<MutableList<DutyEvent>>
    lateinit var eventsList: MutableLiveData<MutableList<DutyEvent>>
    val events = mutableListOf<DutyEvent>()
    lateinit var repository: EventDetailRepository
    lateinit var internalRepository: EventRepository

    val currentUser = FirebaseAuth.getInstance().currentUser!!

    fun init(viewedUserId: String, loadingScreen: ConstraintLayout, pairId: Int, application: Application, context: Context, activity: Activity) {
        repository = EventDetailRepository(application, context)
        internalRepository = EventRepository(application)
        localEventsList = internalRepository.events
        eventsList = MutableLiveData()

        if (viewedUserId == currentUser.uid){
            eventsList.value = localEventsList.value

            if (eventsList.value != null) updateDataInTheInternet(pairId, eventsList.value!!)

            loadingScreen.visibility = View.GONE
        } else {
            db.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null){
                    val result = utask.result!!
                    val classSnapshot = result.child(viewedUserId).child(CLASSES)
                    classSnapshot.children.forEach { ds ->
                        val classUid = ds.child(CLASS_INFO).child(CLASS_ID).value.toString()
                        if (classUid == selectedClass.id) {
                            ds.child(DUTY_LIST).child(pairId.toString()).child(EVENT_LIST).children.forEach {
                                events.add(DutyEvent(
                                    it.key.toString().toInt(),
                                    it.child(EVENT_NAME).value.toString(),
                                    it.child(EVENT_TIME).value.toString(),
                                    pairId,
                                    selectedClass.id
                                ))
                            }
                            loadingScreen.visibility = View.GONE
                            eventsList.value = events
                        }
                    }
                    loadingScreen.visibility = View.GONE
                } else {
                    appIsOffline = true
                    activity.onBackPressed()
                }
            }
        }
    }

    fun deleteEvent(index: Int){
        internalRepository.deleteSingleEvent(eventsList.value!![index])
        eventsList.value!!.removeAt(index)
        for (id in 0 until eventsList.value!!.size){
            eventsList.value!![id].id = id
            internalRepository.updateSinglePairEvent(eventsList.value!![id])
        }
        eventsList.value = eventsList.value
        repository.updateData(eventsList.value!!)
    }

    fun clearList(){
        eventsList.value!!.clear()
        eventsList.value = eventsList.value
        internalRepository.deletePairEvents(memorisedPairId, selectedClass.id)
        repository.updateData(eventsList.value!!)
    }

    fun updateData(viewedUserId: String, id: Int, context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                val classSnapshot = result.child(viewedUserId).child(CLASSES)
                classSnapshot.children.forEach { cds ->
                    val classUid = cds.child(CLASS_INFO).child(CLASS_ID).value.toString()
                    if (classUid == selectedClass.id) {
                        cds.child(DUTY_LIST).child(id.toString()).child(EVENT_LIST).children.forEach { ds ->
                            if (!events.map{ it.id }.contains(ds.key.toString().toInt())) events.add(DutyEvent(
                                ds.key.toString().toInt(),
                                ds.child(EVENT_NAME).value.toString(),
                                ds.child(EVENT_TIME).value.toString(),
                                id,
                                selectedClass.id
                            ))
                        }
                        eventsList.value = events
                    }
                }
            } else {
                showConnectionProblem(context)
            }
            mainRefreshLayout.isRefreshing = false
        }
    }

    fun updateDataInTheInternet(pairId: Int, events: MutableList<DutyEvent>){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                val updatedEventsIndexes = mutableListOf<Int>()
                val eventPath = result.child(currentUser.uid).child(CLASSES).child(selectedClass.id).child(DUTY_LIST).child(pairId.toString()).child(EVENT_LIST)
                eventPath.children.forEach {
                    if (events.map{ event -> event.id.toString() }.contains(it.key.toString())){
                        val event = events[events.map{ elem -> elem.id.toString() }.indexOf(it.key.toString())]
                        updatedEventsIndexes.add(events.indexOf(event))
                        it.child(EVENT_NAME).ref.setValue(event.event)
                        it.child(EVENT_TIME).ref.setValue(event.date)
                    } else {
                        it.ref.removeValue()
                    }
                }

                if (updatedEventsIndexes.size != events.size){
                    events.forEach { event ->
                        if (!updatedEventsIndexes.contains(events.indexOf(event))){
                            val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(event.classId)

                            classSnapshot.child(DUTY_LIST).child(event.pairId.toString()).child(EVENT_LIST).child(event.id.toString()).child(EVENT_NAME).ref.setValue(event.event)
                            classSnapshot.child(DUTY_LIST).child(event.pairId.toString()).child(EVENT_LIST).child(event.id.toString()).child(EVENT_TIME).ref.setValue(event.date)
                        }
                    }
                }
            }
        }
    }
}