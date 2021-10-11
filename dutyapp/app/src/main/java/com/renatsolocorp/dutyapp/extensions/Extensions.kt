package com.renatsolocorp.dutyapp.extensions

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import com.renatsolocorp.dutyapp.R
import com.renatsolocorp.dutyapp.classes.editableclass.*
import com.renatsolocorp.dutyapp.database.eventdb.*
import com.renatsolocorp.dutyapp.database.pairdb.DutyPair
import com.renatsolocorp.dutyapp.users.User
import java.util.*

const val smallSeparator = "<!#%&(>"
const val separator = "<@$^*)>"

val firstNames = listOf("Karl", "Franz", "Ostap", "Friedrich", "Henry", "Richard", "Oleg", "Renat", "Adolfus", "Perseus")
val lastNames = listOf("Kovalov", "Shevchenko", "Kvitka", "Kulish", "Osnov'yanenko", "Grigorovich", "Bagautdinov", "Lothbrok", "Rurikovich", "Halitskiy")
val nickNames = listOf("The Straight", "The Crimean Boy", "The PussyCat", "The Virus", "The President", "The Dictator", "The Juggernaut", "The Gay", "The Flower", "The Epic")
//TODO add localization for these names

var creatingNewClass = false
var addingPair = false
var deletingPair = false

//fragments
const val PROFILE_FRAGMENT = "profile"
const val PROFILE_SETTINGS_FRAGMENT = "profile_settings"
const val NEW_CLASS_FRAGMENT = "new_class"
const val VIEWED_CLASS_FRAGMENT = "viewed_class_fragment"
const val EVENT_DETAIL_FRAGMENT = "event_detail_fragment"
const val MY_CLASSES_FRAGMENT = "my_classes"
const val FOLLOWED_FRAGMENT = "followed"
const val FOLLOWERS_FRAGMENT = "friends"
const val SEARCH_FRAGMENT = "search"

//main menu items and buttons
const val PROFILE_ITEM = "profile_item"
const val NEW_CLASS_ITEM = "new_class_item"
const val MY_CLASSES_ITEM = "my_classes_item"
const val FOLLOWED_ITEM = "followed_item"
const val FOLLOWERS_ITEM = "followers_item"
const val FIND_PEOPLE_ITEM = "find_people_item"
const val PROFILE_SETTINGS_BUTTON = "profile_settings_button"
const val CREATE_NEW_CLASS_BUTTON = "create_new_class_button"
const val EVENT_DETAIL_BUTTON = "event_detail_button"

fun getIndexOfPair(pair: DutyPair): Int{
    var k = 0
    var a = -1
    for (i in pairsList.indices){
        a++
        if (pairsList[k].id == pair.id) break else k++
    }
    return if (a == k) k else //-1
        k
}

fun checkForNameChanges(position: Int, text: String): Boolean{
    return position < pairsList.size && pairsList[position].name != text && text != "empty"
}

fun resetIds(pairs: MutableList<DutyPair>){
    var k = 0
    pairs.forEach { it.id = k; k++ }
}

fun clearWhitespaces(text: String, entirely: Boolean = false): String {
    val index = mutableListOf<Int>()
    for (i in text.indices) if (text[i].isWhitespace()) index.add(i) else if (!entirely) break
    return deleteChars(text, index)
}

fun automataUltra(text: String): String{
    val index = mutableListOf<Int>()
    if (text.isNotEmpty() && text[0].isWhitespace()) for (i in text.indices) if (text[i].isWhitespace()) index.add(i) else break
    if (text.isNotEmpty() && text[text.length - 1].isWhitespace()) for (i in text.length-1 downTo 0) if (text[i].isWhitespace()) index.add(i) else break
    return deleteChars(text, index)
}

fun deleteChars(string: String, index: MutableList<Int>):String{
    var returnText = ""
    if (index.size != 0) for (i in string.indices) if (!index.contains(i)) returnText += string[i]
    return if (returnText == "") string else returnText
}

fun calculateBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    var stepLength = 0
    return if (height > width) {
        stepLength = (height-width)/2
        Bitmap.createBitmap(bitmap, 0, stepLength, width, width)
    } else {
        stepLength = (width-height)/2
        Bitmap.createBitmap(bitmap, stepLength, 0, height, height)
    }
}

fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    Log.d("sdsdsd", "${source.width} ${source.height}")
    return if (source.width < source.height) Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true) else source
}

fun generateClassUid(): String{
    var id = ""
    val sample = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    for (i in 0..27){
        id += sample.random()
    }
    return id
}

fun MutableList<DutyPair>.copy(): MutableList<DutyPair> {
    val returnable = mutableListOf<DutyPair>()
    this.forEach { returnable.add(it) }
    return returnable
}

fun checkIfToShow(string: String): Boolean{
    return if (string == "null") true else string.toBoolean()
}

fun filterQueryText(inputUsers: MutableList<User>, query: String): MutableList<User> {
    return if (query.contains("[0-9]".toRegex())){
        inputUsers.filter{ it.name.contains(query.replace("[0-9]".toRegex(), "")) && it.name.contains(
            query.replace(
                "[a-zA-Z]".toRegex(),
                ""
            )
        ) }.toMutableList()
    } else {
        inputUsers.filter{ it.name.contains(query) }.toMutableList()
    }
}

