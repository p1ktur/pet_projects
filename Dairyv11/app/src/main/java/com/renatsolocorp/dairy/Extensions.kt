package com.renatsolocorp.dairy

import android.content.Context
import android.util.Log
import android.util.TypedValue
import com.renatsolocorp.dairy.lessons_logic.*
import java.util.*
import kotlin.math.abs


fun String.shorten(): String{
    var returnText = ""

    if(this != ""){
        if (this.contains(" ") && this.split(" ")[0].length <= 6){
            returnText = this.split(" ")[0] + "..."
        }else if(this.length > 6){
            returnText = this.dropLast(this.length - 6) + "..."
        }else{
            returnText = this
        }
    }else{
        returnText = this
    }

    val index = mutableListOf<Int>()
    for(i in returnText.indices){
        if (returnText[i].isWhitespace()){
            index.add(i)
        }
    }

    return deleteChars(returnText, index)
}

fun deleteChars(string: String, index: MutableList<Int>):String{
    var returnText = ""

    for (i in string.indices){
        if (!index.contains(i)){
            returnText += string[i]
        }
    }

    return returnText
}

const val smallSeparator = "|?>|<"
const val separator = "@#$%^"
const val megaSeparator = "&*()_~"

fun MutableList<String>.toPreferenceString(): String{
    var toBeReturned = ""

    for (i in this.indices){
        toBeReturned += this[i]
        if (i != this.size-1){
            toBeReturned += separator
        }
    }

    return toBeReturned
}

fun MutableList<MutableList<String>>.toStringLine(): String{
    var toBeReturned = ""

    for (i in this.indices){
        toBeReturned += (this[i].toPreferenceString())
        if (i != this.size-1){
            toBeReturned += megaSeparator
        }
    }

    return toBeReturned
}

fun String.toStringMutableList(): MutableList<String>{
    return if(this == "") {
        mutableListOf(
            smallSeparator + smallSeparator,
            smallSeparator + smallSeparator,
            smallSeparator + smallSeparator,
            smallSeparator + smallSeparator,
            smallSeparator + smallSeparator,
            smallSeparator + smallSeparator,
            smallSeparator + smallSeparator)
    }else {
        this.split(separator).toMutableList()
    }
}

fun String.toStringList(): MutableList<MutableList<String>>{
    val toBeReturned = mutableListOf<MutableList<String>>()

    for (i in this.split(megaSeparator).indices){
        toBeReturned.add(this.split(megaSeparator)[i].toStringMutableList())
    }

    return toBeReturned
}

fun checkForListErrors(lessonsList: MutableList<MutableList<String>>): Boolean{
    var noErrors = true
    for (i in lessonsList.indices){
        for (j in lessonsList[i].indices){
            if (!lessonsList[i][j].contains(smallSeparator)){
                noErrors = false
                break
            }
            if (!noErrors) break
        }
        if (!noErrors) break
    }
    return noErrors
}

