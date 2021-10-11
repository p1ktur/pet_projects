package com.renatsolocorp.dutyapp.classes.eventdetails

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.text.*
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.database.eventdb.*
import com.renatsolocorp.dutyapp.extensions.*
import com.renatsolocorp.dutyapp.main.*
import kotlinx.android.synthetic.main.detail_element_layout.view.*

class EventDetailAdapter(val list: MutableList<DutyEvent>, val context: Context, val application: Application, val userId: String): RecyclerView.Adapter<EventDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val numberText = view.detail_number_text
        val eventText = view.event_name_text
        val dateText = view.event_date_text
        val dateTextView = view.date_text_view
        val clickable = view.clickable_layout
        val detailLayout = view.info_layout
        val pidText = view.pid_text

        val currentUser = FirebaseAuth.getInstance().currentUser!!

        fun initListeners(context: Context, position: Int){
            clickable.setOnLongClickListener {
                if (viewedUserId == currentUser.uid){
                    val builder = AlertDialog.Builder(context)

                    val message = SpannableString(context.getString(R.string.sure_delete_event) + " №${position + 1}?")
                    message.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.textColor)), 0, message.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    builder.setMessage(message)
                    builder.setTitle(context.getString(R.string.delete_this_event))
                    builder.setPositiveButton(context.getString(R.string.ok)) { dialogInterface: DialogInterface, index: Int ->
                        eventViewModel.deleteEvent(position)
                    }
                    builder.setNegativeButton(context.getString(R.string.cancel)) { dialogInterface: DialogInterface, i: Int -> }
                    builder.setCancelable(true)

                    val dialog = builder.create()
                    dialog.window!!.setBackgroundDrawable(context.resources.getDrawable(R.drawable.action_bg))
                    dialog.show()
                } else {
                    val string = if (dateText.visibility == View.VISIBLE) dateText.text.toString() + ": " + eventText.text.toString() else eventText.text.toString()

                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Pair event", string)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(context, context.getString(R.string.copied_to_clipboard_text), Toast.LENGTH_SHORT).show()
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.detail_element_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.numberText.text = "${position+1}."

        when {
            list[position].event.contains(PAIR_CHANGED_EVENT_NAME) -> {
                val spanStringBuilder = SpannableStringBuilder()
                spanStringBuilder.append(context.getString(R.string.pair_name_changed_to) + " ")
                val spanPairName = SpannableString("${list[position].event.replace(PAIR_CHANGED_EVENT_NAME, "")}")
                spanPairName.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.colorAccent)), 0, spanPairName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanPairName.setSpan(UnderlineSpan(), 0, spanPairName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanStringBuilder.append(spanPairName)
                holder.eventText.setText(spanStringBuilder, TextView.BufferType.SPANNABLE)
            }
            list[position].event.contains(SKIPPED_EVENT_NAME) -> {
                val spanStringBuilder = SpannableStringBuilder()
                spanStringBuilder.append(context.getString(R.string.pair_was_skipped))
                val spanString = SpannableString("(+1 " + context.getString(R.string.debt) + ")")
                spanString.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.colorAccent)), 0, spanString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanString.setSpan(UnderlineSpan(), 0, spanString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanStringBuilder.append(spanString)
                holder.eventText.setText(spanStringBuilder, TextView.BufferType.SPANNABLE)
            }
            list[position].event.contains(ADDED_EVENT_NAME) -> {
                val toAddName = list[position].event.replace("${ADDED_EVENT_NAME}as ", "")
                val spanStringBuilder = SpannableStringBuilder()
                spanStringBuilder.append("${context.getString(R.string.pair_was_added)} " + context.getString(R.string.ass) + " ")
                val spanPairName = SpannableString(toAddName)
                spanPairName.setSpan(ForegroundColorSpan(context.resources.getColor(R.color.colorAccent)), 0, spanPairName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanPairName.setSpan(UnderlineSpan(), 0, spanPairName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                spanStringBuilder.append(spanPairName)
                holder.eventText.setText(spanStringBuilder, TextView.BufferType.SPANNABLE)
            }
            else -> holder.eventText.text = translateEvent(list[position].event, context)
        }

        if (list[position].date == "") {
            holder.dateText.visibility = View.GONE
            holder.dateTextView.visibility = View.GONE
            if (FirebaseAuth.getInstance().currentUser!!.uid == viewedUserId) holder.clickable.visibility = View.GONE
            holder.detailLayout.setBackgroundDrawable(bgUnselected)
        }else if (list[position].date == "Currently") {
            holder.dateText.visibility = View.GONE
            holder.dateTextView.visibility = View.GONE
            if (FirebaseAuth.getInstance().currentUser!!.uid == viewedUserId) holder.clickable.visibility = View.GONE
            holder.detailLayout.setBackgroundDrawable(bgCurrent)
        }  else {
            holder.dateText.text = translateDate(list[position].date, context)
            holder.dateText.visibility = View.VISIBLE
            holder.dateTextView.visibility = View.VISIBLE
            holder.clickable.visibility = View.VISIBLE
            holder.detailLayout.setBackgroundDrawable(bgUnselected)
        }

        holder.initListeners(context, position)
    }

    override fun getItemCount(): Int = list.size
}