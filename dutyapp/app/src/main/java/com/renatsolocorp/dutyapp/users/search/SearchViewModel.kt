package com.renatsolocorp.dutyapp.users.search

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

class SearchViewModel: ViewModel() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    lateinit var userData: MutableLiveData<MutableList<User>>
    var users = mutableListOf<User>()

    fun init(userLoadingScreen: ConstraintLayout, emptyUsersText: TextView, context: Context){
        userData = MutableLiveData()

        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                result.children.forEach {
                    if (it.key.toString() != currentUser.uid){
                        val name = it.child(USER_INFO).child(USERNAME).value.toString()
                        val grade = it.child(USER_INFO).child(GRADE).value.toString()
                        val gradeShow = it.child(USER_INFO).child(GRADE_SHOW).value.toString().toBoolean()
                        val isFollowing = it.child(USER_INFO).child(FOLLOWERS_LIST).children.map { elem -> elem.key }.contains(currentUser.uid)
                        val imageLocation = it.child(USER_INFO).child(PROFILE_IMAGE_LOCATION).value.toString()
                        users.add(User(
                            it.key.toString(),
                            name,
                            grade,
                            gradeShow,
                            isFollowing,
                            imageLocation
                        ))
                        userData.value = users
                        userLoadingScreen.visibility = View.GONE
                        emptyUsersText.visibility = if (users.size == 0) View.VISIBLE else View.GONE
                    }
                }
            } else {
                userLoadingScreen.visibility = View.GONE
                emptyUsersText.visibility = View.VISIBLE
            }
        }
        userData.value = users
        emptyUsersText.visibility = if (userData.value!!.size == 0) View.VISIBLE else View.GONE
    }

    fun updateData(context: Context){
        db.getReference(USERS).get().addOnCompleteListener { utask ->
            if (utask.isSuccessful && utask.result != null){
                val result = utask.result!!
                result.children.forEach {
                    if (it.key.toString() != currentUser.uid && !users.map{ user -> user.id }.contains(it.key.toString())){
                        val name = it.child(USER_INFO).child(USERNAME).value.toString()
                        val grade = it.child(USER_INFO).child(GRADE).value.toString()
                        val gradeShow = it.child(USER_INFO).child(GRADE_SHOW).value.toString().toBoolean()
                        val isFollowing = it.child(USER_INFO).child(FOLLOWERS_LIST).children.map { elem -> elem.key }.contains(currentUser.uid)
                        val imageLocation = it.child(USER_INFO).child(PROFILE_IMAGE_LOCATION).value.toString()
                        users.add(User(
                            it.key.toString(),
                            name,
                            grade,
                            gradeShow,
                            isFollowing,
                            imageLocation
                        ))
                        userData.value = users
                    } else {
                        userData.value = users
                    }
                }
            } else {
                showConnectionProblem(context)
            }
            mainRefreshLayout.isRefreshing = false
        }
    }
}