fun checkFilteringQuery(inputUsers: MutableList<User>, query: String): Boolean {
    return if (query.contains("[0-9]".toRegex())){
        inputUsers.map{ it.name.contains(query.replace("[0-9]".toRegex(), "")) && it.name.contains(
            query.replace(
                "[a-zA-Z]".toRegex(),
                ""
            )
        ) }.contains(true)
    } else {
        inputUsers.map{ it.name.contains(query) }.contains(true)
    }
}

fun getIndexOfPair(list: MutableList<DutyPair>, pair: DutyPair): Int{
    var result = 0
    list.forEach {
        if (it == pair) result = it.id
    }
    return result
}

fun showConnectionProblem(context: Context){
    Toast.makeText(context, context.getString(R.string.connection_problem), Toast.LENGTH_SHORT).show()
}

fun String.shorten(size: Int): String{
    return if (this.contains(" ")){
        var string = ""
        val list = this.split(" ")

        for (i in list.indices){
            if (list[i] != ""){
                if ((string + " ${list[i]}").length <= size){
                    string += if (i != 0) " ${list[i]}" else list[i]
                } else break
            }
        }

        if (clearWhitespaces(string, true).length == clearWhitespaces(this, true).length){
            this
        } else {
            if (list[list.size-1].isNotEmpty()) {
                "$string..."
            } else string
        }

    } else if (this.length > size) this.dropLast(this.length - size) + "..." else this
}

fun translateDayOfWeek(day: String, context: Context): String{
    return when (day) {
        "Monday" -> context.getString(R.string.monday)
        "Tuesday" -> context.getString(R.string.tuesday)
        "Wednesday" -> context.getString(R.string.wednesday)
        "Thursday" -> context.getString(R.string.thursday)
        "Friday" -> context.getString(R.string.friday)
        "Saturday" -> context.getString(R.string.saturday)
        "Sunday" -> context.getString(R.string.sunday)
        else -> day
    }
}

fun translateMonth(month: String, context: Context): String{
    return when (month) {
        "January" -> context.getString(R.string.january)
        "February" -> context.getString(R.string.february)
        "March" -> context.getString(R.string.march)
        "April" -> context.getString(R.string.april)
        "May" -> context.getString(R.string.may)
        "June" -> context.getString(R.string.june)
        "July" -> context.getString(R.string.july)
        "August" -> context.getString(R.string.august)
        "September" -> context.getString(R.string.september)
        "October" -> context.getString(R.string.october)
        "November" -> context.getString(R.string.november)
        "December" -> context.getString(R.string.december)
        "Jan" -> context.getString(R.string.january).take(3)
        "Feb" -> context.getString(R.string.february).take(3)
        "Mar" -> context.getString(R.string.march).take(3)
        "Apr" -> context.getString(R.string.april).take(3)
        "Jun" -> context.getString(R.string.june).take(3)
        "Jul" -> context.getString(R.string.july).take(3)
        "Aug" -> context.getString(R.string.august).take(3)
        "Sep" -> context.getString(R.string.september).take(3)
        "Oct" -> context.getString(R.string.october).take(3)
        "Nov" -> context.getString(R.string.november).take(3)
        "Dec" -> context.getString(R.string.december).take(3)
        else -> month
    }
}

fun translateDate(date: String, context: Context): String{
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
    var day = values[1].dropLast(2)

    day = when(day[day.length-1]){
        '1' -> {
            if (day[0] != '1') day + context.getString(R.string.st) else day + context.getString(R.string.th)
        }
        '2' -> {
            if (day[0] != '1') day + context.getString(R.string.nd) else day + context.getString(R.string.th)
        }
        '3' -> {
            if (day[0] != '1') day + context.getString(R.string.rd) else day + context.getString(R.string.th)
        }
        else -> day + context.getString(R.string.th)
    }

    val tempCalendar = Calendar.getInstance().apply { set(year.toInt(), month.toInt(), day.filter{ it.isDigit() }.toInt()) }
    val dayOfWeek = tempCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)!!

    if (day[0] == '0') day = day.drop(1)

    return if (date.contains(":")){
        val s = "${translateDayOfWeek(dayOfWeek, context)}, $day " + context.getString(R.string.of) + " ${translateMonth(values[2], context)} $year, ${values[4]}"
        s.replace("   ", " ")
    } else {
        val s = "${translateDayOfWeek(dayOfWeek, context)}, $day " + context.getString(R.string.of) + " ${translateMonth(values[2], context)} $year"
        s.replace("   ", " ")
    }
}

fun translateEvent(event: String, context: Context): String{
    return when(event){
        WAS_DUTY_EVENT_NAME -> context.getString(R.string.pair_was_on_duty)
        IS_DUTY_EVENT_NAME -> context.getString(R.string.pair_is_currently_on_duty)
        SET_ON_DUTY_EVENT_NAME -> context.getString(R.string.pair_was_set_on_duty)
        else -> event
    }
}

fun fadeOut(view: View){
    val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0f)
    animator.duration = 500L
    animator.doOnEnd { view.visibility = View.GONE }
    animator.start()
}

fun fadeIn(view: View){
    val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1.0f)
    animator.duration = 500L
    animator.doOnEnd { view.visibility = View.VISIBLE }
    animator.start()
}