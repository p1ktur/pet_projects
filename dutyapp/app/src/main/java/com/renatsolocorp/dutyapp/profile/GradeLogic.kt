package com.renatsolocorp.dutyapp.profile

import android.widget.NumberPicker

private var numberList = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")
private val letterList = arrayOf("А", "Б", "В", "Г", "Д")

private var globalNumberPicker: NumberPicker? = null
private var globalLetterPicker: NumberPicker? = null

fun initGradePickers(numberPicker: NumberPicker, letterPicker: NumberPicker){
    globalNumberPicker = numberPicker
    globalLetterPicker = letterPicker

    numberPicker.displayedValues = numberList
    numberPicker.minValue = 0
    numberPicker.maxValue = numberList.size-1

    letterPicker.displayedValues = letterList
    letterPicker.minValue = 0
    letterPicker.maxValue = letterList.size-1

}

fun gradeToText(numberPicker: NumberPicker, letterPicker: NumberPicker): String{
    return "${numberList[numberPicker.value]}-${letterList[letterPicker.value]}"
}

fun textToGrade(text: String){
    if (text.split("-").size > 1) {
        globalNumberPicker!!.value = numberList.indexOf(text.split("-")[0])
        globalLetterPicker!!.value = letterList.indexOf(text.split("-")[1])
    } else {
        globalNumberPicker!!.value = 0
        globalLetterPicker!!.value = 0
    }
}