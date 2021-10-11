package com.renatsolocorp.dutyapp.users.followers

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

class FollowersViewModel: ViewModel() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    lateinit var followersUserData: MutableLiveData<MutableList<User>>
    var followersUsers = mutableListOf<User>()
    var followersUids = mutableListOf<String>()

    fun init(followersLoadingScreen: ConstraintLayout, emptyFollowersText: TextView, context: Context){
        followersUserData = MutableLiveData()

        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                result.child(currentUser.uid).child(USER_INFO).child(FOLLOWERS_LIST).children.forEach { ds ->
                    followersUids.add(ds.value.toString())
                    val name = result.child(ds.value.toString()).child(USER_INFO).child(USERNAME).value.toString()
                    val grade = result.child(ds.value.toString()).child(USER_INFO).child(GRADE).value.toString()
                    val gradeShow = result.child(ds.value.toString()).child(USER_INFO).child(GRADE_SHOW).value.toString().toBoolean()
                    val isFollowing = result.child(ds.value.toString()).child(USER_INFO).child(FOLLOWERS_LIST).children.map{elem -> elem.key}.contains(currentUser.uid)
                    val imageLocation = result.child(ds.value.toString()).child(USER_INFO).child(PROFILE_IMAGE_LOCATION).value.toString()
                    followersUsers.add(User(
                        ds.value.toString(),
                        name,
                        grade,
                        gradeShow,
                        isFollowing,
                        imageLocation
                    ))
                    if (result.child(currentUser.uid).child(USER_INFO).child(FOLLOWERS_LIST).childrenCount.toString().toInt() == followersUids.size){
                        followersUserData.value = followersUsers
                        followersLoadingScreen.visibility = View.GONE
                        emptyFollowersText.visibility = if (followersUserData.value!!.size == 0) View.VISIBLE else View.GONE
                    }
                }
            } else {
                followersLoadingScreen.visibility = View.GONE
                emptyFollowersText.visibility = View.VISIBLE
            }
            followersUserData.value = followersUsers
        }
    }

    fun updateData(context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                result.child(currentUser.uid).child(USER_INFO).child(FOLLOWERS_LIST).children.forEach { ds ->
                    if (!followersUids.contains(ds.value.toString())){
                        followersUids.add(ds.value.toString())
                        val name = result.child(ds.value.toString()).child(USER_INFO).child(USERNAME).value.toString()
                        val grade = result.child(ds.value.toString()).child(USER_INFO).child(GRADE).value.toString()
                        val gradeShow = result.child(ds.value.toString()).child(USER_INFO).child(GRADE_SHOW).value.toString().toBoolean()
                        val isFollowing = result.child(ds.value.toString()).child(USER_INFO).child(FOLLOWERS_LIST).children.map{elem -> elem.key}.contains(currentUser.uid)
                        val imageLocation = result.child(ds.value.toString()).child(USER_INFO).child(PROFILE_IMAGE_LOCATION).value.toString()
                        followersUsers.add(User(
                                ds.value.toString(),
                                name,
                                grade,
                                gradeShow,
                                isFollowing,
                                imageLocation,
                        ))
                    }
                    if (result.child(currentUser.uid).child(USER_INFO).child(FOLLOWERS_LIST).childrenCount.toString().toInt() == followersUids.size){
                        followersUserData.value = followersUsers
                    }
                }
            } else {
                showConnectionProblem(context)
            }
            mainRefreshLayout.isRefreshing = false
            followersUserData.value = followersUsers
        }
    }
}