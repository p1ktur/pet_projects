package com.renatsolocorp.dutyapp.classes.editableclass

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.classes.eventdetails.EventDetailFragment
import com.renatsolocorp.dutyapp.classes.eventdetails.EventDetailRepository
import com.renatsolocorp.dutyapp.database.eventdb.PAIR_CHANGED_EVENT_NAME
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.database.pairdb.PairRepository
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.pair_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

var selectedPosition: Int? = null
var fatherText = ""
var fatherNumber = 0

class EditableClassAdapter(
    val pairs: MutableList<DutyPair>,
    val application: Application,
    val fragmentManager: FragmentManager,
    val context: Context,
    val activity: Activity
): RecyclerView.Adapter<EditableClassAdapter.ViewHolder>() {
    class ViewHolder(layout: View, application: Application, context: Context): RecyclerView.ViewHolder(
        layout
    ) {
        val eventRepository = EventDetailRepository(application, context)

        val pairLayout = layout.pair_layout
        val numberText = layout.number_text
        val pairText = layout.pair_text
        val debtCount = layout.debt_count
        val dutiesCount = layout.duties_count
        val dutyDetail = layout.detail_button
        val lastDutyText = layout.last_duty_date
        val clickerLayout = layout.clickable_layout

        @SuppressLint("ClickableViewAccessibility")
        fun addListener(
            application: Application,
            fragmentManager: FragmentManager,
            context: Context,
            position: Int,
            activity: Activity
        ){
            pairText.addTextChangedListener {
                if (editing && initFinished) {
                    var text = it.toString()
                    if (it.toString().length > 128) {
                        while (text.length > 128) text = text.dropLast(1)
                        pairText.setText(text)
                        pairText.setSelection(text.length)
                    }
                    if (text.isNotEmpty() && text[0].isWhitespace()) {
                        text = clearWhitespaces(text)
                        pairText.setText(text)
                        pairText.setSelection(0)
                    }
                    memorisedName = pairText.text.toString()
                    if (numberText.text.toString().dropLast(1).toInt() == currentPair.id+1) {
                        currentPair.name = memorisedName
                        pairsList[getIndexOfPair(currentPair)].name = memorisedName
                        currentPairText.text = automataUltra(memorisedName)
                    }
                    if (numberText.text.toString().dropLast(1).toInt() == selectedPair.id+1) {
                        pairsList[getIndexOfPair(selectedPair)].name = memorisedName
                    }

                    pairsList[position].name = memorisedName
                    GlobalScope.launch {
                        PairRepository(application).updatePair(pairsList[position])
                    }
                }
            }

            clickerLayout.setOnClickListener {
                selectedPosition = null
                memorisedPairId = this.numberText.text.dropLast(1).toString().toInt() - 1
                if (editing && fatherNumber < pairsList.size) changeSelectedPair(
                    memorisedPairId,
                    context,
                    activity
                ) else fatherNumber = memorisedPairId
            }

            pairLayout.setOnClickListener {
                selectedPosition = null
                memorisedPairId = this.numberText.text.dropLast(1).toString().toInt()-1
                if (editing && fatherNumber < pairsList.size) changeSelectedPair(
                    memorisedPairId,
                    context,
                    activity
                ) else fatherNumber = memorisedPairId
            }

            dutyDetail.setOnClickListener {
                if (fatherNumber < pairsList.size){
                    val number = fatherNumber
                    var text = pairsList[fatherNumber].name
                    if (text[text.length - 1].isWhitespace()) {
                        while (text[text.length - 1].isWhitespace()) text = text.dropLast(1)
                        if (number == currentPair.id + 1) {
                            currentPair.name = text
                            pairsList[getIndexOfPair(currentPair)].name = text
                            currentPairText.text = text
                        }
                        if (number == selectedPair.id + 1) {
                            pairsList[getIndexOfPair(selectedPair)].name = text
                        }
                        selectedPair.name = text
                    }
                }
                if (checkForNameChanges(selectedPair.id, fatherText)) eventRepository.addEvent(
                    selectedPair.id,
                    PAIR_CHANGED_EVENT_NAME + pairsList[selectedPair.id].name
                )
                memorisedPairId = this.numberText.text.dropLast(1).toString().toInt()-1

                editing = false
                mainDrawerButton.visibility = View.GONE
                classMenuButton.visibility = View.GONE
                mainBackButton.visibility = View.VISIBLE
                mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                eventDetailMenuButton.visibility = View.VISIBLE
                currentFragment = EVENT_DETAIL_FRAGMENT
                imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
                fragmentManager.beginTransaction().replace(
                    R.id.main_fragment_container, EventDetailFragment(
                        FirebaseAuth.getInstance().currentUser!!.uid,
                        application
                    )
                ).addToBackStack(null).commit()
                mainDrawer.closeDrawer(GravityCompat.START)
            }
        }

        private fun changeSelectedPair(position: Int, context: Context, activity: Activity){
            imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)

            val number = fatherNumber
            var text = pairsList[fatherNumber].name
            if (text.isNotEmpty() && text[text.length - 1].isWhitespace()) {
                while (text[text.length - 1].isWhitespace()) text = text.dropLast(1)
                if (number == currentPair.id+1) {
                    currentPair.name = text
                    pairsList[getIndexOfPair(currentPair)].name = text
                    currentPairText.text = text
                }
                if (number == selectedPair.id+1) {
                    pairsList[getIndexOfPair(selectedPair)].name = text
                }
                selectedPair.name = text
            }
            if (checkForNameChanges(selectedPair.id, fatherText)) eventRepository.addEvent(
                selectedPair.id,
                PAIR_CHANGED_EVENT_NAME + pairsList[selectedPair.id].name
            )

            editableViewModel.updateSinglePair(pairsList, selectedPair.id, context)
            selectedPair = pairsList[position]
            fatherText = this.pairText.text.toString()
            fatherNumber = position
            (editableListView.adapter as EditableClassAdapter).notifyDataSetChanged()
            selectedPosition = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.pair_layout,
                parent,
                false
            ), application, context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.numberText.text = "${position + 1}."
        if (holder.pairText.text.toString() != pairs[position].name) holder.pairText.setText(pairs[position].name)
        holder.debtCount.text = pairs[position].debts.toString()
        holder.dutiesCount.text = pairs[position].dutiesAmount.toString()

        holder.lastDutyText.text = if (pairs[position].dutyTime == 0L) context.getString(R.string.pair_was_not_on_duty_yet) else {
            translateDate(formatDate(pairs[position].dutyTime.toString()), context)
        }

        holder.addListener(application, fragmentManager, context, position, activity)

        updateBackgrounds(holder, position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int = pairs.size

    private fun updateBackgrounds(holder: ViewHolder, position: Int){
        if (pairs[position].isCurrent){
            if (position == selectedPair.id){
                if (selectedPosition == null){
                    selectedPosition = position
                    holder.pairLayout.background = bgSelected
                    holder.pairText.isActivated = true
                    holder.pairText.isCursorVisible = true
                    holder.clickerLayout.visibility = View.GONE
                }else if(selectedPosition!! == position){
                    holder.pairLayout.background = bgSelected
                    holder.clickerLayout.visibility = View.GONE
                }
                fatherText = holder.pairText.text.toString()
            }else{
                holder.pairLayout.background = bgCurrent
                holder.pairText.isActivated = false
                holder.pairText.isCursorVisible = false
                holder.clickerLayout.visibility = View.VISIBLE
            }
        } else {
            if (position == selectedPair.id){
                if (selectedPosition == null){
                    selectedPosition = position
                    holder.pairLayout.background = bgSelected
                    holder.pairText.isActivated = true
                    holder.pairText.isCursorVisible = true
                    holder.clickerLayout.visibility = View.GONE
                }else if(selectedPosition!! == position){
                    holder.pairLayout.background = bgSelected
                    holder.clickerLayout.visibility = View.GONE
                }
                fatherText = holder.pairText.text.toString()
            }else{
                holder.pairLayout.background = bgUnselected
                holder.pairText.isActivated = false
                holder.pairText.isCursorVisible = false
                holder.clickerLayout.visibility = View.VISIBLE
            }
        }

        var text = holder.pairText.text.toString()
        if (text.isNotEmpty() && text[text.length - 1].isWhitespace()) {
            while (text[text.length - 1].isWhitespace()) text = text.dropLast(1)
            if (position == currentPair.id) {
                currentPair.name = text
                pairsList[getIndexOfPair(currentPair)].name = text
                currentPairText.text = text
            }
            if (position == selectedPair.id) {
                pairsList[getIndexOfPair(selectedPair)].name = text
            }
            holder.pairText.setText(text)
        }
    }
}