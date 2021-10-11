package com.renatsolocorp.dairy.lessons_logic

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.*
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.lesson_layout.view.horizontal_line
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_checkbox
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_homework
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_name
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_number
import com.renatsolocorp.dairy.*

var editMode = false
var notificationMode = false

var bigLayoutsId = mutableListOf<Int>()   //42
var numberTextId = mutableListOf<Int>()   //42
var staticTextNameId = mutableListOf<Int>()   //42
var staticTextHWId = mutableListOf<Int>()   //42
var verticalLinesId = mutableListOf<Int>()  //84
var horizontalLinesId = mutableListOf<Int>()  //36

fun createDaysList(layoutList: MutableList<LinearLayout>, iterator: Int) {

    val lessons = mutableListOf(
        LinearLayout(context),
        LinearLayout(context),
        LinearLayout(context),
        LinearLayout(context),
        LinearLayout(context),
        LinearLayout(context),
        LinearLayout(context))

    for (i in lessons.indices) {

        //Log.d("sd", "LESSONS SIZE ${lessons[i].size}")

        val lessonNumber = TextView(context)
        lessonNumber.text = "${i + 1}."
        lessonNumber.textSize = 8f
        lessonNumber.setTextColor(getAttributeColor(context, R.attr.scrollTextColor))
        lessonNumber.gravity = Gravity.CENTER

        val numberParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
        numberParams.setMargins(10, 20, 0, 0)
        lessonNumber.layoutParams = numberParams
        lessonNumber.id = getViewId(numberTextId, 42, i+6*iterator)
        lessons[i].addView(lessonNumber)

        val blackLine1 = LinearLayout(context)
        blackLine1.setBackgroundColor(getAttributeColor(context, R.attr.scrollLinesColor))
        val blParams1 = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
        blParams1.setMargins(10, 0, 0, 0)
        blackLine1.layoutParams = blParams1
        blackLine1.id = getViewId(verticalLinesId, 84, i+1+6*iterator)
        lessons[i].addView(blackLine1)

        val staticLessonName = TextView(context)
        staticLessonName.text = lessonsList[iterator][i].split(smallSeparator)[0].shorten()
        staticLessonName.setTextColor(getAttributeColor(context, R.attr.scrollTextColor))
        staticLessonName.textSize = 8f
        staticLessonName.gravity = Gravity.BOTTOM
        staticLessonName.minWidth = 40
        staticLessonName.setLines(1)
        val staticNameParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT)
        staticNameParams.marginStart = 10
        staticLessonName.layoutParams = staticNameParams
        staticLessonName.id = getViewId(staticTextNameId, 42, i+2+6*iterator)
        lessons[i].addView(staticLessonName)


        val blackLine2 = LinearLayout(context)
        val blParams2 = LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT)
        blackLine2.setBackgroundColor(getAttributeColor(context, R.attr.scrollLinesColor))
        blParams2.setMargins(10, 0, 0, 0)
        blackLine2.layoutParams = blParams2
        blackLine2.id = getViewId(verticalLinesId, 84, i+3+6*iterator)
        lessons[i].addView(blackLine2)

        val staticLessonHW = TextView(context)
        staticLessonHW.text = lessonsList[iterator][i].split(smallSeparator)[1].shorten()
        staticLessonHW.setTextColor(getAttributeColor(context, R.attr.scrollTextColor))
        staticLessonHW.textSize = 8f
        staticLessonHW.gravity = Gravity.BOTTOM
        staticLessonHW.setLines(1)
        val staticHWParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        staticHWParams.marginStart = 10
        staticLessonHW.layoutParams = staticHWParams
        staticLessonHW.id = getViewId(staticTextHWId, 42, i+4+6*iterator)
        lessons[i].addView(staticLessonHW)

        //Log.d("sd", "LESSONS SIZE ${lessons[i].size}")

        val lessonParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lessonParams.marginStart = 10
        lessonParams.layoutDirection = LinearLayout.HORIZONTAL
        lessons[i].layoutParams = lessonParams
        //lessons[i].setBackgroundResource(R.drawable.lesson_layout_background)
        lessons[i].id = getViewId(bigLayoutsId, 42, i+5+6*iterator)
        //Log.d("sd", "${layoutList[iterator]} ${lessons[i]}")
        layoutList[iterator].addView(lessons[i])

        if (i < 6) {
            val blackLine = LinearLayout(context)
            val blParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
            blackLine.setBackgroundColor(getAttributeColor(context, R.attr.scrollLinesColor))
            blParams.setMargins(10, 0, 10, 0)
            blackLine.layoutParams = blParams
            blackLine.id = getViewId(horizontalLinesId, 42, i+6+6*iterator)
            layoutList[iterator].addView(blackLine)
        }
    }
    setLessons(layoutList[iterator])
}

