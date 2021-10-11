package com.renatsolocorp.dutyapp.main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.classes.editableclass.*
import com.renatsolocorp.dutyapp.classes.eventdetails.*
import com.renatsolocorp.dutyapp.classes.myclasses.MyClassesFragment
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.eventdb.*
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.profile.globalProfilePreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

fun addPast(context: Context){
    val index = getIndexOfPair(selectedPair)
    pairsList[index].dutiesAmount++
    editableViewModel.updateSinglePair(pairsList, index, context)
}

fun removePast(context: Context){
    val index = getIndexOfPair(selectedPair)
    if (pairsList[getIndexOfPair(selectedPair)].dutiesAmount > 0) pairsList[getIndexOfPair(selectedPair)].dutiesAmount--
    editableViewModel.updateSinglePair(pairsList, index, context)
}

fun addDebt(context: Context){
    val index = getIndexOfPair(selectedPair)
    pairsList[getIndexOfPair(selectedPair)].debts++
    editableViewModel.updateSinglePair(pairsList, index, context)
}

fun removeDebt(context: Context){
    val index = getIndexOfPair(selectedPair)
    if (pairsList[getIndexOfPair(selectedPair)].debts > 0) pairsList[getIndexOfPair(selectedPair)].debts--
    editableViewModel.updateSinglePair(pairsList, index, context)
}

fun addPair(application: Application, context: Context){
    selectedPosition = null

    editableViewModel.addPair(application, context)
}

fun removePair(context: Context) {
    selectedPosition = null

    editableViewModel.deletePair(context)
}

fun skipPair(application: Application, context: Context){
    pairsList[getIndexOfPair(selectedPair)].debts++
    val skippedPairIndex = getIndexOfPair(selectedPair)

    if (getIndexOfPair(selectedPair) == pairsList.size-1){
        selectedPair = pairsList[0]
    }else{
        selectedPair = pairsList[getIndexOfPair(selectedPair) +1]
    }

    if (skippedPairIndex == currentPair.id) {
        currentPair = selectedPair
        checkForCurrentPair(FirebaseAuth.getInstance().currentUser!!.uid, application, context)
    }

    EventDetailRepository(application, context).addEvent(skippedPairIndex, "$SKIPPED_EVENT_NAME(+1 debt)")

    selectedPosition = null

    editableViewModel.updateList(pairsList, context)
}

fun setCurrentPair(application: Application, context: Context){
    if (selectedPair.id != currentPair.id){
        selectedPosition = null

        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        if (tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
            tempCalendar.add(Calendar.DAY_OF_MONTH, -1)
        }else if(tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            tempCalendar.add(Calendar.DAY_OF_MONTH, -2)
        }
        val timeInMillis = getTimeInMillis(tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH)).toLong()

        pairsList[getIndexOfPair(selectedPair)].dutyTime = timeInMillis
        selectedPair.dutyTime = timeInMillis

        currentPair = pairsList[getIndexOfPair(selectedPair)]

        EventDetailRepository(application, context).addEvent(getIndexOfPair(selectedPair), SET_ON_DUTY_EVENT_NAME, formatDate(currentPair.dutyTime.toString()))

        checkForCurrentPair(FirebaseAuth.getInstance().currentUser!!.uid, application, context)

        editableViewModel.updateList(pairsList, context)
    }
}

