package com.renatsolocorp.dutyapp.extensions

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.renatsolocorp.dutyapp.classes.editableclass.*
import com.renatsolocorp.dutyapp.classes.eventdetails.*
import com.renatsolocorp.dutyapp.database.eventdb.*
import com.renatsolocorp.dutyapp.main.selectedClass
import java.util.*

fun checkForCurrentPair(viewedUserId: String, application: Application, context: Context){
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    val tempCalendar = Calendar.getInstance()
    tempCalendar.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
    if (tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
        tempCalendar.add(Calendar.DAY_OF_MONTH, -1)
    }else if(tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
        tempCalendar.add(Calendar.DAY_OF_MONTH, -2)
    }
    val timeInMillis = getTimeInMillis(tempCalendar.get(Calendar.YEAR), tempCalendar.get(
        Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH)).toLong()
    if (currentPair.dutyTime < timeInMillis){
        if (currentPair.dutyTime == 0L) {
            currentPair.dutyTime = timeInMillis
            if (getIndexOfPair(currentPair) <= pairsList.size-1){
                pairsList[getIndexOfPair(currentPair)].isCurrent = true
                pairsList[getIndexOfPair(currentPair)].dutyTime  = timeInMillis
            }
        } else {
            val moveCalendar = Calendar.getInstance()

            moveCalendar.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
            moveCurrentPair(timeInMillis, moveCalendar, application, context)
        }
    }else{
        currentPairText.text = currentPair.name
        if (getIndexOfPair(currentPair) <= pairsList.size-1){
            pairsList[getIndexOfPair(currentPair)].isCurrent = true
            pairsList[getIndexOfPair(currentPair)].dutyTime  = timeInMillis
        }
    }

    currentPairText.text = currentPair.name

    pairsList.forEach {
        if (it.id to it.name != currentPair.id to currentPair.name){
            it.isCurrent = false
        }
    }

    if (viewedUserId == currentUser.uid) editableViewModel.updateList(pairsList, context)
}

fun getTimeInMillis(year: Int, month: Int, day: Int): String{
    val year = year.toString()
    var month = "0$month"
    var day = "0$day"
    if (month.length == 3) month = month.drop(1)
    if (day.length == 3) day = day.drop(1)
    return year + month + day
}

fun subYearDays(first: Int, second: Int): Int{
    return Calendar.getInstance().apply { set(Calendar.DAY_OF_YEAR, first) }.get(Calendar.DAY_OF_YEAR) - Calendar.getInstance().apply { set(Calendar.DAY_OF_YEAR, second) }.get(Calendar.DAY_OF_YEAR)
}

fun getDayDifference(firstTime: String, secondTime: String): Int{
    var daySum = 0
    var firstTime = firstTime
    var secondTime = secondTime
    if (firstTime.toLong() == 0L) firstTime = unFormatDate(getCurrentDate(WAS_DUTY_EVENT_NAME))
    if (secondTime.toLong() == 0L) secondTime = unFormatDate(getCurrentDate(WAS_DUTY_EVENT_NAME))
    val firstYear = firstTime.dropLast(firstTime.length-4).toInt()
    val secondYear = secondTime.dropLast(secondTime.length-4).toInt()
    val firstMonth = firstTime.drop(4).dropLast(firstTime.drop(4).length-2).toInt()
    val secondMonth = secondTime.drop(4).dropLast(secondTime.drop(4).length-2).toInt()
    val firstDay = firstTime.drop(firstTime.length-2).toInt()
    val secondDay = secondTime.drop(secondTime.length-2).toInt()

    val yearDiff = secondYear - firstYear
    val monthDiff = secondMonth - firstMonth

    if(yearDiff != 0){
        val firstList = mutableListOf<Int>()
        if (firstMonth != 11) {
            var k = firstMonth
            while (k < 11) {
                firstList.add(k)
                k++
            }
        }

        for (i in firstList){
            daySum += getMonthDays(i, firstYear)
        }

        val secondList = mutableListOf<Int>()
        if (secondMonth != 0) {
            var k = 0
            while (k < secondMonth) {
                secondList.add(k)
                k++
            }
        }

        for (i in secondList){
            daySum += getMonthDays(i, secondYear)
        }

        val list = mutableListOf<Int>()
        var k = firstMonth
        while (k < secondMonth-1) {
            list.add(k)
            k++
        }
        for (i in list) {
            daySum += getMonthDays(i, firstYear)
        }
        daySum += (getMonthDays(firstMonth, firstYear) - firstDay) + secondDay


        if (yearDiff > 1) {
            var k = 0
            for (i in firstYear..secondYear){
                if (i%4 == 0){
                    k++
                }
            }
            var yearsToCount = yearDiff - 1
            while (k != 0){
                yearsToCount--
                k--
                daySum += 366
            }
            while (yearsToCount != 0){
                yearsToCount--
                daySum += 365
            }
        }
    }else{
        if (monthDiff >= 1) {
            val list = mutableListOf<Int>()
            var k = firstMonth
            while (k < secondMonth-1) {
                list.add(k)
                k++
            }

            for (i in list) {
                daySum += getMonthDays(i, firstYear)
            }
            daySum += (getMonthDays(firstMonth, firstYear) - firstDay) + secondDay
        }else{
            daySum += secondDay - firstDay
        }
    }
    return daySum
}

