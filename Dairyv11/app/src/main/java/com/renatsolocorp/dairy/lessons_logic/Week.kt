package com.renatsolocorp.dairy.lessons_logic

import android.util.Log
import android.widget.TextView
import java.util.*

class Week(week: Int, month: Int, year: Int, weekRange: IntRange) {

    var week = week
    var month = month
    var year = year
    var weekRange = weekRange

    fun setText(mainView: TextView, paramsView: TextView, todayView: TextView){
        mainView.text = "Week ${getWeek()}"

        val tempCalendar = Calendar.getInstance()

        if (weekRangeToString().length > 7){
            tempCalendar.set(Calendar.MONTH, this.month+1)
        }else{
            tempCalendar.set(Calendar.MONTH, this.month)
        }

        paramsView.text = "${weekRangeToString()} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)} ${this.year}"

        todayView.text = "Today: ${Calendar.getInstance().get(Calendar.DAY_OF_MONTH)} ${Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)} ${Calendar.getInstance().get(Calendar.YEAR)}"
        todayView.text = todayView.text.toString() + " ${Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)}".toUpperCase()

    }

    fun getWeek(): String{
        val tempDay = Calendar.getInstance()
        tempDay.set(this.year-1, 8, 1)

        if (tempDay.get(Calendar.DAY_OF_WEEK) == 1 || tempDay.get(Calendar.DAY_OF_WEEK) == 7) {
            var k = 0
            while (tempDay.get(Calendar.DAY_OF_WEEK) != 2) {
                k++
                tempDay.set(this.year - 1, 8, 1 + k)
            }
        }
        val firstWeek = tempDay.get(Calendar.WEEK_OF_YEAR)

        return if (this.week >= firstWeek && this.year == tempDay.get(Calendar.YEAR)){
            "${this.week - firstWeek + 1}"
        }else if (this.year == tempDay.get(Calendar.YEAR)+1){
            val tempWeek = Calendar.getInstance()
            tempWeek.set(this.year, 8, 1)

            if (tempWeek.get(Calendar.DAY_OF_WEEK) == 1 || tempWeek.get(Calendar.DAY_OF_WEEK) == 7) {
                var k = 0
                while (tempWeek.get(Calendar.DAY_OF_WEEK) != 2) {
                    k++
                    tempWeek.set(this.year, 8, 1 + k)
                }
            }
            val newFirstWeek = tempWeek.get(Calendar.WEEK_OF_YEAR)

            if (this.week >= newFirstWeek && this.year == tempWeek.get(Calendar.YEAR)){
                "${this.week - newFirstWeek + 1}"
            }else{
                "${Calendar.getInstance().getActualMaximum(Calendar.WEEK_OF_YEAR) - newFirstWeek + this.week}" //add 1 to make normal numbers on emulator
            }
        }else{
            "ERROR"
        }
    }

    fun weekRangeToString(): String{
        val tempDay = Calendar.getInstance()
        tempDay.setWeekDate(this.year, this.week, Calendar.MONDAY)

        val tempCalendar = Calendar.getInstance()
        tempCalendar.set(Calendar.MONTH, this.month)
        Log.d("sd", "$weekRange ${this.week} ${this.month} ${this.year}")
        return if (tempDay.get(Calendar.DATE)+6 > tempDay.getActualMaximum(Calendar.DAY_OF_MONTH)){
            if (tempCalendar.get(Calendar.MONTH) == 11){
                "${this.weekRange.first} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)} ${this.year-1} - ${this.weekRange.last - tempDay.getActualMaximum(Calendar.DAY_OF_MONTH)}"
            }else {
                "${this.weekRange.first} ${tempCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH)} - ${this.weekRange.last - tempDay.getActualMaximum(Calendar.DAY_OF_MONTH)}"

            }
        }else{
            "${this.weekRange.first}-${this.weekRange.last}"
        }
    }
}