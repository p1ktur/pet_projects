package com.renatsolocorp.dutyapp.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.*
import com.renatsolocorp.dutyapp.classes.editableclass.*
import com.renatsolocorp.dutyapp.users.followed.FollowedFragment
import com.renatsolocorp.dutyapp.users.followers.FollowersFragment
import com.renatsolocorp.dutyapp.classes.myclasses.MyClassesFragment
import com.renatsolocorp.dutyapp.database.APP_THEME
import com.renatsolocorp.dutyapp.database.LANGUAGE
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.users.search.SearchFragment
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.login.LoginActivity
import com.renatsolocorp.dutyapp.login.loggingIn
import com.renatsolocorp.dutyapp.profile.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.frame_layout.*
import kotlinx.android.synthetic.main.main_toolbar.*

var selectedClass = DutyClass("", "", "", "", "", "")
var viewedUserId = ""

var currentFragment = PROFILE_ITEM

var profileToFollowers = false
var profileToFollowed = false
var profileToSearch = false
var deletingClass = false

lateinit var bgSelected: Drawable
lateinit var bgUnselected: Drawable
lateinit var bgCurrent: Drawable

lateinit var mainRefreshLayout: SwipeRefreshLayout
lateinit var mainLoadingScreen: ConstraintLayout
lateinit var mainDrawerButton: ImageButton
lateinit var mainDrawer: DrawerLayout
lateinit var mainNavigationView: NavigationView
lateinit var headerImage: ImageView
lateinit var headerName: TextView
lateinit var profileSettingsButton: ImageButton
lateinit var profileSettingsMenuButton: ImageButton
lateinit var mainBackButton: ImageButton
lateinit var createNewClassButton: FloatingActionButton
lateinit var classMenuButton: ImageButton
lateinit var eventDetailMenuButton: ImageButton
lateinit var mainSearchView: SearchView
lateinit var mainTextField: TextView

lateinit var imm: InputMethodManager
lateinit var db: FirebaseDatabase

class MainActivity : AppCompatActivity() {

    val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(null)
        setContentView(R.layout.activity_main)

        mainLoadingScreen = editable_class_loading_screen
        mainLoadingScreen.setOnClickListener {  }

        if (currentUser == null){
            loggingIn = true
            finish()
            startActivity(Intent(this, LoginActivity::class.java).apply{ flags = Intent.FLAG_ACTIVITY_CLEAR_TOP })
        } else {
            db = FirebaseDatabase.getInstance()
            viewedUserId = currentUser.uid
        }

        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        bgSelected = getDrawable(R.color.selection)!!
        bgUnselected = getDrawable(R.color.secondaryBackgroundColor)!!
        bgCurrent = getDrawable(R.drawable.current_bg)!!

        mainRefreshLayout = main_refresh_layout
        mainDrawerButton = main_drawer_button
        mainDrawer = main_drawer_layout
        mainNavigationView = main_navigation_view
        headerImage = mainNavigationView.getHeaderView(0).findViewById(R.id.header_profile_image)
        headerImage.clipToOutline = true
        headerName = mainNavigationView.getHeaderView(0).findViewById(R.id.header_username)
        profileSettingsButton = profile_settings_button
        profileSettingsMenuButton = profile_settings_menu_button
        mainBackButton = main_back_button
        createNewClassButton = create_new_class_button
        classMenuButton = class_menu_button
        eventDetailMenuButton = event_detail_menu_button
        mainSearchView = main_search_view
        mainTextField = main_text_field

        setSupportActionBar(findViewById(R.id.main_toolbar))

        mainDrawerButton.setOnClickListener {
            mainDrawer.openDrawer(GravityCompat.START)
        }
        