fun moveCurrentPair(timeInMillis: Long, tempCalendar: Calendar, application: Application, context: Context){
    val currentUser = FirebaseAuth.getInstance().currentUser!!

    //val dayDiff = getDaysToMove(currentPair.dutyTime.toString(), getDayDifference(currentPair.dutyTime.toString(), timeInMillis.toString()))
    val dayDiff = getDayDifference(currentPair.dutyTime.toString(), timeInMillis.toString())

    val tempCalendarDiff = Calendar.getInstance().apply {
        set(tempCalendar.get(Calendar.YEAR), tempCalendar.get(Calendar.MONTH), tempCalendar.get(Calendar.DAY_OF_MONTH))
        add(Calendar.DAY_OF_MONTH, -(dayDiff))
    }

    var i = 0
    while (i < dayDiff){
        pairsList[getIndexOfPair(currentPair)].isCurrent = false
        pairsList[getIndexOfPair(currentPair)].dutiesAmount++
        if (pairsList[getIndexOfPair(currentPair)].debts != 0) pairsList[getIndexOfPair(currentPair)].debts--

        if (selectedClass.creatorId == currentUser.uid) EventDetailRepository(application, context).addEvent(getIndexOfPair(currentPair), WAS_DUTY_EVENT_NAME, formatDate(currentPair.dutyTime.toString()))

        currentPair = if (getIndexOfPair(currentPair) == pairsList.size-1){
            pairsList[0]
        }else{
            pairsList[getIndexOfPair(currentPair) +1]
        }

        when (Calendar.FRIDAY) {
            tempCalendarDiff.get(Calendar.DAY_OF_WEEK) -> {
                tempCalendarDiff.add(Calendar.DAY_OF_MONTH, +3)
                i+=3
            }
            else -> {
                tempCalendarDiff.add(Calendar.DAY_OF_MONTH, 1)
                i++
            }
        }

        val timeInMillisDiff = getTimeInMillis(tempCalendarDiff.get(Calendar.YEAR), tempCalendarDiff.get(Calendar.MONTH), tempCalendarDiff.get(Calendar.DAY_OF_MONTH)).toLong()

        currentPair.isCurrent = true
        currentPair.dutyTime = timeInMillisDiff
        pairsList[getIndexOfPair(currentPair)].isCurrent = true

        pairsList[getIndexOfPair(currentPair)].dutyTime  = timeInMillisDiff
        currentPairText.text = currentPair.name
    }

    if (dayDiff != 0) {
        //for (i in pairsList) mainViewModel.update(i)
    }
}

fun getDaysToMove(timeInMillis: String, daysDiff: Int): Int{
    var daysToMove = daysDiff
    var timeInMillis = timeInMillis
    if (timeInMillis.toLong() == 0L) timeInMillis = unFormatDate(getCurrentDate(WAS_DUTY_EVENT_NAME))

    val day = timeInMillis.drop(timeInMillis.length-2).toInt()
    val month = timeInMillis.drop(4).dropLast(timeInMillis.drop(4).length-2).toInt()
    val year = timeInMillis.dropLast(timeInMillis.length-4).toInt()


    return daysToMove
}

