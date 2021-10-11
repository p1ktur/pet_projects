package com.renatsolocorp.dutyapp.classes.viewedclass

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
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.mainRefreshLayout
import com.renatsolocorp.dutyapp.main.selectedClass
import com.renatsolocorp.dutyapp.profile.appIsOffline

class ViewedClassViewModel: ViewModel() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!
    lateinit var pairRepository: PairRepository

    lateinit var pairsList: MutableLiveData<MutableList<DutyPair>>
    lateinit var pairsListData: LiveData<MutableList<DutyPair>>
    var pairs = mutableListOf<DutyPair>()

    fun init(viewedUserId: String, inputClass: DutyClass, loadingScreen: ConstraintLayout, application: Application, activity: Activity){
        pairRepository = PairRepository(application)
        pairsListData = pairRepository.pairs
        pairsList = MutableLiveData()

        if (!appIsOffline){
            if (viewedUserId == currentUser.uid){
                pairRepository.getViewedPairs(inputClass.id)
                loadingScreen.visibility = View.GONE
            } else {
                db.getReference(USERS).get().addOnCompleteListener { utask ->
                    if (utask.isSuccessful && utask.result != null){
                        val result = utask.result!!
                        val path = result.child(inputClass.creatorId).child(CLASSES).child(inputClass.id).child(DUTY_LIST)
                        path.children.forEach {
                            pairs.add(DutyPair(
                                name = it.child(FULLNAME).value as String,
                                id = it.key.toString().toInt(),
                                debts = it.child(DEBTS).value.toString().toInt(),
                                dutiesAmount = it.child(DUTIES_AMOUNT).value.toString().toInt(),
                                dutyTime = it.child(DUTY_TIME).value.toString().toLong(),
                                isCurrent = it.child(IS_CURRENT).value.toString().toBoolean()
                            ))
                        }
                        pairsList.value = pairs
                        loadingScreen.visibility = View.GONE
                    } else {
                        appIsOffline = true
                        activity.onBackPressed()
                    }
                }
            }
        } else {
            activity.onBackPressed()
        }
    }

    fun updateData(viewedUserId: String, inputClass: String, context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                result.child(viewedUserId).child(CLASSES).children.forEach { ds ->
                    val classUid = ds.child(CLASS_INFO).child(CLASS_ID).value.toString()
                    if (classUid == inputClass) {
                        classNameText.text = ds.child(CLASS_INFO).child(CLASS_NAME).value.toString()
                        onDutyText.text =  ds.child(DUTY_LIST).childrenCount.toString()
                        creatorNameText.text = ds.child(CLASS_INFO).child(CLASS_CREATOR_NAME).value.toString()
                        classGradeText.text = ds.child(CLASS_INFO).child(CLASS_GRADE).value.toString()

                        pairs.clear()
                        ds.child(DUTY_LIST).children.forEach {
                            pairs.add(DutyPair(
                                name = it.child(FULLNAME).value as String,
                                id = it.key.toString().toInt(),
                                debts = it.child(DEBTS).value.toString().toInt(),
                                dutiesAmount = it.child(DUTIES_AMOUNT).value.toString().toInt(),
                                dutyTime = it.child(DUTY_TIME).value.toString().toLong(),
                                isCurrent = it.child(IS_CURRENT).value.toString().toBoolean()
                            ))
                        }
                        pairsList.value = pairs
                    }
                }
            } else {
                showConnectionProblem(context)
            }
            mainRefreshLayout.isRefreshing = false
        }
    }
}