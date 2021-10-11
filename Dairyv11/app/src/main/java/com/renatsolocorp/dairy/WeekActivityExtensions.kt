package com.renatsolocorp.dairy

import android.content.Context
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_homework
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_name
import kotlinx.android.synthetic.main.lesson_layout.view.lesson_number
import kotlinx.android.synthetic.main.lesson_layout_additional.view.*
import com.renatsolocorp.dairy.lessons_logic.*
import com.renatsolocorp.dairy.lessons_logic.day_activity_logic.*
import com.renatsolocorp.dairy.notifications_logic.UrgentHomeworkAdapter
import kotlinx.android.synthetic.main.activity_week.*
import java.util.*

fun nextWeek(){
    val yearMonthWeek = getYearMonthWeek(selectedWeek, "forward")
    val newWeek = Week(
        yearMonthWeek[0],
        yearMonthWeek[1],
        yearMonthWeek[2],
        0..0
    )
    newWeek.weekRange = getWeekRange(newWeek)

    globalPreference.saveLessons(lessonsList, selectedWeek)
    val toCopyLessonsList = lessonsList

    if (!globalPreference.checkIfWeekExists(newWeek)){
        globalPreference.addWeak(newWeek)
        selectedWeek = newWeek
    }else{
        selectedWeek = newWeek
    }

    checkForCurrentWeek()

    selectedWeek.setText(weekText, weekParams, todayParams)

    lessonsList = globalPreference.getLessons(selectedWeek)

    if (scheduleOption){
        copySchedule(toCopyLessonsList, lessonsList)
    }

    for (i in daysList.indices){
        setLessons(daysList[i])
    }

    for (i in daysNamesViewsList.indices){
        daysNamesViewsList[i].text = if (i == 2 && setDate(selectedWeek.weekRange.first+i).length > 5) "Wed." + setDate(selectedWeek.weekRange.first+i) else dayOfWeekNames[i] + setDate(selectedWeek.weekRange.first+i)
    }
}

fun prevWeek(){
    val yearMonthWeek = getYearMonthWeek(selectedWeek, "backward")
    val newWeek = Week(
        yearMonthWeek[0],
        yearMonthWeek[1],
        yearMonthWeek[2],
        0..0
    )
    newWeek.weekRange = getWeekRange(newWeek)

    globalPreference.saveLessons(lessonsList, selectedWeek)

    val toCopyLessonsList = lessonsList

    if (!globalPreference.checkIfWeekExists(newWeek)){
        globalPreference.addWeak(newWeek)
        selectedWeek = newWeek
    }else{
        selectedWeek = newWeek
    }

    checkForCurrentWeek()

    selectedWeek.setText(weekText, weekParams, todayParams)

    lessonsList = globalPreference.getLessons(selectedWeek)

    if (scheduleOption){
        copySchedule(toCopyLessonsList, lessonsList)
    }

    for (i in daysList.indices){
        setLessons(daysList[i])
    }

    for (i in daysNamesViewsList.indices){
        daysNamesViewsList[i].text = if (i == 2 && setDate(selectedWeek.weekRange.first+i).length > 5) "Wed." + setDate(selectedWeek.weekRange.first+i) else dayOfWeekNames[i] + setDate(selectedWeek.weekRange.first+i)
    }
}

fun changeWeek(steps: Pair<Int, Int>, urgentsWeekAndYear: Pair<Int, Int>){
    Log.d("sd", "changing week $steps")
    var resultWeek = selectedWeek

    if (steps.first > 0){
        for (i in 1..steps.first){
            val yearMonthWeek = getYearMonthWeek(resultWeek, "forward")
            val newWeek = Week(
                yearMonthWeek[0],
                yearMonthWeek[1],
                yearMonthWeek[2],
                0..0
            )
            newWeek.weekRange = getWeekRange(newWeek)
            resultWeek = newWeek
        }
    }else if (steps.first < 0){
        for (i in steps.first..-1){
            val yearMonthWeek = getYearMonthWeek(resultWeek, "backward")
            val newWeek = Week(
                yearMonthWeek[0],
                yearMonthWeek[1],
                yearMonthWeek[2],
                0..0
            )
            newWeek.weekRange = getWeekRange(newWeek)
            resultWeek = newWeek
        }
    }

    globalPreference.saveLessons(lessonsList, selectedWeek)

    if (!globalPreference.checkIfWeekExists(resultWeek)){
        globalPreference.addWeak(resultWeek)
        selectedWeek = resultWeek
    }else{
        selectedWeek = resultWeek
    }

    checkForCurrentWeek()

    selectedWeek.setText(weekText, weekParams, todayParams)

    lessonsList = globalPreference.getLessons(selectedWeek)
    for (i in daysList.indices){
        setLessons(daysList[i])
    }

    for (i in daysNamesViewsList.indices){
        daysNamesViewsList[i].text = if (i == 2 && setDate(selectedWeek.weekRange.first+i).length > 5) "Wed." + setDate(selectedWeek.weekRange.first+i) else dayOfWeekNames[i] + setDate(selectedWeek.weekRange.first+i)
    }

    if (resultWeek.getWeek() != steps.second.toString()) {
        changeWeek(compareWeeks(urgentsWeekAndYear.first, urgentsWeekAndYear.second), urgentsWeekAndYear)
    }
}

