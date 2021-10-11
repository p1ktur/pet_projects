package com.renatsolocorp.dutyapp.classes.editableclass

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.selectedClass

class EditableRepository {

    //all is done in background

    val currentUser = FirebaseAuth.getInstance().currentUser!!

    fun updateData() {
        val klass = selectedClass.copy()

        if (klass.id.isNotEmpty()) db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(klass.id)

                classSnapshot.child(CLASS_INFO).child(CLASS_NAME).ref.setValue(classNameText.text.toString())
                classSnapshot.child(CLASS_INFO).child(CLASS_ID).ref.setValue(klass.id)
                classSnapshot.child(CLASS_INFO).child(CLASS_DUTY_AMOUNT).ref.setValue(pairsList.size.toString())
                classSnapshot.child(CLASS_INFO).child(CLASS_CREATOR_ID).ref.setValue(currentUser.uid)
                classSnapshot.child(CLASS_INFO).child(CLASS_CREATOR_NAME).ref.setValue("${currentUser.displayName}")

                pairsList.forEach {
                    classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(FULLNAME).ref.setValue(it.name)
                    classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DEBTS).ref.setValue(it.debts)
                    classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DUTIES_AMOUNT).ref.setValue(it.dutiesAmount)
                    classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(IS_CURRENT).ref.setValue(it.isCurrent)
                    classSnapshot.child(DUTY_LIST).child(it.id.toString()).child(DUTY_TIME).ref.setValue(it.dutyTime)
                }

                classSnapshot.child(DUTY_LIST).children.forEach {
                    val value = it.key.toString().toInt()
                    if (!pairsList.map { elem -> elem.id }.contains(value)) it.ref.removeValue()
                }
            }
        }
    }

    fun updateSinglePair(id: Int, context: Context) {
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                val classSnapshot = result.child(currentUser.uid).child(CLASSES).child(selectedClass.id)
                classSnapshot.child(DUTY_LIST).child(id.toString()).child(FULLNAME).ref.setValue(pairsList[id].name)
                classSnapshot.child(DUTY_LIST).child(id.toString()).child(DEBTS).ref.setValue(pairsList[id].debts)
                classSnapshot.child(DUTY_LIST).child(id.toString()).child(DUTIES_AMOUNT).ref.setValue(pairsList[id].dutiesAmount)
                classSnapshot.child(DUTY_LIST).child(id.toString()).child(IS_CURRENT).ref.setValue(pairsList[id].isCurrent)
                classSnapshot.child(DUTY_LIST).child(id.toString()).child(DUTY_TIME).ref.setValue(pairsList[id].dutyTime)
            }
        }
    }
}