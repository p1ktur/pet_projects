package com.renatsolocorp.dutyapp.users.followers

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
import com.renatsolocorp.dutyapp.extensions.FOLLOWERS_FRAGMENT
import com.renatsolocorp.dutyapp.extensions.checkFilteringQuery
import com.renatsolocorp.dutyapp.extensions.filterQueryText
import com.renatsolocorp.dutyapp.main.*
import com.renatsolocorp.dutyapp.users.User
import kotlinx.android.synthetic.main.fragment_followers.*

class FollowersFragment(val application: Application) : Fragment() {

    lateinit var followersViewModel: FollowersViewModel

    var followersUsers = mutableListOf<User>()
    var searching = false

    lateinit var followersList: RecyclerView
    lateinit var searchView: SearchView
    lateinit var followersLoadingScreen: ConstraintLayout
    lateinit var listLoadingScreen: ConstraintLayout
    lateinit var emptyFollowersText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_followers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTextField.text = getString(R.string.followers)
        if (!profileToFollowers){
            mainSearchView.visibility = View.VISIBLE
            editing = false
            selectedClass.clear()
            currentFragment = FOLLOWERS_FRAGMENT
        }

        initViews()
    }

    private fun initViews(){
        followersLoadingScreen = followers_loading_screen
        followersLoadingScreen.setOnClickListener {  }
        listLoadingScreen = list_loading_screen
        listLoadingScreen.setOnClickListener {  }

        emptyFollowersText = followers_list_empty_text

        searchView = mainSearchView

        followersList = followers_list
        followersList.adapter = FollowersAdapter(followersUsers, context!!, application, fragmentManager!!)
        followersList.layoutManager = LinearLayoutManager(context!!)

        followersViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(
            FollowersViewModel::class.java)
        followersViewModel.init(listLoadingScreen, emptyFollowersText, context!!)
        followersViewModel.followersUserData.observe(this, {
            followersUsers = it

            val query = searchView.query.toString()
            if (searchView.query != null){
                val adapter = FollowersAdapter(filterQueryText(followersUsers, query), context!!, application, fragmentManager!!)
                followersList.adapter = adapter
                emptyFollowersText.visibility = if (checkFilteringQuery(followersUsers, query)) View.GONE else View.VISIBLE
            } else {
                val adapter = FollowersAdapter(followersUsers, context!!, application, fragmentManager!!)
                val savedState = followersList.layoutManager?.onSaveInstanceState()
                followersList.adapter = adapter
                followersList.layoutManager?.onRestoreInstanceState(savedState)
            }
        })

        mainRefreshLayout.setOnRefreshListener {
            db.goOnline()
            followersViewModel.updateData(context!!)
        }

        searchView.setOnSearchClickListener {
            searchView.layoutParams = searchView.layoutParams.apply{width = 0}
            mainTextField.visibility = View.GONE
            searching = true
        }

        if (profileToFollowers) {
            searchView.performClick()
            searchView.isIconified = false
            profileToFollowers = false
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
                if (checkFilteringQuery(followersUsers, query!!)) {
                    val adapter = FollowersAdapter(filterQueryText(followersUsers, query), context!!, application, fragmentManager!!)
                    followersList.adapter = adapter
                    emptyFollowersText.visibility = View.GONE
                } else {
                    emptyFollowersText.visibility = View.VISIBLE
                }
                imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val adapter = FollowersAdapter(filterQueryText(followersUsers, query!!), context!!, application, fragmentManager!!)
                followersList.adapter = adapter
                emptyFollowersText.visibility = if (checkFilteringQuery(followersUsers, query)) View.GONE else View.VISIBLE

                return true
            }
        })

        followersLoadingScreen.visibility = View.GONE
    }
}