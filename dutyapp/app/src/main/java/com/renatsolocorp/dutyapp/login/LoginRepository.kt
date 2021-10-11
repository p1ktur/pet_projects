package com.renatsolocorp.dutyapp.login

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.auth.User
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassDatabase
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.eventdb.DutyEvent
import com.renatsolocorp.dutyapp.database.eventdb.EventDatabase
import com.renatsolocorp.dutyapp.database.eventdb.EventRepository
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.database.pairdb.PairDatabase
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.main.MainActivity
import com.renatsolocorp.dutyapp.profile.ProfilePreferences
import com.renatsolocorp.dutyapp.profile.globalProfilePreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

var loggingIn = false

class LoginRepository(val context: Context, val application: Application) {
    val profilePreferences = ProfilePreferences(context)

    private val classDao = ClassDatabase.get(application).getDao()
    private val pairDao = PairDatabase.get(application).getDao()
    private val eventDao = EventDatabase.get(application).getDao()

    fun updateUserDataAndFinish(usersSnapshot: DataSnapshot){
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val infoSnapshot = usersSnapshot.child(currentUser.uid).child(USER_INFO)

        profilePreferences.saveProfileDataOnSignIn(
            username = infoSnapshot.child(USERNAME).value.toString(),
            birthday = infoSnapshot.child(BIRTHDAY).value.toString(),
            grade = infoSnapshot.child(GRADE).value.toString(),
            bio = infoSnapshot.child(BIO).value.toString(),
            email = infoSnapshot.child(EMAIL).value.toString(),
            mobile = infoSnapshot.child(MOBILE).value.toString(),
            ownClasses = infoSnapshot.child(OWN_CLASSES).value.toString(),
            following = infoSnapshot.child(FOLLOWING).value.toString(),
            followers = infoSnapshot.child(FOLLOWERS).value.toString()
        )

        val preferences = ProfilePreferences(context)
        if (preferences.customGetData(APP_THEME) == "") preferences.customSaveData("1", APP_THEME)
        if (preferences.customGetData(LANGUAGE) == "") preferences.customSaveData("en", LANGUAGE)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(context, intent, null)
    }

    fun clearAllClassesEntirely(){
        GlobalScope.launch {
            classDao.clearAllClasses()
            pairDao.clearAllPairs()
            eventDao.clearAllEvents()
        }
    }
}