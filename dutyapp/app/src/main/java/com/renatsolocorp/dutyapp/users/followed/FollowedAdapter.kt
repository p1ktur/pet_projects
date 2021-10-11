package com.renatsolocorp.dutyapp.users.followed

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.classes.editableclass.editing
import com.renatsolocorp.dutyapp.database.PROFILE_IMAGES
import com.renatsolocorp.dutyapp.database.USERS
import com.renatsolocorp.dutyapp.extensions.PROFILE_FRAGMENT
import com.renatsolocorp.dutyapp.extensions.showConnectionProblem
import com.renatsolocorp.dutyapp.main.*
import com.renatsolocorp.dutyapp.profile.ProfileFragment
import com.renatsolocorp.dutyapp.users.User
import kotlinx.android.synthetic.main.user_element_layout.view.*

class FollowedAdapter(private val list: MutableList<User>, val context: Context, val application: Application, val fragmentManager: FragmentManager): RecyclerView.Adapter<FollowedAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val userImage = view.elem_user_image
        val usernameText = view.elem_username_text
        val userGradeField = view.elem_grade_field
        val userGradeText = view.elem_grade_text
        val isFollowingText = view.following_user_field
        val photoDivider = view.elem_photo_divider
        val clickable = view.clickable_layout

        fun initListeners(userId: String, application: Application, fragmentManager: FragmentManager, context: Context){
            clickable.setOnClickListener {
                val db = FirebaseDatabase.getInstance()
                db.getReference(USERS).get().addOnCompleteListener { utask ->
                    if (utask.isSuccessful && utask.result != null){
                        mainBackButton.visibility = View.VISIBLE
                        mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        mainDrawerButton.visibility = View.GONE
                        mainSearchView.apply {
                            visibility = View.GONE
                            layoutParams = mainSearchView.layoutParams.apply{width = ConstraintLayout.LayoutParams.WRAP_CONTENT}
                            onActionViewCollapsed()
                        }
                        mainTextField.visibility = View.VISIBLE
                        editing = false
                        selectedClass.clear()
                        viewedUserId = userId
                        profileToFollowed = true
                        currentFragment = PROFILE_FRAGMENT
                        fragmentManager.beginTransaction().replace(R.id.main_fragment_container, ProfileFragment(userId, application)).addToBackStack(null).commit()
                        mainDrawer.closeDrawer(GravityCompat.START)
                    } else {
                        showConnectionProblem(context)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.user_element_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.usernameText.text = list[position].name
        holder.userGradeText.text = list[position].grade
        holder.isFollowingText.text = if (list[position].isFollowing) context.getString(R.string.following) else context.getString(R.string.not_following)

        if (!list[position].gradeShow || list[position].grade == "null" || list[position].grade == "") {
            holder.userGradeField.visibility = View.GONE
            holder.userGradeText.visibility = View.GONE
        } else {
            holder.userGradeField.visibility = View.VISIBLE
            holder.userGradeText.visibility = View.VISIBLE
        }
        holder.userGradeField.visibility = View.GONE
        holder.userGradeText.visibility = View.GONE

        holder.userImage.clipToOutline = true

        holder.photoDivider.visibility = if (position == list.size - 1) View.VISIBLE else View.GONE

        val storage = FirebaseStorage.getInstance()
        val reference = storage.reference.child("$PROFILE_IMAGES/${list[position].id}.jpg")
        if (list[position].userImageLocation != "null") reference.downloadUrl.addOnSuccessListener {
            Glide.with(context).load(it).centerCrop().transition(DrawableTransitionOptions.withCrossFade()).into(holder.userImage)
        }

        holder.initListeners(list[position].id, application, fragmentManager, context)
    }

    override fun getItemCount() = list.size
}