fun getViewId(list: MutableList<Int>, maxSize: Int, index: Int): Int{
    if (list.size < maxSize){
        list.add(View.generateViewId())
        return list[list.size-1]
    }else{
        return list[index]
    }
}

fun recordLessons(lessonsViewsList: MutableList<LinearLayout>, preference: Preferences){
    cleanLessons(lessonsViewsList)
    var k = 0
    if (!notificationMode){
        lessonsViewsList.forEach {
            lessonsList[getSelectedDayId()][k] = it.lesson_name.text.toString() + smallSeparator + it.lesson_homework.text + smallSeparator + it.lesson_checkbox.isChecked.toString()
            k++
        }
    }else{
        lessonsViewsList.forEach {
            lessonsList[getSelectedDayId()][k] = lessonsList[getSelectedDayId()][k].split(smallSeparator)[0] + smallSeparator + lessonsList[getSelectedDayId()][k].split(smallSeparator)[1] + smallSeparator + it.lesson_checkbox.isChecked.toString()
            k++
        }
    }
    preference.saveLessons(lessonsList, selectedWeek!!)
}

fun setLessons(layout: LinearLayout){
    for (i in daysList.indices){
        if (layout == daysList[i]){
            var j = 0
            layout.forEach {
                if (bigLayoutsId.contains(it.id) && j < 7){
                    (it as LinearLayout).forEach {
                        if (staticTextNameId.contains(it.id)){
                            (it as TextView).text = lessonsList[i][j].split(smallSeparator)[0].shorten()
                        }else if (staticTextHWId.contains(it.id)){
                            (it as TextView).text = lessonsList[i][j].split(smallSeparator)[1].shorten()
                        }
                    }
                    j++
                }
            }
        }
    }
}

fun setViewsLessons(lessonsViewsList: MutableList<LinearLayout>){
    var k = 0
    lessonsViewsList.forEach {
        it.lesson_number.text = "${k+1}."
        it.lesson_name.setText(lessonsList[getSelectedDayId()][k].split(smallSeparator)[0])
        it.lesson_homework.setText(lessonsList[getSelectedDayId()][k].split(smallSeparator)[1])
        it.lesson_checkbox.isChecked = lessonsList[getSelectedDayId()][k].split(smallSeparator)[2].toBoolean()

        initEditTexts(lessonsViewsList)

        if (k == lessonsViewsList.size-1) it.horizontal_line.visibility = View.GONE

        k++
    }
}

fun initEditTexts(lessonsViewsList: MutableList<LinearLayout>){
    lessonsViewsList.forEach {
        val lesson = it
        lesson.lesson_homework.isFocusable = false
        lesson.lesson_homework.isFocusableInTouchMode = false
        lesson.lesson_homework.isEnabled = false

        lesson.lesson_homework.addTextChangedListener {
            if (lesson.lesson_homework.lineCount > 12){
                lesson.lesson_homework.setText(it!!.dropLast(1))
                lesson.lesson_homework.setSelection(lesson.lesson_homework.text.length)
            }
        }

        lesson.lesson_name.isFocusable = false
        lesson.lesson_name.isFocusableInTouchMode = false
        lesson.lesson_name.isEnabled = false

        lesson.lesson_name.addTextChangedListener {
            if (lesson.lesson_name.lineCount > 12){
                lesson.lesson_name.setText(it!!.dropLast(1))
                lesson.lesson_name.setSelection(lesson.lesson_name.text.length)
            }
        }
    }

}

fun cleanLessons(lessonsViewsList: MutableList<LinearLayout>){
    lessonsViewsList.forEach {
        if (it.lesson_homework.text.isBlank()) it.lesson_homework.setText("")
        while (it.lesson_homework.text.toString() != "" && it.lesson_homework.text[it.lesson_homework.text.length-1].isWhitespace()){
            it.lesson_homework.setText(it.lesson_homework.text.dropLast(1))
        }
    }
}

