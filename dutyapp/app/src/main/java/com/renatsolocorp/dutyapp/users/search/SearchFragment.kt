package com.renatsolocorp.dutyapp.users.search

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
import com.renatsolocorp.dutyapp.extensions.SEARCH_FRAGMENT
import com.renatsolocorp.dutyapp.extensions.checkFilteringQuery
import com.renatsolocorp.dutyapp.extensions.filterQueryText
import com.renatsolocorp.dutyapp.main.*
import com.renatsolocorp.dutyapp.users.User
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment(val application: Application) : Fragment() {
    lateinit var searchViewModel: SearchViewModel

    var users = mutableListOf<User>()
    var searching = false

    lateinit var searchView: SearchView
    lateinit var searchLoadingScreen: ConstraintLayout
    lateinit var usersList: RecyclerView
    lateinit var listLoadingScreen: ConstraintLayout
    lateinit var emptyUsersText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTextField.text = getString(R.string.users)
        if (!profileToFollowed){
            mainSearchView.visibility = View.VISIBLE
            editing = false
            selectedClass.clear()
            currentFragment = SEARCH_FRAGMENT
        }

        initViews()
    }

    private fun initViews(){
        searchLoadingScreen = search_loading_screen
        searchLoadingScreen.setOnClickListener {  }
        listLoadingScreen = users_loading_screen
        listLoadingScreen.setOnClickListener {  }

        searchView = mainSearchView

        emptyUsersText = users_list_empty_text

        usersList = users_list
        usersList.adapter = SearchAdapter(users, context!!, application, fragmentManager!!)
        usersList.layoutManager = LinearLayoutManager(context!!)

        searchViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(
            SearchViewModel::class.java)
        searchViewModel.init(listLoadingScreen, emptyUsersText, context!!)
        searchViewModel.userData.observe(this, {
            users = it

            val query = searchView.query.toString()
            if (searchView.query != null){
                val adapter = SearchAdapter(filterQueryText(users, query), context!!, application, fragmentManager!!)
                usersList.adapter = adapter
                emptyUsersText.visibility = if (checkFilteringQuery(users, query)) View.GONE else View.VISIBLE
            } else {
                val adapter = SearchAdapter(users, context!!, application, fragmentManager!!)
                val savedState = usersList.layoutManager?.onSaveInstanceState()
                usersList.adapter = adapter
                usersList.layoutManager?.onRestoreInstanceState(savedState)
            }
        })

        mainRefreshLayout.setOnRefreshListener {
            db.goOnline()
            searchViewModel.updateData(context!!)
        }

        searchView.setOnSearchClickListener {
            searchView.layoutParams = searchView.layoutParams.apply{width = 0}
            mainTextField.visibility = View.GONE
            searching = true
        }

        if (profileToSearch) {
            searchView.performClick()
            searchView.isIconified = false
            profileToSearch = false
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
                if (checkFilteringQuery(users, query!!)) {
                    val adapter = SearchAdapter(filterQueryText(users, query), context!!, application, fragmentManager!!)
                    usersList.adapter = adapter
                    emptyUsersText.visibility = View.GONE
                } else {
                    emptyUsersText.visibility = View.VISIBLE
                }
                imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val adapter = SearchAdapter(filterQueryText(users, query!!), context!!, application, fragmentManager!!)
                usersList.adapter = adapter
                emptyUsersText.visibility = if (checkFilteringQuery(users, query)) View.GONE else View.VISIBLE

                return true
            }
        })

        searchLoadingScreen.visibility = View.GONE
    }
}