package com.renatsolocorp.dutyapp.profile

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.database.eventdb.DutyEvent
import com.renatsolocorp.dutyapp.database.eventdb.EventRepository
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.extensions.calculateBitmap
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.mainLoadingScreen

class ProfileRepository(val viewedUserId: String, val context: Context, val application: Application) {

    val currentUser = FirebaseAuth.getInstance().currentUser!!

    fun getUsersData(
        userUsernameText: TextView,
        userBirthdayText: TextView,
        userGradeText: TextView,
        userBioText: TextView,
        userEmailText: TextView,
        userMobileText: TextView,
        userOwnClassesText: TextView,
        userFollowingText: TextView,
        userFollowersText: TextView,
        userImage: ImageView
    ){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!
                val infoSnapshot = result.child(viewedUserId).child(USER_INFO)
                userUsernameText.text = infoSnapshot.child(USERNAME).value.toString()
                userBirthdayText.text = infoSnapshot.child(BIRTHDAY).value.toString()
                userGradeText.text = infoSnapshot.child(GRADE).value.toString()
                userBioText.text = infoSnapshot.child(BIO).value.toString()
                userEmailText.text = infoSnapshot.child(EMAIL).value.toString()
                userMobileText.text = infoSnapshot.child(MOBILE).value.toString()
                userOwnClassesText.text = infoSnapshot.child(OWN_CLASSES).value.toString()
                userFollowingText.text = infoSnapshot.child(FOLLOWING).value.toString()
                userFollowersText.text = infoSnapshot.child(FOLLOWERS).value.toString()

                userBirthdayText.visibility = if (infoSnapshot.child(BIRTHDAY_SHOW).value.toString().toBoolean()) View.VISIBLE else View.GONE
                userGradeText.visibility = if (infoSnapshot.child(GRADE_SHOW).value.toString().toBoolean()) View.VISIBLE else View.GONE
                userBioText.visibility = if (infoSnapshot.child(BIO_SHOW).value.toString().toBoolean()) View.VISIBLE else View.GONE
                userEmailText.visibility = if (infoSnapshot.child(EMAIL_SHOW).value.toString().toBoolean()) View.VISIBLE else View.GONE
                userMobileText.visibility = if (infoSnapshot.child(MOBILE_SHOW).value.toString().toBoolean()) View.VISIBLE else View.GONE

            } else {
                showConnectionProblem(context)
            }
        }
    }
}