fun saveChanges(loadingScreen: ConstraintLayout, fragmentManager: FragmentManager, application: Application, activity: Activity?){
    val currentUser = FirebaseAuth.getInstance().currentUser!!
    val internalRepository = ClassRepository(application)
    val eventRepository = EventRepository(application)

    loadingScreen.visibility = View.VISIBLE

    pairsList.forEach {
        if (it.name.isEmpty()) it.name = application.applicationContext.getString(R.string.empty)
        PairRepository(application).updatePair(it)
    }

    val klass = DutyClass(
        id = selectedClass.id,
        name = classNameText.text.toString(),
        dutyAmount = pairsList.size.toString(),
        creatorId = currentUser.uid,
        creatorName = currentUser.displayName!!,
        grade = yourGradeText.text.toString(),
        gradeShow = globalProfilePreferences.customGetBooleanData(GRADE_SHOW),
        show = showClassCheckbox.isChecked,
        isPinnedByCurrentUser = selectedClass.isPinnedByCurrentUser,
        createdTime = getCurrentTime().toLong()
    )

    internalRepository.addClass(klass)

    db.getReference(USERS).get().addOnCompleteListener { utask ->
        if (utask.isSuccessful && utask.result != null) {
            val result = utask.result!!

            val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(klass.id)
            classSnapshot.ref.removeValue()

            classSnapshot.child(CLASS_INFO).child(CLASS_NAME).ref.setValue(klass.name)
            classSnapshot.child(CLASS_INFO).child(CLASS_ID).ref.setValue(klass.id)
            classSnapshot.child(CLASS_INFO).child(CLASS_DUTY_AMOUNT).ref.setValue(klass.dutyAmount)
            classSnapshot.child(CLASS_INFO).child(CLASS_CREATOR_ID).ref.setValue(klass.creatorId)
            classSnapshot.child(CLASS_INFO).child(CLASS_CREATOR_NAME).ref.setValue(klass.creatorName)
            classSnapshot.child(CLASS_INFO).child(CLASS_GRADE).ref.setValue(klass.grade)
            classSnapshot.child(CLASS_INFO).child(CLASS_SHOW).ref.setValue(klass.show)
            classSnapshot.child(CLASS_INFO).child(CLASS_GRADE_SHOW).ref.setValue(klass.gradeShow)
            classSnapshot.child(CLASS_INFO).child(CLASS_CREATED_TIME).ref.setValue(klass.createdTime.toString())

            pairsList.forEach {
                classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(FULLNAME).ref.setValue(it.name)
                classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DEBTS).ref.setValue(it.debts)
                classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DUTIES_AMOUNT).ref.setValue(it.dutiesAmount)
                classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(IS_CURRENT).ref.setValue(it.isCurrent)
                classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DUTY_TIME).ref.setValue(it.dutyTime)

                GlobalScope.launch {
                    eventRepository.eventDao.getPairEventsAsList(it.id, it.classId).forEach{ event ->
                        classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(EVENT_LIST).child(event.id.toString()).child(EVENT_NAME).ref.setValue(event.event)
                        classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(EVENT_LIST).child(event.id.toString()).child(EVENT_TIME).ref.setValue(event.date)
                    }
                }
            }

            classSnapshot.child(DUTY_LIST).children.forEach {
                val value = it.key.toString().toInt()
                if (!pairsList.map{ pair -> pair.id }.contains(value)) it.ref.removeValue()
            }
        } else {
            showConnectionProblem(activity?.baseContext!!)
        }

        selectedPair = DutyPair(application.applicationContext.getString(R.string.empty), id = 0, isCurrent = true, dutyTime = 0L, classId = selectedClass.id)
        creatingNewClass = false
        editing = false
        mainBackButton.visibility = View.GONE
        mainDrawerButton.visibility = View.VISIBLE
        classMenuButton.visibility = View.GONE
        profileSettingsButton.visibility = View.GONE
        createNewClassButton.visibility = View.GONE
        imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        currentFragment = MY_CLASSES_FRAGMENT
        fragmentManager.beginTransaction().replace(R.id.main_fragment_container, MyClassesFragment(application)).commit()
    }
}

fun changeShowOptions(isChecked: Boolean, context: Context, application: Application){
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    selectedClass.show = isChecked
    ClassRepository(application).updateClass(selectedClass)

    db.getReference(USERS).get().addOnCompleteListener { utask ->
        if (utask.isSuccessful && utask.result != null) {
            val result = utask.result!!
            val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(selectedClass.id)
            if (isChecked){
                classSnapshot.child(CLASS_INFO).child(CLASS_SHOW).ref.setValue("true")
            } else {
                classSnapshot.child(CLASS_INFO).child(CLASS_SHOW).ref.setValue("false")
            }
        } else {
            Toast.makeText(context, context.getString(R.string.change_show_options_failed), Toast.LENGTH_SHORT).show()
        }
    }
}

