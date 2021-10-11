package com.renatsolocorp.dutyapp.classes.viewedclass

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.classes.editableclass.*
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.extensions.VIEWED_CLASS_FRAGMENT
import com.renatsolocorp.dutyapp.extensions.checkForCurrentPair
import com.renatsolocorp.dutyapp.extensions.getTimeInMillis
import com.renatsolocorp.dutyapp.extensions.shorten
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.class_element_layout.*
import kotlinx.android.synthetic.main.fragment_viewed_class.*
import java.util.*

lateinit var classNameText: TextView
lateinit var onDutyText: TextView
lateinit var creatorNameText: TextView
lateinit var classGradeText: TextView

lateinit var viewedViewModel: ViewedClassViewModel

class ViewedClassFragment(val viewedClass: DutyClass, val viewedUserId: String, val application: Application) : Fragment() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!
    var initFinished = false

    lateinit var viewedListView: RecyclerView
    lateinit var viewedClassLoadingScreen: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_viewed_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTextField.text = viewedClass.name.shorten(16)
        currentFragment = VIEWED_CLASS_FRAGMENT
        initFinished = false
        mainDrawerButton.visibility = View.GONE
        mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mainBackButton.visibility = View.VISIBLE

        initViews()
    }

    private fun initViews(){
        viewedClassLoadingScreen = viewed_class_loading_screen
        viewedClassLoadingScreen.setOnClickListener{  }

        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
        val timeInMillis = getTimeInMillis(tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH))

        val emptyPair = DutyPair("empty", id = 0, isCurrent = true, dutyTime = timeInMillis.toLong())
        pairsList = mutableListOf(emptyPair)
        currentPair = pairsList[0]
        selectedPair = pairsList[0]
        memorisedName = selectedPair.name
        fatherText = selectedPair.name

        currentPairText = current_pair_text

        classNameText = viewed_class_name_text
        onDutyText = viewed_on_duty_text
        creatorNameText = viewed_elder_name_text
        classGradeText = viewed_class_grade_text

        viewedListView = list_recycler_view
        viewedListView.adapter = ViewedClassAdapter(pairsList, application, context!!, fragmentManager!!, viewedUserId)
        viewedListView.layoutManager = LinearLayoutManager(context!!)

        viewedViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(ViewedClassViewModel::class.java)
        viewedViewModel.init(viewedUserId, viewedClass, viewedClassLoadingScreen, application, activity!!)
        viewedViewModel.pairsList.observe(this, androidx.lifecycle.Observer {
            pairsList = it
            pairsList.forEach { pair -> if (pair.isCurrent) currentPair = pair; currentPairText.text = currentPair.name }

            if (!initFinished) {
                initFinished = true
                checkForCurrentPair(viewedClass.creatorId, application, context!!)
                selectedPair = currentPair
                memorisedName = selectedPair.name
                fatherText = selectedPair.name
                viewedViewModel.pairsList.value = pairsList
            }

            val adapter = ViewedClassAdapter(pairsList, application, context!!, fragmentManager!!, viewedUserId)
            val savedState = viewedListView.layoutManager?.onSaveInstanceState()
            viewedListView.adapter = adapter
            viewedListView.layoutManager?.onRestoreInstanceState(savedState)
        })

        viewedViewModel.pairsListData.observe(this, androidx.lifecycle.Observer {
            viewedViewModel.pairsList.value = it
        })

        classNameText.text = viewedClass.name
        onDutyText.text =  viewedClass.dutyAmount
        creatorNameText.text = viewedClass.creatorName
        classGradeText.text = viewedClass.grade
        classGradeText.visibility = View.GONE
        viewed_grade_field.visibility = View.GONE

        mainRefreshLayout.setOnRefreshListener {
            db.goOnline()
            viewedViewModel.updateData(viewedClass.creatorId, viewedClass.id, context!!)
        }
    }

}