fun getMonthDays(month: Int, year: Int): Int{
    var k = 0
    if(year%4 == 0) k=1
    return when(month){
        5, 6, 7 -> 0
        0, 2, 4, 9, 11 -> 31
        1 -> 28+k
        else -> 30
    }
}

fun getCurrentDate(eventName: String, weekendSkip: Boolean = false): String{
    val tempCalendar = Calendar.getInstance()
    if (weekendSkip && tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
        tempCalendar.add(Calendar.DAY_OF_MONTH, -1)
    }else if(weekendSkip && tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
        tempCalendar.add(Calendar.DAY_OF_MONTH, -2)
    }
    val year = tempCalendar.get(Calendar.YEAR)
    val month = tempCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)
    var day = tempCalendar.get(Calendar.DAY_OF_MONTH).toString()
    var hour = tempCalendar.get(Calendar.HOUR_OF_DAY).toString()
    var minute = tempCalendar.get(Calendar.MINUTE).toString()
    var second = tempCalendar.get(Calendar.SECOND).toString()
    val dayOfWeek = tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)

    day = when(day[day.length-1]){
        '1' -> {
            if (day.length == 1 || day[0] != '1') "${day}st" else "${day}th"
        }
        '2' -> {
            if (day.length == 1 || day[0] != '1') "${day}nd" else "${day}th"
        }
        '3' -> {
            if (day.length == 1 || day[0] != '1') "${day}rd" else "${day}th"
        }
        else -> {
            "${day}th"
        }
    }

    if (hour.length == 1) hour = "0$hour"
    if (minute.length == 1) minute = "0$minute"
    if (second.length == 1) second = "0$second"

    val timePart = "$hour:$minute:$second"
    val datePart = "$day of $month $year"

    return when (eventName){
        WAS_DUTY_EVENT_NAME -> "$dayOfWeek, $datePart"
        IS_DUTY_EVENT_NAME -> "Currently"
        else -> "$dayOfWeek, $datePart, $timePart"
    }
}

fun formatDate(timeInMillis: String): String{
    var day = timeInMillis.drop(timeInMillis.length-2)
    var month = timeInMillis.drop(4).dropLast(timeInMillis.drop(4).length-2)
    val year = timeInMillis.dropLast(timeInMillis.length-4)

    val tempCalendar = Calendar.getInstance().apply { set(year.toInt(), month.toInt(), day.toInt()) }
    month = tempCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)!!
    val dayOfWeek = tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)

    day = when(day[day.length-1]){
        '1' -> {
            if (day[0] != '1') "${day}st" else "${day}th"
        }
        '2' -> {
            if (day[0] != '1') "${day}nd" else "${day}th"
        }
        '3' -> {
            if (day[0] != '1') "${day}rd" else "${day}th"
        }
        else -> {
            "${day}th"
        }
    }
    if (day[0] == '0') day = day.drop(1)

    return "$dayOfWeek, $day of $month $year"
}

fun unFormatDate(date: String): String {
    val values = date.replace("of ", "").split(" ")
    val year = values[3]
    val month = when (values[2]) {
        "January" -> "00"
        "February" -> "01"
        "March" -> "02"
        "April" -> "03"
        "May" -> "04"
        "June" -> "05"
        "July" -> "06"
        "August" -> "07"
        "September" -> "08"
        "October" -> "09"
        "November" -> "10"
        "December" -> "11"
        else -> "00"
    }
    val day = values[1].dropLast(2)

    return year + month + day
}

fun getCurrentTime(): String{
    return unFormatTime(getCurrentDate(SET_ON_DUTY_EVENT_NAME))
}

fun unFormatTime(date: String): String {
    val values = date.replace("of ", "").replace(",", "").split(" ")
    val year = values[3]
    val month = when (values[2]) {
        "January" -> "00"
        "February" -> "01"
        "March" -> "02"
        "April" -> "03"
        "May" -> "04"
        "June" -> "05"
        "July" -> "06"
        "August" -> "07"
        "September" -> "08"
        "October" -> "09"
        "November" -> "10"
        "December" -> "11"
        else -> "00"
    }
    val day = values[1].dropLast(2)

    val time = values[4].split(":")[0] + values[4].split(":")[1] + values[4].split(":")[2]

    return year + month + day + time
}