        if (currentUser != null) supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, ProfileFragment(currentUser.uid, application)).commit()

        mainNavigationView.menu.findItem(R.id.profile_item).setOnMenuItemClickListener {
            if (currentFragment != PROFILE_FRAGMENT) changeViewsVisibilities(PROFILE_ITEM)
            true
        }

        mainNavigationView.menu.findItem(R.id.new_class_item).setOnMenuItemClickListener {
            if (currentFragment != NEW_CLASS_FRAGMENT) changeViewsVisibilities(NEW_CLASS_ITEM)
            true
        }

        mainNavigationView.menu.findItem(R.id.my_classes_item).setOnMenuItemClickListener {
            if (currentFragment != MY_CLASSES_FRAGMENT) changeViewsVisibilities(MY_CLASSES_ITEM)
            true
        }

        mainNavigationView.menu.findItem(R.id.followed_item).setOnMenuItemClickListener {
            if (currentFragment != FOLLOWED_FRAGMENT) changeViewsVisibilities(FOLLOWED_ITEM)
            true
        }

        mainNavigationView.menu.findItem(R.id.followers_item).setOnMenuItemClickListener {
            if (currentFragment != FOLLOWERS_FRAGMENT) changeViewsVisibilities(FOLLOWERS_ITEM)
            true
        }

        mainNavigationView.menu.findItem(R.id.find_people_item).setOnMenuItemClickListener {
            if (currentFragment != SEARCH_FRAGMENT) changeViewsVisibilities(FIND_PEOPLE_ITEM)
            true
        }

        profileSettingsButton.visibility = View.GONE
        profileSettingsButton.setOnClickListener {
            changeViewsVisibilities(PROFILE_SETTINGS_BUTTON)
        }

        mainBackButton.setOnClickListener {
            onBackPressed()
        }

        createNewClassButton.setOnClickListener {
            changeViewsVisibilities(CREATE_NEW_CLASS_BUTTON)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val preferences = ProfilePreferences(newBase!!)
        val language = preferences.customGetData(LANGUAGE)
        super.attachBaseContext(SettingsContextWrapper.wrap(newBase, language))
    }

    private fun changeViewsVisibilities(id: String, userId: String = ""){
        db.goOnline()
        mainRefreshLayout.isEnabled = true
        profileSettingsButton.visibility = View.GONE
        createNewClassButton.visibility = View.GONE
        classMenuButton.visibility = View.GONE
        eventDetailMenuButton.visibility = View.GONE
        mainBackButton.visibility = View.GONE
        mainDrawerButton.visibility = View.VISIBLE
        profileSettingsMenuButton.visibility = View.GONE
        mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        mainSearchView.apply {
            visibility = View.GONE
            layoutParams = mainSearchView.layoutParams.apply{width = ConstraintLayout.LayoutParams.WRAP_CONTENT}
            onActionViewCollapsed()
        }
        mainTextField.visibility = View.VISIBLE

        when(id){
            PROFILE_ITEM -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, ProfileFragment(currentUser!!.uid, application)).commit()
            }
            PROFILE_FRAGMENT -> {
                if (viewedUserId != currentUser!!.uid){
                    mainSearchView.visibility = View.VISIBLE
                    mainTextField.visibility = View.GONE
                    editing = false
                    selectedClass.clear()
                    viewedUserId = currentUser.uid
                    currentFragment = FOLLOWED_FRAGMENT
                    when {
                        profileToFollowed -> {
                            supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, FollowedFragment(application)).addToBackStack(null).commit()
                        }
                        profileToSearch -> {
                            supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, SearchFragment(application)).addToBackStack(null).commit()
                        }
                        profileToFollowers -> {
                            supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, FollowersFragment(application)).addToBackStack(null).commit()
                        }
                    }
                } else {
                    super.moveTaskToBack(true)
                }
            }
            NEW_CLASS_ITEM -> {
                creatingNewClass = true
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, EditableClassFragment(application)).addToBackStack(null).commit()
            }
            VIEWED_CLASS_FRAGMENT -> {
                if (viewedUserId == currentUser!!.uid) {
                    profileSettingsButton.visibility = View.VISIBLE
                    mainBackButton.visibility = View.GONE
                    mainDrawerButton.visibility = View.VISIBLE
                } else {
                    mainBackButton.visibility = View.VISIBLE
                    mainDrawerButton.visibility = View.GONE
                }
                selectedClass.clear()
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, ProfileFragment(userId, application)).addToBackStack(null).commit()
            }
            MY_CLASSES_ITEM -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, MyClassesFragment(application)).commit()
            }
            FOLLOWED_ITEM -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, FollowedFragment(application)).commit()
            }
            FOLLOWERS_ITEM -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, FollowersFragment(application)).commit()
            }
            FIND_PEOPLE_ITEM -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, SearchFragment(application)).commit()
            }
            PROFILE_SETTINGS_BUTTON -> {
                mainBackButton.visibility = View.VISIBLE
                mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                mainDrawerButton.visibility = View.GONE
                profileSettingsMenuButton.visibility = View.VISIBLE
                currentFragment = PROFILE_SETTINGS_FRAGMENT
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, ProfileSettingsFragment(application)).addToBackStack(null).commit()
            }
            CREATE_NEW_CLASS_BUTTON -> {
                classMenuButton.visibility = View.VISIBLE
                mainBackButton.visibility = View.VISIBLE
                mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                mainDrawerButton.visibility = View.GONE
                creatingNewClass = true
                editing = true
                selectedClass.clear()
                currentFragment = NEW_CLASS_FRAGMENT
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, EditableClassFragment(application)).addToBackStack(null).commit()
            }
            NEW_CLASS_FRAGMENT -> {
                editing = false
                if (creatingNewClass) editableViewModel.revertChanges(selectedClass.id, this) else editableViewModel.updateList(pairsList, this)
                creatingNewClass = false
                super.onBackPressed()
            }
            EVENT_DETAIL_FRAGMENT -> {
                if (userId == currentUser!!.uid){
                    classMenuButton.visibility = View.VISIBLE
                    editing = true
                    currentFragment = NEW_CLASS_FRAGMENT
                } else {
                    mainBackButton.visibility = View.VISIBLE
                    mainDrawerButton.visibility = View.GONE
                    currentFragment = VIEWED_CLASS_FRAGMENT
                }
                super.onBackPressed()
            }
            PROFILE_SETTINGS_FRAGMENT -> {
                profileSettingsButton.visibility = View.VISIBLE
                currentFragment = PROFILE_FRAGMENT

                if (appTheme != globalProfilePreferences.customGetData(APP_THEME).toInt()) {
                    if (language != globalProfilePreferences.customGetData(LANGUAGE)) {
                        globalProfilePreferences.customSaveData(language, LANGUAGE)
                    }
                    finish()
                    globalProfilePreferences.customSaveData(appTheme.toString(), APP_THEME)
                    AppCompatDelegate.setDefaultNightMode(appTheme)
                    startActivity(intent)
                } else if (language != globalProfilePreferences.customGetData(LANGUAGE)) {
                    globalProfilePreferences.customSaveData(language, LANGUAGE)
                    finish()
                    startActivity(intent)
                } else super.onBackPressed()
            }
        }
        mainDrawer.closeDrawer(GravityCompat.START)
    }

    override fun onResume() {
        super.onResume()
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        db.goOnline()
    }

    override fun onPause() {
        super.onPause()
        db.goOnline()
    }

    override fun onBackPressed() {
        if(supportFragmentManager.backStackEntryCount != 0){
            when(currentFragment){
                PROFILE_FRAGMENT -> changeViewsVisibilities(PROFILE_FRAGMENT)
                NEW_CLASS_FRAGMENT -> {
                    if (creatingNewClass) {
                        if (deletingClass){
                            changeViewsVisibilities(NEW_CLASS_FRAGMENT)
                        } else {
                            val builder = AlertDialog.Builder(this)

                            val message = SpannableString(getString(R.string.sure_leave_changes_lost))
                            message.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                            builder.setMessage(message)
                            builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                                changeViewsVisibilities(NEW_CLASS_FRAGMENT)
                            }
                            builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                            builder.setCancelable(true)

                            val dialog = builder.create()
                            dialog.window!!.setBackgroundDrawable(AppCompatResources.getDrawable(this, R.drawable.action_bg))
                            dialog.show()
                        }
                    } else {
                        changeViewsVisibilities(NEW_CLASS_FRAGMENT)
                    }
                }
                EVENT_DETAIL_FRAGMENT -> changeViewsVisibilities(EVENT_DETAIL_FRAGMENT, viewedUserId)
                VIEWED_CLASS_FRAGMENT -> changeViewsVisibilities(VIEWED_CLASS_FRAGMENT, viewedUserId)
                MY_CLASSES_FRAGMENT -> moveTaskToBack(true)
                FOLLOWED_FRAGMENT -> moveTaskToBack(true)
                FOLLOWERS_FRAGMENT -> moveTaskToBack(true)
                SEARCH_FRAGMENT -> moveTaskToBack(true)
                PROFILE_SETTINGS_FRAGMENT -> changeViewsVisibilities(PROFILE_SETTINGS_FRAGMENT)
                else -> super.onBackPressed()
            }
        }else{
            moveTaskToBack(true)
        }
    }
}
