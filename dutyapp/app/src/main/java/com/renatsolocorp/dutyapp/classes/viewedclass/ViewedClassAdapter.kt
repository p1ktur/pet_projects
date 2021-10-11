package com.renatsolocorp.dutyapp.classes.viewedclass

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.classes.editableclass.*
import com.renatsolocorp.dutyapp.classes.eventdetails.EventDetailFragment
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.pair_viewed_layout.view.*

class ViewedClassAdapter(val pairs: MutableList<DutyPair>, val application: Application, val context: Context, val fragmentManager: FragmentManager, val viewedUserId: String): RecyclerView.Adapter<ViewedClassAdapter.ViewHolder>() {
    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val pairLayout = view.pair_layout
        val numberText = view.number_text
        val pairText = view.pair_text
        val debtCount = view.debt_count
        val dutiesCount = view.duties_count
        val dutyDetail = view.detail_button
        val lastDutyText = view.last_duty_date
        val clickable = view.viewed_clickable_layout

        fun addListener(application: Application, context: Context, fragmentManager: FragmentManager, viewedUserId: String, pair: DutyPair){
            dutyDetail.setOnClickListener {
                memorisedPairId = this.numberText.text.dropLast(1).toString().toInt()-1

                mainDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                currentFragment = EVENT_DETAIL_FRAGMENT
                fragmentManager.beginTransaction().replace(R.id.main_fragment_container, EventDetailFragment(viewedUserId, application)).addToBackStack(null).commit()
                mainDrawer.closeDrawer(GravityCompat.START)
            }

            clickable.setOnLongClickListener {
                var string = pairText.text.toString()

                string += if (pair.isCurrent){
                    context.getString(R.string.is_on_duty_today)
                } else {
                    if (pair.dutyTime == 0L){
                        context.getString(R.string.was_not_on_duty_yet)
                    } else {
                        context.getString(R.string.was_on_duty_on) + formatDate(pair.dutyTime.toString()) + "."
                    }
                }

                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Duty pair", string)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(context, context.getString(R.string.copied_to_clipboard_text), Toast.LENGTH_SHORT).show()

                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.pair_viewed_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.numberText.text = "${position + 1}."
        holder.pairText.text = pairs[position].name
        holder.debtCount.text = pairs[position].debts.toString()
        holder.dutiesCount.text = pairs[position].dutiesAmount.toString()

        holder.lastDutyText.text = if (pairs[position].dutyTime == 0L) context.getString(R.string.pair_was_not_on_duty_yet) else {
            translateDate(formatDate(pairs[position].dutyTime.toString()), context)
        }

        holder.addListener(application, context, fragmentManager, viewedUserId, pairs[position])

        holder.pairLayout.background = if (position == currentPair.id) bgCurrent else bgUnselected
    }

    override fun getItemCount() = pairs.size
}