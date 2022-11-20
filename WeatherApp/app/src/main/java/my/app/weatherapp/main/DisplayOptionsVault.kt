package my.app.weatherapp.main

import android.content.Context
import android.content.SharedPreferences

//class for saving options/data
class DisplayOptionsVault(context: Context, mode: Int) {
    private val SHARED_PREFERENCES = "SHARED_PREFERENCES"
    private val DISPLAY_OPTIONS = "DISPLAY_OPTIONS"
    private val DISPLAYED_YEAR_INDEX = "DISPLAYED_YEAR_INDEX"
    private val OPENED_FRAGMENT = "OPENED_FRAGMENT"

    private val sp = context.getSharedPreferences(SHARED_PREFERENCES, mode)

    fun saveDisplayOption(option: Int) {
        val e = sp.edit()

        e.putInt(DISPLAY_OPTIONS, option)

        e.apply()
    }

    fun saveDisplayedYearIndex(year: Int) {
        val e = sp.edit()

        e.putInt(DISPLAYED_YEAR_INDEX, year)

        e.apply()
    }

    fun saveOpenedFragment(index: Int) {
        val e = sp.edit()

        e.putInt(OPENED_FRAGMENT, index)

        e.apply()
    }

    fun getDisplayOption(): Int {
        return sp.getInt(DISPLAY_OPTIONS, 0)
    }

    fun getDisplayedYearIndex(): Int {
        return sp.getInt(DISPLAYED_YEAR_INDEX, 0)
    }

    fun getOpenedFragment(): Int {
        return sp.getInt(OPENED_FRAGMENT, 0)
    }
}