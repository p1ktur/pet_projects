package com.renatsolocorp.dutyapp.profile

import android.app.Application
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.classes.editableclass.editing
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.database.FOLLOWING_LIST
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.fragment_profile.*

lateinit var globalProfilePreferences: ProfilePreferences
lateinit var profileViewModel: ProfileViewModel

var usersClasses = mutableListOf<DutyClass>()
var pinnedClasses = mutableListOf<DutyClass>()

lateinit var userImage: ImageView

var appIsOffline = false

class ProfileFragment(val viewedUserId: String, val application: Application) : Fragment() {

    lateinit var repository: ProfileRepository

    lateinit var usernameText: TextView
    lateinit var userBirthdayText: TextView
    lateinit var userGradeText: TextView
    lateinit var userBioText: TextView
    lateinit var userEmailText: TextView
    lateinit var userMobileText: TextView
    lateinit var userOwnClassesText: TextView
    lateinit var userFollowingText: TextView
    lateinit var userFollowersText: TextView

    lateinit var usernameField: TextView
    lateinit var userBirthdayField: TextView
    lateinit var userGradeField: TextView
    lateinit var userBioField: TextView
    lateinit var userEmailField: TextView
    lateinit var userMobileField: TextView
    lateinit var userOwnClassesField: TextView
    lateinit var userFollowingField: TextView
    lateinit var userFollowersField: TextView

    lateinit var followButton: Button

    lateinit var infoField: TextView
    lateinit var contactsField: TextView

    lateinit var dividerOne: View
    lateinit var dividerThree: View
    lateinit var dividerFour: View

    lateinit var profileLoadingScreen: ConstraintLayout

    lateinit var pinnedClassesField: TextView
    lateinit var userClassesList: RecyclerView

    val currentUser = FirebaseAuth.getInstance().currentUser!!

