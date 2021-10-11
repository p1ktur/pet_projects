package com.renatsolocorp.dutyapp.users.followed

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.db
import com.renatsolocorp.dutyapp.main.mainRefreshLayout
import com.renatsolocorp.dutyapp.users.User

class FollowedViewModel: ViewModel() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    lateinit var followedUserData: MutableLiveData<MutableList<User>>
    var followedUsers = mutableListOf<User>()
    var followedUids = mutableListOf<String>()

    fun init(followedLoadingScreen: ConstraintLayout, emptyFollowedText: TextView, context: Context){
        followedUserData = MutableLiveData()

        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null) {
                val result = utask.result!!
                result.child(currentUser.uid).child(USER_INFO).child(FOLLOWING_LIST).children.forEach { ds ->
                    followedUids.add(ds.value.toString())
                    val name = result.child(ds.value.toString()).child(USER_INFO).child(USERNAME).value.toString()
                    val grade = result.child(ds.value.toString()).child(USER_INFO).child(GRADE).value.toString()
                    val gradeShow = result.child(ds.value.toString()).child(USER_INFO).child(GRADE_SHOW).value.toString().toBoolean()
                    val isFollowing = true
                    val imageLocation = result.child(ds.value.toString()).child(USER_INFO).child(PROFILE_IMAGE_LOCATION).value.toString()
                    followedUsers.add(User(
                            ds.value.toString(),
                            name,
                            grade,
                            gradeShow,
                            isFollowing,
                            imageLocation
                    ))
                    if (result.child(currentUser.uid).child(USER_INFO).child(FOLLOWING_LIST).childrenCount.toString().toInt() == followedUids.size){
                        followedUserData.value = followedUsers
                        followedLoadingScreen.visibility = View.GONE
                        emptyFollowedText.visibility = if (followedUserData.value!!.size == 0) View.VISIBLE else View.GONE
                    }
                }
            } else {
                followedLoadingScreen.visibility = View.GONE
                emptyFollowedText.visibility = View.VISIBLE
            }
            followedUserData.value = followedUsers
        }
    }

    fun updateData(context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                result.child(currentUser.uid).child(USER_INFO).child(FOLLOWING_LIST).children.forEach { ds ->
                    if (!followedUids.contains(ds.value.toString())){
                        followedUids.add(ds.value.toString())
                        val name = result.child(ds.value.toString()).child(USER_INFO).child(USERNAME).value.toString()
                        val grade = result.child(ds.value.toString()).child(USER_INFO).child(GRADE).value.toString()
                        val gradeShow = result.child(ds.value.toString()).child(USER_INFO).child(GRADE_SHOW).value.toString().toBoolean()
                        val isFollowing = true
                        val imageLocation = result.child(ds.value.toString()).child(USER_INFO).child(PROFILE_IMAGE_LOCATION).value.toString()
                        followedUsers.add(User(
                                ds.value.toString(),
                                name,
                                grade,
                                gradeShow,
                                isFollowing,
                                imageLocation
                        ))
                    }
                    if (result.child(currentUser.uid).child(USER_INFO).child(FOLLOWING_LIST).childrenCount.toString().toInt() == followedUids.size){
                        followedUserData.value = followedUsers
                    }
                }
            } else {
                showConnectionProblem(context)
            }
            mainRefreshLayout.isRefreshing = false
            followedUserData.value = followedUsers
        }
    }
}