fun getYearMonthWeek(week: Week, option: String): MutableList<Int>{
    if (week.week == -1 || week.week == 0) week.week = 52
    if (week.week == 53) week.week = 1
    if (week.month == -1) week.month = 11

    var trWeek = week.week
    val trMonth: Int
    var trYear = week.year

    if (option == "forward") {
        if (week.week == Calendar.getInstance().getActualMaximum(Calendar.WEEK_OF_YEAR)) {
            trYear++
            trWeek = 1
        } else {
            trWeek++
        }

        val tempDay = Calendar.getInstance()
        tempDay.setWeekDate(trYear, trWeek, Calendar.MONDAY)

        if (tempDay.getActualMaximum(Calendar.DAY_OF_MONTH) < tempDay.get(Calendar.DATE) + 6) {
            if (week.month == 11) {
                trYear++
            }
        }

    }else if (option == "backward"){
        if (week.week == 1) {
            trYear--
            trWeek = Calendar.getInstance().getActualMaximum(Calendar.WEEK_OF_YEAR)-1
        } else {
            trWeek--
        }

        val tempDay = Calendar.getInstance()
        tempDay.setWeekDate(trYear, trWeek, Calendar.MONDAY)

        if (7 > tempDay.get(Calendar.DATE) + 6) {
            if (week.month == 0) {
                trYear--
            }
        }
    }

    if(abs(week.week - trWeek) > 1 && abs(week.week - trWeek) != 51){
        if (option == "forward"){
            trWeek = week.week + 1
        }else if (option == "backward"){
            trWeek = week.week - 1
        }
    }

    if(abs(week.year - trYear) > 1){
        if (option == "forward"){
            trYear = week.year + 1
        }else if (option == "backward"){
            trYear = week.year - 1
        }
    }

    if (trWeek == -1 || trWeek == 0) trWeek = 52
    if (trWeek == 53) trWeek = 1
        
    val anotherCalendar = Calendar.getInstance()
    anotherCalendar.setWeekDate(trYear, trWeek, Calendar.MONDAY)
    trMonth = anotherCalendar.get(Calendar.MONTH)

    Log.d("ld", "ymw = $trYear $trMonth $trWeek")
    return mutableListOf(trWeek, trMonth, trYear)
}

fun getCurrentWeekRange(): IntRange{
    val currentWeekDay = if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != 1) Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 else 7
    val translateValue = currentWeekDay - 1

    return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - translateValue..Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - translateValue + 6
}

fun getWeekRange(week: Week): IntRange{
    val firstDay = Calendar.getInstance()
    firstDay.setWeekDate(week.year, week.week, Calendar.MONDAY)

    return firstDay.get(Calendar.DATE)..firstDay.get(Calendar.DATE)+6
}

fun checkIfWeekExists(weekCode: String, preference: Preferences): Boolean{
    return preference.preference.getString(weekCode, null) != null //|| preference.preference.getString(weekCode.split(smallSeparator)[1], null) != null
}

fun getWeekCode(week: Week): String{
    return "<${week.year}${week.month}${week.week}>"
}

fun copySchedule(from: MutableList<MutableList<String>>, where: MutableList<MutableList<String>>){
    var broken = false

    for (i in from.indices){
        for (j in from[i].indices){
            if (where[i][j].split(smallSeparator)[0] != ""){
                broken = true
                break
            }
        }
        if (broken) break
    }

    if (!broken){
        for (i in 0 until from.size){
            for (j in 0 until from[i].size){
                where[i][j] = from[i][j].split(smallSeparator)[0] + smallSeparator + where[i][j].split(smallSeparator)[1] + smallSeparator
            }
        }
    }
}

fun setDate(dayOfMonth: Int): String {
    var finalDate = ""
    val tempCalendar = Calendar.getInstance()
    tempCalendar.set(Calendar.MONTH, selectedWeek.month)

    if (selectedWeek.weekRange.last > tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)){
        if (dayOfMonth > tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)){
            finalDate += " 0${dayOfMonth-tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)}.${selectedWeek.month+2}"
        }else{
            finalDate += " $dayOfMonth.${selectedWeek.month+1}"
        }
    }else if (dayOfMonth in 0..9){
        finalDate = " 0$dayOfMonth.${selectedWeek.month+1}"
    }else{
        finalDate = " $dayOfMonth.${selectedWeek.month+1}"
    }

    return finalDate
}

fun log(text: String){
    Log.d("sd", text.replace(smallSeparator, "smallSeparator").replace(separator, "separator").replace(megaSeparator, "megaSeparator"))
}

fun dayToIterator(day: String): Int{
    return when(day){
        "Monday" -> 0
        "Tuesday" -> 1
        "Wednesday" -> 2
        "Thursday" -> 3
        "Friday" -> 4
        "Saturday" -> 5
        else -> 0
    }
}

fun String.eliminateWhitespace(): String{
    val returnText = this
    val index = mutableListOf<Int>()
    for(i in (returnText.length-1).downTo(0)){
        if (returnText[i].isWhitespace()){
            index.add(i)
        }else break
    }
    return deleteChars(returnText, index)
}