    var ifCurrentUserFollowed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewedUserId == currentUser.uid) {
            mainTextField.text = getString(R.string.app_name)
            profileSettingsButton.visibility = View.VISIBLE
            mainBackButton.visibility = View.GONE
            mainDrawerButton.visibility = View.VISIBLE
        } else {
            mainTextField.visibility = View.GONE
            mainBackButton.visibility = View.VISIBLE
            mainDrawerButton.visibility = View.GONE
        }
        editing = false
        selectedClass.clear()
        currentFragment = PROFILE_FRAGMENT

        repository = ProfileRepository(viewedUserId, context!!, application)
        globalProfilePreferences = ProfilePreferences(context!!)
        profileViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(ProfileViewModel::class.java)

        usernameText = user_username_text
        userImage = user_profile_image
        userImage.clipToOutline = true
        userBirthdayText = user_birthday_text
        userGradeText = user_grade_text
        userBioText = user_bio_text
        userEmailText = user_email_text
        userMobileText = user_mobile_text
        userOwnClassesText = user_own_classes_text
        userFollowingText = user_following_text
        userFollowersText = user_followers_text

        usernameField = username_field
        userBirthdayField = birthday_field
        userGradeField = grade_field
        userBioField = bio_field
        userEmailField = email_field
        userMobileField = mobile_field
        userOwnClassesField = own_classes_field
        userFollowingField = following_field
        userFollowersField = followers_field

        followButton = follow_button

        profileLoadingScreen = profile_loading_screen
        profileLoadingScreen.setOnClickListener {  }

        pinnedClassesField = pinned_classes_field
        userClassesList = user_classes_list

        infoField = info_field
        contactsField = contacts_field

        dividerOne = divider_one
        dividerThree = divider_three
        dividerFour = divider_four

        initViews()

        initListeners()
    }

    private fun initViews(){
        if (viewedUserId == currentUser.uid){
            userClassesList.adapter = ProfileAdapter(pinnedClasses.sortedBy{ it.pinnedTime }.toMutableList(), application, fragmentManager!!, viewedUserId, context!!)
            userClassesList.layoutManager = LinearLayoutManager(context)

            profileViewModel.initForCurrentUser(profileLoadingScreen, application, context!!, userFollowingText, userFollowersText)
            profileViewModel.pinnedClassesList.observe(this, { klassList ->
                pinnedClasses = klassList
                if (pinnedClasses.map { it.creatorId }.filterNot { it == currentUser.uid }.isEmpty() && pinnedClasses.size != 0) {
                    mainRefreshLayout.isEnabled = false
                    userClassesList.visibility = View.VISIBLE
                    dividerThree.visibility = View.VISIBLE
                    pinnedClassesField.visibility = View.VISIBLE
                    dividerFour.visibility = View.VISIBLE
                } else if (pinnedClasses.size == 0) {
                    mainRefreshLayout.isEnabled = false
                    userClassesList.visibility = View.GONE
                    dividerThree.visibility = View.GONE
                    pinnedClassesField.visibility = View.GONE
                    dividerFour.visibility = View.GONE
                } else {
                    mainRefreshLayout.isEnabled = true
                    userClassesList.visibility = View.VISIBLE
                    dividerThree.visibility = View.VISIBLE
                    pinnedClassesField.visibility = View.VISIBLE
                    dividerFour.visibility = View.VISIBLE
                }
                val savedState = userClassesList.layoutManager?.onSaveInstanceState()
                val adapter = ProfileAdapter(pinnedClasses.sortedBy { it.pinnedTime }.toMutableList(), application, fragmentManager!!, viewedUserId, context!!)
                userClassesList.adapter = adapter
                userClassesList.layoutManager?.onRestoreInstanceState(savedState)
            })

            profileViewModel.localPinnedClassesList.observe(this, {
                profileViewModel.pinnedClassesList.value = it
            })

            mainRefreshLayout.isEnabled = false
            mainRefreshLayout.setOnRefreshListener {
                db.goOnline()
                profileViewModel.updateDataForCurrentUser(context!!)
            }

            usernameText.text = currentUser.displayName

            val path = globalProfilePreferences.customGetData(PROFILE_IMAGE_LOCATION + currentUser.uid)
            if (path != "") {
                val uri = Uri.parse(path)
                userImage.setImageURI(uri)
                headerImage.setImageURI(uri)
            }

            globalProfilePreferences.getProfileData(
                usernameText,
                userBirthdayText,
                userGradeText,
                userBioText,
                userEmailText,
                userMobileText,
                userOwnClassesText,
                userFollowingText,
                userFollowersText
            )

            //headerName.text = usernameText.text

            userClassesList.visibility = View.GONE
            profileSettingsButton.visibility = View.VISIBLE
            profileLoadingScreen.visibility = View.GONE
        } else {
            userClassesList.adapter = ProfileAdapter(usersClasses, application, fragmentManager!!, viewedUserId, context!!)
            userClassesList.layoutManager = LinearLayoutManager(context)

            profileViewModel.init(viewedUserId, context!!, profileLoadingScreen, application, userImage, activity!!)
            profileViewModel.usersClassesList.observe(this, {
                usersClasses = it
                if (usersClasses.size == 0) {
                    userClassesList.visibility = View.GONE
                    dividerThree.visibility = View.GONE
                    pinnedClassesField.visibility = View.GONE
                    dividerFour.visibility = View.GONE
                } else {
                    userClassesList.visibility = View.VISIBLE
                    dividerThree.visibility = View.VISIBLE
                    pinnedClassesField.visibility = View.VISIBLE
                    dividerFour.visibility = View.VISIBLE
                }
                val adapter = ProfileAdapter(usersClasses, application, fragmentManager!!, viewedUserId, context!!)
                val savedState = userClassesList.layoutManager?.onSaveInstanceState()
                userClassesList.adapter = adapter
                userClassesList.layoutManager?.onRestoreInstanceState(savedState)
            })
            profileViewModel.userInfoData.observe(this, {
                usernameText.text = it[0]
                userBirthdayText.text = it[1]
                userGradeText.text = it[2]
                userBioText.text = it[3]
                userEmailText.text = it[4]
                userMobileText.text = it[5]
                userOwnClassesText.text = it[6]
                userFollowingText.text = it[7]
                userFollowersText.text = it[8]

                userBirthdayText.visibility = if (checkIfToShow(it[9])) View.VISIBLE else View.GONE
                userGradeText.visibility = if (checkIfToShow(it[10])) View.VISIBLE else View.GONE
                userBioText.visibility = if (checkIfToShow(it[11])) View.VISIBLE else View.GONE
                userEmailText.visibility = if (checkIfToShow(it[12])) View.VISIBLE else View.GONE
                userMobileText.visibility = if (checkIfToShow(it[13])) View.VISIBLE else View.GONE

                userBirthdayField.visibility = userBirthdayText.visibility
                userGradeField.visibility = userGradeText.visibility
                userBioField.visibility = userBioText.visibility
                userEmailField.visibility = userEmailText.visibility
                userMobileField.visibility = userMobileText.visibility

                mainTextField.visibility = View.VISIBLE
                mainTextField.text = it[0].shorten(16)

                initVisibilities()
            })

            mainRefreshLayout.setOnRefreshListener {
                db.goOnline()
                profileViewModel.updateData(viewedUserId, context!!)
            }

            db.getReference(USERS).child(currentUser.uid).child(USER_INFO).get().addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null){
                    val result = task.result!!
                    if (result.child(FOLLOWING_LIST).children.map{ it.key.toString() }.contains(viewedUserId)){
                        followButton.text = getString(R.string.following)
                    } else {
                        if (result.child(FOLLOWERS_LIST).children.map{ it.key.toString() }.contains(viewedUserId)){
                            ifCurrentUserFollowed = true
                            followButton.text = getString(R.string.follow_back)
                        } else {
                            ifCurrentUserFollowed = false
                            followButton.text = getString(R.string.follow)
                        }
                    }
                }
            }
        }

        userBirthdayField.visibility = userBirthdayText.visibility
        userGradeField.visibility = userGradeText.visibility
        userBioField.visibility = userBioText.visibility
        userEmailField.visibility = userEmailText.visibility
        userMobileField.visibility = userMobileText.visibility
        userOwnClassesField.visibility = userOwnClassesText.visibility
        userFollowingField.visibility = userFollowingText.visibility
        userFollowersField.visibility = userFollowersText.visibility

        initVisibilities()
    }

    private fun initVisibilities(){
        if (userBirthdayText.text == "" || userBirthdayText.text == "null") {
            userBirthdayText.visibility = View.GONE
            userBirthdayField.visibility = View.GONE
        }
        if (userGradeText.text == "" || userGradeText.text == "null"){
            userGradeText.visibility = View.GONE
            userGradeField.visibility = View.GONE
        }
        if (userBioText.text == "" || userBioText.text == "null"){
            userBioText.visibility = View.GONE
            userBioField.visibility = View.GONE
        }
        if (userEmailText.text == "" || userEmailText.text == "null"){
            userEmailText.visibility = View.GONE
            userEmailField.visibility = View.GONE
        }
        if (userMobileText.text == "" || userMobileText.text == "null"){
            userMobileText.visibility = View.GONE
            userMobileField.visibility = View.GONE
        }

        if (userEmailField.visibility == View.GONE && userMobileField.visibility == View.GONE) {
            contactsField.visibility = View.GONE
            dividerOne.visibility = View.GONE
        } else {
            contactsField.visibility = View.VISIBLE
            dividerOne.visibility = View.VISIBLE
        }

        dividerThree.visibility = if (userClassesList.visibility == View.GONE) View.GONE else View.VISIBLE
        if (viewedUserId == currentUser.uid) {
            pinnedClassesField.visibility = if (userClassesList.visibility == View.GONE) View.GONE else View.VISIBLE
            dividerFour.visibility = if (userClassesList.visibility == View.GONE) View.GONE else View.VISIBLE
        } else {
            pinnedClassesField.text = usernameText.text.toString() + getString(R.string.s_classes)
            pinnedClassesField.visibility = if (userClassesList.visibility == View.GONE) View.GONE else View.VISIBLE
            dividerFour.visibility = if (userClassesList.visibility == View.GONE) View.GONE else View.VISIBLE
        }

        followButton.visibility = if (viewedUserId == currentUser.uid) View.GONE else View.VISIBLE
    }

    private fun initListeners(){
        followButton.setOnClickListener {
            db.getReference(USERS).get().addOnCompleteListener { utask ->
                if (utask.isSuccessful && utask.result != null){
                    val result = utask.result!!
                    var followingCount = "0"
                    if (followButton.text == getString(R.string.follow) || followButton.text == getString(R.string.follow_back)){
                        result.child(currentUser.uid).child(USER_INFO).child(FOLLOWING_LIST).child(viewedUserId).ref.setValue(viewedUserId)
                        val path1 = result.child(currentUser.uid).child(USER_INFO).child(FOLLOWING)
                        path1.ref.setValue((path1.value.toString().toInt() + 1).toString())
                        followingCount = (path1.value.toString().toInt() + 1).toString()
                        globalProfilePreferences.customSaveData(followingCount, FOLLOWING)

                        result.child(viewedUserId).child(USER_INFO).child(FOLLOWERS_LIST).child(currentUser.uid).ref.setValue(currentUser.uid)
                        val path2 = result.child(viewedUserId).child(USER_INFO).child(FOLLOWERS)
                        path2.ref.setValue((path2.value.toString().toInt() + 1).toString())

                        userFollowersText.text = (userFollowersText.text.toString().toInt() + 1).toString()
                        followButton.text = getString(R.string.following)
                    } else if (followButton.text == getString(R.string.following)){
                        result.child(currentUser.uid).child(USER_INFO).child(FOLLOWING_LIST).child(viewedUserId).ref.removeValue()
                        val path1 = result.child(currentUser.uid).child(USER_INFO).child(FOLLOWING)
                        path1.ref.setValue((path1.value.toString().toInt() - 1).toString())
                        followingCount = (path1.value.toString().toInt() - 1).toString()
                        globalProfilePreferences.customSaveData(followingCount, FOLLOWING)

                        result.child(viewedUserId).child(USER_INFO).child(FOLLOWERS_LIST).child(currentUser.uid).ref.removeValue()
                        val path2 = result.child(viewedUserId).child(USER_INFO).child(FOLLOWERS)
                        path2.ref.setValue((path2.value.toString().toInt() - 1).toString())

                        userFollowersText.text = (userFollowersText.text.toString().toInt() - 1).toString()
                        if (ifCurrentUserFollowed) followButton.text = getString(R.string.follow_back) else followButton.text = getString(R.string.follow)
                    }
                } else {
                    showConnectionProblem(context!!)
                }
            }
        }
    }
}