fun compareWeeks(urgentsWeek: Int, urgentsYear: Int): Pair<Int, Int>{
    val neededWeek = urgentsWeek
    val weekDifference = urgentsWeek - weekText.text.split(" ")[1].toInt()
    val yearDifference = urgentsYear - getWeekTextYear(weekParams.text.toString()).toInt()

    var toReturn = if (yearDifference != 0) {
            Log.d("sd", "differs ${weekDifference} ${yearDifference}")
            if (yearDifference == 1) {
                if (urgentsWeek in 1..18) {
                    if (weekText.text.split(" ")[1].toInt() in 1..18) {
                        //Log.d("sd", "1.1")
                        weekDifference + (yearDifference) * 52 - 1
                    } else {
                        //Log.d("sd", "2.1")
                        weekDifference + (yearDifference + 1) * 52 - 1
                    }
                } else {
                    if (weekText.text.split(" ")[1].toInt() in 1..18) {
                        //Log.d("sd", "3.1")
                        weekDifference + (yearDifference - 1) * 52 - 1
                    } else {
                        //Log.d("sd", "4.1")
                        weekDifference + (yearDifference) * 52 - 1
                    }
                }
            } else if (yearDifference > 0) {
                if (urgentsWeek in 1..18) {
                    if (weekText.text.split(" ")[1].toInt() in 1..18) {
                        //Log.d("sd", "1.2")
                        weekDifference + (yearDifference) * 52 - 1
                    } else {
                        //Log.d("sd", "2.2")
                        weekDifference + (yearDifference + 1) * 52 - 1
                    }
                } else {
                    if (weekText.text.split(" ")[1].toInt() in 1..18) {
                        //Log.d("sd", "3.2")
                        weekDifference + (yearDifference - 1) * 52 - 1
                    } else {
                        //Log.d("sd", "4.2")
                        weekDifference + (yearDifference) * 52 - 1
                    }
                }
            } else if (yearDifference == -1) {
                if (urgentsWeek in 1..18) {
                    if (weekText.text.split(" ")[1].toInt() in 1..18) {
                        //Log.d("sd", "1.3")
                        weekDifference + (yearDifference) * 52
                    } else {
                        //Log.d("sd", "2.3")
                        weekDifference + (yearDifference + 1) * 52 + 1
                    }
                } else {
                    if (weekText.text.split(" ")[1].toInt() in 1..18) {
                        //Log.d("sd", "3.3")
                        weekDifference + (yearDifference - 1) * 52 + 1
                    } else {
                        //Log.d("sd", "4.3")
                        weekDifference + (yearDifference) * 52
                    }
                }
            } else if (yearDifference < 0) {
                if (urgentsWeek in 1..18) {
                    if (weekText.text.split(" ")[1].toInt() in 1..18) {
                        //Log.d("sd", "1.4")
                        weekDifference + (yearDifference) * 52 + 1
                    } else {
                        //Log.d("sd", "2.4")
                        weekDifference + (yearDifference + 1) * 52 + 1
                    }
                } else {
                    if (weekText.text.split(" ")[1].toInt() in 1..18) {
                        //Log.d("sd", "3.4")
                        weekDifference + (yearDifference - 1) * 52 + 1
                    } else {
                        //Log.d("sd", "4.4")
                        weekDifference + (yearDifference) * 52 + 1
                    }
                }
            } else {
                0
            }
        } else {
            if (urgentsWeek in 1..18) {
                if (weekText.text.split(" ")[1].toInt() in 1..18) {
                    //Log.d("sd", "5")
                    weekDifference
                } else {
                    //Log.d("sd", "6")
                    weekDifference + 52
                }
            } else {
                if (weekText.text.split(" ")[1].toInt() in 1..18) {
                    //Log.d("sd", "7")
                    weekDifference - 52
                } else {
                    //Log.d("sd", "8")
                    weekDifference
                }
            }
        }
    Log.d("sd", "$neededWeek $toReturn")
    return toReturn to neededWeek
}

