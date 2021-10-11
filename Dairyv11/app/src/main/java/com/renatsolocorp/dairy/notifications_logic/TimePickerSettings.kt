package com.renatsolocorp.dairy.notifications_logic

import android.os.Build
import android.util.Log
import android.widget.DatePicker
import android.widget.NumberPicker
import com.renatsolocorp.dairy.nightMode
import com.renatsolocorp.dairy.todayDay
import com.renatsolocorp.dairy.todayMonth
import com.renatsolocorp.dairy.todayYear
import java.util.*

private var dateValues = arrayOf("", "", "", "", "")
private val displayedHours = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
private val displayedMinutes = arrayOf("0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55")

var emptyCalendar = Calendar.getInstance()
private var todayCalendar = Calendar.getInstance()

fun initDateTimePickers(datePicker: NumberPicker, hourPicker: NumberPicker, minutePicker: NumberPicker){
    datePicker.isFocusable = false
    datePicker.isFocusableInTouchMode = false
    datePicker.isActivated = false
    hourPicker.isFocusable = false
    hourPicker.isFocusableInTouchMode = false
    minutePicker.isFocusable = false
    minutePicker.isFocusableInTouchMode = false

    emptyCalendar = Calendar.getInstance()
    todayCalendar = Calendar.getInstance()
    
    val tempCalendar = Calendar.getInstance()
    val currentValue = "${tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)}, ${tempCalendar.get(Calendar.DAY_OF_MONTH)} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)}"

    changeDateValues(currentValue, currentValue, datePicker)

    datePicker.setOnValueChangedListener { picker, oldVal, newVal ->
        changeDateValues(dateValues[oldVal], dateValues[newVal], datePicker)
        updatePickers(datePicker, hourPicker, minutePicker)
    }

    hourPicker.displayedValues = displayedHours
    hourPicker.minValue = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    hourPicker.maxValue = 24
    if (Calendar.getInstance().get(Calendar.MINUTE) >= 55 && hourPicker.minValue != hourPicker.maxValue) {
        hourPicker.displayedValues = displayedHours.mix(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1)
        hourPicker.minValue = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 2
    }else{
        hourPicker.displayedValues = displayedHours.mix(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
        hourPicker.minValue = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1
    }
    hourPicker.maxValue = 24
    hourPicker.update()
    
    hourPicker.setOnScrollListener { view, scrollState ->
        updatePickers(datePicker, hourPicker, minutePicker)
    }

    minutePicker.displayedValues = displayedMinutes.mix(Calendar.getInstance().get(Calendar.MINUTE).roundMinutes()/5)
    minutePicker.minValue = Calendar.getInstance().get(Calendar.MINUTE).roundMinutes()/5+1
    minutePicker.maxValue = 12

    minutePicker.setOnScrollListener { view, scrollState ->
        updatePickers(datePicker, hourPicker, minutePicker)
    }

    minutePicker.value = Calendar.getInstance().get(Calendar.MINUTE).roundMinutes()/5 + 1
}

fun updatePickers(datePicker: NumberPicker, hourPicker: NumberPicker, minutePicker: NumberPicker){
    val calendarTime = Calendar.getInstance()
    calendarTime.set(Calendar.MONTH,  dateValues[datePicker.value].split(" ")[2].monthIntoNumber())
    calendarTime.set(Calendar.DAY_OF_MONTH,  dateValues[datePicker.value].split(" ")[1].toInt())
    calendarTime.set(Calendar.YEAR,  emptyCalendar.get(Calendar.YEAR))

    val tempCalendar = Calendar.getInstance()
    if (tempCalendar.get(Calendar.HOUR_OF_DAY) == 23 && tempCalendar.get(Calendar.MINUTE) >= 55){
        if (todayDay == tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) && todayMonth == tempCalendar.getActualMaximum(Calendar.MONTH)){
            tempCalendar.set(todayYear++, 0, 1, 0, 0, 0)
        }else if (todayDay == tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)){
            tempCalendar.set(todayYear, todayMonth++, 1, 0, 0, 0)
        }else{
            tempCalendar.set(todayYear, todayMonth, todayDay++, 0, 0, 0)
        }
    }else tempCalendar.set(todayYear, todayMonth, todayDay, 0, 0, 0)

    if (calendarTime.get(Calendar.YEAR) == tempCalendar.get(Calendar.YEAR) && calendarTime.get(Calendar.MONTH) == tempCalendar.get(Calendar.MONTH) && calendarTime.get(Calendar.DAY_OF_MONTH) == tempCalendar.get(Calendar.DAY_OF_MONTH)){
        if (Calendar.getInstance().get(Calendar.MINUTE) >= 55 && hourPicker.minValue != hourPicker.maxValue) {
            hourPicker.displayedValues = displayedHours.mix(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1)
            hourPicker.minValue = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 2
        }else {
            hourPicker.displayedValues = displayedHours.mix(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
            hourPicker.minValue = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1
        }
        if (hourPicker.value == hourPicker.minValue){
            minutePicker.displayedValues = displayedMinutes.mix(Calendar.getInstance().get(Calendar.MINUTE).roundMinutes()/5)
            minutePicker.minValue = Calendar.getInstance().get(Calendar.MINUTE).roundMinutes()/5+1
        }else{
            minutePicker.displayedValues = displayedMinutes
            minutePicker.minValue = 1
        }
    }else{
        hourPicker.displayedValues = displayedHours
        hourPicker.minValue = 1

        minutePicker.displayedValues = displayedMinutes
        minutePicker.minValue = 1
    }

    minutePicker.update()
    hourPicker.update()
}

