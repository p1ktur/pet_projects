package com.renatsolocorp.dutyapp.classes.myclasses

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
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.main.createNewClassButton
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.deletingClass
import com.renatsolocorp.dutyapp.main.selectedClass

class MyClassesViewModel: ViewModel() {
    lateinit var myClassesList: LiveData<MutableList<DutyClass>>
    lateinit var classesList: MutableLiveData<MutableList<DutyClass>>
    private val classes: MutableList<DutyClass> = mutableListOf()

    private val repository = MyClassesRepository()
    lateinit var internalRepository: ClassRepository

    val currentUser =  FirebaseAuth.getInstance().currentUser!!

    fun init(loadingScreen: ConstraintLayout, application: Application, context: Context){
        internalRepository = ClassRepository(application)
        myClassesList = internalRepository.myClasses
        classesList = MutableLiveData()
        classesList.value = myClassesList.value ?: mutableListOf()

        if (deletingClass && classesList.value!!.size == 0) deletingClass = false else if (deletingClass) {
            internalRepository.getAllClasses(currentUser.uid)
            deletingClass = false
        } else internalRepository.getAllClasses(currentUser.uid)

        loadingScreen.visibility = View.GONE
        createNewClassButton.visibility = View.VISIBLE
    }

    private fun removeClass(uid: String, application: Application, context: Context){
        internalRepository.deleteClassWithId(uid)
        PairRepository(application).deleteClassPairs(uid)
        if (classesList.value!!.size != 0) classesList.value!!.removeAt(classesList.value!!.map{ it.id }.indexOf(uid))
        classesList.value = classesList.value
        repository.removeClass(uid, context)
    }

    fun updateDataInTheInternet(){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                internalRepository.updateDataInTheInternet(result)
            }
        }
    }
}