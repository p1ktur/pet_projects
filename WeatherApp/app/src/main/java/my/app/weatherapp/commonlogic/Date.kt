package my.app.weatherapp.commonlogic

import java.util.*

//date class to display date
data class Date(
    var year: Int = 2022,
    var month: Int = 8,
    var day: Int = 17
) {
    //returns next day relating to current date
    fun getNextDay(): Date {
        val nextDate = Date(year, month, day)
        when (month) {
            1 -> if (year % 4 == 0 && year % 100 != 0) {
                if (day == 29) nextDate.apply { month++; day = 1 } else nextDate.apply { day++ }
            } else {
                if (day == 28) nextDate.apply { month++; day = 1 } else nextDate.apply { day++ }
            }
            3, 5, 8, 10 -> if (day == 30) nextDate.apply { month++; day = 1 } else nextDate.apply { day++ }
            11 -> if (day == 31) nextDate.apply { year++; month = 0; day = 1 } else nextDate.apply { day++ }
            else -> if (day == 31) nextDate.apply { month++; day = 1 } else nextDate.apply { day++ }
        }
        return nextDate
    }

    //returns date as formatted text
    override fun toString(): String {
        return if (month in 0..8) {
            if (day in 1..9) {
                "0$day.0${month + 1}.$year"
            } else {
                "$day.0${month + 1}.$year"
            }
        } else {
            if (day in 1..9) {
                "0$day.${month + 1}.$year"
            } else {
                "$day.${month + 1}.$year"
            }
        }
    }

    //overrides ==, >=, <= operators
    operator fun compareTo(date: Date): Int {
        return if (formatDateToComparable() == date.formatDateToComparable()) {
            0
        } else if (formatDateToComparable() < date.formatDateToComparable()) {
            -1
        } else {
            1
        }
    }

    //returns date as number which can be compared
    private fun formatDateToComparable(): Long {
        return if (month in 0..8) {
            if (day in 1..9) {
                "${year}0${month + 1}0$day".toLong()
            } else {
                "${year}0${month + 1}$day".toLong()
            }
        } else {
            if (day in 1..9) {
                "${year}${month + 1}0$day".toLong()
            } else {
                "${year}${month + 1}$day".toLong()
            }
        }
    }

    //returns current date as number of day of the current year
    fun getDayOfYear(): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar.get(Calendar.DAY_OF_YEAR)
    }
}