fun changeDateValues(oldValue: String, newValue: String, datePicker: NumberPicker){
    dateValues[2] =  newValue

    val oldCalendar = Calendar.getInstance()
    oldCalendar.set(Calendar.MONTH,  oldValue.split(" ")[2].monthIntoNumber())
    oldCalendar.set(Calendar.DAY_OF_MONTH,  oldValue.split(" ")[1].toInt())
    oldCalendar.set(Calendar.YEAR,  emptyCalendar.get(Calendar.YEAR))

    val tempCalendar = Calendar.getInstance()
    tempCalendar.set(Calendar.MONTH,  newValue.split(" ")[2].monthIntoNumber())
    tempCalendar.set(Calendar.DAY_OF_MONTH,  newValue.split(" ")[1].toInt())
    tempCalendar.set(Calendar.YEAR,  emptyCalendar.get(Calendar.YEAR))

    if (tempCalendar.get(Calendar.DAY_OF_YEAR) == 1 && (oldCalendar.get(Calendar.DAY_OF_YEAR) == 365 || oldCalendar.get(Calendar.DAY_OF_YEAR) == 366) ){
        tempCalendar.set(Calendar.YEAR, tempCalendar.get(Calendar.YEAR)+1)
        emptyCalendar.set(Calendar.YEAR, tempCalendar.get(Calendar.YEAR))
    }else if(oldCalendar.get(Calendar.DAY_OF_YEAR) == 1 && (tempCalendar.get(Calendar.DAY_OF_YEAR) == 365 || tempCalendar.get(Calendar.DAY_OF_YEAR) == 366)){
        tempCalendar.set(Calendar.YEAR, tempCalendar.get(Calendar.YEAR)-1)
        emptyCalendar.set(Calendar.YEAR, tempCalendar.get(Calendar.YEAR))
    }

    tempCalendar.set(Calendar.DAY_OF_YEAR, tempCalendar.get(Calendar.DAY_OF_YEAR)-2)
    dateValues[0] = "${tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)}, ${tempCalendar.get(Calendar.DAY_OF_MONTH)} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)}"
    tempCalendar.set(Calendar.DAY_OF_YEAR, tempCalendar.get(Calendar.DAY_OF_YEAR)+1)
    dateValues[1] = "${tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)}, ${tempCalendar.get(Calendar.DAY_OF_MONTH)} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)}"

    tempCalendar.set(Calendar.DAY_OF_YEAR, tempCalendar.get(Calendar.DAY_OF_YEAR)+2)
    dateValues[3] = "${tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)}, ${tempCalendar.get(Calendar.DAY_OF_MONTH)} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)}"
    tempCalendar.set(Calendar.DAY_OF_YEAR, tempCalendar.get(Calendar.DAY_OF_YEAR)+1)
    dateValues[4] = "${tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)}, ${tempCalendar.get(Calendar.DAY_OF_MONTH)} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)}"

    Log.d("sd", "old: $oldValue new: $newValue")
    if (ifTodayDay(dateValues.indexOf(newValue))) {
        dateValues = dateValues.mix(2)
        datePicker.displayedValues = dateValues
        datePicker.minValue = 0
        datePicker.maxValue = 2
        datePicker.value = 0
    }else if (ifTodayDay(dateValues.indexOf(oldValue))){
        dateValues = dateValues.mix(1)
        datePicker.displayedValues = dateValues
        datePicker.minValue = 0
        datePicker.maxValue = 3
        datePicker.value = 1
    }else{
        datePicker.displayedValues = dateValues
        datePicker.minValue = 0
        datePicker.maxValue = 4
        datePicker.value = 2
    }
    displayDateValues(datePicker)
}

fun ifTodayDay(value: Int): Boolean{
    val tempCalendar = Calendar.getInstance()
    val daysValue = "${tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)}, ${tempCalendar.get(Calendar.DAY_OF_MONTH)} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)}"
    
    return dateValues[value] == daysValue
}

fun displayDateValues(datePicker: NumberPicker){
    Log.d("sd", "------------------------------")
    for (i in datePicker.displayedValues.indices){
        Log.d("sd", "${datePicker.displayedValues[i]} || ${datePicker.displayedValues[datePicker.minValue]} <-> ${datePicker.displayedValues[datePicker.maxValue]}")
    }
    Log.d("sd", "------------------------------")
}

fun String.monthIntoNumber(): Int{
    return when(this){
        "Jan" -> 0
        "Feb" -> 1
        "Mar" -> 2
        "Apr" -> 3
        "May" -> 4
        "Jun" -> 5
        "Jul" -> 6
        "Aug" -> 7
        "Sep" -> 8
        "Oct" -> 9
        "Nov" -> 10
        "Dec" -> 11
        else -> 0
    }
}

fun Int.roundMinutes(): Int{
    var integer = this
    while (integer%5 != 0 || integer == this){
        integer++
    }
    if (integer == 60) integer = 0

    return integer
}

fun Array<String>.mix(quantityToEnd: Int): Array<String>{
    //quantity which is put from beginning to end
    return (this.drop(quantityToEnd) + this.dropLast(this.size-quantityToEnd)).toTypedArray()
}

fun NumberPicker.update(){
    val value = this.value
    this.value = this.minValue
    this.value = value
}

