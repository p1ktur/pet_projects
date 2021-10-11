package com.renatsolocorp.dairy.lessons_logic.day_activity_logic

import android.content.Context
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.lesson_layout.view.*
import com.renatsolocorp.dairy.*

class LessonAdapter(lessonsList: MutableList<String>, context: Context, urgentPosition: String?): RecyclerView.Adapter<LessonAdapter.ViewHolder>() {

    var list = lessonsList
    var viewList = mutableListOf<ViewHolder>()
    val urgentPosition = urgentPosition

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val lessonNumber = itemView.lesson_number
        val lessonName = itemView.lesson_name
        val lessonHomework = itemView.lesson_homework
        val lessonCheckbox = itemView.lesson_checkbox
        val horizontalLine = itemView.horizontal_line
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        viewList.add(ViewHolder(LayoutInflater.from(context).inflate(R.layout.lesson_layout, parent, false)))
        return viewList[viewList.size-1]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.lessonNumber.text = (position+1).toString() + "."
        holder.lessonName.setText(list[position].split(smallSeparator)[0])
        holder.lessonName.isFocusable = false
        holder.lessonName.isFocusableInTouchMode = false
        holder.lessonName.isEnabled = false
        holder.lessonName.maxLines = 12
        holder.lessonHomework.setText(list[position].split(smallSeparator)[1])
        holder.lessonHomework.isFocusable = false
        holder.lessonHomework.isFocusableInTouchMode = false
        holder.lessonHomework.isEnabled = false
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(120)
        holder.lessonHomework.filters = filterArray
        holder.lessonHomework.addTextChangedListener {
            if (it!!.length >= 4){
                if (it[it.length-1].isWhitespace() && it[it.length-2].isWhitespace() && it[it.length-3].isWhitespace() && it[it.length-4].isWhitespace()){
                    holder.lessonHomework.setText(it.dropLast(1))
                    holder.lessonHomework.setSelection(holder.lessonHomework.text.length)

                }
            }
            if (holder.lessonHomework.lineCount > 12){
                holder.lessonHomework.setText(it.dropLast(1))
                holder.lessonHomework.setSelection(holder.lessonHomework.text.length)
            }
        }

        holder.lessonCheckbox.isChecked = list[position].split(smallSeparator)[2].toBoolean()
        if (position+1 == list.size) holder.horizontalLine.visibility = View.GONE
        if (position+1 != list.size) holder.horizontalLine.visibility = View.VISIBLE
        animateNumber(holder, urgentPosition, position)
    }

    fun animateNumber(viewHolder: ViewHolder, urgentPosition: String?, currentPosition: Int){
        if (urgentPosition != null){
            val lessonNumber = urgentHomeworkList[urgentPosition.toInt()].split(separator)[0].split(smallSeparator)[1][0]
            if (currentPosition+1 == lessonNumber.toString().toInt()){
                viewHolder.lessonNumber.startAnimation(lessonNumberAnimation)
            }
        }
    }

}