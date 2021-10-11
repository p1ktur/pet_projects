package com.renatsolocorp.dutyapp.classes.myclasses

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.db

class MyClassesRepository {

    val currentUser = FirebaseAuth.getInstance().currentUser!!

    fun updateData(list: MutableList<DutyClass>, context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                result.child(currentUser.uid).child(CLASSES).children.forEach {
                    val index = list.map{ elem -> elem.id }.indexOf(it.key.toString())
                    if (index >= 0) {
                        val curClass = list[index]
                        it.child(CLASS_INFO).child(CLASS_NAME).ref.setValue(curClass.name)
                        it.child(CLASS_INFO).child(CLASS_DUTY_AMOUNT).ref.setValue(curClass.dutyAmount)
                        it.child(CLASS_INFO).child(CLASS_CREATOR_ID).ref.setValue(curClass.creatorId)
                        it.child(CLASS_INFO).child(CLASS_ID).ref.setValue(curClass.id)
                    } else {
                        it.ref.removeValue()
                    }
                }
            }
        }
    }

    fun removeClass(uid: String, context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                val pinnersList = mutableListOf<String>()
                result.child(currentUser.uid).child(CLASSES).child(uid).child(CLASS_INFO).child(CLASS_PINNED_LIST).children.forEach {
                    pinnersList.add(it.value.toString())
                }
                pinnersList.forEach {
                    result.child(it).child(USER_INFO).child(PINNED_CLASSES_LIST).child(uid).ref.removeValue()
                }
                result.child(currentUser.uid).child(CLASSES).child(uid).ref.removeValue()
            } else {
                showConnectionProblem(context)
            }
        }
    }
}