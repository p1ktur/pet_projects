package com.renatsolocorp.dairy.notifications_logic

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.urgent_homework_layout.view.*
import com.renatsolocorp.dairy.*

const val additional = "Additional"

class UrgentHomeworkAdapter(homeworkList: MutableList<String>, context: Context, onUrgentClickListener: OnUrgentClickListener): RecyclerView.Adapter<UrgentHomeworkAdapter.ViewHolder>(){
    val list = homeworkList
    val context = context
    val onUrgentClickListener = onUrgentClickListener

    class ViewHolder(view: View, onUrgentClickListener: OnUrgentClickListener): RecyclerView.ViewHolder(view), View.OnClickListener{
        val week = view.urgent_week
        val date = view.urgent_date
        val homework = view.urgent_homework
        val identifier = view.notified_identifier
        val onUrgentClickListener = onUrgentClickListener

        fun initClickListeners(){
            itemView.setOnClickListener {
                onUrgentClickListener.onItemClick(adapterPosition)
            }
        }

        override fun onClick(v: View?) {

        }
    }

    interface OnUrgentClickListener{
        fun onItemClick(position: Int){

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.urgent_homework_layout, parent, false), onUrgentClickListener)
        viewHolder.initClickListeners()
        return viewHolder
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.week.text = list[position].split(separator)[0].split(smallSeparator)[0].split(" ")[0] + " " + list[position].split(separator)[0].split(smallSeparator)[0].split(" ")[1]
        holder.date.text = list[position].split(separator)[0].split(smallSeparator)[0].split(" ")[2] + " " + list[position].split(separator)[0].split(smallSeparator)[0].split(" ")[3]
        holder.homework.text = list[position].split(separator)[0].split(smallSeparator)[1]
        holder.identifier.background = if (urgentHomeworkList[position].split(separator)[1].toLong() == 0L) identifierGreen else identifierRed
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun removeItem(position: Int){
        urgentHomeworkList.removeAt(position)
        notifyItemRemoved(position)
        globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
        if (urgentHomeworkList.size == 0) noHomeworkYet.visibility = View.VISIBLE else noHomeworkYet.visibility = View.GONE
    }

    fun addItem(text: String){
        Log.d("sd", "$text")
        var allowed = true
        for (i in urgentHomeworkList.indices){
            if (urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[0].contains(text.split(separator)[0].split(smallSeparator)[0]) &&
                (urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[1].contains(text.split(separator)[0].split(smallSeparator)[1][0]) || (text.split(separator)[0].split(smallSeparator)[1][0] == '8' &&
                        urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[1].contains(additional)))) {
                if (urgentHomeworkList[i].split(separator)[1] != text.split(separator)[1]){
                    urgentHomeworkList[i] = text
                    allowed = false
                }else allowed = false
            }
        }
        if (allowed) urgentHomeworkList.add(text)
        notifyItemInserted(urgentHomeworkList.size)
        if (urgentHomeworkList.size == 0) noHomeworkYet.visibility = View.VISIBLE else noHomeworkYet.visibility = View.GONE
    }

    fun checkLessonsForUrgentChanges(){
        for (i in urgentHomeworkList.indices){
            if (urgentHomeworkList[i].contains("Week ${selectedWeek.getWeek()}") && urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[2] == getWeekTextYear(weekParams.text.toString())){
                for (j in dayOfWeekNames.indices){
                    if (urgentHomeworkList[i].contains(dayOfWeekNames[j])){
                        urgentHomeworkList[i] = urgentHomeworkList[i].replace(additional, "8.")
                        val iterator = dayToIterator(dayOfWeekNames[j])
                        val lessons = globalPreference.getLessons(selectedWeek)
                        var lessonNumber = "${urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[1][0]}."
                        if (lessonNumber == "8.") lessonNumber = additional
                        val lessonName = "${lessons[iterator][urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[1][0].toString().toInt()-1].split(separator)[0].split(smallSeparator)[0].eliminateWhitespace()}: "
                        val lessonNumberName = (lessonNumber + lessonName).replace(".:",".")
                        val homework = lessons[iterator][urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[1][0].toString().toInt()-1].split(separator)[0].split(smallSeparator)[1].eliminateWhitespace()
                        val notificationYear = urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[2]
                        urgentHomeworkList[i] = urgentHomeworkList[i].split(separator)[0].split(smallSeparator)[0] + smallSeparator + lessonNumberName + homework + smallSeparator + notificationYear + separator + urgentHomeworkList[i].split(separator)[1]

                        notifyItemChanged(i)
                    }
                }
            }
        }
        globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
        if (urgentHomeworkList.size == 0) noHomeworkYet.visibility = View.VISIBLE else noHomeworkYet.visibility = View.GONE
    }

    fun clearSelectedWeekUrgents() {
        if (urgentHomeworkList.size != 0){
            for (i in urgentHomeworkList.indices) {
                if (urgentHomeworkList[i].contains("Week ${selectedWeek.getWeek()}")) {
                    urgentHomeworkList.removeAt(i)
                    notifyItemRemoved(i)
                    clearSelectedWeekUrgents()
                    break
                }
            }

            globalPreference.saveNotifications(
                urgentsToString(
                    urgentHomeworkList
                )
            )
            if (urgentHomeworkList.size == 0) noHomeworkYet.visibility = View.VISIBLE else noHomeworkYet.visibility = View.GONE
        }
    }

    fun clearSelectedDayUrgents(day: String) {
        if (urgentHomeworkList.size != 0){
            for (i in urgentHomeworkList.indices) {
                if (urgentHomeworkList[i].contains(day) && urgentHomeworkList[i].contains("Week ${selectedWeek.getWeek()}")) {
                    urgentHomeworkList.removeAt(i)
                    notifyItemRemoved(i)
                    clearSelectedDayUrgents(day)
                    break
                }
            }
            globalPreference.saveNotifications(
                urgentsToString(
                    urgentHomeworkList
                )
            )
            if (urgentHomeworkList.size == 0) noHomeworkYet.visibility = View.VISIBLE else noHomeworkYet.visibility = View.GONE
        }
    }

    fun clearAllUrgents(){
        for (i in (urgentHomeworkList.size-1).downTo(0)) {
            urgentHomeworkList.removeAt(i)
            notifyItemRemoved(i)
        }
        globalPreference.saveNotifications(
            urgentsToString(
                urgentHomeworkList
            )
        )
        if (urgentHomeworkList.size == 0) noHomeworkYet.visibility = View.VISIBLE else noHomeworkYet.visibility = View.GONE
    }
}