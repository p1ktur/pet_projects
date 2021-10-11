package com.renatsolocorp.dutyapp.classes.editableclass

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
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.classes.eventdetails.EventDetailRepository
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.eventdb.ADDED_EVENT_NAME
import com.renatsolocorp.dutyapp.database.eventdb.EventRepository
import com.renatsolocorp.dutyapp.database.eventdb.WAS_DUTY_EVENT_NAME
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.selectedClass
import com.renatsolocorp.dutyapp.profile.globalProfilePreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EditableClassViewModel : ViewModel() {
    lateinit var localPairsList: LiveData<MutableList<DutyPair>>
    lateinit var pairsList: MutableLiveData<MutableList<DutyPair>>
    var pairs: MutableList<DutyPair> = mutableListOf()
    private val repository = EditableRepository()
    lateinit var internalRepository: PairRepository
    lateinit var classRepository: ClassRepository
    lateinit var eventRepository: EventRepository

    val currentUser = FirebaseAuth.getInstance().currentUser!!

    fun init(loadingScreen: ConstraintLayout, application: Application, context: Context) {
        internalRepository = PairRepository(application)
        classRepository = ClassRepository(application)
        eventRepository = EventRepository(application)
        localPairsList = internalRepository.pairs
        pairsList = MutableLiveData()
        loadingScreen.visibility = View.VISIBLE

        if (!creatingNewClass || selectedClass.id != "") {
            pairsList.value = if (localPairsList.value != null) localPairsList.value!!.sortedBy{ it.id }.toMutableList() else localPairsList.value

            val klass = selectedClass
            classNameText.setText(klass.name)
            onDutyText.text = klass.dutyAmount
            yourNameText.text = klass.creatorName
            showClassCheckbox.isChecked = klass.show

            if (klass.grade == "") {
                editGradeField.visibility = View.GONE
                yourGradeText.visibility = View.GONE
            } else {
                yourGradeText.text = klass.grade
            }
            editGradeField.visibility = View.GONE
            yourGradeText.visibility = View.GONE

            updateDataInTheInternet(context)

            loadingScreen.visibility = View.GONE
        } else if (selectedClass.id == ""){
            selectedClass.id = generateClassUid()

            val emptyPair = DutyPair(context.getString(R.string.empty), id = 0, isCurrent = true, dutyTime = 0L, classId = selectedClass.id)

            pairs.add(emptyPair)
            internalRepository.addEmptyPair(pairs)

            val className = context.getString(R.string.new_class_name)
            val dutyAmount = pairs.size.toString()
            val creatorName = "${currentUser.displayName}"
            val gradeText = globalProfilePreferences.customGetData(GRADE)

            selectedClass.name = className
            selectedClass.dutyAmount = dutyAmount
            selectedClass.creatorName = creatorName
            selectedClass.grade = gradeText
            selectedClass.createdTime = getCurrentTime().toLong()

            classNameText.setText(className)
            onDutyText.text = dutyAmount
            yourNameText.text = creatorName
            yourGradeText.text = gradeText

            if (gradeText == "") {
                editGradeField.visibility = View.GONE
                yourGradeText.visibility = View.GONE
            } else {
                yourGradeText.text = gradeText
            }
            editGradeField.visibility = View.GONE
            yourGradeText.visibility = View.GONE

            globalProfilePreferences.customSaveData((globalProfilePreferences.customGetData(OWN_CLASSES).toInt() + 1).toString(), OWN_CLASSES)

            pairsList.value = pairs

            db.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null) {
                    val result = utask.result!!

                    val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(selectedClass.id)

                    classSnapshot.child(CLASS_INFO).child(CLASS_NAME).ref.setValue(className)
                    classSnapshot.child(CLASS_INFO).child(CLASS_ID).ref.setValue(selectedClass.id)
                    classSnapshot.child(CLASS_INFO).child(CLASS_DUTY_AMOUNT).ref.setValue(dutyAmount)
                    classSnapshot.child(CLASS_INFO).child(CLASS_CREATOR_ID).ref.setValue(currentUser.uid)
                    classSnapshot.child(CLASS_INFO).child(CLASS_CREATOR_NAME).ref.setValue(creatorName)
                    classSnapshot.child(CLASS_INFO).child(CLASS_GRADE).ref.setValue(gradeText)
                    classSnapshot.child(CLASS_INFO).child(CLASS_CREATED_TIME).ref.setValue(selectedClass.createdTime.toString())
                    classSnapshot.child(CLASS_INFO).child(CLASS_SHOW).ref.setValue("true")

                    pairs.forEach {
                        classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(FULLNAME).ref.setValue(it.name)
                        classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DEBTS).ref.setValue(it.debts)
                        classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DUTIES_AMOUNT).ref.setValue(it.dutiesAmount)
                        classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(IS_CURRENT).ref.setValue(it.isCurrent)
                        classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DUTY_TIME).ref.setValue(it.dutyTime)
                    }

                    val ownClassesPath = result.child(currentUser.uid).child(USER_INFO).child(OWN_CLASSES)
                    ownClassesPath.ref.setValue((ownClassesPath.value.toString().toInt() + 1).toString())
                }
            }

            loadingScreen.visibility = View.GONE
        }
    }

    fun addPair(application: Application, context: Context){
        addingPair = true
        val toAddName = "${firstNames.random()} ${lastNames.random()} \"${nickNames.random()}\""
        val size = if (pairsList.value == null) 0 else pairsList.value!!.size
        val newPair = DutyPair(toAddName, id = size, isCurrent = false, dutyTime = 0L, classId = selectedClass.id)

        if (creatingNewClass && pairsList.value!!.size == 1 && pairsList.value!![0].name == context.getString(R.string.empty)) {
            internalRepository.deletePair(pairsList.value!![0], null, newPair.apply { isCurrent = true })
            pairsList.value!!.clear()
        } else {
            internalRepository.addPair(newPair)
        }

        pairsList.value!!.add(newPair)

        selectedPair = pairsList.value!!.last()

        pairsList.value = pairsList.value

        repository.updateData()
        EventDetailRepository(application, context).addEvent(pairsList.value!!.size-1, "${ADDED_EVENT_NAME}as $toAddName")
        onDutyText.text = pairsList.value!!.size.toString()
        selectedClass.dutyAmount = pairsList.value!!.size.toString()
        classRepository.updateClass(selectedClass)
    }

    fun deletePair(context: Context){
        deletingPair = true
        val oldIndex = getIndexOfPair(selectedPair)
        var newIndex = oldIndex

        internalRepository.deletePair(pairsList.value!![oldIndex], oldIndex)
        pairsList.value!!.removeAt(oldIndex)

        when (oldIndex){
            0 -> newIndex = 0
            else -> newIndex--
        }

        if (pairsList.value!!.size == 0){
            pairsList.value!!.add(DutyPair(context.getString(R.string.empty), id = 0, isCurrent = true, dutyTime = 0L, classId = selectedClass.id))
        }
        resetIds(pairsList.value!!)
        selectedPair = pairsList.value!![newIndex]

        //pairsList.value = pairsList.value

        repository.updateData()
        onDutyText.text = pairsList.value!!.size.toString()
        selectedClass.dutyAmount = pairsList.value!!.size.toString()
        classRepository.updateClass(selectedClass)
        deletingPair = false
    }

    fun resetIds(){
        val pairs = pairsList.value
        if (pairsList.value != null) for (i in 0 until pairsList.value!!.size){
            pairs!![i].id = i
        }
        pairsList.value = pairs
    }

    fun updateList(pairs: MutableList<DutyPair>, context: Context){
        pairsList.value = pairs
        internalRepository.updateAllPairs(pairs)
        selectedClass.apply {
            name = classNameText.text.toString()
            dutyAmount = onDutyText.text.toString()
            grade = yourGradeText.text.toString()
        }
        onDutyText.text = pairs.size.toString()
        classRepository.updateClass(selectedClass)
        repository.updateData()
    }

    fun updateSinglePair(pairs: MutableList<DutyPair>, id: Int, context: Context){
        pairsList.value!![id] = pairs[id]
        pairsList.value = pairsList.value
        internalRepository.updatePair(pairs[id])
        repository.updateSinglePair(id, context)
    }

    fun revertChanges(selectedClass: String, context: Context) {
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!
                val value = (result.child(currentUser.uid).child(USER_INFO).child(OWN_CLASSES).value.toString().toInt() - 1).toString()
                result.child(currentUser.uid).child(CLASSES).child(selectedClass).ref.removeValue()
                result.child(currentUser.uid).child(USER_INFO).child(OWN_CLASSES).ref.setValue(value)
                GlobalScope.launch {
                    globalProfilePreferences.customSaveData(classRepository.classDao.getAllClassesAsList(currentUser.uid).size.toString(), OWN_CLASSES)
                }
            }
        }
    }

    private fun updateDataInTheInternet(context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                val pairs = pairsList.value ?: mutableListOf()
                val updatedPairsIndexes = mutableListOf<Int>()
                val pairPath = result.child(currentUser.uid).child(CLASSES).child(selectedClass.id).child(DUTY_LIST)
                pairPath.children.forEach {
                    if (pairs.map{ pair -> pair.id.toString() }.contains(it.key.toString())){
                        val pair = pairs[pairs.map{ elem -> elem.id.toString() }.indexOf(it.key.toString())]
                        updatedPairsIndexes.add(pairs.indexOf(pair))
                        it.child(DEBTS).ref.setValue(pair.debts.toString())
                        it.child(DUTIES_AMOUNT).ref.setValue(pair.dutiesAmount.toString())
                        it.child(DUTY_TIME).ref.setValue(pair.dutyTime.toString())
                        it.child(FULLNAME).ref.setValue(pair.name)
                        it.child(IS_CURRENT).ref.setValue(pair.isCurrent.toString())
                        internalRepository.updatePairEventsInTheInternet(pair, context)
                    } else {
                        it.ref.removeValue()
                    }
                }

                if (updatedPairsIndexes.size != pairs.size){
                    pairs.forEach { pair ->
                        if (!updatedPairsIndexes.contains(pairs.indexOf(pair))){
                            val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(pair.classId)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(FULLNAME).ref.setValue(pair.name)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(DEBTS).ref.setValue(pair.debts)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(DUTIES_AMOUNT).ref.setValue(pair.dutiesAmount)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(IS_CURRENT).ref.setValue(pair.isCurrent)
                            classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(DUTY_TIME).ref.setValue(pair.dutyTime)

                            GlobalScope.launch {
                                eventRepository.eventDao.getPairEventsAsList(pair.id, pair.classId).forEach{ event ->
                                    classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(EVENT_LIST).child(event.id.toString()).child(EVENT_NAME).ref.setValue(event.event)
                                    classSnapshot.child(DUTY_LIST).child(pair.id.toString()).child(EVENT_LIST).child(event.id.toString()).child(EVENT_TIME).ref.setValue(event.date)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDataFromTheInternet(){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                val pairs = pairsList.value ?: mutableListOf()

                val pairPath = result.child(currentUser.uid).child(CLASSES).child(selectedClass.id).child(DUTY_LIST)
                pairPath.children.forEach {
                    pairs.add(DutyPair(
                        id = it.key.toString().toInt(),
                        classId = selectedClass.id,
                        debts = it.child(DEBTS).value.toString().toInt(),
                        dutiesAmount = it.child(DUTIES_AMOUNT).value.toString().toInt(),
                        dutyTime = it.child(DUTY_TIME).value.toString().toLong(),
                        name = it.child(FULLNAME).value.toString(),
                        isCurrent = it.child(IS_CURRENT).value.toString().toBoolean()
                    ))
                }
            }
        }
    }
}