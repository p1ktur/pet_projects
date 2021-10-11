package com.renatsolocorp.dutyapp.classes.eventdetails

import android.app.AlertDialog
import android.app.Application
import android.content.DialogInterface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.classes.editableclass.memorisedPairId
import com.renatsolocorp.dutyapp.classes.editableclass.pairsList
import com.renatsolocorp.dutyapp.database.eventdb.DutyEvent
import com.renatsolocorp.dutyapp.database.eventdb.IS_DUTY_EVENT_NAME
import com.renatsolocorp.dutyapp.extensions.shorten
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.fragment_event_detail.*
import androidx.core.content.ContextCompat.getColor as getColor

lateinit var eventViewModel: EventDetailViewModel
var eventsList = mutableListOf<DutyEvent>()

class EventDetailFragment(val viewedUserId: String, val application: Application) : Fragment() {
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    lateinit var isCurrentTextView: TextView
    lateinit var eventRecyclerView: RecyclerView
    lateinit var eventLoadingScreen: ConstraintLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        classMenuButton.visibility = View.GONE

        initViews()

        //some day fix that shit and make extra events now spawn

        eventRecyclerView = detail_recycler_view
        eventRecyclerView.adapter = EventDetailAdapter(eventsList, context!!, application, viewedUserId)
        eventRecyclerView.layoutManager = LinearLayoutManager(context!!)

        val emptyEvent = DutyEvent(id = 0, event = getString(R.string.nothing_happened_yet), pairId = memorisedPairId, classId = selectedClass.id)

        eventViewModel = ViewModelProvider.AndroidViewModelFactory(application).create(EventDetailViewModel::class.java)
        eventViewModel.init(viewedUserId, eventLoadingScreen, memorisedPairId, application, context!!, activity!!)
        eventViewModel.eventsList.observe(this, { list ->
            isCurrentTextView.visibility = if (pairsList[memorisedPairId].isCurrent) View.VISIBLE else View.GONE
            eventsList = list ?: mutableListOf()
            if (selectedClass.creatorId == currentUser.uid && eventsList.size != 0) eventViewModel.updateDataInTheInternet(memorisedPairId, eventsList)

            if (eventsList.isEmpty() && pairsList[memorisedPairId].isCurrent) {
                eventsList.add(DutyEvent(id = 0, event = IS_DUTY_EVENT_NAME, date = "Currently", pairId = memorisedPairId, classId = selectedClass.id))
            } else if (eventsList.isEmpty()) eventsList.add(emptyEvent) else if (pairsList[memorisedPairId].isCurrent){
                eventsList.add(DutyEvent(id = eventsList.size, event = IS_DUTY_EVENT_NAME, date = "Currently", pairId = memorisedPairId, classId = selectedClass.id))
            }

            val adapter = EventDetailAdapter(eventsList, context!!, application, viewedUserId)
            val savedState = eventRecyclerView.layoutManager?.onSaveInstanceState()
            eventRecyclerView.adapter = adapter
            eventRecyclerView.layoutManager?.onRestoreInstanceState(savedState)
        })

        eventViewModel.localEventsList.observe(this, {
            eventViewModel.eventsList.value = it
        })

        mainRefreshLayout.setOnRefreshListener {
            db.goOnline()
            eventViewModel.updateData(viewedUserId, memorisedPairId, context!!)
        }

        mainRefreshLayout.isEnabled = viewedUserId != currentUser.uid
    }

    private fun initViews(){
        eventLoadingScreen = detail_loading_screen
        eventLoadingScreen.setOnClickListener {  }

        isCurrentTextView = is_current_text_view
        eventRecyclerView = detail_recycler_view

        mainTextField.text = pairsList[memorisedPairId].name.shorten(16)

        eventDetailMenuButton.setOnClickListener {
            val context = context!!
            val popupMenu = PopupMenu(context, eventDetailMenuButton)
            popupMenu.menuInflater.inflate(R.menu.event_detail_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.clear_events_item -> {
                        val builder = AlertDialog.Builder(context)
                        val message = SpannableString(getString(R.string.sure_delete_event_history)).also {
                            it.setSpan(ForegroundColorSpan(getColor(context, R.color.textColor)), 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }

                        builder.setMessage(message)
                        builder.setTitle(getString(R.string.delete_event_history))
                        builder.setPositiveButton(getString(R.string.ok)) { _: DialogInterface, _: Int ->
                            eventViewModel.clearList()
                        }
                        builder.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface, _: Int -> }
                        builder.setCancelable(true)

                        val dialog = builder.create()
                        dialog.show()
                    }
                    else -> {}
                }
                true
            }
            popupMenu.show()
        }
    }

}