package com.renatsolocorp.dutyapp.classes.myclasses

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.classdb.DutyClass
import com.renatsolocorp.dutyapp.classes.editableclass.editing
import com.renatsolocorp.dutyapp.database.*
import com.renatsolocorp.dutyapp.extensions.MY_CLASSES_FRAGMENT
import com.renatsolocorp.dutyapp.main.*
import com.renatsolocorp.dutyapp.profile.globalProfilePreferences
import kotlinx.android.synthetic.main.fragment_my_classes.*

lateinit var myClassesViewModel: MyClassesViewModel
var firstLaunch = true

class MyClassesFragment(val application: Application) : Fragment() {

    val mAuth = FirebaseAuth.getInstance()
    val currentUser = mAuth.currentUser

    private var classesList: MutableList<DutyClass> = mutableListOf()

    lateinit var myClassesEmptyText: TextView
    lateinit var myClassesLoadingScreen: ConstraintLayout
    lateinit var myClassesList: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_classes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTextField.text = getString(R.string.your_classes)
        mainRefreshLayout.isEnabled = false
        mainDrawerButton.visibility = View.VISIBLE
        createNewClassButton.visibility = View.VISIBLE
        editing = false
        currentFragment = MY_CLASSES_FRAGMENT

        myClassesEmptyText = my_classes_empty_text
        myClassesLoadingScreen = my_classes_loading_screen
        myClassesLoadingScreen.setOnClickListener {  }
        myClassesList = my_classes_list
        myClassesList.adapter = MyClassesAdapter(classesList.sortedByDescending{ it.createdTime }.toMutableList(), application, fragmentManager!!, context!!)
        myClassesList.layoutManager = LinearLayoutManager(context!!)

        myClassesViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(MyClassesViewModel::class.java)
        myClassesViewModel.init(myClassesLoadingScreen, application, context!!)
        myClassesViewModel.classesList.observe(this, {
            classesList = it ?: mutableListOf()
            val savedState = myClassesList.layoutManager?.onSaveInstanceState()
            val adapter = MyClassesAdapter(classesList.sortedByDescending{ klass -> klass.createdTime }.toMutableList(), application, fragmentManager!!, context!!)
            myClassesList.layoutManager?.onRestoreInstanceState(savedState)
            myClassesList.adapter = adapter

            if (it != null) db.getReference(USERS).child(currentUser!!.uid).child(USER_INFO).child(OWN_CLASSES).get().addOnCompleteListener { task ->
                if (task.isSuccessful && task.result!!.value != null) {
                    task.result!!.ref.setValue(it.size)
                    globalProfilePreferences.customSaveData(it.size.toString(), OWN_CLASSES)
                }
            }

            myClassesEmptyText.visibility = if (classesList.isEmpty()) View.VISIBLE else View.GONE

            selectedClass.clear()
        })

        myClassesViewModel.myClassesList.observe(this, {
            myClassesViewModel.classesList.value = it
        })
    }
}