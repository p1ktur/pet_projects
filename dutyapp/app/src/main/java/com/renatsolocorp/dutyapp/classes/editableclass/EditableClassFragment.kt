package com.renatsolocorp.dutyapp.classes.editableclass

import android.app.Application
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.classes.eventdetails.EventDetailRepository
import com.renatsolocorp.dutyapp.classes.myclasses.MyClassesRepository
import com.renatsolocorp.dutyapp.database.USERS
import com.renatsolocorp.dutyapp.database.classdb.ClassRepository
import com.renatsolocorp.dutyapp.database.eventdb.PAIR_CHANGED_EVENT_NAME
import com.renatsolocorp.dutyapp.database.eventdb.WAS_DUTY_EVENT_NAME
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.fragment_editable_class.*
import kotlinx.android.synthetic.main.fragment_editable_class.list_recycler_view

var pairsList = mutableListOf<DutyPair>()

var memorisedName = ""
var memorisedPairId = 0

var editing = false
var initFinished = false

lateinit var editableListView: RecyclerView

lateinit var currentPair: DutyPair
lateinit var selectedPair: DutyPair
lateinit var currentPairText: TextView

lateinit var classNameText: EditText
lateinit var onDutyText: TextView
lateinit var yourNameText: TextView
lateinit var yourGradeText: TextView
lateinit var showClassCheckbox: CheckBox
lateinit var editGradeField: TextView

lateinit var editableViewModel: EditableClassViewModel

