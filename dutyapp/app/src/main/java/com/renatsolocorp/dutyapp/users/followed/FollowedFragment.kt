package com.renatsolocorp.dutyapp.users.followed

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.classes.editableclass.editing
import com.renatsolocorp.dutyapp.extensions.FOLLOWED_FRAGMENT
import com.renatsolocorp.dutyapp.extensions.checkFilteringQuery
import com.renatsolocorp.dutyapp.extensions.filterQueryText
import com.renatsolocorp.dutyapp.main.*
import com.renatsolocorp.dutyapp.users.User
import kotlinx.android.synthetic.main.fragment_followed.*

class FollowedFragment(val application: Application) : Fragment() {
    lateinit var followedViewModel: FollowedViewModel

    var followedUsers = mutableListOf<User>()
    var searching = false

    lateinit var followedList: RecyclerView
    lateinit var searchView: SearchView
    lateinit var followedLoadingScreen: ConstraintLayout
    lateinit var listLoadingScreen: ConstraintLayout
    lateinit var emptyFollowedText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_followed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTextField.text = getString(R.string.followed)
        if (!profileToFollowed){
            mainSearchView.visibility = View.VISIBLE
            editing = false
            selectedClass.clear()
            currentFragment = FOLLOWED_FRAGMENT
        }

        initViews()
    }

    fun initViews(){
        followedLoadingScreen = followed_loading_screen
        followedLoadingScreen.setOnClickListener {  }
        listLoadingScreen = followed_followed_loading_screen
        listLoadingScreen.setOnClickListener {  }

        emptyFollowedText = followed_list_empty_text

        searchView = mainSearchView

        followedList = followed_list
        followedList.adapter = FollowedAdapter(followedUsers, context!!, application, fragmentManager!!)
        followedList.layoutManager = LinearLayoutManager(context!!)

        followedViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(FollowedViewModel::class.java)
        followedViewModel.init(listLoadingScreen, emptyFollowedText, context!!)
        followedViewModel.followedUserData.observe(this, {
            followedUsers = it

            val query = searchView.query.toString()
            if (searchView.query != null){
                val adapter = FollowedAdapter(filterQueryText(followedUsers, query), context!!, application, fragmentManager!!)
                followedList.adapter = adapter
                emptyFollowedText.visibility = if (checkFilteringQuery(followedUsers, query)) View.GONE else View.VISIBLE
            } else {
                val adapter = FollowedAdapter(followedUsers, context!!, application, fragmentManager!!)
                val savedState = followedList.layoutManager?.onSaveInstanceState()
                followedList.adapter = adapter
                followedList.layoutManager?.onRestoreInstanceState(savedState)
            }
        })

        mainRefreshLayout.setOnRefreshListener {
            db.goOnline()
            followedViewModel.updateData(context!!)
        }

        searchView.setOnSearchClickListener {
            searchView.layoutParams = searchView.layoutParams.apply{width = 0}
            mainTextField.visibility = View.GONE
            searching = true
        }

        if (profileToFollowed) {
            searchView.performClick()
            searchView.isIconified = false
            profileToFollowed = false
        }

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus){
                searching = true
            }
        }

        searchView.setOnCloseListener {
            searchView.layoutParams = searchView.layoutParams.apply{width = ConstraintLayout.LayoutParams.WRAP_CONTENT}
            searchView.onActionViewCollapsed()
            mainTextField.visibility = View.VISIBLE
            searching = false
            searchView.clearFocus()
            true
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (checkFilteringQuery(followedUsers, query!!)) {
                    val adapter = FollowedAdapter(filterQueryText(followedUsers, query), context!!, application, fragmentManager!!)
                    followedList.adapter = adapter
                    emptyFollowedText.visibility = View.GONE
                } else {
                    emptyFollowedText.visibility = View.VISIBLE
                }
                imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val adapter = FollowedAdapter(filterQueryText(followedUsers, query!!), context!!, application, fragmentManager!!)
                followedList.adapter = adapter
                emptyFollowedText.visibility = if (checkFilteringQuery(followedUsers, query)) View.GONE else View.VISIBLE

                return true
            }
        })

        followedLoadingScreen.visibility = View.GONE
    }

}