fun editButtonToDefault(context: Context){
    editMode = false
    if (!notificationMode) recordLessons(lessonsViewsList, globalPreference)

    showButton(context, editButton)
    showButton(context, clearDayButton)
    showButton(context, notificationButton)

    dayBackButton.setImageDrawable(arrowBack)
    dayName.text = daysNamesViewsList[getSelectedDayId()].text

    (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()
    lessonsViewsList.forEach {
        it.lesson_name.isFocusable = false
        it.lesson_name.isFocusableInTouchMode = false
        it.lesson_name.isEnabled = false

        it.lesson_homework.isFocusable = false
        it.lesson_homework.isFocusableInTouchMode = false
        it.lesson_homework.isEnabled = false
    }
}

fun notificationButtonToDefault(context: Context){
    notificationMode = false
    notificationToast.cancel()

    showButton(context, notificationButton)
    showButton(context, clearDayButton)
    showButton(context, editButton)

    dayBackButton.setImageDrawable(arrowBack)
    dayName.text = daysNamesViewsList[getSelectedDayId()].text

    if (pickerLayout.visibility == View.VISIBLE){
        val animation = timePickerDisappear
        pickerLayout.startAnimation(animation)
        pickerLayout.postOnAnimation {
            pickerLayout.visibility = View.GONE
        }
    } else {
        pickerLayout.visibility = View.GONE
    }

    //lessonContainter.setBackgroundColor(colorPrimary)
    var k = 0
    lessonsViewsList.forEach {
        it.setBackgroundColor(colorPrimary)
        it.lesson_number.minWidth = 0
        it.lesson_number.textSize = 20f
        it.lesson_number.setHintTextColor(colorPrimaryDark)
        it.lesson_homework.setText(lessonsList[getSelectedDayId()][k].split(smallSeparator)[1])
        k++
    }
}

fun hideButton(context: Context, view: View){
    if (view.visibility == View.VISIBLE){
        if (view == clearDayButton){
            view.visibility = View.GONE
        }else{
            val animation = AnimationUtils.loadAnimation(context, R.anim.button_disappear)
            view.startAnimation(animation)
            view.postOnAnimation {
                view.visibility = View.GONE
            }
        }
    }
}

fun showButton(context: Context, view: View){
    if (view.visibility == View.GONE) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.button_appear)
        view.visibility = View.VISIBLE
        view.startAnimation(animation)
    }
}

fun lockDayButtons(option: String){
    if(option == "lock"){
        dayLayout.isEnabled = false
        editButton.isEnabled = false
        notificationButton.isEnabled = false
        clearDayButton.isEnabled = false

        dayBackButton.isEnabled = false
        dayBackButton.isClickable = false
        dayBackButton.isActivated = false
    }else if(option == "unlock"){
        dayLayout.isEnabled = true
        editButton.isEnabled = true
        notificationButton.isEnabled = true
        clearDayButton.isEnabled = true

        dayBackButton.isEnabled = true
        dayBackButton.isClickable = true
        dayBackButton.isActivated = true
    }
}

fun animateNumber(lessonsViewList: MutableList<LinearLayout>, option: String?){
    if (option != null && option != "open"){
        val lessonNumber = urgentHomeworkList[option.toInt()].split(separator)[0].split(smallSeparator)[1][0]
        lessonsViewList.forEach {
            if (it.lesson_number.text[0] == lessonNumber) it.lesson_number.startAnimation(lessonNumberAnimation)
            if (it.lesson_number.text[0] == '8' && lessonNumber == 'A') it.lesson_name_static.startAnimation(lessonNumberAnimation)
        }
    }
}

fun checkForCurrentWeek(){
    val tempCalendar = Calendar.getInstance()
    if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == 1 && Calendar.getInstance().firstDayOfWeek == Calendar.SUNDAY){
        tempCalendar.setWeekDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) - 1, Calendar.MONDAY)
        currentWeek = Week(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) - 1, tempCalendar.get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR), getCurrentWeekRange())
    } else {
        tempCalendar.setWeekDate(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), Calendar.MONDAY)
        currentWeek = Week(Calendar.getInstance().get(Calendar.WEEK_OF_YEAR), tempCalendar.get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR), getCurrentWeekRange())
    }
    currentWeek.weekRange = getWeekRange(currentWeek)
    currentWeek.setText(weekText, weekParams, todayParams)
    
    if (selectedWeek.week != currentWeek.week) showButton(context, returnButton) else hideButton(context, returnButton)
    if (selectedWeek.week != currentWeek.week) hideButton(context, currentWeekText) else showButton(context, currentWeekText)

}