fun urgentsToString(urgents: MutableList<String>): String{
    var toReturn = ""
    for (i in urgents.indices){
        if (i != urgents.size -1 ) toReturn += urgents[i] + megaSeparator else toReturn += urgents[i]
    }
    return toReturn
}

fun sortNotifications(): MutableList<String>{
    Log.d("sd", "core $urgentHomeworkList")
    val sortedUrgents = mutableListOf<String>()
    var millisList = mutableListOf<Long>()
    return if (urgentHomeworkList.size != 0){
        for (i in urgentHomeworkList.indices){
            millisList.add(urgentHomeworkList[i].split(separator)[1].toLong())
        }
        Log.d("sd", "millis core $millisList")
        millisList = millisList.sorted().toMutableList()
        Log.d("sd", "millis sorted $millisList")
        for (i in millisList.indices){
            for (j in urgentHomeworkList.indices){
                if (urgentHomeworkList[j].split(separator)[1].toLong() == millisList[i]){
                    var allowed = true
                    if (sortedUrgents.size != 0){
                        for (a in sortedUrgents.indices){
                            if (sortedUrgents[a].contains(urgentHomeworkList[j].split(separator)[0])) allowed = false
                        }
                    }
                    if (allowed) {
                        sortedUrgents.add(urgentHomeworkList[j])
                        break
                    }
                }
            }
            if (sortedUrgents.size == millisList.size) break
        }
        Log.d("sd", "sorted $sortedUrgents")
        Log.d("sd", "system time millis ${System.currentTimeMillis()}")
        sortedUrgents
    }else urgentHomeworkList
}

fun getUrgentPosition(time: Long): Int{
    var toReturnPosition = 0
    for (i in urgentHomeworkList.indices){
        if (urgentHomeworkList[i].split(separator)[1].toLong() == time){
            toReturnPosition = i
        }
    }
    return toReturnPosition
}

fun emptyUrgentTime(urgent: String): String{
    return urgent.split(separator)[0] + separator + 0
}

fun getWeekTextYear(text: String): String{
    return if (text.split(" ").size == 6){
        text.split(" ")[text.split(" ").size-1]
    } else if (text.split(" ").size == 7){
        text.split(" ")[2]
    } else {
        text.split(" ")[2]
    }
}

fun updateTheme(context: Context){
    if(nightMode){
        context.setTheme(R.style.DarkAppTheme)
        currentAppTheme = R.style.DarkAppTheme
        dialogAppTheme = R.style.DarkDialogTheme
    }else{
        context.setTheme(R.style.AppTheme)
        currentAppTheme = R.style.AppTheme
        dialogAppTheme = R.style.LightDialogTheme
    }
}

fun getAttributeColor(context: Context, attributeId: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(attributeId, typedValue, true)

    return context.resources.getColor(typedValue.resourceId)
}

fun getSelectedDayId(): Int{
    var iterator = 0
    for(i in daysList.indices){
        if (selectedDay == daysList[i]) {
            iterator = i
            break
        }
    }
    return iterator
}

fun clearAllGlobalVars(){
    lessonsList = globalPreference.getDefault()

    intentOpenDayNumber = 0
    selectedLesson = null
    contentText = ""
    notificationId = 0

    dayOfWeekNames = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    selectedDay = null
    daysList = mutableListOf()
    daysLayouts = mutableListOf()
    daysNamesLayouts = mutableListOf()
    daysNamesViewsList = mutableListOf()
    scrollsList = mutableListOf()
    lessonsViewsList = mutableListOf()

    todayYear = Calendar.getInstance().get(Calendar.YEAR)
    todayMonth = Calendar.getInstance().get(Calendar.MONTH)
    todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    bigLayoutsId = mutableListOf()
    numberTextId = mutableListOf()
    staticTextNameId = mutableListOf()
    staticTextHWId = mutableListOf()
    verticalLinesId = mutableListOf()
    horizontalLinesId = mutableListOf()
}
