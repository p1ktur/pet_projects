package com.renatsolocorp.dairy.lessons_logic.day_activity_logic

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_day.*
import kotlinx.android.synthetic.main.activity_week.*
import kotlinx.android.synthetic.main.lesson_layout.view.*
import com.renatsolocorp.dairy.*
import com.renatsolocorp.dairy.lessons_logic.*
import com.renatsolocorp.dairy.notifications_logic.UrgentHomeworkAdapter
import com.renatsolocorp.dairy.notifications_logic.emptyCalendar
import com.renatsolocorp.dairy.notifications_logic.initDateTimePickers
import com.renatsolocorp.dairy.notifications_logic.monthIntoNumber
import java.util.*

val UPDATE = "UPDATE"

lateinit var arrowBack: Drawable
lateinit var timePickerDisappear: Animation
var colorSelector = 0
var colorPrimary = 0
var colorPrimaryDark = 0

private lateinit var lessonsRecyclerView: RecyclerView

var selectedDayId = 0

class DayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTheme(this)
        setContentView(R.layout.activity_day)

        arrowBack = getDrawable(R.drawable.ic_baseline_arrow_back_36)!!
        timePickerDisappear = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
        colorSelector = getAttributeColor(this, R.attr.lessonSelectorColor)
        colorPrimary = getAttributeColor(this, R.attr.colorPrimary)
        colorPrimaryDark = getAttributeColor(this, R.attr.colorPrimaryDark)

        selectedDayId = intent.getIntExtra(intentOpenDayId, 0)

        day_name.text = daysNamesViewsList[selectedDayId].text

        //lessonsRecyclerView = lesson_list
        //lessonsRecyclerView.layoutManager = LinearLayoutManager(this)
        //lessonsRecyclerView.adapter = LessonAdapter(lessonsList[selectedDayId], this, intent.getStringExtra(numberAnimation))

        editButton = edit_button
        notificationButton = notification_setting_button
        pickerLayout = picker_layout
        dayBackButton = day_back_button
        dayName = day_name
        clearDayButton = clear_day_button

        dayBackButton.isClickable = true
    }

    fun clearDayOnClick(view: View){
        val builder = AlertDialog.Builder(this, dialogAppTheme)
        builder.setTitle("Warning!")
        builder.setMessage("It will clear current day schedule and homework. Are you sure you want to proceed?")
        builder.setPositiveButton(Html.fromHtml("Yes")) { dialogInterface: DialogInterface, i: Int ->
            lessonNumberAnimation.cancel()
            notificationButtonToDefault(this)
            editButtonToDefault(this)
            lessonsRecyclerView.forEach {
                it.lesson_name.setText("")
                it.lesson_homework.setText("")
                it.lesson_checkbox.isChecked = false
            }
            lessonsList[selectedDayId] = globalPreference.getDefault()[selectedDayId]
            (urgentHomework.adapter as UrgentHomeworkAdapter).clearSelectedDayUrgents(dayOfWeekNames[selectedDayId])
            (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()
            globalPreference.saveLessons(lessonsList, selectedWeek)
            globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
        }
        builder.setNegativeButton(Html.fromHtml("No")) { dialogInterface: DialogInterface, i: Int -> }
        builder.setCancelable(true)
        builder.show()
    }

    fun dayBackOnClick(view: View){
        if (editMode){
            editButtonToDefault(this)
        }else if (notificationMode){
            notificationButtonToDefault(this)
        }else {
            dayBackButton.isClickable = false
            lessonNumberAnimation.cancel()
            //recordLessons(lessonsRecyclerView, globalPreference)
            (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()

            setLessons(selectedDay!!)
            selectedDay = null

            finishActivity(0)
            finish()
        }
    }

    fun editButtonOnClick(view: View){
        if(notificationMode) notificationButtonToDefault(this)
        editMode = !editMode

        if (!editMode){
            //recordLessons(lessonsRecyclerView, globalPreference)

            editButtonToDefault(this)

            (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()
            stopService(notificationServiceIntent)
            startService(notificationServiceIntent)
        }else{
            day_back_button.setImageDrawable(getDrawable(R.drawable.ic_clear_black_36dp))
            hideButton(this, editButton)
            hideButton(this, notificationButton)
            hideButton(this, clearDayButton)
            dayName.text = "Editing  "
        }

        lessonsRecyclerView.forEach {
            it.lesson_name.isFocusable = editMode
            it.lesson_name.isFocusableInTouchMode = editMode
            it.lesson_name.isEnabled = editMode

            it.lesson_homework.isFocusable = editMode
            it.lesson_homework.isFocusableInTouchMode = editMode
            it.lesson_homework.isEnabled = editMode
        }
    }

    fun notificationButtonOnClick(view: View){
        if (editMode) editButtonToDefault(this)
        notificationMode = !notificationMode

        if (notificationMode){
            notificationToast.show()

            dayBackButton.setImageDrawable(getDrawable(R.drawable.ic_clear_black_36dp))
            lessonsRecyclerView.setBackgroundColor(colorSelector)

            hideButton(this, editButton)
            hideButton(this, notificationButton)
            hideButton(this, clearDayButton)

            dayName.text = "Create notification  "

            lessonsRecyclerView.forEach {
                it.setBackgroundColor(colorSelector)
                it.lesson_number.minWidth = 160
                it.lesson_number.textSize = 26f
                it.lesson_number.setHintTextColor(getColor(R.color.colorBlack))
                it.lesson_homework.setText(it.lesson_homework.text.toString().shorten())

                it.lesson_number.setOnClickListener {
                    notificationToast.cancel()
                    initDateTimePickers(date_picker, hour_picker, minute_picker)
                    if (pickerLayout.visibility == View.GONE && notificationMode){
                        pickerLayout.visibility = View.VISIBLE
                        val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_appear)
                        pickerLayout.startAnimation(animation)
                    } else if (pickerLayout.visibility == View.VISIBLE && notificationMode){
                        val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
                        pickerLayout.startAnimation(animation)
                        pickerLayout.postOnAnimation {
                            pickerLayout.visibility = View.GONE
                        }
                    }
                    selectedLesson = (it as TextView).text.toString().dropLast(1).toInt()
                }
            }
        } else {
            notificationButtonToDefault(this)
        }
    }

    fun pickerOkButtonOnClick(view: View){
        if (notificationMode){
            notificationToast.cancel()

            val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
            pickerLayout.startAnimation(animation)
            pickerLayout.postOnAnimation {
                pickerLayout.visibility = View.GONE
            }

            notificationButtonToDefault(this)
            showButton(this, editButton)

            notificationMode = false

            lessonsRecyclerView.forEach {
                if (it.lesson_number.text.toString().dropLast(1).toInt() == selectedLesson){
                    contentText = "Week ${selectedWeek.getWeek()} ${dayOfWeekNames[selectedDayId]}${setDate(selectedWeek.weekRange.first+ selectedDayId)} $smallSeparator$selectedLesson.${it.lesson_name.text}: ${it.lesson_homework.text}"
                    intentOpenDayNumber = selectedDayId
                }
            }

            val calendarTime = Calendar.getInstance()
            calendarTime.set(Calendar.MONTH,  date_picker.displayedValues[date_picker.value].split(" ")[2].monthIntoNumber())
            calendarTime.set(Calendar.DAY_OF_MONTH,  date_picker.displayedValues[date_picker.value].split(" ")[1].toInt())
            calendarTime.set(Calendar.YEAR,  emptyCalendar.get(Calendar.YEAR))
            calendarTime.set(Calendar.HOUR_OF_DAY, hour_picker.value-1)
            calendarTime.set(Calendar.MINUTE, (minute_picker.value-1)*5)
            calendarTime.set(Calendar.SECOND, 0)
            val textToAdd = contentText + smallSeparator + selectedWeek.year + separator + calendarTime.timeInMillis
            Log.d("sd", "texttoadd $textToAdd ${emptyCalendar.get(Calendar.YEAR)}")
            (urgentHomework.adapter as UrgentHomeworkAdapter).addItem(textToAdd)
            globalPreference.addNotification(textToAdd)
            (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()

            stopService(notificationServiceIntent)
            startService(notificationServiceIntent)

            selectedLesson = null
        }
    }

    fun pickerCancelButtonOnClick(view: View){
        notificationToast.cancel()
        val animation = AnimationUtils.loadAnimation(this, R.anim.time_picker_disappear)
        pickerLayout.startAnimation(animation)
        pickerLayout.postOnAnimation {
            pickerLayout.visibility = View.GONE
        }
        selectedLesson = null
    }

    override fun onBackPressed() {
        if (editMode){
            editButtonToDefault(this)
        }else if (notificationMode){
            notificationButtonToDefault(this)
        }else {
            dayBackButton.isClickable = false
            lessonNumberAnimation.cancel()
            //recordLessons(lessonsRecyclerView, globalPreference)
            (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()

            setLessons(selectedDay!!)
            selectedDay = null

            finishActivity(0)
            finish()
        }
    }

    override fun onPause() {
        super.onPause()

        //recordLessons(lessonsRecyclerView, globalPreference)

        globalPreference.saveLessons(lessonsList, selectedWeek)
        globalPreference.saveNotifications(urgentsToString(urgentHomeworkList))
        Log.d("sd", "day onPaused")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("sd", "day onDestroyed")
    }
}

fun editButtonToDefault(context: Context){
    editMode = false
    //if (!notificationMode) recordLessons(lessonsRecyclerView, globalPreference)

    showButton(context, editButton)
    showButton(context, clearDayButton)
    showButton(context, notificationButton)

    dayBackButton.setImageDrawable(arrowBack)
    dayName.text = daysNamesViewsList[selectedDayId].text

    (urgentHomework.adapter as UrgentHomeworkAdapter).checkLessonsForUrgentChanges()
    lessonsRecyclerView.forEach {
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
    dayName.text = daysNamesViewsList[selectedDayId].text

    if (pickerLayout.visibility == View.VISIBLE){
        val animation = timePickerDisappear
        pickerLayout.startAnimation(animation)
        pickerLayout.postOnAnimation {
            pickerLayout.visibility = View.GONE
        }
    } else {
        pickerLayout.visibility = View.GONE
    }

    lessonsRecyclerView.setBackgroundColor(colorPrimary)
    var k = 0
    lessonsRecyclerView.forEach {
        it.setBackgroundColor(colorPrimary)
        it.lesson_number.minWidth = 0
        it.lesson_number.textSize = 20f
        it.lesson_number.setHintTextColor(colorPrimaryDark)
        it.lesson_homework.setText(lessonsList[selectedDayId][k].split(smallSeparator)[1])
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