class EditableClassFragment(private val application: Application) : Fragment() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    lateinit var editableLoadingScreen: ConstraintLayout

    lateinit var editingButtonGroup: ConstraintLayout
    lateinit var internalButtonGroup: ConstraintLayout
    lateinit var addDutiesButton: ImageButton
    lateinit var removeDutiesButton: ImageButton
    lateinit var addDebtButton: ImageButton
    lateinit var removeDebtButton: ImageButton
    lateinit var addPairButton: ImageButton
    lateinit var removePairButton: ImageButton
    lateinit var skipButton: Button
    lateinit var setButton: Button
    lateinit var classSaveChangesButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editable_class, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTextField.text = if (selectedClass.name == ""){
            if (creatingNewClass){
                getString(R.string.creating_new_class)
            } else{
                getString(R.string.app_name)
            }
        } else {
            selectedClass.name.shorten(16)
        }

        mainRefreshLayout.isEnabled = false
        classMenuButton.visibility = View.VISIBLE
        mainBackButton.visibility = View.VISIBLE
        profileSettingsButton.visibility = View.GONE
        mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        mainDrawerButton.visibility = View.GONE
        editing = true
        currentFragment = NEW_CLASS_FRAGMENT

        initViews()
    }

    private fun initViews(){
        editableLoadingScreen = editable_class_loading_screen
        editableLoadingScreen.setOnClickListener{  }

        val emptyPair = DutyPair(getString(R.string.empty), id = 0, isCurrent = true, dutyTime = 0L, classId = selectedClass.id)
        if (pairsList.size == 0) pairsList = mutableListOf(emptyPair)
        currentPair = pairsList[0]
        selectedPair = pairsList[0]
        memorisedName = selectedPair.name
        fatherText = selectedPair.name

        classNameText = class_name_edit_text
        onDutyText = on_duty_text
        yourNameText = your_name_text
        yourGradeText = grade_text
        editGradeField = edit_grade_field
        showClassCheckbox = show_to_others_checkbox

        editableListView = list_recycler_view
        editableListView.adapter = EditableClassAdapter(pairsList, application, fragmentManager!!, context!!, activity!!)
        editableListView.layoutManager = LinearLayoutManager(context!!)

        editableViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(EditableClassViewModel::class.java)
        editableViewModel.init(editableLoadingScreen, application, context!!)
        editableViewModel.pairsList.observe(this, {
            pairsList = it ?: mutableListOf()
            pairsList.forEach { pair ->
                if (pair.isCurrent) {
                    pair.dutyTime = unFormatDate(getCurrentDate(WAS_DUTY_EVENT_NAME, true)).toLong()
                    currentPair = pair
                    currentPairText.text = currentPair.name
                }
            }


            if (!initFinished && pairsList.size != 0) {
                initFinished = true
                editableViewModel.resetIds()
                checkForCurrentPair(FirebaseAuth.getInstance().currentUser!!.uid, application, context!!)
                selectedPair = currentPair
                memorisedName = selectedPair.name
                fatherText = selectedPair.name
            }

            val adapter = EditableClassAdapter(pairsList, application, fragmentManager!!, context!!, activity!!)
            val savedState = editableListView.layoutManager?.onSaveInstanceState()
            editableListView.adapter = adapter
            editableListView.layoutManager?.onRestoreInstanceState(savedState)
            if (addingPair){
                editableListView.scrollToPosition(pairsList.size-1)
                addingPair = false
            }
        })

        editableViewModel.localPairsList.observe(this, {
            if (it.size != 0) editableViewModel.pairsList.value = it
        })

        currentPairText = current_pair_text
        editingButtonGroup = editing_button_group
        internalButtonGroup = internal_button_group
        internalButtonGroup.visibility = View.VISIBLE
        addDutiesButton = add_past_button
        removeDutiesButton = remove_past_button
        addDebtButton = add_debt_button
        removeDebtButton = remove_debt_button
        addPairButton = add_pair_button
        removePairButton = remove_pair_button
        skipButton = skip_button
        setButton = set_button
        classSaveChangesButton = editing_save_changes_button

        classNameText.addTextChangedListener {
            val text = it.toString()
            mainTextField.text = if (text == ""){
                if (creatingNewClass){
                    getString(R.string.creating_new_class)
                } else{
                    getString(R.string.app_name)
                }
            } else {
                text.shorten(16)
            }
        }

        classSaveChangesButton.setOnClickListener {
            saveChanges(editableLoadingScreen, fragmentManager!!, application, activity)
        }

        showClassCheckbox.setOnCheckedChangeListener { _, isChecked ->
            changeShowOptions(isChecked, context!!, application)
        }

        addDutiesButton.setOnClickListener {
            if (pairsList.contains(selectedPair)) addPast(context!!)
        }

        removeDutiesButton.setOnClickListener {
            if (pairsList.contains(selectedPair)) removePast(context!!)
        }

        addDebtButton.setOnClickListener {
            if (pairsList.contains(selectedPair)) addDebt(context!!)
        }

        removeDebtButton.setOnClickListener {
            if (pairsList.contains(selectedPair)) removeDebt(context!!)
        }

        addPairButton.setOnClickListener {
            addPair(application, context!!)
            imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
        }

        removePairButton.setOnClickListener {
            if (pairsList.contains(selectedPair)) {
                imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)

                val builder = AlertDialog.Builder(context!!)

                val message = SpannableString(getString(R.string.sure_want_to_delete) + "\"${selectedPair.name}\"?" + getString(R.string.whole_history_will_be_lost))
                message.setSpan(ForegroundColorSpan(getColor(context!!, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                builder.setMessage(message)
                builder.setTitle(getString(R.string.delete_this_pair))
                builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                    removePair(context!!)
                }
                builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                builder.setCancelable(true)

                val dialog = builder.create()
                dialog.window!!.setBackgroundDrawable(getDrawable(context!!, R.drawable.action_bg))
                dialog.show()
            }
        }

        skipButton.setOnClickListener {
            if (pairsList.contains(selectedPair)) {
                skipPair(application, context!!)
                imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
            }
        }

        setButton.setOnClickListener {
            if (pairsList.contains(selectedPair)) {
                setCurrentPair(application, context!!)
                imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
            }
        }

        classMenuButton.setOnClickListener {
            val context = context!!
            val popupMenu = PopupMenu(context, classMenuButton)
            popupMenu.menuInflater.inflate(R.menu.class_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId){
                    R.id.clear_all_debts -> {
                        val builder = AlertDialog.Builder(context)
                        val message = SpannableString(getString(R.string.sure_clear_debts))
                        message.setSpan(ForegroundColorSpan(getColor(context, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        builder.setMessage(message)
                        builder.setTitle(getString(R.string.clear_all_debts_title))
                        builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                            for (i in pairsList) { i.debts = 0 }
                            editableViewModel.updateList(pairsList, context)
                        }
                        builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                        builder.setCancelable(true)
                        val dialog = builder.create()
                        dialog.window!!.setBackgroundDrawable(getDrawable(context, R.drawable.action_bg))
                        dialog.show()
                    }
                    R.id.clear_all_past_duties -> {
                        val builder = AlertDialog.Builder(context)
                        val message = SpannableString(getString(R.string.sure_clear_duties))
                        message.setSpan(ForegroundColorSpan(getColor(context, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        builder.setMessage(message)
                        builder.setTitle(getString(R.string.clear_all_duties))
                        builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                            for (i in pairsList) { i.dutiesAmount = 0 }
                            editableViewModel.updateList(pairsList, context)
                        }
                        builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                        builder.setCancelable(true)
                        val dialog = builder.create()
                        dialog.window!!.setBackgroundDrawable(getDrawable(context, R.drawable.action_bg))
                        dialog.show()
                    }
                    R.id.clear_all_pairs_history -> {
                        val builder = AlertDialog.Builder(context)
                        val message = SpannableString(getString(R.string.sure_every_pair_events_clear))
                        message.setSpan(ForegroundColorSpan(getColor(context, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        builder.setMessage(message)
                        builder.setTitle(getString(R.string.clear_all_event_histories))
                        builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                            EventDetailRepository(application, context).clearEvents(memorisedPairId)
                        }
                        builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                        builder.setCancelable(true)
                        val dialog = builder.create()
                        dialog.window!!.setBackgroundDrawable(getDrawable(context, R.drawable.action_bg))
                        dialog.show()
                    }
                    R.id.clear_all_pairs -> {
                        val builder = AlertDialog.Builder(context)
                        val message = SpannableString(getString(R.string.sure_delete_all_pairs))
                        message.setSpan(ForegroundColorSpan(getColor(context, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        builder.setMessage(message)
                        builder.setTitle(getString(R.string.delete_all_pairs))
                        builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                            selectedPair = DutyPair(context.getString(R.string.empty), id = 0, isCurrent = true, dutyTime = 0L)
                            fatherText = context.getString(R.string.empty)
                            fatherNumber = 0
                            pairsList.clear()
                            pairsList.add(selectedPair)
                            editableViewModel.updateList(pairsList, context)
                            checkForCurrentPair(FirebaseAuth.getInstance().currentUser!!.uid, application, context)
                        }
                        builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                        builder.setCancelable(true)
                        val dialog = builder.create()
                        dialog.window!!.setBackgroundDrawable(getDrawable(context, R.drawable.action_bg))
                        dialog.show()
                    }
                    R.id.delete_class_item -> {
                        val builder = AlertDialog.Builder(context)
                        val message = SpannableString(getString(R.string.sure_delete_this_class))
                        message.setSpan(ForegroundColorSpan(getColor(context, R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        builder.setMessage(message)
                        builder.setTitle(getString(R.string.delete_this_class))
                        builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                            editableViewModel.classRepository.deleteClassEntirely(selectedClass.id)
                            MyClassesRepository().removeClass(currentUser.uid, context)
                            db.getReference(USERS).get().addOnSuccessListener {
                                ClassRepository(application).updateDataInTheInternet(it)
                            }
                            deletingClass = true
                            activity?.onBackPressed()
                        }
                        builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                        builder.setCancelable(true)
                        val dialog = builder.create()
                        dialog.window!!.setBackgroundDrawable(getDrawable(context, R.drawable.action_bg))
                        dialog.show()
                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun checkIfNamesChanged(){
        val number = selectedPair.id + 1
        var text = selectedPair.name
        if (text[text.length-1].isWhitespace()) {
            while (text[text.length-1].isWhitespace()) text = text.dropLast(1)
            if (number == currentPair.id+1) {
                currentPair.name = text
                pairsList[getIndexOfPair(currentPair)].name = text
                currentPairText.text = text
            }
            if (number == selectedPair.id+1) {
                pairsList[getIndexOfPair(selectedPair)].name = text
            }
        }
        if (checkForNameChanges(selectedPair.id, fatherText)) EventDetailRepository(application, context!!).addEvent(selectedPair.id, PAIR_CHANGED_EVENT_NAME + pairsList[selectedPair.id].name)
        fatherText = selectedPair.name
    }

    override fun onPause() {
        super.onPause()
        checkIfNamesChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (creatingNewClass) editableViewModel.revertChanges(selectedClass.id, context!!)
    }
}