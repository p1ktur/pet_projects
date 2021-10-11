package com.renatsolocorp.dairy

import android.content.Context
import com.renatsolocorp.dairy.lessons_logic.Week

const val PREFERENCE_NAME = "PreferenceName"
const val SCHEDULE_OPTION_NAME = "ScheduleOptionName"
const val NOTIFICATIONS = "Notifications"
const val NIGHTMODE_OPTION_NAME = "NightmodeOptionName"

class Preferences(context: Context) {

    val defaultStringValue = mutableListOf(
        mutableListOf(smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator),
        mutableListOf(smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator),
        mutableListOf(smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator),
        mutableListOf(smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator),
        mutableListOf(smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator),
        mutableListOf(smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator, smallSeparator + smallSeparator)
    ).toStringLine()

    val preference = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun saveLessons(lessonsList: MutableList<MutableList<String>>, week: Week){
        val editor = preference.edit()
        editor.putString(getWeekCode(week), lessonsList.toStringLine())
        editor.apply()
    }

    fun getLessons(week: Week): MutableList<MutableList<String>>{
        return preference.getString(getWeekCode(week), defaultStringValue)!!.toStringList()
    }

    fun getDefault(): MutableList<MutableList<String>>{
        return defaultStringValue.toStringList()
    }

    fun addWeak(week: Week){
        val editor = preference.edit()
        editor.putString(getWeekCode(week), defaultStringValue)
        editor.apply()
    }

    fun checkIfWeekExists(week: Week): Boolean{
        return preference.getString(getWeekCode(week), null) != null
    }

    fun clearPreferences(){
        val editor = preference.edit()
        editor.clear()
        editor.apply()
    }

    fun addNotification(text: String){
        val editor = preference.edit()
        editor.putString(NOTIFICATIONS, preference.getString(NOTIFICATIONS, "") + megaSeparator + text)
        editor.apply()
    }

    fun saveNotifications(text: String){
        val editor = preference.edit()
        editor.putString(NOTIFICATIONS, text)
        editor.apply()
    }

    fun getNotifications(): String{
        return preference.getString(NOTIFICATIONS, "")!!
    }

    fun saveScheduleOption(option: Boolean){
        val editor = preference.edit()
        editor.putBoolean(SCHEDULE_OPTION_NAME, option)
        editor.apply()
    }

    fun getScheduleOption(): Boolean{
        return preference.getBoolean(SCHEDULE_OPTION_NAME, false)
    }

    fun saveNightmodeOption(mode: Boolean){
        val editor = preference.edit()
        editor.putBoolean(NIGHTMODE_OPTION_NAME, mode)
        editor.apply()
    }

    fun getNightmode(): Boolean{
        return preference.getBoolean(NIGHTMODE_OPTION_NAME, false)
    }
}






